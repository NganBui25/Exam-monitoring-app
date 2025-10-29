package Client.Controller.Teacher;
import java.util.ArrayList;
import java.util.Map;

import Client.Constant;
import Client.commom.DTO.InContest.Teacher.ImageModel;

public class CheckThread extends Thread {
	private TeacherController par;

	public CheckThread(TeacherController par) {
		this.par = par;
	}

	public void run() {
		while (par.running) {
			try {
                // Tối ưu: Nếu không có gì, ngủ 10ms thay vì 1000ns
                if (par.images.isEmpty()) {
                    Thread.sleep(10);
                    continue; // Quay lại đầu vòng lặp
                }

                // LOGIC CŨ CỦA BẠN (GIỮ NGUYÊN - VÌ NÓ ĐÃ ĐÚNG)
                // Nó quét 'par.images' (giờ chỉ chứa Keyframe)
                for (Map.Entry<Integer, ArrayList<ImageModel>> entry : par.images.entrySet()) {
                    int studentNum = entry.getKey();
                    ArrayList<ImageModel> studentImages = entry.getValue();
                    for (int i = 0; i < studentImages.size(); i++) {
                        ImageModel img = studentImages.get(i);
                        if (img == null)
                            continue;
                        
                        // Nếu Keyframe đã hoàn chỉnh
                        if (img.getTotal() == img.getData().size()) {
                            while (studentNum >= par.viewList.size())
                                par.viewList.add(null);
                            
                            // Đẩy Keyframe vào viewList để ViewThread xử lý
                            par.viewList.set(studentNum, img);
                            studentImages.set(i, null); // Xóa khỏi map lắp ráp
                            break;
                        } 
                        // Nếu Keyframe bị lỗi (mất gói và quá giờ)
                        else if (System.currentTimeMillis() - img.getTime() > Constant.TIMEOUT) {
                            studentImages.set(i, null); // Hủy bỏ
                        }
                    }
                }
                
                // Ngủ 1 chút để nhường CPU
                Thread.sleep(5);

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                // Bắt các lỗi khác (ví dụ: lỗi đồng bộ)
                e.printStackTrace();
            }
        }
	}
}