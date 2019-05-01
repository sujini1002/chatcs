package com.cafe24.network.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;


public class ChatServerThread extends Thread {
	
	private String nickname;
	private List<Writer> writers;
	private Socket socket;
	private BufferedReader br;
	private PrintWriter pr;
	
	public ChatServerThread(Socket socket,List writers) {
		this.socket = socket;
		this.writers = writers;
	}

	@Override
	public void run() {
		//1. 연결한 소캣의 주소를 가져움
		InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress)socket.getRemoteSocketAddress();
		String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress();
		int remotePort = inetRemoteSocketAddress.getPort();
		
		ChatServer.log("connected bt client["+remoteHostAddress + "," + remotePort + "]");
		
		try {
				//2. 스트림 얻기
				br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
				pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"),true);
			
				//3. 요청 처리
				while(true) {
						String request = br.readLine();
						//System.out.println("-----"+request+"----------");
						if(request == null) {
							ChatServer.log("클라이언트로 부터 연결이 끊어짐");
							break;
						}
						//4. 프로토콜 분석
						String[] tokens = request.split(":");
						
						//tokens 테스트
//						for(String tmp : tokens) {
//							System.out.println("------테스트----- : tmp : "+tmp);
//						}
						
						if("JOIN".equals(tokens[0])) {
							doJoin(tokens[1],pr);
						}else if("MESSAGE".equals(tokens[0])) {
							doMessage(tokens[1]);
						}else if("QUIT".equals(tokens[0])) {
							doQuit(pr);
						}else {
							ChatServer.log("ERROR : 알 수 없는 요청 ["+tokens[0]+"]");
						}
						
					}//end while
			}catch(SocketException e) {
				//갑자기 끊어진 오류
				//doQuit(pr);
				System.out.println("[server] sudden closed by client");
			}catch(IOException e) {
				e.printStackTrace();
			}finally {
				try {
					if(socket != null && !socket.isClosed())
						socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	}

	private void doQuit(PrintWriter pr) {
		removeWriter(pr);
		
		//ack
		pr.println("delete ok");
		pr.flush();
		
		String data = this.nickname + "님이 퇴장하였습니다.";
		broadcast(data);
	}

	private void removeWriter(PrintWriter pr) {
		synchronized (writers) {
			writers.remove(pr);
		}
		
	}
	//메세지 받기
	private void doMessage(String message) {
		String data = this.nickname + ": "+ message;
		broadcast(data);
	}

	private void doJoin(String nickname, PrintWriter pr) {
		this.nickname = nickname;
		
		String data = nickname + "님이 들어왔습니다.";
		broadcast(data);
		
		addWriter(pr);
		
		//ack
		pr.println("join:ok");
		pr.flush();
		
	}

	private void addWriter(PrintWriter pr) {
		synchronized (writers) {
			writers.add(pr);
		}
	}
	private void broadcast(String data) {
		synchronized (writers) {
			for(Writer writer : writers) {
				PrintWriter pr = (PrintWriter)writer;
				pr.println(data);
				pr.flush();
			}
		}
	}
	

}//end ChatServerThread
