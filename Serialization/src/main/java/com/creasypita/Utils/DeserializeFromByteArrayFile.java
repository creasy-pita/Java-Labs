package com.creasypita.Utils;

import com.creasypita.obj.Person;

import java.io.*;

public class DeserializeFromByteArrayFile {
    public static void main(String[] args) {
        // 1. 从文件读取字节数组
        byte[] serializedData = readBytesFromFile("E:\\temp3\\person_bytes.ser");

        // 2. 将字节数组反序列化为对象
        Person person = (Person) deserializeFromBytes(serializedData);

        System.out.println("从字节数组反序列化的对象: " + person);
    }

    // 从文件读取字节数组
    private static byte[] readBytesFromFile(String filename) {
        try (FileInputStream fileIn = new FileInputStream(filename);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileIn.read(buffer)) != -1) {
                byteOut.write(buffer, 0, bytesRead);
            }
            return byteOut.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("文件读取失败", e);
        }
    }

    // 将字节数组反序列化为对象
    private static Object deserializeFromBytes(byte[] data) {
        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
             ObjectInputStream in = new ObjectInputStream(byteIn)) {

            return in.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}
