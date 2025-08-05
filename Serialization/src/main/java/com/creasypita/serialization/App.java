package com.creasypita.serialization;

import java.io.*;

/**
 * Created by lujq on 11/22/2021.
 */
public class App {

    public static void main(String[] args) throws IOException {

        String filename = "E:\\work\\myproject\\java\\Java-Labs\\Serialization\\src\\main\\resources\\output\\a.txt";
//        Obj1 obj1 = new Obj1();
//        obj1.setF1("abcdef");
//        FileOutputStream fileOutputStream = new FileOutputStream(filename);
//
//        try(ObjectOutputStream output = new ObjectOutputStream(fileOutputStream)) {
//            output.writeObject(obj1);
//        }

        FileInputStream fileInputStream = new FileInputStream(filename);
        try(ObjectInputStream input = new ObjectInputStream(fileInputStream);){
            try {
                Obj1 o = ((Obj1) input.readObject());
                System.out.println(o.getF1());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    class Obj2{
        private static final long serialVersionUID = 1L;
        public String getF1() {
            return f1;
        }

        public void setF1(String f1) {
            this.f1 = f1;
        }

        public String getF2() {
            return f2;
        }

        public void setF2(String f2) {
            this.f2 = f2;
        }

        private String f1;
        private String f2;
    }
}
