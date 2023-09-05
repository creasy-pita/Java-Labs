package com.creasypita.io;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;

/**
 * Created by lujq on 9/5/2023.
 * 功能：服务端完成客户端字符的大写转换
 *
 * TransClient
 * 需要从键盘中输入，所以考虑IO流，可以考虑缓存输入字符流bufferReader
 * 输入源设备：键盘
 * 目标： socket
 *
 * 步骤
 * 开启socket连接端口
 * 创建控制台输入流
 * 输入字符换行后输出到socket的输出流
 * 获取socket输入流响应大写字符
 * 输入over时结束输出，关闭资源
 *
 * TransServer
 * 输入源：socket输入流
 * 目标：socket输出流
 *
 * 步骤
 * 建立服务
 * 获取客户端的字符数据转换为大写，回传给客户端
 * 如果检查到over,结束并关闭资源
 */
public class SockectDemo {


}

class TransClient{
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("127.0.0.1", 8000);
        BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in));

        BufferedWriter bufOut = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader bufIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String line = null;
        while((line = bufr.readLine())!=null){
            if ("over".equals(line)) {
                break;
            }
            //发送到服务端
            bufOut.write(line);
            //发送换行，服务端readline才能获取到数据并跳出阻塞
            bufOut.newLine();
            bufOut.flush();
            //等待服务端放回字符数据
            String responseLine = bufIn.readLine();
            //获取后打印
            System.out.println(responseLine);
        }
        bufr.close();
        bufIn.close();
        //socket.close会向服务端发送结束标记-1
        s.close();
    }
}

class TransServer{

    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket(8000);
        Socket s = ss.accept();
        BufferedReader bfIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter bfwr = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        String line = null;
        while((line = bfIn.readLine())!=null){
            System.out.println("server get:" + line);
            bfwr.write(line.toUpperCase(Locale.ROOT));
            bfwr.newLine();
            bfwr.flush();
        }

        bfIn.close();
        bfwr.close();
        s.close();
        ss.close();
    }

}
