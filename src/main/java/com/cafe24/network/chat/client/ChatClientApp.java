package com.cafe24.network.chat.client;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

import com.cafe24.network.chat.server.ChatServer;
import com.cafe24.network.chat.server.ChatServerThread;

public class ChatClientApp {
	
	private static final String SERVER_IP = "127.0.0.1";

	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);
		Socket socket = null;

		while( true ) {
			
			System.out.println("대화명을 입력하세요.");
			System.out.print(">>> ");
			name = scanner.nextLine();
			
			//대화명 입력받으면 윈도우창 띄움
			if (name.isEmpty() == false ) {
				break;
			}
			
			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
		}
		try {
			//1. 소캣 만들고
			socket = new Socket();
			socket.connect(new InetSocketAddress(SERVER_IP, ChatServer.PORT));
			//2. iostream 작업
			BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"),true);
			
			//3. join 프로토콜 만들기
			String join = "JOIN:"+name;
			pr.println(join);
			
			//4. join이 성공하면 창 띄우기 
			new ChatWindow(name,socket).show();
			
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
				if(scanner != null)scanner.close();
		}

	}

}
