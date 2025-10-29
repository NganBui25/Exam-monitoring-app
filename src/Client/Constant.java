package Client;

import java.io.IOException;
import java.net.InetAddress;

public class Constant {
	
	//Server
	public static String IP = "localhost";
	public static InetAddress serverAddress;
	public static final int tcpPort = 8888;
	public static final int udpPort = 9999;
	public static final int PACKET_SIZE = 1104;
	public static final long KEYFRAME_INTERVAL = 5000;
	public static final int TITLE_WIDTH = 100;
	public static final int TITLE_HEIGHT = 100;

	static {
		try {
			serverAddress = InetAddress.getByName(IP);
		} catch (IOException e) {
			System.exit(1);
		}
	}
	
	public static final byte PACKET_TYPE_KEYFRAME = 0x00;
	public static final byte PACKET_TYPE_DELTA = 0x01;
	
	public static final int HEADER_SIZE = 9;
	
	public static final int OFFET_IMG_NUM = 1;
	public static final int OFFSET_PACKET_TYPE = 2; // [Byte 2]
	// Keyframe
	public static final int OFFSET_KEY_TOTAL = 3;   // [Byte 3]
	public static final int OFFSET_KEY_NUM = 4;     // [Byte 4]
	// Delta
	public static final int OFFSET_DELTA_X = 5;     // [Byte 5-6]
	public static final int OFFSET_DELTA_Y = 7;     // [Byte 7-8]
	
	// Kích thước dữ liệu ảnh (CẬP NHẬT LẠI)
	public static final int IMAGE_SEGMENT = PACKET_SIZE - HEADER_SIZE; // 1104 - 9 = 1095
	

	//Student
	public static final int NORMAL_WIDTH = 450;
	public static final int NORMAL_HEIGHT = 300;
	public static final int FOCUS_WIDTH = 900;
	public static final int FOCUS_HEIGHT = 600;
	public static final long KEYBOARD_DURATION = 10000; // 5 phut: 300000
	public static final int MAX_SCREENS = 4;
	public static final int MAX_CAMS = 8;
	public static int camWidth;
	public static int camHeight;
	public static int screenWidth;
	public static int screenHeight;
	public static int DELTA_THRESHOLD = 50;
	
	//Teacher
	public static final int PROCESS_THREADS = 3;
	public static final int VIEW_THREADS = 1;
	public static final long TIMEOUT = 4;
}
