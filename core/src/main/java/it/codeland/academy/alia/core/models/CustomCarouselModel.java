package it.codeland.academy.alia.core.models;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Model(
    adaptables = SlingHttpServletRequest.class,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class CustomCarouselModel {

    @SlingObject
    private SlingHttpServletRequest request;

    @ValueMapValue
    private String autoplaySpeed;

    @ValueMapValue
    private String transitionStyle;

    @ValueMapValue
    private String captionOverlay;

    private List<SlideItem> slides = new ArrayList<>();

    @PostConstruct
    protected void init() {
        Resource resource = request.getResource();
        // Read child slide resources
        for (Resource child : resource.getChildren()) {
            String subText = extractSubText(child);
            slides.add(new SlideItem(child, subText));
        }
    }

    // Reads text property from a hardcoded Text component inside the slide resource
    private String extractSubText(Resource slideResource) {
        for (Resource child : slideResource.getChildren()) {
            ValueMap vm = child.getValueMap();
            String rt = vm.get("sling:resourceType", String.class);
            if (rt != null && rt.contains("text")) {
                return vm.get("text", String.class);
            }
        }
        return null;
    }

    public List<SlideItem> getSlides() { return slides; }
    public String getAutoplaySpeed() { return autoplaySpeed != null ? autoplaySpeed : "5000"; }
    public String getTransitionStyle() { return transitionStyle != null ? transitionStyle : "slide"; }
    public String getCaptionOverlay() { return captionOverlay; }

    // Inner class representing a single slide
    public static class SlideItem {
        private final Resource resource;
        private final String subText;

        public SlideItem(Resource resource, String subText) {
            this.resource = resource;
            this.subText = subText;
        }

        public String getPath() { return resource.getPath(); }
        public String getName() { return resource.getName(); }
        public String getSubText() { return subText; }
    }
}