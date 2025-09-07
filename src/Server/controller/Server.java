package Server.controller;
//ifconfig | grep "inet " | grep -v 127.0.0.1

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import Server.dto.Room;

public class Server {
	public static Map<Integer, Room> rooms = new ConcurrentHashMap<>();
	public static AtomicInteger NEW_ROOM_ID = new AtomicInteger(0);
	public static AtomicInteger ID = new AtomicInteger(0);
	public static final long TIMEOUT = 3000;

	public static void main(String[] args) {
		new Thread(new TCPServer()).start();
		new Thread(new CleanThread()).start();
	}
}

//TCP Server class
class TCPServer implements Runnable {

	@Override
	public void run() {
		try (ServerSocket tcpServer = new ServerSocket(8888)) {
			while (true) {
				Socket clientSocket = tcpServer.accept();
				new Thread(new TCPHandler(clientSocket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

