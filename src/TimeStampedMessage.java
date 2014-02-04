
public class TimeStampedMessage extends Message {

	private TimeStamp msgTS = null;
	
	public TimeStampedMessage(String dest, String kind, Object data, TimeStamp ts) {
		super(dest, kind, data);
		this.msgTS = ts;
		// TODO Auto-generated constructor stub
	}

	public TimeStamp getMsgTS() {
		return msgTS;
	}

	public String getMsg() {
		return super.toString();
	}
	
	public void setMsgTS(TimeStamp msgTS) {
		this.msgTS = msgTS;
	}
	
	public Message makeCopy() {
		Message result = new TimeStampedMessage(this.getDest(), this.getKind(), this.getData(), this.msgTS);
		result.set_source(this.getSrc());
		result.set_duplicate(this.isDuplicate());
		result.set_seqNum(this.getSeqNum());
		return result;
	}
	
	public void dumpMsg(){
		System.out.println("Msg: src: " + super.getSrc());
		System.out.println("Msg: dest: " + super.getDest());
		System.out.println("Msg: Kind: " + super.getKind());
		System.out.println("Msg: seqnum: " + super.getSeqNum());
		System.out.println("Msg: data:  " + super.getData());
		System.out.println("Msg: TS:  " + msgTS.toString());
	}

	public String toString() {
		return (super.toString() + msgTS.toString()); 
	}
}
