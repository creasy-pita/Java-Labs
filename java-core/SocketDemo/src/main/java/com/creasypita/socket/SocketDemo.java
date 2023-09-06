package com.creasypita.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class SocketDemo {
}

/**
 * 需求
 * 服务端监听指定客户端的，接收并打印客户端的数据
 *
 * 创建udp客户端
 * 从控制台输入数据，发送到指定的host(ip或者域名)，端口
 * 输入886时结束，关闭资源
 *
 */
class UdpSend{

    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket();
        BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while((line = bufr.readLine()) != null){
            if ("886".equals(line)) {
                break;
            }
            byte[] bytes = line.getBytes();
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), 10001);
            ds.send(dp);
        }
        ds.close();
    }
}

class UdpRec {
    public static void main(String[] args) throws IOException {
        DatagramSocket ds = new DatagramSocket(10001);
        while (true) {
            byte[] bytes = new byte[1024];
            DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
            ds.receive(dp);
            System.out.println(dp.getSocketAddress() + "::" + new String(dp.getData(), StandardCharsets.UTF_8));
        }
    }
}

/**
 * 聊天客户端1，包括发送，接受数据
 * 开启发送端，往10001端口发送数据
 * 开启接收端，从10002端口接受数据
 */
class UdpSendAndRecClient1{

    public static void main(String[] args) {
        int sendPort = 10001;
        int receivePort = 10002;
        new UdpSendThread(sendPort,"[11]").start();
        new UdpRecThread(receivePort).start();
    }
}
/**
 * 聊天客户端2，包括发送，接受数据
 * 开启发送端，往10002端口发送数据
 * 开启接收端，从10001端口接受数据
 */
class UdpSendAndRecClient2{

    public static void main(String[] args) {
        int sendPort = 10002;
        int receivePort = 10001;
        new UdpSendThread(sendPort,"[22]").start();
        new UdpRecThread(receivePort).start();
    }
}

/**
 * 循环接受控制台输入，并发送到指定
 */
class UdpSendThread extends Thread{

    private int sendPort;
    private String clientName;

    UdpSendThread(int sendPort, String clientName){
        this.sendPort = sendPort;
        this.clientName = clientName;
    }

    @Override
    public void run() {
        DatagramSocket ds = null;
        try{
            ds = new DatagramSocket();
            BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));
            String line;
            while((line = bufr.readLine()) != null){
                if ("886".equals(line)) {
                    break;
                }
                byte[] bytes = (clientName + "::"+ line).getBytes();
                DatagramPacket dp = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), sendPort);
                ds.send(dp);
            }

        } catch (IOException e){
            e.printStackTrace();
        } finally{
            if (ds !=null) {
                ds.close();
            }
        }
    }
}

class UdpRecThread extends Thread{
    private int receivePort;
    public UdpRecThread(int receivePort){
        this.receivePort = receivePort;
    }

    @Override
    public void run(){
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(receivePort);
            while (true) {
                byte[] bytes = new byte[1024];
                DatagramPacket dp = new DatagramPacket(bytes, bytes.length);
                ds.receive(dp);
                System.out.println(dp.getAddress() + "::" + new String(dp.getData(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}