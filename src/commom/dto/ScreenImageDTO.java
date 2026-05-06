package commom.dto;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ScreenImageDTO {
	public BufferedImage img; //đối tượng chứa hình ảnh thực sự trong bộ nhớ RAM
	public Graphics2D g2d;
	
	public ScreenImageDTO(int width, int height) {
		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
	}
	// Tạo đối tượng ScreenImageDTO cung cấp chiều rộng và chiều cao
	/*
	 - cấp phát 1 vùng nhớ RAM để chứa hình ảnh với kích thước width, height
	 - Tạo 1 cây bút
	 - setRenderingHint: cài đặt cho cây bút vẽ ở chế độ "Tốc độ cao"
	 */
}
