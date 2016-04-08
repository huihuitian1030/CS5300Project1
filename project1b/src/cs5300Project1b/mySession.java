package cs5300Project1b;

import java.util.Date;

public class mySession {
	
	SessionID sessionID;
	
	int version;
	String message = Constant.welcomeMsg;
	Date expirationTime; 
	
	public mySession(SessionID sid, int v, Date expTime){
		sessionID = sid;
		version = v;
		expirationTime = expTime;
	}
	
	public String serialize(){
		return sessionID.serialize()+"__"+version+"__"+message+"__"+expirationTime.toString();
	}

	public SessionID getSessionID(){
		return sessionID;
	}
	
	public int getVersion(){
		return version;
	}
	
	public void addVersion(){
		version = version +1;
	}
	
	public String getMessage(){
		return message;
	}
	
	public void setMessage(String str){
		if(str.length() > Constant.msgMaxLen){
			str = str.substring(0,Constant.msgMaxLen);
		}
		message = str;
		
	}
	
	public void setExpireTime(Date newDate){
		expirationTime = newDate;
	}
	
	public Date getExpireTime(){
		return expirationTime;
	}
	
	

	

	
}
