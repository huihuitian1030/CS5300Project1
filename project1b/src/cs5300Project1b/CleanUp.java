package cs5300Project1b;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is a single deamon thread used to do the garbage collection.
 * It will be constructed though RPCServer.
 *
 */
public class CleanUp extends Thread{
	private ConcurrentHashMap<String, SessionState> sessionTable;
	
	/**
	 * The constructor of CleanUp thread, set the session table filed.
	 * @param sessionTable, the session table need to do the garbage collection.
	 */
	public CleanUp (ConcurrentHashMap<String, SessionState> sessionTable) {
		this.sessionTable = sessionTable;
	}
	
	/**
	 * This thread will keep running this function when start. It will go over the sessionTable and remove the
	 * expired session from session table every constant clean up time. 
	 */
	@Override
	public void run() {
		try {
			Thread.sleep(Constant.cleanUpTime);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true) {
			Iterator<String> keys;
			if(sessionTable.size() > 0){
				keys = sessionTable.keySet().iterator();
				Date current = new Date();
				while(keys.hasNext()){
					String key = keys.next();
					if (sessionTable.get(key).getExpireTime() < current.getTime()) {
						sessionTable.remove(key);
					}
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