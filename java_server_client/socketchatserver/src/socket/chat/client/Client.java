package socket.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import socket.chat.Const;

public class Client {
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;

	public static void main(String[] args) {
		new Client();
	}

	public Client() {
		Scanner scan = new Scanner(System.in);

		System.out.println("give server ip");

		String ip = scan.nextLine();

		try {
			socket = new Socket(ip, Const.Port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			System.out.println("your name:");
			out.println(scan.nextLine());

			System.out.println("your password:");
			out.println(scan.nextLine());

			String result = in.readLine();
			if ("wrong".equals(result)) {
				close();
				return;
			}

			Resender resend = new Resender();
			resend.start();

			String str = "";
			while (!str.equals(".exit")) {
				str = scan.nextLine();
				out.println(str);
			}
			resend.setStop();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private void close() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private class Resender extends Thread {

		private boolean stoped;

		public void setStop() {
			stoped = true;
		}

		@Override
		public void run() {
			try {
				while (!stoped) {
					String str = in.readLine();
					if (str == null)  {
						continue;
					}
					if (".server_exit".equals(str)) {
						close();
					}
					System.out.println(str);
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}

}
