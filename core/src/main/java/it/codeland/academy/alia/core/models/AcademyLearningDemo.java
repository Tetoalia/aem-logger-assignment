package it.codeland.academy.alia.core.models;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.Self;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Model(
    adaptables = Resource.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class AcademyLearningDemo {

    @Self
    private Resource resource;

    @Inject
    @Named("jcr:title")
    private String title;

    @Inject
    @Optional
    private String customDescription;

    @PostConstruct
    protected void init() {
        // Read from the page jcr:content (as required)
        ResourceResolver resolver = resource.getResourceResolver();
        PageManager pageManager = resolver.adaptTo(PageManager.class);
        Page page = pageManager != null ? pageManager.getContainingPage(resource) : null;
        Resource pageContent = page != null ? page.getContentResource() : null;

        if (pageContent != null) {
            if (StringUtils.isBlank(title)) {
                title = pageContent.getValueMap().get("jcr:title", "");
            }
            customDescription = pageContent.getValueMap().get("customDescription", customDescription);
        }

        if (StringUtils.isNotBlank(customDescription)) {
            customDescription = customDescription.toUpperCase();
        } else {
            customDescription = "Not Available";
        }
    }

    public String getTitle() {
        return title;
    }

    public String getCustomDescription() {
        return customDescription;
    }
}