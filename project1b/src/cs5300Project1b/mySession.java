package cs5300Project1b;

import java.util.Date;

public class mySession {
	
	String sessionID;
	int version;
	String message;
	Date expirationTime; // for testing, the live period of a session is set to 30s
	
	

	public mySession(String sid, int v, Date expTime){
		
		sessionID = sid;
		version = v;
		expirationTime = expTime;
	}

	public String getSessionID(){
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
		message = str;
	}
	
	public void setExpireTime(Date newDate){
		expirationTime = newDate;
	}
	
	public Date getExpireTime(){
		return expirationTime;
	}
	
	

	

	
}
