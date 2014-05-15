package mg.reservation.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class ReservationServer extends AbstractHandler
{
	public void handle(String target,
			Request baseRequest,
			HttpServletRequest request,
			HttpServletResponse response)
			throws IOException, ServletException
	{
		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
		response.getWriter().println("<h1>Message</h1>");
	}

	public static void main(String[] args) throws Exception
	{
		// server.setHandler(new ReservationServer());

//		ServletHolder sh = new ServletHolder(ServletContainer.class);
//		sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
//		sh.setInitParameter("com.sun.jersey.config.property.packages", "mg.reservation.rest");// Set the package where the services reside
//		sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");
//		Server server = new Server(8080);
//		ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
//		context.addServlet(sh, "/*");
				
		Server server = new Server(8888);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

//        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/webapi/*");
//        jerseyServlet.setInitOrder(1);
//        jerseyServlet.setInitParameter("jersey.config.server.provider.packages","mg.reservation.rest");
//
//        ServletHolder staticServlet = context.addServlet(DefaultServlet.class,"/*");
//        staticServlet.setInitParameter("resourceBase","src/main/webapp");
//        staticServlet.setInitParameter("pathInfoOnly","true");
        
		server.start();
		server.join();

	}
}