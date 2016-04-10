package cs5300Project1b;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    /**
     * @throws IOException 
     * @see HttpServlet#HttpServlet()
     */
    public AppServer() throws IOException {
        super();
        BufferedReader br = new BufferedReader(new FileReader("local-ipv4"));
        this.IPAddr = br.readLine();
        br.close();
        br = new BufferedReader(new FileReader("ami-launch-index"));
        this.svrID = br.readLine();
        br.close();
        br = new BufferedReader(new FileReader("reboot-num"));
        this.rebootNum = Integer.parseInt(br.readLine());
        br.close();
        br = new BufferedReader(new FileReader("db-data"));
        String map = br.readLine();
        String[] nodes = map.split("\\;");
        for(int r = 0 ; r < nodes.length -1 ;r++){
        	String node = nodes[r];
        	String id = node.split(" ")[0];
        	String ip = node.split(" ")[1];
        	idIPmap.put(id, ip);
        }
        br.close();
        rpcClient = new RPCClient(this);
        rpcServer = new RPCServer(this);
  
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			SessionID sid = new SessionID();
			newCookie = AppServerWrite(sid,0,Constant.welcomeMsg,request);	
		}
		
		//cookie is not null
		else{
			String[] token = curCookie.getValue().split("_");
			SessionID sid = new SessionID(token[0]);
			int version = Integer.parseInt(token[1]);
			String primaryID = token[2];
			String secondID = token[3];
			ArrayList<String> destAddr = new ArrayList<String>();
			destAddr.add(primaryID);
			destAddr.add(secondID);
			
			// write
			if(replace){
				newCookie = AppServerWrite(sid, version, msg, request);
			}
			//read
			else{
				newCookie = AppServerRead(sid,version,destAddr,request);
			}	
		}
		newCookie.setMaxAge(Constant.expTime);
		response.addCookie(newCookie);
		request.setAttribute("cookie", newCookie);	
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
	
	
	public Cookie AppServerWrite(SessionID sid,int version,String msg,HttpServletRequest request){
		SessionState ss1 = new SessionState(sid,version,msg);
		ArrayList<String> destAddr = genWQRandomAddrs();
		String wStr = rpcClient.SessionWriteClient(ss1,destAddr);
		String[] reply_data = wStr.trim().split("\\__");
		SessionID sid2 = new SessionID(reply_data[2]);
		String primaryID = reply_data[0];
		String secondID = reply_data[1];
		SessionState ss2 = new SessionState(sid2,version+1,msg);
		request.setAttribute("SessionState", ss2);
		Cookie newCookie = new Cookie(Constant.cookieName,createCookieValue(ss2,primaryID,secondID));
		return newCookie;
	}
	
	public Cookie AppServerRead(SessionID sid, int version, ArrayList<String> destAddr,HttpServletRequest request){
		String rStr = rpcClient.SessionReadClient(sid, version, destAddr);
		
		if(rStr.equals("Failure")){
			SessionID sid2 = new SessionID();
			return AppServerWrite(sid2,0,Constant.welcomeMsg,request);
		}
		else{
			SessionState ss2 = new SessionState(rStr);
			return AppServerWrite(sid,version,ss2.getMessage(),request);
		}
	}

	//create the value for the new cookie using session ID and version number 
	private String createCookieValue(SessionState ss, String primaryID, String secondID){
		return ss.getSessionID().serialize()+ "_" + ss.getVersion() + "_" + primaryID+"_" + secondID;
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
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i =0;i<Constant.N;i++){
			list.add(i);
		}
		Collections.shuffle(list);
		ArrayList<String> destAddr = new ArrayList<String>();
		for(int i = 0;i<Constant.WQ;i++){
			destAddr.add(idIPmap.get(String.valueOf(list.get(i))));
		}
		return destAddr;
	}
}
	
	