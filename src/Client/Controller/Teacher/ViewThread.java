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
//		while (par.running) {
//			if(par.viewList.size() == 0) {
//				try {
//					Thread.sleep(0, 1000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			try {
//				//Duyệt qua danh sách các ảnh chờ hiển thị
//				for(int i = 0; i < par.viewList.size(); i++) {
//					if(par.viewList.get(i) != null) {
//						long start = System.nanoTime();
//						ImageModel tmp = par.viewList.get(i);
//						par.viewList.set(i, null);
//						//Lắp rắp ảnh
//						byte[] completeData = assemblePackets(tmp.getData());
//						par.setImage(completeData, i);
//						System.out.println(System.nanoTime() - start);
//					}
//				}
//				try {
//				Thread.sleep(1);
//				} catch (InterruptedException e) {
//					//TODO Auto-generated catch block
//					e.printStackTrace();
//			}
//			} catch (NullPointerException e) {
//				e.printStackTrace();
//			}
//		}
//		Map<Integer, Boolean> updatedCanvases = new HashMap<>();
//		while(par.running) {
//			try {
//				for(int i = 0; i < par.viewList.size(); i++) {
//					ImageModel keyframeModel = par.viewList.get(i);
//					if(keyframeModel != null) {
//						par.viewList.set(i, null);
//						byte[] completeData = assemblePackets(keyframeModel.getData());
//						BufferedImage keyframeImage = ImageIO.read(new ByteArrayInputStream(completeData));
//						
//						par.studentCanva.put(i, keyframeImage);
//						par.setImage(completeData, i);
//					}
//				} 
//				Packet deltaPacket;
//				while((deltaPacket = par.deltaQueue.poll()) != null) {
//					byte[] data = deltaPacket.getData();
//					int studentNum = data[0] & 0xFF;
//					byteBuffer.clear();
//					byteBuffer.put(data, Constant.OFFSET_DELTA_X,2);
//					byteBuffer.flip();
//					int tileX = byteBuffer.getShort();
//					
//					byteBuffer.clear();
//                    byteBuffer.put(data, Constant.OFFSET_DELTA_Y, 2);
//                    byteBuffer.flip();
//                    int tileY = byteBuffer.getShort();
//                    
//                    BufferedImage canvas = par.studentCanva.get(studentNum);
//                    
//                    if(canvas == null) {
//                    	continue;
//                    }
//                    //Lấy dữ liệu ảnh của ô
//                    int imgLength = deltaPacket.getLength() - Constant.HEADER_SIZE;
//                    ByteArrayInputStream bais = new ByteArrayInputStream(data, Constant.HEADER_SIZE,imgLength);
//                    BufferedImage tileImage = ImageIO.read(bais);
//                    
//                    //Vẽ đè ô này len canvas
//                    Graphics2D g = canvas.createGraphics();
//                    g.drawImage(tileImage, tileX, tileY, null);
//                    g.dispose();
//                    
//                    updatedCanvases.put(studentNum, true);
//				}
//				// === CÔNG VIỆC 3: CẬP NHẬT VIEW SAU KHI VẼ DELTA ===
//                // Nén lại và gửi các canvas "bẩn" lên View
//                if (!updatedCanvases.isEmpty()) {
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    for (Map.Entry<Integer, Boolean> entry : updatedCanvases.entrySet()) {
//                        int studentNum = entry.getKey();
//                        BufferedImage canvas = par.studentCanva.get(studentNum);
//                        
//                        // Nén lại canvas (đã update) thành JPEG
//                        baos.reset();
//                        ImageIO.write(canvas, "jpg", baos);
//                        
//                        // Gửi byte[] JPEG mới lên View
//                        par.setImage(baos.toByteArray(), studentNum);
//                    }
//                    updatedCanvases.clear(); // Xóa dấu
//                }
//
//                // Ngủ 1 chút (ví dụ 10ms ~ 100 FPS) để nhường CPU
//                Thread.sleep(10);
//			} catch(Exception e) {
//				
//			}
//		}
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