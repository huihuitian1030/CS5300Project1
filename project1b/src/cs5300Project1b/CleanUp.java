package cs5300Project1b;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class CleanUp extends Thread{
	private ConcurrentHashMap<String, SessionState> sessionTable;
	
	public CleanUp (ConcurrentHashMap<String, SessionState> sessionTable) {
		this.sessionTable = sessionTable;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(15*1000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true) {
			Iterator<String> keys = sessionTable.keySet().iterator();
			Date current = new Date();
			while(keys.hasNext()){
				String key = keys.next();
				if (sessionTable.get(key).getExpireTime() < current.getTime()) {
					sessionTable.remove(key);
				}
			}
			try{
				Thread.sleep(Constant.cleanUpTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	} 
}