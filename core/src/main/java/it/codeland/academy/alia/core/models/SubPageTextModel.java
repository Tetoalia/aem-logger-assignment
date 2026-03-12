package it.codeland.academy.alia.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import com.adobe.cq.wcm.core.components.models.ListItem;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = SubPageTextModel.class,
    resourceType = "alia/components/customlist",
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class SubPageTextModel {

    @SlingObject
    private ResourceResolver resourceResolver;

    @SlingObject
    private SlingHttpServletRequest request;

    private static final String TEXT_PATH = "/jcr:content/root/container/container/text";
    private static final int PAGE_SIZE = 2; // items per page

    private List<PageItem> pageItems = new ArrayList<>();
    private int currentPageNum = 1;
    private int totalPages = 1;
    private String basePath;

    @PostConstruct
    protected void init() {
        // Step 6: Read current page number from selector
        // URL: /content/alia/us/en.p2.html → selector = "p2" → page 2
        String[] selectors = request.getRequestPathInfo().getSelectors();
        for (String selector : selectors) {
            if (selector.matches("p\\d+")) {
                try {
                    currentPageNum = Integer.parseInt(selector.substring(1));
                } catch (NumberFormatException e) {
                    currentPageNum = 1;
                }
            }
        }

        // Base path for pagination URLs
        basePath = request.getRequestPathInfo().getResourcePath();

        // Get all pages from Core List model
        com.adobe.cq.wcm.core.components.models.List coreList =
            request.adaptTo(com.adobe.cq.wcm.core.components.models.List.class);

        if (coreList == null) return;

        Collection<ListItem> items = coreList.getListItems();
        if (items == null) return;

        // Build full list with subpage text
        List<PageItem> allItems = new ArrayList<>();
        for (ListItem item : items) {
            String pagePath = item.getPath();
            String subText = null;

            Resource textResource = resourceResolver.getResource(pagePath + TEXT_PATH);
            if (textResource != null) {
                subText = textResource.getValueMap().get("text", String.class);
            }

            allItems.add(new PageItem(
                item.getTitle() != null ? item.getTitle() : item.getName(),
                pagePath,
                subText
            ));
        }

        // Calculate total pages (minimum 1 to prevent IndexOutOfBoundsException when empty)
        totalPages = Math.max(1, (int) Math.ceil((double) allItems.size() / PAGE_SIZE));

        // Clamp current page
        if (currentPageNum < 1) currentPageNum = 1;
        if (currentPageNum > totalPages) currentPageNum = totalPages;

        // Slice items for current page
        int from = (currentPageNum - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allItems.size());
        pageItems = allItems.subList(from, to);
    }

    // Getters for HTL
    public List<PageItem> getPageItems()   { return pageItems; }
    public int getCurrentPageNum()         { return currentPageNum; }
    public int getTotalPages()             { return totalPages; }
    public boolean isHasPrevious()         { return currentPageNum > 1; }
    public boolean isHasNext()             { return currentPageNum < totalPages; }

    public String getPreviousUrl() {
        if (currentPageNum == 2) return basePath + ".html";
        return basePath + ".p" + (currentPageNum - 1) + ".html";
    }

    public String getNextUrl() {
        return basePath + ".p" + (currentPageNum + 1) + ".html";
    }

    // Inner class
    public static class PageItem {
        private final String title;
        private final String path;
        private final String subText;

        public PageItem(String title, String path, String subText) {
            this.title = title;
            this.path = path;
            this.subText = subText;
        }

        public String getTitle()   { return title; }
        public String getPath()    { return path; }
        public String getSubText() { return subText; }
    }
}