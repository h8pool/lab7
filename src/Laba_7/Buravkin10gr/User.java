package Laba_7.Danilin_8gr;

import java.io.*;
import java.util.ArrayList;

public class User {
    private static String name;
    private static int addres;


    public User (String name, int addres) {
        this.name = name;
        this.addres = addres;
    }



    public  static String getName() {
        return name;
    }

    public static int getAddres() {
        return addres;
    }
}
