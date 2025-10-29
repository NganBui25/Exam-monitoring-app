package Client.View;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/**
 * Đây là một JPanel "thông minh" thay thế cho JLabel.
 * Nó sở hữu một "bảng vẽ" (canvas) BufferedImage của riêng mình.
 * Nó có thể nhận Keyframe (ảnh đầy đủ) hoặc Delta-frame (ô) và tự vẽ lại.
 */
public class StudentDisplayPanel extends JPanel {

    // "Bảng vẽ" (canvas) mà chúng ta sẽ vẽ lên
    private BufferedImage canvas;

    public StudentDisplayPanel() {
        // Kích thước mặc định, bạn có thể thay đổi
        setPreferredSize(new Dimension(450, 300)); 
    }

    /**
     * Hàm quan trọng nhất, được gọi mỗi khi repaint().
     * Nó chỉ đơn giản là vẽ 'canvas' lên màn hình.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas != null) {
            // Vẽ canvas, co giãn vừa với kích thước của Panel
            g.drawImage(canvas, 0, 0, getWidth(), getHeight(), null);
        }
    }

    /**
     * HÀM MỚI 1: Nhận một Keyframe (ảnh đầy đủ).
     * Giải nén byte[] và THAY THẾ hoàn toàn canvas.
     */
    public void updateKeyframe(byte[] jpegData) {
        try {
            this.canvas = ImageIO.read(new ByteArrayInputStream(jpegData));
            this.repaint(); // Yêu cầu vẽ lại toàn bộ panel
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * HÀM MỚI 2: Nhận một Delta-frame (ô).
     * Giải nén ô và VẼ ĐÈ lên canvas.
     */
    public void drawDeltaTile(byte[] tileData, int x, int y) {
        // Nếu chưa có canvas (chưa nhận Keyframe), bỏ qua ô này
        if (this.canvas == null) return; 

        try {
            BufferedImage tileImage = ImageIO.read(new ByteArrayInputStream(tileData));
            
            // Lấy "bút vẽ" của canvas và VẼ ĐÈ ô đó lên
            Graphics2D g = this.canvas.createGraphics();
            g.drawImage(tileImage, x, y, null);
            g.dispose(); // Cất bút
            
            // Yêu cầu chỉ vẽ lại VÙNG NHỎ đã thay đổi (cực kỳ nhanh)
            this.repaint(x, y, tileImage.getWidth(), tileImage.getHeight());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Bạn có thể thêm các hàm như setText, setBorder nếu cần
}