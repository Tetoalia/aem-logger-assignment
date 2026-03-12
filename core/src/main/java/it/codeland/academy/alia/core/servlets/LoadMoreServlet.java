package it.codeland.academy.alia.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 2 — Load More Servlet, registered by resource type.
 *
 * URL pattern: /content/…/jcr:content/…/customlist.loadmore.json?page=N
 *
 * Returns JSON:
 * {
 *   "items": [ { "title": "…", "path": "…", "url": "…", "subText": "…" }, … ],
 *   "hasMore": true|false,
 *   "currentPage": N,
 *   "totalPages": T
 * }
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
    resourceTypes = "alia/components/customlist",
    selectors    = "loadmore",
    extensions   = "json",
    methods      = "GET"
)
@ServiceDescription("Load More Servlet for customlist component")
public class LoadMoreServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 4;
    private static final String TEXT_PATH = "/jcr:content/root/container/container/text";

    @Override
    protected void doGet(final SlingHttpServletRequest request,
                         final SlingHttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        // Read page parameter (1-based)
        int page = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null) {
            try { page = Math.max(1, Integer.parseInt(pageParam)); }
            catch (NumberFormatException ignored) { /* default 1 */ }
        }

        ResourceResolver resolver = request.getResourceResolver();
        Resource listResource = request.getResource();

        // Read rootPath from component dialog properties
        String rootPath = listResource.getValueMap().get("parentPage", String.class);
        if (rootPath == null) {
            rootPath = listResource.getValueMap().get("pages", String.class);
        }

        List<String[]> allItems = new ArrayList<>(); // [title, path, subText]

        if (rootPath != null) {
            Resource rootResource = resolver.getResource(rootPath);
            if (rootResource != null) {
                try {
                    Node rootNode = rootResource.adaptTo(Node.class);
                    if (rootNode != null) {
                        NodeIterator children = rootNode.getNodes();
                        while (children.hasNext()) {
                            Node child = children.nextNode();
                            if (child.isNodeType("cq:Page")) {
                                String childPath = child.getPath();
                                String title = child.getName();
                                if (child.hasNode("jcr:content")) {
                                    Node content = child.getNode("jcr:content");
                                    if (content.hasProperty("jcr:title")) {
                                        title = content.getProperty("jcr:title").getString();
                                    }
                                }
                                String subText = null;
                                Resource textRes = resolver.getResource(childPath + TEXT_PATH);
                                if (textRes != null) {
                                    subText = textRes.getValueMap().get("text", String.class);
                                }
                                allItems.add(new String[]{title, childPath, subText});
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    response.setStatus(500);
                    response.getWriter().write("{\"error\":\"Repository error\"}");
                    return;
                }
            }
        }

        int totalPages = allItems.isEmpty() ? 1 : (int) Math.ceil((double) allItems.size() / PAGE_SIZE);
        if (page > totalPages) page = totalPages;

        int from = (page - 1) * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, allItems.size());

        PrintWriter writer = response.getWriter();
        writer.write("{");
        writer.write("\"currentPage\":" + page + ",");
        writer.write("\"totalPages\":" + totalPages + ",");
        writer.write("\"hasMore\":" + (page < totalPages) + ",");
        writer.write("\"items\":[");
        for (int i = from; i < to; i++) {
            String[] item = allItems.get(i);
            if (i > from) writer.write(",");
            writer.write("{");
            writer.write("\"title\":" + jsonString(item[0]) + ",");
            writer.write("\"path\":" + jsonString(item[1]) + ",");
            writer.write("\"url\":" + jsonString(item[1] + ".html") + ",");
            writer.write("\"subText\":" + jsonString(item[2]));
            writer.write("}");
        }
        writer.write("]}");
    }

    /** Wraps a value in a JSON string, handling null. */
    private String jsonString(String value) {
        if (value == null) return "null";
        return "\"" + value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r") + "\"";
    }
}
