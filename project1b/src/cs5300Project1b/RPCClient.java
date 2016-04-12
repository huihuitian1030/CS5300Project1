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
		this.appServer = as;
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
		System.out.println("rpc client send callmsg to rpc server in read: "+ callMsg);
		outBuf = callMsg.getBytes(); 
		for(String host : destAddr){
			//if(host.equals(appServer.getAddr())){
			//	continue;
			//}
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(host);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			DatagramPacket sendPkt = new DatagramPacket(outBuf, outBuf.length, addr, Constant.portProj1bRPC);
			try {
				//System.out.println(appServer.getSvrID()+ " send pkt to " + host);
				rpcSocket.send(sendPkt);
				System.out.println("send packet sucess in RPC client read!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		byte[] inBuf = new byte[Constant.UDP_PACKET_LENGTH];
		DatagramPacket recvPkt = new DatagramPacket(inBuf, inBuf.length);
		Integer recvCallID = -1;
		int count = 0;
		try {
			do {
				recvPkt.setLength(inBuf.length);
		        rpcSocket.receive(recvPkt);
		        String replyMsg = new String(recvPkt.getData());
		        String[] token = replyMsg.trim().split("\\__");
				System.out.println("the reply message from rpc server to rpc client is "+ replyMsg);
		        recvCallID = new Integer(token[0]);
		        if(recvCallID == cid && !token[1].split("\\|")[0].equals("None") ){
		        	recvStr = token[1];
		        	break;
		        } else{
		        	count++;
		        }
		    } while(count < Constant.R);
		} catch(SocketTimeoutException stoe) {
			recvPkt = null;
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
		
		if(recvStr.length()==0){
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
		System.out.println("send packet to RPC server with msg: "+ callMsg);

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
				System.out.println(appServer.getAddr()+ " send pkt to " + host);
				rpcSocket.send(sendPkt);
				System.out.println("send packet sucess in RPC client write!");
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
		        System.out.println("reply msg from rpc server for write: "+replyMsg);
		        String[] token = replyMsg.trim().split("\\__");
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
		
        System.out.println("rpc client send to appserver str: "+recvStr);
		rpcSocket.close();
		return recvStr;
	}

}
