package com.cafe24.network.chat.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
	
	private static final int PORT = 5180;
	
	
	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		List<PrintWriter> writers = new ArrayList<PrintWriter>();
		try {
			//1. 서버소켓 생성
			serverSocket = new ServerSocket();
			
			//2. 바인딩(binding)
			serverSocket.bind(new InetSocketAddress("0.0.0.0", PORT));
			log("연결 기다림,,, PORT :"+PORT);
			
			while(true) {
				//3. accept : 클라이언트의 연결요청을 기다린다.
				Socket socket = serverSocket.accept(); // 블라킹 (Blocking) -connect가 들어오면 연결
			
				new ChatServerThread(socket,writers).start();
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (serverSocket != null && !serverSocket.isClosed())
					serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}//end main
	public static void log(String log) {
		System.out.println("[server# "+Thread.currentThread().getId()+"]" + log);
	}
}
