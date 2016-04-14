package cs5300Project1b;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This RPCServer class implement RPC Server function though a single thread. Each RPCServer contains
 * a global server ID, reboot number, session table and a garbage collection thread.
 */
public class RPCServer extends Thread {

	private String svrID;
	private int rebootNum;
	private ConcurrentHashMap<String, SessionState> sessionTable;
	private Thread clean;
	
	/**
	 * This constructor of RPCServer set the server Id and reboot number though
	 * reading the "ami-launch-index"and "reboot-num" files. Also initialized an ConcurrentHashMap as
	 * session table, the key is sessionID--version number which map to the particular session state object.
	 * Also it creates a single deamon thread to discard the expired session from session table.
	 * @throws IOException
	 */
	public RPCServer () throws IOException {
	     
	    BufferedReader br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/ami-launch-index"));
	    this.svrID = br.readLine().trim();
	    br.close();
	    br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/reboot-num"));
	    this.rebootNum = Integer.parseInt(br.readLine().trim());
	    br.close();
		sessionTable = new ConcurrentHashMap<String, SessionState>();
		clean = new CleanUp(sessionTable);
		clean.setDaemon(true);
		clean.start();
	}
	
	/**
	 * RPCServer will keep running this function as a loop when start, each time it receives a DatagramPacket 
	 * from a DatagramSocket which constructed by the explicit port number, computes a reply message based on
	 * the operationCode and sends a reply DatagramPacket using the same DatagramSocket. If the operation
	 * Code is read, then it will read the session from the session table using the given session Id from 
	 * input message and reply the serialized session state information. If the operation code is write, 
	 * then it will write the given session into the session table.
	 */
	@Override
	public void run() {
		DatagramSocket rpcSocket =  null;
		try {
			rpcSocket = new DatagramSocket(Constant.portProj1bRPC);
			while(true) {
				byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
				DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
				
				try {
					rpcSocket.receive(recvPkt);
					InetAddress returnAddr = recvPkt.getAddress();
					int returnPort = recvPkt.getPort();
					inBuf = recvPkt.getData();
					String inMessage = new String(inBuf);
					String[] messageInfo = inMessage.trim().split("\\__");
					assert(messageInfo.length == 3);
					String callId = messageInfo[0];
					String operationCode = messageInfo[1];
					String argument = messageInfo[2];
					byte[] outBuf = null;
					switch (operationCode) {
					case Constant.READ:
						SessionState session = SessionRead(argument);
						String rmsg = "" + callId + "__" + session.serialize() + "__" + this.svrID;
						outBuf = rmsg.getBytes();
						break;
						
					case Constant.WRITE:
						SessionID sid = SessionWrite(argument);
						String wmsg = "" + callId + "__" + sid.serialize()+"__"+this.svrID;
						outBuf = wmsg.getBytes();
						break;
					default:
						throw new IllegalArgumentException("Illegal operationCode");    
					}
					
					DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, returnAddr, returnPort);
					rpcSocket.send(sendPkt);
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				}
				
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} finally{
			rpcSocket.close();
		}
	}
	
	/**
	 * This function handle the read operation case, read and return the session state from session table 
	 * though the given key. If cannot find the corresponding session, it will return a new session state which
	 * contains the "None" server id information. 
	 * @param key which is the combination of session id and version number.
	 * @return a session state. 
	 */
	public SessionState SessionRead(String key) {
		Date now = new Date();
		if (sessionTable.containsKey(key) && sessionTable.get(key).getExpireTime() > now.getTime()) {
			SessionState ss = sessionTable.get(key);
			return ss;
		} else {
			return new SessionState(new SessionID("None",this.rebootNum, 0));
		}
	}
	
	/**
	 * This function handle the write operation case. create and write the given serialized session state 
	 * information to the session table. The key will be the combination of session id and version number stored
	 * in the given session state.
	 * @param sessionState, a serialized session state information. 
	 * @return a session ID. 
	 */
	public SessionID SessionWrite(String sessionState) {
		SessionState givenSS = new SessionState(sessionState);
		SessionID currentID;
		SessionState currentSS;
		String key;
		currentID = givenSS.getSessionID();
		currentSS = new SessionState(currentID,givenSS.getVersion(), givenSS.getMessage());
		currentSS.addVersion();
		key = currentID.serialize() + "--" + currentSS.getVersion();
		sessionTable.put(key, currentSS);
		return currentID;
	}
}
