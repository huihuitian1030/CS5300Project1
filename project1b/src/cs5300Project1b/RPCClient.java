package cs5300Project1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * 
 * This class is the implementation of RPC client. 
 * First it gets the information from the application server.
 * It is responsible for sending UDP packets to and receiving UDP packets from RPC server.
 * Then RPC client send the correct response to application server.
 *
 */
public class RPCClient {
	private Integer callID = 1;
	public RPCClient(){}
	
	
	/**
	 * 
	 * @param sid , the current sessionID.
	 * @param version, the current version number, it is the key in the session table together with the sessionID.
	 * @param destAddr, the target servers which store the session state.
	 * @return the message sent back to application server.
	 * 
	 * This function is the action for read request.
	 * It builds socket and start the packet transformation between RPC client and RPC server.
	 * The destAddr store two svrIDs which the current session state is written on. 
	 * If that session state is timed out, the RPC server replies with a session ID beginning with "None".
	 * The function traverse the destAddr to get the first valid response with the correct callID and response session ID.
	 */
	public String SessionReadClient(SessionID sid, int version, ArrayList<String> destAddr){
		
		DatagramSocket rpcSocket = null;
		String recvStr = "";
		try{
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(5000);
		}catch (SocketException e){
			e.printStackTrace();
		}
		
		int cid = -1;
		synchronized (callID){
			cid = callID;
			callID++;
		}
		
		byte[] outBuf = new byte[Constant.UDP_PACKET_LENGTH];
		String callMsg = ""+cid + "__" + Constant.READ + "__" + sid.serialize()+"--"+version;
		outBuf = callMsg.getBytes(); 
		for(String host : destAddr){
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, Constant.portProj1bRPC);
			try {
				rpcSocket.send(sendPkt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		Integer recvCallID = -1;
		int count = 0;
		boolean success = false;
		try {
			do {
				recvPkt.setLength(inBuf.length);
		        rpcSocket.receive(recvPkt);
		        String replyMsg = new String(recvPkt.getData());
		        String[] token = replyMsg.trim().split("\\__");
		        recvCallID = new Integer(token[0]);
		        recvStr = token[1]+"__"+token[token.length - 1];
		        if(recvCallID == cid ){
		        	count++;
		        	if(!recvStr.split("\\|")[0].equals("None")){
		        		success = true;}
		        }
		    } while(count<Constant.R && !success);
		} catch(SocketTimeoutException stoe) {
			recvPkt = null;
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		 
		if(!success){
			recvStr = "Failure";
		}
		rpcSocket.close();
		return recvStr;
	}
	
	/**
	 * 
	 * @param ss, the session state we want to write in the session table
	 * @param destAddr, the W servers that the write request is sent to 
	 * @return the message sent back to application server.
	 * 
	 * This function is the action for write request.
	 * It builds socket and start the packet transformation between RPC client and RPC server.
	 * It send write request to W RPC server and stop when receive WQ valid response with the same callID.
	 */
	public String SessionWriteClient(SessionState ss, ArrayList<String> destAddr){
		DatagramSocket rpcSocket = null;
		try{
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(5000);
		}catch (SocketException e){
			e.printStackTrace();
		}
		
		int cid = -1;
		synchronized (callID) {
			cid = callID;
			callID++;
		}
		byte[] outBuf = new byte[Constant.UDP_PACKET_LENGTH];
		String callMsg = ""+ cid+ "__" + Constant.WRITE + "__" + ss.serialize();
		outBuf = callMsg.getBytes();
	
		for(String host : destAddr){
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, Constant.portProj1bRPC);
			try {
				rpcSocket.send(sendPkt);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		ArrayList<String> replyList = new ArrayList<String>();
		String recvSvrID = "";
		String recvSsID = "";
		byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		Integer recvCallID = -1;
		String recvStr = "";
		try {
			do {
				recvPkt.setLength(inBuf.length);
		        rpcSocket.receive(recvPkt);
		        String replyMsg = new String(recvPkt.getData());
		        String[] token = replyMsg.trim().split("\\__");
		        recvCallID = new Integer(token[0]);
		        if(recvCallID == cid){
		        	replyList.add(replyMsg);
		        	recvSvrID = recvSvrID + token[2] + "__"; 
		        	recvSsID = token[1];
		        }
		    } while(replyList.size() < Constant.WQ);
		} catch(SocketTimeoutException stoe) {
			stoe.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			if (replyList.size() < Constant.WQ) {
				recvStr =  Constant.socketTimeOutWQMessage + "__";
			}
			recvStr += recvSvrID + recvSsID;
			rpcSocket.close();
		}
		return recvStr.trim();
	}
}
