package cs5300Project1b;

public class SessionID {
	
	private int svrID;
	private int rebootNum;
	private int sessNum;
	
	public SessionID(int svrID,int sessNum){
		this.svrID = svrID;
		this.rebootNum = 0;
		this.sessNum = sessNum;
	}
	
	public String serialize(){
		return ""+svrID+"__"+rebootNum+"__"+sessNum;
	}
	
	public int getSvrID(){
		return svrID;
	}
	
	public int getRebootNum(){
		return rebootNum;
	}
	
	public void setRebootNum( int rebootNum ) {
		this.rebootNum = rebootNum;
	}
	
	public int getSessNum(){
		return sessNum;
	}
	
	public void increamentRootNum(){
		this.rebootNum++;
	}

}
