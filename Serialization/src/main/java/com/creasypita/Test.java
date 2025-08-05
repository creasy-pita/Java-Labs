package com.creasypita;

import com.creasypita.io.DiagnosticObjectInputStream;
import com.creasypita.obj.Department;
import com.creasypita.obj.Leader;
import com.creasypita.obj.User;

import java.io.*;

/**
 * Created by lujq on 8/3/2025.
 */
public class Test {
    public static void main(String[] args) {
        User user = new User();
        Department department = new Department();
        Leader leader = new Leader();
        leader.setName("l");
        department.setName("d");
        department.setLeader(leader);
        user.setName("a");
        user.setDepartment(department);

//        System.out.println(deepClone(user).getDepartment().getName());
        System.out.println(deepClone2(user).getDepartment().getName());

    }

    public static User deepClone2(Object obj){
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
//        DiagnosticObjectInputStream ois = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            bais = new ByteArrayInputStream(baos.toByteArray());
//            ois= new DiagnosticObjectInputStream(bais);
            ois= new ObjectInputStream(bais);
            return (User)ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Clone Object failed in IO："+e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found.："+e.getMessage(), e);
        }finally{
            try{
                if ( bais != null ) bais.close();
                if ( baos != null ) baos.close();
                if ( oos != null ) oos.close();
                if ( ois != null ) ois.close();
            }catch(IOException e){
            }
        }
    }


    public static User deepClone(Object obj){
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            bais = new ByteArrayInputStream(baos.toByteArray());
            ois= new ObjectInputStream(bais);
            return (User)ois.readObject();
        } catch (IOException e) {
            throw new RuntimeException("Clone Object failed in IO："+e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found.："+e.getMessage(), e);
        }finally{
            try{
                if ( bais != null ) bais.close();
                if ( baos != null ) baos.close();
                if ( oos != null ) oos.close();
                if ( ois != null ) ois.close();
            }catch(IOException e){
            }
        }
    }
}
