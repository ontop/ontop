package sesameWrapper;

import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class StartJetty implements Runnable {

	private Server server;

	public StartJetty() {
		server = new Server(8080);
		String jetty_home = "C:/Users/TiBagosi/Downloads/jetty-distribution-9.0.0.M0/jetty-distribution-9.0.0.M0";

		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath("/openrdf-sesame");
		webapp.setWar(jetty_home + "/webapps/openrdf-sesame.war");

		WebAppContext webapp2 = new WebAppContext();
		webapp2.setContextPath("/openrdf-workbench");
		webapp2.setWar(jetty_home + "/webapps/openrdf-workbench.war");
		
		webapp.setExtraClasspath("lib");
		webapp2.setExtraClasspath("lib");

		HandlerCollection handlers = new HandlerCollection();
		handlers.setHandlers(new Handler[] { webapp, webapp2 });

		server.setHandler(handlers);

		FileWriter w;
		try {
			w = new FileWriter("test.txt");

			w.write("hi");
			w.flush();
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void start() {
		run();
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		Thread t = new Thread(new StartJetty());
		t.run();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
