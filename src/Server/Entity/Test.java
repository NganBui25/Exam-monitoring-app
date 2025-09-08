package Server.entity;

import java.util.Date;

public class Test {
	private int id;
	private int user_id;
	private String name;
	private Date created;
	
	public Test(int id, int user_id, String name, Date created) {
		this.id = id;
		this.user_id = user_id;
		this.name = name;
		this.created = created;
	}
	public int getId() {
		return id;
	}
	public int getUserID() {
		return user_id;
	}
	public String getName() {
		return name;
	}
	public Date getCreate() {
		return created;
	}
}
