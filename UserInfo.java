package nirc;

public class UserInfo {
	String nickname;
	String username;
	String realname;
	String hostname;
	String password;
	
	public UserInfo(String nickname, String username, String realname, String hostname, String password) {
		this.nickname = nickname;
		this.username = username;
		this.realname = realname;
		this.hostname = hostname;
		this.password = password;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}