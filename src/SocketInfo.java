/* 18-842 Distributed Systems
 * Lab 0
 * Group 41 - ajaltade & dil1
 */

public class SocketInfo {

	private String name;
	private String ip;
	int port;
	private String clockType = "logical"; // Workaround
	
	public SocketInfo() {
		
	}
	
	public SocketInfo(String name, String ip, int port) {
		this.name = name;
		this.ip = ip;
		this.port = port;
	}
	
	public SocketInfo(String name, String ip, int port, String clockType) {
		this.name = name;
		this.ip = ip;
		this.port = port;
		this.clockType = clockType;
	}
	
	public String getClockType() {
		return this.clockType;
	}
	
	public String getName() {
		return name;
	}

	public void setClockType(String clockType) {
		this.clockType = clockType;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return "SocketInfo [name=" + name + ", ip=" + ip + ", port=" + port
				+ ", clockType=" + clockType + "]";
	}
}
