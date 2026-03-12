package it.codeland.academy.alia.core.servlets;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceDescription;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Part 3 — Datasource Servlet for Granite Select dropdown.
 * Registered by the custom resource type: alia/datasource/colors
 */
@Component(service = Servlet.class)
@SlingServletResourceTypes(
    resourceTypes = "alia/datasource/colors",
    methods = "GET"
)
@ServiceDescription("Dynamic Datasource for Color Dropdown")
public class ColorsDataSourceServlet extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        ResourceResolver resolver = request.getResourceResolver();
        List<Resource> resourceList = new ArrayList<>();

        // Hardcoded color options for the assignment
        String[][] colors = {
            {"None", ""},
            {"Red", "color-red"},
            {"Green", "color-green"},
            {"Blue", "color-blue"},
            {"Yellow", "color-yellow"}
        };

        for (String[] color : colors) {
            String text = color[0];
            String value = color[1];

            ValueMap vm = new ValueMapDecorator(new HashMap<>());
            vm.put("text", text);
            vm.put("value", value);

            resourceList.add(new ValueMapResource(
                resolver,
                new ResourceMetadata(),
                "nt:unstructured",
                vm
            ));
        }

        DataSource ds = new SimpleDataSource(resourceList.iterator());
        request.setAttribute(DataSource.class.getName(), ds);
    }
}
