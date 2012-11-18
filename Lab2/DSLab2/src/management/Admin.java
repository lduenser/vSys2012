package management;

public class Admin {

	private String username;
	private String pwd;
	private Boolean online = false;
	
	public Admin(String username, String pwd){
		this.username = username;
		this.pwd = pwd;
		online = false;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public Boolean getOnline() {
		return online;
	}

	public void setOnline(Boolean online) {
		this.online = online;
	}
}
