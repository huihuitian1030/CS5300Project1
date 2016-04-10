package cs5300Project1b;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class server
 */
@WebServlet("/server")
public class AppServer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private SimpleDBView sdbView;

	private RPCServer rpcServer;
	private RPCClient rpcClient;
	private String IPAddr = Constant.defaultIPAddr;
	private String svrID = "None";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AppServer() {
        super();
        
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
		// cookie is null
		if(curCookie==null){
			SessionID sid = new SessionID();
			SessionState ss = new SessionState(sid);

			String wStr = rpcClient.SessionWriteClient(ss);
			String[] reply_data = wStr.trim().split("\\__");
			SessionID sid2 = new SessionID(reply_data[2]);
			String primaryID = reply_data[0];
			String secondID = reply_data[1];
			SessionState ss2 = new SessionState(sid2,0,msg);
			request.setAttribute("SessionState", ss2);	
			curCookie = new Cookie(Constant.cookieName, createCookieValue(ss2,primaryID,secondID));
			
			
		}
		
		//cookie is not null
		else{
			String[] token = curCookie.getValue().split("__");
			String primaryID = token[2];
			String secondID = token[3];
			SessionID sid = new SessionID(token[0]);
			int version = Integer.parseInt(token[1]);
			
			// write
			if(replace){
				SessionState ss = new SessionState(sid,version,msg);
				String wStr = rpcClient.SessionWriteClient(ss);
				String[] reply_data = wStr.trim().split("\\__");
				SessionID sid2 = new SessionID(reply_data[2]);
				primaryID = reply_data[0];
				secondID = reply_data[1];
				SessionState ss2 = new SessionState(sid2,version+1,msg);
				request.setAttribute("SessionState", ss2);	
				curCookie = new Cookie(Constant.cookieName, createCookieValue(ss2,primaryID,secondID));
			}
			//read
			else{
				ArrayList<String> destAddr = new ArrayList<String>();
				//TODO not id, need to change to ip address
				destAddr.add(primaryID);
				destAddr.add(secondID);
				String rStr = rpcClient.SessionReadClient(sid, version, destAddr);
				String[] reply_data = rStr.trim().split("\\__");
				if(reply_data.length == 3){
					SessionID sid2 = new SessionID(reply_data[2]);
					primaryID = reply_data[0];
					secondID = reply_data[1];
					SessionState ss2 = new SessionState(sid2,version+1,msg);
					request.setAttribute("SessionState", ss2);	
					curCookie = new Cookie(Constant.cookieName, createCookieValue(ss2,primaryID,secondID));
				}else{
					SessionState ss2 = new SessionState(reply_data[0]);
					request.setAttribute("SessionState", ss2);	
					curCookie = new Cookie(Constant.cookieName, createCookieValue(ss2,primaryID,secondID));
				}
				
				
			}
			
			
		}

		response.addCookie(curCookie);
		request.setAttribute("cookie", curCookie);	
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


	//create the value for the new cookie using session ID and version number 
	private String createCookieValue(SessionState ss, String primaryID, String secondID){
		return ss.getSessionID().serialize()+ "_" + ss.getVersion() + "_" + primaryID+"_" + secondID;
	}

	
	public String getAddr(){
		return this.IPAddr;
	}
	
	public String getSvrID(){
		return this.svrID;
	}
}
	
	