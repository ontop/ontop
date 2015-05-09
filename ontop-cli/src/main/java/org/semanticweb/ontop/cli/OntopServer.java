package org.semanticweb.ontop.cli;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.airlift.airline.Command;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

@Command(name = "server",
        description = "ontop server",
        hidden = true)
public class OntopServer extends AbstractHandler implements OntopCommand{

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html; charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("<h1>Hello World</h1>");
        response.getWriter().println(request.toString());
        baseRequest.setHandled(true);
    }

    public static void main(String[] args) throws Exception {
        new OntopServer().run();
    }

    @Override
    public void run() {
        Server server = new Server(8080);
        server.setStopAtShutdown(true);
        server.setHandler(new OntopServer());
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
