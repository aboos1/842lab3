/* 18-842 Distributed Systems
 * Lab 0
 * Group 41 - ajaltade & dil1
 */

import java.io.Serializable;

public abstract class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String src;
	private String dest;
	private String kind;
	private Object data;
	private boolean duplicate;
	private int seqNum;
	
	public Message(String dest, String kind, Object data) {
		this.dest = dest;
		this.kind = kind;
		this.data = data;
		this.duplicate = false;
	}

	// These settors are used by MessagePasser.send, not your app
	public void set_source(String source) {
		this.src = source;
	}
	public void set_seqNum(int sequenceNumber) {
		this.seqNum = sequenceNumber;
	}
	public void set_duplicate(Boolean dupe) {
		this.duplicate = dupe;
	}
	// other accessors, toString, etc as needed
	
	public String getDest() {
		return dest;
	}
	public String getKind() {
		return kind;
	}
	public Object getData() {
		return data;
	}
	public boolean isDuplicate() {
		return duplicate;
	}
	public int getSeqNum() {
		return seqNum;
	}
	public String getSrc() {
		return src;
	}
	@Override
	public String toString() {
		return "Message [src=" + src + ", dest=" + dest + ", kind=" + kind + ", data=" + data
				+ ", duplicate=" + duplicate + ", seqNum=" + seqNum + "]";
	}
	public abstract Message makeCopy();
}
