package com.shelfx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class PortThread extends Thread {
	private ArrayList<EchoThread> lists;
	private int port=0;
	public PortThread(int port,ArrayList<EchoThread> lists) {
		this.lists = lists;
		this.port=port;
		System.out.println("Port Thread created");
	}

	public void run() {
		ServerSocket serverSocket = null;
		Socket socket = null;
		System.out.println("Trying Socket Server Started on port "+port+"...");
		try {
			serverSocket = new ServerSocket(port);
			//serverSocket.setSoTimeout(25000);
			System.out.println("Socket Server Started on port "+port);
		} catch (IOException e) {
			e.printStackTrace();

		}
		while (true) {
			try {
				socket = serverSocket.accept();
				try{
					// new threa for a client
					synchronized (lists) {
						EchoThread echo = new EchoThread(socket,lists);
						socket.setSoTimeout(25000);
						lists.add(echo);	
						echo.start();
					}

				} finally {
					//socket.close();
				}

			} catch (IOException e) {
				System.out.println("I/O error: " + e);
			}

		}
	}

}
