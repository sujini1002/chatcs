package com.cafe24.network.chat.client;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private Socket socket;
	private innerclass thread;
	//private BufferedReader br;

	public ChatWindow(String name,Socket socket) {
		frame = new Frame(name);
		this.socket = socket;
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		
		this.thread = new innerclass(socket);
		thread.start();
	}
	private void finish() {
//			try {
//				PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"),true);
//				pr.println("QUIT");
//				thread.join();
//				if(socket==null&&!socket.isClosed()) {
//					socket.close();
//				}
//				System.exit(0);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} 
			try {
				if(socket!=null && socket.isClosed() == false) {
					socket.close();
					System.exit(0);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
		
		
	
	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent actionEvent ) {
				//버튼 누를때 전송
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if(keyCode == KeyEvent.VK_ENTER) {
					//Enter쳤을 때 전송
					sendMessage();
				}
			}
		});

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish();
			}
		});
		frame.setVisible(true);
		frame.pack();
		//thread 생성
	}
	//쓰레드에 불러야한다.
	private void updateTextArea(String message) {
		textArea.append(message);
		textArea.append("\n");
	}
	private void sendMessage() {
		String message = textField.getText();
		
		//귓소말 구별
		if(message.length()>5 && "[귓속말]".equals(message.substring(0, 5))) {
			message = message.substring(6,message.length());
			whisper(message);
		}else {
			System.out.println("[client]가 보낸 메세지"+message);
			ToServer("MESSAGE:"+message);
		}
			
		//창비우기
		textField.setText("");
		textField.requestFocus();	
		
	}
	private void whisper(String message) {
		String[] whisperMessage = message.split(":");
		if(whisperMessage.length<2) {
			updateTextArea("===========================");
			updateTextArea("귓속말 전하는 형식이 잘 못 되었습니다 .");
			updateTextArea("[귓속말]:[수신자]:메세지 ");
			updateTextArea("다시 시도해 주세요.");
			updateTextArea("===========================");
		}else {
			
			updateTextArea("귓속말로 "+whisperMessage[0]+"에게 ["+whisperMessage[1]+"]라고 보내셨습니다.");
			String receiver = whisperMessage[0].substring(1, whisperMessage[0].length()-1);
			message = "WHISPER:"+receiver+":"+whisperMessage[1];
			ToServer(message);
		}
	}
	private void ToServer(String data) {
		
		try {
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),true);
			pr.println(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private  class innerclass extends Thread {

		private Socket socket;
		public innerclass(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				BufferedReader br =  new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
				while (true) {

					String newMessage = br.readLine();
					if (newMessage == null) {
						break;
					}
					updateTextArea(newMessage);
				}
			} catch (IOException e) {
				System.out.println("[client] 퇴장하였습니다.");
			}
		}
	}
		
	
	
}
