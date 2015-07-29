import java.io.IOException;
import javax.servlet.http.*;

public class ExecuteServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Malvik.main(null);

        resp.setContentType("text/plain");
        resp.getWriter().println("{ \"result\": \"Executed\" }");

    }
}
