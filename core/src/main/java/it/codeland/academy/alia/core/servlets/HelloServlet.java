package it.codeland.academy.alia.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Servlet registered by path at /bin/alia/hello.
 * Returns an HTML snippet that is injected into the page via AJAX from
 * clientlib-hello.
 */
@Component(service = Servlet.class, property = {
        "sling.servlet.paths=/bin/alia/hello",
        "sling.servlet.methods=GET"
})
public class HelloServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(final SlingHttpServletRequest request,
            final SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        response.getWriter().write(
                "<div class=\"hello-servlet-result\">" +
                        "  <h3>Hello from AEM Servlet!</h3>" +
                        "  <p>This content was injected via an AJAX call to <code>/bin/alia/hello</code>.</p>" +
                        "  <p>Served at: " + java.time.LocalDateTime.now() + "</p>" +
                        "</div>");
    }
}
