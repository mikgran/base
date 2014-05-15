package mg.reservation.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(asyncSupported = false,
		name = "MyServlet",
		urlPatterns = { "/reservation" },
		initParams = {
				@WebInitParam(name = "webInitParam1", value = "Hello"),
				@WebInitParam(name = "webInitParam2", value = "World !!!")
		})
public class ReservationServlet extends HttpServlet {

	private static final long serialVersionUID = -5377899412368237345L;

	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			out.println("<html><head><title>MyServlet</title></head><body>");
			out.write(getServletConfig().getInitParameter("webInitParam1") + " ");
			out.write(getServletConfig().getInitParameter("webInitParam2"));
			out.println("</body>");
			out.println("</html>");
		} finally {
			out.close();
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	@Override
	public String getServletInfo() {
		return "Short description";
	}
}