package commom.model;

public class Packet {
	private int length; //Độ dài của mảng byte
	private byte[] data; // 1 dãy byte nhị phân của 1 phần bức ảnh, dữ liệu thô

	public Packet(int length, byte[] data) {
		this.length = length;
		this.data = data;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}