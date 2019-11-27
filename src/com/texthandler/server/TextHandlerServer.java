package com.texthandler.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TextHandlerServer {
	
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(8888);
		while(true) {
			Socket client = server.accept();
			System.out.println("一个客户端建立了连接");
			new Thread(new Channel(client)).start();
		}
	}
	
	static class Channel implements Runnable{

		private Socket client;
		private boolean isRunning;
		private DataInputStream dis;
		private DataOutputStream dos;
		private FileReader fr;
		private FileOutputStream fos;
		
		public Channel(Socket client) {
			this.client = client;
			try {
				dis = new DataInputStream(client.getInputStream());
				dos = new DataOutputStream(client.getOutputStream());
				fr = new FileReader(new File(FinalData.filePath));
				isRunning = true;
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("---1------");
			}
		}
		
		@Override
		public void run() {
			while(isRunning) {
				String Request = Receive();
				String text = "";
				if("load".equals(Request)) {
					System.out.println("收到请求");
					try {
						char[] buffer = new char[10*1024];
						while(fr.read(buffer) != -1) {
							Send(new String(buffer));
							buffer = new char[10*1024];
						}
						dos.flush();
					} catch (IOException e) {
						System.out.println("---4------");
					}
				}else if("upload".equals(Request)){
					System.out.println("收到请求");
					String fileName = "img/"+getRandomString(5)+".jpg";
					try {
						fos = new FileOutputStream(new File(fileName));
						byte[] temp = new byte[1024*10];
						int length = 0;
						try {
							while ((length = dis.read(temp, 0, temp.length)) > 0) {
							    fos.write(temp, 0, length);
							    fos.flush();
							}
						} catch (IOException e) {
							System.out.println("-----9-----");
						}
					} catch (FileNotFoundException e) {
						System.out.println("-----8-----");
					}
				}
				
			}
		}
		
		private String Receive() {
			String request = "";
			try {
				request = dis.readUTF();
			} catch (IOException e) {
				System.out.println("---2------");
				release();
			}
			return request;
		}
		
		private void Send(String byteTxt) {
			try {
				dos.writeUTF(byteTxt);
			} catch (IOException e) {
				System.out.println("---3------");
				System.out.println(e.getMessage());
				release();
			}
		}

		private void release() {
			this.isRunning = false;
			Utils.close(dis, dos, fr, client);
		}
		
		private String getRandomString(int length){
		     String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		     Random random = new Random();
		     StringBuffer sb = new StringBuffer();
		     for(int i = 0; i < length; i++){
		       int number = random.nextInt(62);
		       sb.append(str.charAt(number));
		     }
		     return sb.toString();
		 }
	}
}
