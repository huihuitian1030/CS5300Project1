package cs5300Project1b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class RPCClient {
	private Integer callID = 1;
	AppServer appServer;
	public RPCClient(AppServer as){
		this.appServer = as ;
		//System.out.println(appServer.getAddr() + " "+appServer.getSvrID());
	}
	
	public String SessionReadClient(SessionID sid, int version, ArrayList<String> destAddr){
		DatagramSocket rpcSocket = null;
		String recvStr = "";
		try{
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(10000);
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
			if(host.equals(appServer.getAddr())){
				continue;
			}
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
		try {
			do {
				recvPkt.setLength(inBuf.length);
		        rpcSocket.receive(recvPkt);
		        String replyMsg = new String(recvPkt.getData());
		        String[] token = replyMsg.split("\\__");
		        recvCallID = new Integer(token[0]);
		        if(recvCallID == cid){
		        	recvStr = token[1];
		        }
		        } while(recvCallID != cid);
			} catch(SocketTimeoutException stoe) {
				recvPkt = null;
			} catch(IOException ioe) {
				ioe.printStackTrace();
		    }
		
		if(recvPkt == null){
			recvStr = "Failure";
		}
		
		rpcSocket.close();
		
		return recvStr;
	}
	
	
	public String SessionWriteClient(SessionState ss, ArrayList<String> destAddr){
		
		DatagramSocket rpcSocket = null;
		try{
			rpcSocket = new DatagramSocket();
			rpcSocket.setSoTimeout(10000);
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
			System.out.println(host);
			if(host.equals(appServer.getAddr())){
				continue;
			}
			
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
		try {
			do {
				recvPkt.setLength(inBuf.length);
		        rpcSocket.receive(recvPkt);
		        String replyMsg = new String(recvPkt.getData());
		        String[] token = replyMsg.split("\\__");
		        recvCallID = new Integer(token[0]);
		        if(recvCallID == cid){
		        	replyList.add(replyMsg);
		        	recvSvrID = recvSvrID + token[2] + "__"; 
		        	recvSsID = token[1];
		        }
		        } while(replyList.size() < Constant.WQ);
			} catch(SocketTimeoutException stoe) {
				recvPkt = null;
			} catch(IOException ioe) {
				ioe.printStackTrace();
		    }
	
		String recvStr = recvSvrID + recvSsID;
		
		
		rpcSocket.close();
		return recvStr;
	}

}
