package commom.dto;

import java.util.LinkedList;
import java.util.Queue;

public class ClientModel {
	private long time;
	private int studentNum;
	private Queue<String> warnings = new LinkedList<>();

	public ClientModel(int studentNum) {
		super();
		this.time = System.currentTimeMillis();
		this.studentNum = studentNum;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getStudentNum() {
		return studentNum;
	}

	public void setStudentNum(int studentNum) {
		this.studentNum = studentNum;
	}
	public Queue<String> getWarnings() {
		return warnings;
	}

	public void addWarning(String message) {
		this.warnings.add(message);
	}
}
