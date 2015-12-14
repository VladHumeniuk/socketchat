package socket.chat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import socket.chat.Const;

public class Server {

	public static void main(String[] args) {
		new Server();
	}

	private List<String> users = new ArrayList<String>(){{add("vlad 123321"); add("android android");}};

	private List<Connection> connections = 
			Collections.synchronizedList(new ArrayList<Connection>());
	private ServerSocket server;

	public Server() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Scanner scanner = new Scanner(System.in);
				while(true) {
					if ("shutdown".equals(scanner.nextLine())) {
						closeAll();
					}
				}
			}
		}).start();
		try {
			server = new ServerSocket(Const.Port);

			while (true) {
				Socket socket = server.accept();

				Connection con = new Connection(socket);
				connections.add(con);

				con.start();

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeAll();
		}
	}

	private boolean login(String user, String pass) {
		return users.contains(user + " " + pass);
	}

	private void closeAll() {
		try {
			server.close();

			synchronized(connections) {
				Iterator<Connection> iter = connections.iterator();
				while(iter.hasNext()) {
					((Connection) iter.next()).close();
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	private class Connection extends Thread {
		private BufferedReader in;
		private PrintWriter out;
		private Socket socket;
	
		private String name = "";

		public Connection(Socket socket) {
			this.socket = socket;
	
			try {
				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
	
			} catch (IOException e) {
				e.printStackTrace();
				close();
			}
		}

		@Override
		public void run() {
			try {
				name = in.readLine();
				String pass = in.readLine();
				if (!login(name, pass)) {
					out.println("wrong");
					close();
				} else {
					out.println("ok");
				}
				synchronized(connections) {
					Iterator<Connection> iter = connections.iterator();
					while(iter.hasNext()) {
						((Connection) iter.next()).out.println(name + " is online");
					}
				}
				
				String str = "";
				while (true) {
					str = in.readLine();
                    if (str == null) break;
					if(str.equals(".exit")) break;

					synchronized(connections) {
						Iterator<Connection> iter = connections.iterator();
						while(iter.hasNext()) {
							((Connection) iter.next()).out.println(name + ": " + str);
						}
					}
				}
				
				synchronized(connections) {
					Iterator<Connection> iter = connections.iterator();
					while(iter.hasNext()) {
						((Connection) iter.next()).out.println(name + " left");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close();
			}
		}

		public void close() {
			try {
				out.println(".server_exit");
				in.close();
				out.close();
				socket.close();

				connections.remove(this);
				if (connections.size() == 0) {
					//Server.this.closeAll();
					//System.exit(0);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}
	}
}
