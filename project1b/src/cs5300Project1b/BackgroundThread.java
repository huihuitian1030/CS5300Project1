package cs5300Project1b;

import java.io.*;
import javax.servlet.*;

/**
 * This class implement a backgound thread, use ServletContextListener to create and start the RPCServer
 * before the HttpServlet is started.
 */
public class BackgroundThread implements ServletContextListener {
    private Thread t = null;
    public void contextInitialized(ServletContextEvent sce) {
        try {
			t = new RPCServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        t.start();
    }
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            t.interrupt();
        } catch (Exception ex) {
        }
    }
}

