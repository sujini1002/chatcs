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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


public class ChatServerThread extends Thread {
	
	private String nickname;
	private Map<String,Writer> writers;
	private Socket socket;
	private BufferedReader br;
	private PrintWriter pr;
	
	public ChatServerThread(Socket socket,Map writers) {
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
						System.out.println("[serve] client Send ["+request+"]");
						if(request == null) {
							ChatServer.log("클라이언트로 부터 연결이 끊어짐");
							doQuit(pr);
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
						}else if("WHISPER".equals(tokens[0])){
							whisper(tokens[1],tokens[2]);
						}else {
							ChatServer.log("ERROR : 알 수 없는 요청 ["+tokens[0]+"]");
						}
						
					}//end while
			}catch(SocketException e) {
				//갑자기 끊어진 오류
				System.out.println("[server] sudden closed by client");
				doQuit(pr);
				//e.printStackTrace();
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

	private void whisper(String receiver, String message) {
		//귓속말 전송하기
		//receiver = receiver.replaceAll("\\[", "");
		//receiver = receiver.replaceAll("\\]", "");
		String data = "["+this.nickname + "]께서 ["+ receiver +"]님에게 ["+ message +"]라고 귓속말을 걸었습니다!";
		sendWhisper(receiver,data);
	}

	private void sendWhisper(String receiver, String data) {
		//귓소말 상대에게 보내기
		synchronized (writers) {
			Writer tmpWriter = writers.get(receiver);
			PrintWriter pr = (PrintWriter)tmpWriter;
			pr.println(data);
			pr.flush();
		}
	}

	private void doQuit(PrintWriter pr) {
		
		try {
			//System.out.println("doQuit에 들어옴");
			removeWriter(pr);
			String data = this.nickname + "님이 퇴장하였습니다.";
			broadcast(data);
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//ack
//		pr.println("delete ok");
//		pr.flush();
		
	}

	private void removeWriter(PrintWriter pr) {
		synchronized (writers) {
			getKey(writers,pr);
		}
		
	}
	private void getKey(Map<String, Writer> writers, PrintWriter pr) {
		Iterator<String> iterWrtiers = new HashSet<String>(writers.keySet()).iterator();
		while(iterWrtiers.hasNext()) {
			String key = iterWrtiers.next();
			if(writers.get(key).equals(pr)) {
				writers.remove(key);
			}
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

	private void addWriter(PrintWriter writer) {
		synchronized (writers) {
			writers.put(nickname,writer);
		}
	}
	private void broadcast(String data) {
		synchronized (writers) {
			for(Map.Entry<String,Writer> writer : writers.entrySet()){
				 PrintWriter pr = (PrintWriter)writer.getValue();
				 pr.println(data);
				 pr.flush();
			}
		}
	}
	

}//end ChatServerThread
