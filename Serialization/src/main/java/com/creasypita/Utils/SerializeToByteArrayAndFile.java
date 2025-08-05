package com.creasypita.Utils;
import com.creasypita.obj.Person;

import java.io.*;

public class SerializeToByteArrayAndFile {
    public static void main(String[] args) {
        Person person = new Person("李四", 30, "上海市浦东新区");

        // 1. 序列化对象到字节数组
        byte[] serializedData = serializeToBytes(person);

        // 2. 将字节数组存储到文件
        saveBytesToFile(serializedData, "E:\\temp3\\person_bytes.ser");

        System.out.println("对象已序列化为字节数组并保存到文件");
    }

    // 将对象序列化为字节数组
    private static byte[] serializeToBytes(Serializable obj) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteOut)) {

            out.writeObject(obj);
            return byteOut.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }

    // 将字节数组保存到文件
    private static void saveBytesToFile(byte[] data, String filename) {
        try (FileOutputStream fileOut = new FileOutputStream(filename)) {
            fileOut.write(data);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }
}
