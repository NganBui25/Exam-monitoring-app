package Client.Controller.Teacher;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream; 
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import Client.Constant;
import Client.commom.DTO.InContest.Teacher.ImageModel;
import Client.commom.DTO.InContest.Teacher.Packet;

public class ViewThread extends Thread {
	private TeacherController par;
	private ByteBuffer byteBuffer = ByteBuffer.allocate(2);

	public ViewThread(TeacherController par) {
		this.par = par;
	}

	public void run() {
		while (par.running) {
            try {
                // Cờ theo dõi xem luồng có làm gì không
                boolean didWork = false; 

                // === CÔNG VIỆC 1: XỬ LÝ KEYFRAME (Ảnh đầy đủ) ===
                // Quét 'viewList' để tìm Keyframe đã lắp ráp xong
                for (int i = 0; i < par.viewList.size(); i++) {
                    ImageModel keyframeModel = par.viewList.get(i);
                    
                    if (keyframeModel != null) {
                        par.viewList.set(i, null); // "Tiêu thụ" ảnh

                        // 1. Lắp ráp các mảnh thành 1 byte[] JPEG
                        byte[] completeData = assemblePackets(keyframeModel.getData());
                        
                        // 2. GỌI HÀM MỚI CỦA VIEW: Gửi byte[] JPEG lên
                        par.view.updateKeyframeForStudent(i, completeData);
                        didWork = true;
                    }
                }

                // === CÔNG VIỆC 2: XỬ LÝ DELTA-FRAME (Ô thay đổi) ===
                // Xử lý nhanh tất cả các ô delta trong 'deltaQueue'
                Packet deltaPacket;
                while ((deltaPacket = par.deltaQueue.poll()) != null) {
                    byte[] data = deltaPacket.getData();
                    
                    // 1. Đọc header
                    int studentNum = data[0] & 0xFF; // Lấy studentNum
                    
                    // Dùng ByteBuffer để đọc 2 byte (short) của X
                    byteBuffer.clear();
                    byteBuffer.put(data, Constant.OFFSET_DELTA_X, 2);
                    byteBuffer.flip();
                    int tileX = byteBuffer.getShort();
                    
                    // Dùng ByteBuffer để đọc 2 byte (short) của Y
                    byteBuffer.clear();
                    byteBuffer.put(data, Constant.OFFSET_DELTA_Y, 2);
                    byteBuffer.flip();
                    int tileY = byteBuffer.getShort();

                    // 2. Lấy dữ liệu ảnh của ô (byte[] JPEG của ô)
                    int imgLength = deltaPacket.getLength() - Constant.HEADER_SIZE;
                    byte[] tileData = new byte[imgLength];
                    System.arraycopy(data, Constant.HEADER_SIZE, tileData, 0, imgLength);

                    // 3. GỌI HÀM MỚI CỦA VIEW: Gửi ô (tile) lên
                    par.view.drawDeltaTileForStudent(studentNum, tileData, tileX, tileY);
                    didWork = true;
                }

                // Nếu không có gì để làm (không có Keyframe, không có Delta)
                // thì ngủ 10ms để nhường CPU
                if (!didWork) {
                    Thread.sleep(10); 
                }
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	}

	public byte[] assemblePackets(Map<Integer, Packet> packets) throws IOException {
        int totalLength = 0;
        int header = Constant.HEADER_SIZE; 
        
        // Tính tổng kích thước (Logic này hơi rủi ro nếu packet đến không đúng thứ tự,
        // nhưng CheckThread đã đảm bảo 'size() == total')
        for (int i = 0; i < packets.size(); i++) {
             Packet packet = packets.get(i);
             if (packet != null) {
                 totalLength += packet.getLength() - header;
             } else {
                 // Nếu 1 packet bị thiếu (lý thuyết CheckThread đã chặn)
                 throw new IOException("Missing packet " + i + " in keyframe assembly");
             }
        }

        byte[] completeData = new byte[totalLength];
        int currentPos = 0;
        
        // Lắp ráp
        for (int i = 0; i < packets.size(); i++) {
            Packet packet = packets.get(i);
            int dataLength = packet.getLength() - header;
            System.arraycopy(packet.getData(), header, completeData, currentPos, dataLength);
            currentPos += dataLength;
        }
        return completeData;
    }
}