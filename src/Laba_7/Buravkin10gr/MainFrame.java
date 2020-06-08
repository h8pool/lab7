package Laba_7.Danilin_8gr;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class MainFrame extends JFrame{
    private static final int FRAME_MINIMUM_WIDTH = 900;
    private static final int FRAME_MINIMUM_HEIGHT = 550;
    private static String name=null;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private JList<String> list;
    private DefaultListModel<String> listModel;
    private HashMap<String, JTextArea> map = new HashMap();
    private HashMap<String, String> map2 = new HashMap();
    private JTextArea forall;

    private static String msgroup="225.4.5.6";
    private static Integer msport=6788;

    private static Integer srvPort;
    private static Integer id;
    private JButton btnsall;
    private static MainFrame THIS;

    private ImageIcon  icon = null;

    private MainFrame() {
        super("Клиент мгновенных сообщений");



            name= JOptionPane.showInputDialog(MainFrame.this,"","Введите ваш логин", JOptionPane.PLAIN_MESSAGE);
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите логин!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }


        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu ballMenu = new JMenu("Пользователи");
        Action Ban = new AbstractAction("Бан") {
            public void actionPerformed(ActionEvent event) {
                String[] Name = new String[10];
                int i=1;
                while(i<listModel.getSize()){
                    String _s=listModel.getElementAt(i);
                    Name[i-1]=(_s.split("@")[0]);
                    i++;
                }
                String result = (String) JOptionPane.showInputDialog(
                        MainFrame.this,
                        "",
                        "Выбор пользователя",
                        JOptionPane.QUESTION_MESSAGE,
                        icon, Name, Name[0]);
                i=1;
                while(i<listModel.getSize()){
                    String _s=listModel.getElementAt(i);
                    if(result.equals((_s.split("@"))[0])){
                        listModel.remove(i);
                        JOptionPane.showMessageDialog(MainFrame.this, "Пользователь  " +result);
                    }
                    i++;
                }
            }

        };
        menuBar.add(ballMenu);
        ballMenu.add(Ban);

        Action Offlene = new AbstractAction("Отключиться") {
            public void actionPerformed(ActionEvent event) {
                offline();
                System.exit(0);

            }

        };
        menuBar.add(ballMenu);
        ballMenu.add(Offlene);


        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - getWidth()) / 2
                , (Toolkit.getDefaultToolkit().getScreenSize().height - getHeight()) / 2);
        setLayout(null);
        setTitle("Чат, ваше имя: "+name);


        id=(int) (Math.random()*1000000000);

        listModel=new DefaultListModel< >();
        listModel.addElement("Все");
        list=new JList< >(listModel);
        list.setLayoutOrientation(JList.VERTICAL);
        final JScrollPane scrollPaneList=new JScrollPane(list);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent arg0) {
                if(list.getSelectedIndex()==0){
                    tabbedPane.setSelectedIndex(0);
                    return;
                }
                if(arg0.getValueIsAdjusting())
                    return;
                int i=0;
                int si=list.getSelectedIndex();
                if(si<0)
                    return;
                String s=listModel.get(si);
                String[] _a=s.split("@");
                s=_a[0];
                while(i<tabbedPane.getTabCount()){
                    if(tabbedPane.getTitleAt(i).equals(s)){
                        tabbedPane.setSelectedIndex(i);
                        return;
                    }
                    i++;
                }
                String[] a = listModel.get(list.getSelectedIndex()).split("@");
                newTab(a[0],a[1]);
            }
        });
        tabbedPane.setLocation(210,2);
        tabbedPane.setSize(450,360);
        scrollPaneList.setLocation(3,3);
        scrollPaneList.setSize(280,560);
        this.add(scrollPaneList);
        this.add(tabbedPane);
        this.getContentPane().addHierarchyBoundsListener(new HierarchyBoundsListener() {
            public void ancestorResized(HierarchyEvent arg0) {
                scrollPaneList.setSize(200,getHeight()-30);
                tabbedPane.setSize(getWidth()-220,getHeight()-30);
            }
            public void ancestorMoved(HierarchyEvent arg0) {}
        });

        MSrecive();
        reciveMSG();
        newTab("Все",null);
    }

    public static void setTHIS(MainFrame THIS) {
        MainFrame.THIS = THIS;
    }

    private void newTab(final String caption, String address){
        String ip = null;
        int port = -1;
        if(address!=null){
            String[] _a=address.split(":");
            ip=_a[0];
            port=Integer.parseInt(_a[1]);
        }

        JPanel panel = new JPanel();
        final JTextArea textAreaHist = new JTextArea(1, 15);
        textAreaHist.setEditable(false);
        textAreaHist.addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent arg0) {

            }

            @Override
            public void focusGained(FocusEvent arg0) {

            }
        });
        final JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaHist);
        map.put(caption, textAreaHist);
        final JTextArea textAreaMsg = new JTextArea(1, 15);
        final JScrollPane scrollPaneMsg = new JScrollPane(textAreaMsg);
        final String ip2=ip;
        final int port2=port;
        final JButton btn=new JButton("Отправить");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                sendMessage(caption, ip2, port2, textAreaMsg, textAreaHist);
            }
        });
        if(address==null){
            btnsall=btn;
            btn.setEnabled(false);

        }
        String hist=map2.get(caption);
        if(hist!=null)
            textAreaHist.append(hist);
        tabbedPane.addTab(caption, panel);
        if(address==null)
            forall=textAreaHist;
        panel.setLayout(null);
        panel.add(btn);
        panel.add(scrollPaneOutgoing);
        panel.add(scrollPaneMsg);
        btn.setSize(120,20);
        btn.setLocation(tabbedPane.getWidth()-400,tabbedPane.getHeight()-82);
        scrollPaneOutgoing.setSize(tabbedPane.getWidth()-20,tabbedPane.getHeight()-250);
        scrollPaneMsg.setSize(tabbedPane.getWidth()-80,140);
        scrollPaneMsg.setLocation(40,tabbedPane.getHeight()-230);
        panel.addHierarchyBoundsListener(new HierarchyBoundsListener() {
            public void ancestorResized(HierarchyEvent e) {
                btn.setSize(120,20);
                btn.setLocation(tabbedPane.getWidth()-400,tabbedPane.getHeight()-82);
                scrollPaneOutgoing.setSize(tabbedPane.getWidth()-20,tabbedPane.getHeight()-250);
                scrollPaneMsg.setSize(tabbedPane.getWidth()-80,140);
                scrollPaneMsg.setLocation(40,tabbedPane.getHeight()-230);
            }
            public void ancestorMoved(HierarchyEvent e) {}
        });
        if(address!=null)
            tabbedPane.setTabComponentAt(tabbedPane.getTabCount()-1,new ButtonTabComponent(tabbedPane,this,caption));
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
    }

    private void sendMessage(String nick, String address, int port, JTextArea textAreaMsg, JTextArea textAreaHist) {
        String message = textAreaMsg.getText();
        textAreaMsg.setText("");
        if(address==null){
            int i=0;
            String ipAddress;
            while(i<listModel.getSize()-1){
                i++;
                ipAddress=(listModel.get(i).split("@"))[1].split(":")[0];
                port=Integer.parseInt(((listModel.get(i).split("@"))[1].split(":")[1]));
                sendData(ipAddress, port, "all", message);
            }
            writeText(forall, "Я",message);
            return;
        }
        sendData(address, port, "private",message);
        System.out.println("Отправлено");
        writeText(textAreaHist, "Я",message);
    }

    private void writeText(JTextArea textarea,String name, String message){

        textarea.append(name+": "+message+"\n");
        textarea.moveCaretPosition(textarea.getText().length());
    }

    private void reciveMSG(){
        new Thread(new Runnable() {
            public void run() {
                try {
                    Integer port=findFreePort();
                    srvPort=port.intValue();
                    final ServerSocket serverSocket = new ServerSocket(port);
                    while (!Thread.interrupted()) {
                        final Socket socket = serverSocket.accept();
                        final DataInputStream in = new DataInputStream(socket.getInputStream());
                        final String priv = in.readUTF();
                        final String senderPort = in.readUTF();
                        final String message = in.readUTF();
                        final String address =((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress();
                        socket.close();
                        if(priv.equals("offline")){
                            removeUser(message);
                            continue;
                        }
                        if(priv.equals("hi")){
                            addUser(message, address, senderPort);
                            continue;
                        }
                        procMSG(priv, message, address, senderPort);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка в работе сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();
    }

    private void procMSG(String priv, String msg, String addr, String port){
        String nick=listModel.get(findInList(addr+":"+port, 1)).split("@")[0];

        if("all".equals(priv)){
            tabbedPane.setSelectedIndex(0);
            writeText(forall, nick,msg);
            return;
        }
        JTextArea t = map.get(nick);
        if(t!=null){
            tabbedPane.setSelectedIndex(findInTabbed(nick));//!
        }else{
            String s=findUser2(nick);
            if(s!=null){
                String[] a=s.split("@");
                newTab(a[0], a[1]);

            }
            t = map.get(nick);
        }
        writeText(t,nick,msg);
    }

    public Integer findFreePort() throws IOException {
        ServerSocket server =new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        return port;
    }

    private static void MSonline(){
        try{
            MulticastSocket s = new MulticastSocket();
            byte buf[] = ("online@"+srvPort.toString()+"@"+name+"@"+id.toString()).getBytes();
            DatagramPacket pack = new DatagramPacket(buf, buf.length, InetAddress.getByName(msgroup), msport);
            s.send(pack);
            s.close();
        }catch(IOException e){
            JOptionPane.showMessageDialog(THIS, "Вы не подключены к сети", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int findInTabbed(String s){
        int i=1;
        while(i<tabbedPane.getTabCount()){
            if(tabbedPane.getTitleAt(i).equals(s)){
                tabbedPane.setSelectedIndex(i);
                return i;
            }
            i++;
        }
        return 0;
    }

    private int findInList(String str, int o){
        int i=1;
        while(i<listModel.getSize()){
            String _s=listModel.getElementAt(i);
            if(str.equals((_s.split("@"))[o])){
                return i;
            }
            i++;
        }
        return 0;
    }

    private String getNamebyaddr(String ipport){
        int i=1;
        while(i<listModel.getSize()){
            String _s=listModel.getElementAt(i);
            if(ipport.equals((_s.split("@"))[1])){
                return _s.split("@")[0];
            }
            i++;
        }
        return null;
    }

    private String findUser2(String str){
        int i=1;
        while(i<listModel.getSize()){
            String _s=listModel.getElementAt(i);
            if(str.equals((_s.split("@"))[0])){
                return _s;
            }
            i++;
        }
        return null;
    }

    private boolean isMe(String nick, String idd, String port){
        if(nick.equals(name) && idd.equals(id.toString()) && port.equals(srvPort.toString())) {
            return true;
        }
        return false;
    }

    private void addUser(String nick, String ip, String port){
        String item = nick+"@"+ip+":"+port;
        listModel.addElement(item);
        btnsall.setEnabled(true);
    }

    private void removeUser(String nick){
        closeTab(nick);
        listModel.remove(findInList(nick,0));
        map2.remove(nick);
        if(listModel.size()<2)
            btnsall.setEnabled(false);
    }

    private void offline(){
        int i=1;
        while(i<listModel.getSize()){
            String _s=listModel.getElementAt(i);
            String s=_s.split("@")[1];
            String[] s_=s.split(":");
            sendData(s_[0],new Integer(s_[1]),"offline",name);
            i++;
        }
    }


    public void closeTab(String title){
        int i=findInTabbed(title);
        if(i>0)
            tabbedPane.remove(i);
        if(map.get(title)!=null)
            map2.put(title,map.get(title).getText());
        map.remove(title);
        tabbedPane.setSelectedIndex(0);
        list.setSelectedIndex(0);
    }

    private void MSrecive(){//!?
        new Thread(new Runnable() {
            public void run() {
                try{
                    MulticastSocket s = new MulticastSocket(msport);
                    s.joinGroup(InetAddress.getByName(msgroup));
                    while(!Thread.interrupted()){
                        byte buf[] = new byte[1024];
                        DatagramPacket pack = new DatagramPacket(buf, buf.length);
                        s.receive(pack);
                        String rdata=new String(pack.getData()).split("\0")[0];
                        String[] _a=rdata.split("@");
                        String idd=_a[3];
                        String nick=_a[2];
                        String port=_a[1];
                        String msg=_a[0];
                        String ip=pack.getAddress().getHostAddress().toString();
                        if(isMe(nick,idd,port)){
                            continue;
                        }
                        if("online".equals(msg)){
                            if(findInList(nick,0)==0){
                                addUser(nick, ip, port);
                                sendData(ip,new Integer(port),"hi",name);
                            }
                        }
                        Thread.sleep(100);
                    }
                    s.leaveGroup(InetAddress.getByName(msgroup));
                    s.close();
                } catch (IOException | InterruptedException e) {
                    JOptionPane.showMessageDialog(THIS, "Вы не подключены к сети", "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        }).start();
    }

    private void sendData(String address, Integer port, String type, String data){
        try {
            Socket socket = new Socket(address,port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(type);
            out.writeUTF(srvPort.toString());
            out.writeUTF(data);
            socket.close();
        } catch (IOException e ) {
            removeUser(getNamebyaddr(address+":"+port));
        }
    }

    public static void main(String[] args) {

        final MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        MSonline();
    }
}