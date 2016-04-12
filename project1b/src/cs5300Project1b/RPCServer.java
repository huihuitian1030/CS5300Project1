package cs5300Project1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException; 
import java.util.concurrent.ConcurrentHashMap;

public class RPCServer extends Thread {
	private static Integer sessNum = 1;
	private String svrID;
	private int rebootNum;
	private ConcurrentHashMap<String, SessionState> sessionTable;
	private Thread clean;

	public RPCServer (AppServer appServer) {
		this.svrID = appServer.getSvrID();
		this.rebootNum = appServer.getRebootNum();
		sessionTable = new ConcurrentHashMap<String, SessionState>();
		clean = new CleanUp(sessionTable);
		clean.setDaemon(true);
		clean.start();
		
	}
	
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
						String rmsg = "" + callId + "__" + session.serialize();
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
	
	public SessionState SessionRead(String key) {
		if (!sessionTable.containsKey(key)) {
			return new SessionState(new SessionID("None",this.rebootNum, 0));
		} else {
			SessionState ss = sessionTable.get(key);
			return ss;
		}
	}
	
	public SessionID SessionWrite(String sessionState) {
		SessionState givenSS = new SessionState(sessionState);
		SessionID currentID;
		SessionState currentSS;
		String key;
		//System.out.println(sessionState);
		//System.out.println(givenSS.getSessionID());
		
		currentID = givenSS.getSessionID();
		currentSS = new SessionState(currentID,givenSS.getVersion(), givenSS.getMessage());
		currentSS.addVersion();
		key = currentID.serialize() + "--" + currentSS.getVersion();
		sessionTable.put(key, currentSS);
		System.out.println(key);
		System.out.println("sessionTable size: "+ sessionTable.size());
		return currentID;
	}
	
	public int getSessionNum(){
		int res = sessNum;
		synchronized(sessNum) {
			sessNum++;
		}
		return res;
	}
}
