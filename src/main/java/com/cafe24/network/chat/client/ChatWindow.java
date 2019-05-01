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

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private Socket socket;

	public ChatWindow(String name,Socket socket) {
		frame = new Frame(name);
		this.socket = socket;
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		
		new innerclass(socket).start();
	}
	private void finish() {
		try {
			//소켓정리는 여기서
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"),true);
			String send = "QUIT";
			pr.println(send);
			System.exit(0);
		} catch (IOException e) {
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
		try {
			PrintWriter pr = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"),true);
			String data = "MESSAGE:"+message;
			//보낸 후 비우기
			//textField로 포커스 맞추기
			pr.println(data);
			textField.setText("");
			textField.requestFocus();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class innerclass extends Thread {

		private Socket socket;
		public innerclass(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				BufferedReader br =  new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
				while (true) {

					String newMessage = br.readLine();
					if (newMessage == null) {
						break;
					}
					updateTextArea(newMessage);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		
	
	
}
