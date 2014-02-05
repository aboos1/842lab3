
public class NackItem {
	String[] srcGrp;
	int seqNum;
	
	public NackItem(String[] srcGrp, int seqNum){
		this.srcGrp = srcGrp;
		this.seqNum = seqNum;
	}
	
	public String[] getSrcGrp(){
		return srcGrp;
	}
	
	public int getSeqNum(){
		return seqNum;
	}
	
}
