import java.util.ArrayList;


public class Group {

	private String groupName = null;
	private ArrayList<String> memberList = null;
	
	public Group(String name) {
		this.groupName = name;
		this.memberList = new ArrayList<String>();
	}
	
	public Group(String groupName, ArrayList<String> memberList) {
		this.groupName = groupName;
		this.memberList = memberList;
	}
	
	public void addMember(String newUser) {
		this.memberList.add(newUser);
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public ArrayList<String> getMemberList() {
		return memberList;
	}

	public void setMemberList(ArrayList<String> memberList) {
		this.memberList = memberList;
	}

	@Override
	public String toString() {
		return "Group [groupName=" + groupName + ", memberList=" + memberList
				+ "]";
	}
	
}
