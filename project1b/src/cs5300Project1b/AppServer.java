package cs5300Project1b;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

/**
 * Servlet implementation class server
 */
@WebServlet("/server")
public class AppServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//private SimpleDBView sdbView;

	private RPCServer rpcServer;
	private RPCClient rpcClient;
	private String IPAddr = Constant.defaultIPAddr;
	private String svrID = "None";
	private int rebootNum = 0;
	private HashMap<String, String> idIPmap;
	private String dbStr = "";
	//Writer writer;
	
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public AppServer() throws IOException {
        super();
        idIPmap = new HashMap<String,String>();
        //writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/usr/share/tomcat8/webapps/filename.txt"), "utf-8"));
        BufferedReader br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/local-ipv4"));
        //BufferedReader br = new BufferedReader(new FileReader("C:/Users/Shitong/Documents/cs5300p1/CS5300Project1/project1b/src/cs5300Project1b/local-ipv4"));
        this.IPAddr = br.readLine().trim();
        br.close();
        br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/ami-launch-index"));
        //br = new BufferedReader(new FileReader("C:/Users/Shitong/Documents/cs5300p1/CS5300Project1/project1b/src/cs5300Project1b/ami-launch-index"));
        this.svrID = br.readLine().trim();
        br.close();
        br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/reboot-num"));
        //br = new BufferedReader(new FileReader("C:/Users/Shitong/Documents/cs5300p1/CS5300Project1/project1b/src/cs5300Project1b/reboot-num"));
        this.rebootNum = Integer.parseInt(br.readLine().trim());
        br.close();
        br = new BufferedReader(new FileReader("/usr/share/tomcat8/webapps/db-data"));
        //br = new BufferedReader(new FileReader("C:/Users/Shitong/Documents/cs5300p1/CS5300Project1/project1b/src/cs5300Project1b/db-data"));
        String map = br.readLine().trim();
        this.dbStr = map;
        String[] nodes = map.split("\\;");
        for(int r = 0 ; r < nodes.length;r++){
        	String node = nodes[r].trim();
        	//System.out.println("ip and ID is " + node );
        	String id = node.split("\\s+")[1].trim();
        	String ip = node.split("\\s+")[0].trim();
        	//writer.write("The id and ip of the server is: "+id+" "+ip);
        	idIPmap.put(id, ip);
        	//System.out.println("idip map"+ id+" "+ip);
        }
        br.close();
        
        
        rpcClient = new RPCClient(this);
        rpcServer = new RPCServer(this);
        rpcServer.setDaemon(true);
        rpcServer.start();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("HTTPRequest comes in");
		// TODO Auto-generated method stub
		Cookie curCookie = null;
		Cookie[] cookies = request.getCookies();
		String action = request.getParameter("function");
		boolean replace = false;
		String msg = null;
		// check whether current action is replace 
		if(action!=null && action.equals("Replace")){	
			replace =true;	
			msg = request.getParameter("newStr");
		}
		if(cookies!=null ){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(Constant.cookieName)){
					curCookie = cookie;
					break;
				}
			}
		}
		Cookie newCookie = null;
		// cookie is null
		if(curCookie==null){
			SessionID sid = new SessionID(this.svrID,this.rebootNum,this.rpcServer.getSessionNum());
			newCookie = AppServerWrite(sid,0,Constant.welcomeMsg,request);	
		}
		
		//cookie is not null
		else{
        	//writer.write("the curCookie value"+curCookie.getValue());

			System.out.println("the curCookie value"+curCookie.getValue());
			
			String[] token = URLDecoder.decode(curCookie.getValue().trim(),"UTF-8").split("_");
			
			SessionID sid = new SessionID(token[0],Integer.parseInt(token[1]),Integer.parseInt(token[2]));
			System.out.println("the sessionID get from cookie is: "+sid.serialize());
			int version = Integer.parseInt(token[3]);
			ArrayList<String> destAddr = new ArrayList<String>();
			for (int i = 4; i<token.length; i++) {
				destAddr.add(idIPmap.get(token[i]));
			}

			// write
			if(replace){
				String rStr = rpcClient.SessionReadClient(sid, version, destAddr).trim();
				if(rStr.equals("Failure")){
					SessionID sid2 = new SessionID(this.svrID,this.rebootNum,this.rpcServer.getSessionNum());
					//TODO : current msg or default?
					newCookie = AppServerWrite(sid2,0,msg,request);	
				}else{
					newCookie = AppServerWrite(sid, version, msg, request);
				}
			}
			//read
			else{
				newCookie = AppServerRead(sid,version,destAddr,request);
			}	
		}
		newCookie.setMaxAge(Constant.expTime);
		newCookie.setDomain("ts679.bigdata.systems");
    	//writer.write("new Cookie value:" + newCookie.getValue());

		System.out.println("new Cookie value:" + newCookie.getValue());
		response.addCookie(newCookie);
		System.out.println("add cookie ");
		request.setAttribute("cookie", newCookie);	
		request.setAttribute("dbStr", this.dbStr);
		request.getRequestDispatcher("main.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//doGet(request, response);
		Cookie curCookie = null;
		Cookie[] cookies = request.getCookies();
		
		if(cookies!=null ){
			for(Cookie cookie : cookies){
				if(cookie.getName().equals(Constant.cookieName)){
					curCookie = cookie;
					break;
				}
			}
		}
		if(curCookie != null){
			curCookie.setMaxAge(0);
			response.addCookie(curCookie);
		}
		request.getRequestDispatcher("logout.jsp").forward(request, response);
	}
	
	
	public Cookie AppServerWrite(SessionID sid,int version,String msg,HttpServletRequest request) throws UnsupportedEncodingException{
		SessionState ss1 = new SessionState(sid,version,msg);
		ArrayList<String> destAddr = genWQRandomAddrs();

		String wStr = rpcClient.SessionWriteClient(ss1,destAddr);
		//System.out.println(wStr.trim());
		String[] reply_data = wStr.trim().split("\\__");
		//assert(reply_data.length == Constant.WQ +1);
		SessionID sid2 = new SessionID(reply_data[reply_data.length -1]);
		ArrayList<String> svrIDs = new ArrayList<String> ();
		for (int i = 0; i< reply_data.length -1; i++) {
			svrIDs.add(reply_data[i]);
		}
		version++;
		//System.out.println("new version is: "+version);
		SessionState ss2 = new SessionState(sid2,version,msg);
		request.setAttribute("SessionState", ss2);
		Cookie newCookie = new Cookie(Constant.cookieName,URLEncoder.encode(createCookieValue(ss2,svrIDs),"UTF-8"));
		return newCookie;
	}
	
	public Cookie AppServerRead(SessionID sid, int version, ArrayList<String> destAddr,HttpServletRequest request) throws UnsupportedEncodingException{
		String rStr = rpcClient.SessionReadClient(sid, version, destAddr).trim();
		if(rStr.equals("Failure")){
			SessionID sid2 = new SessionID(this.svrID,this.rebootNum,this.rpcServer.getSessionNum());
			return AppServerWrite(sid2,0,Constant.welcomeMsg,request);
		}
		else{
			SessionState ss2 = new SessionState(rStr);
			return AppServerWrite(sid,version,ss2.getMessage(),request);
		}
	}

	//create the value for the new cookie using session ID and version number 
	private String createCookieValue(SessionState ss, ArrayList<String> svrIDs){
		StringBuilder sb = new StringBuilder();
		sb.append(ss.getSessionID().serializeForCookie());
		sb.append("_");
		sb.append(ss.getVersion());
		for (int i = 0; i < svrIDs.size();i++) {
			sb.append("_");
			sb.append(svrIDs.get(i));
		}
		return sb.toString();
	}
	
	public int getRebootNum(){
		return this.rebootNum;
	}
	
	public String getAddr(){
		return this.IPAddr;
	}
	
	public String getSvrID(){
		return this.svrID;
	}
	
	public ArrayList<String> genWQRandomAddrs(){
		int[] randomSvrID = new Random().ints(0,Constant.N).distinct().limit(Constant.W).toArray(); 

		ArrayList<String> destAddr = new ArrayList<String>();
		for(int i = 0;i<Constant.W;i++){
			//System.out.println("randomSvrID: " +randomSvrID[i]);
			destAddr.add(idIPmap.get(String.valueOf(randomSvrID[i])));
		}
		return destAddr;
	}
}
	
	