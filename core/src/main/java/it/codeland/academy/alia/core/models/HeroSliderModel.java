package it.codeland.academy.alia.core.models;

import com.adobe.cq.export.json.ComponentExporter;
import com.adobe.cq.export.json.ExporterConstants;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Exporter;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.Self;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sling Model for the Hero Slider component.
 *
 * <p>Reads the multifield child nodes stored under <code>slides</code> and
 * exposes them as a typed list of {@link Slide} objects to the HTL template.</p>
 */
@Model(
    adaptables = SlingHttpServletRequest.class,
    adapters = {HeroSliderModel.class, ComponentExporter.class},
    resourceType = HeroSliderModel.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
@Exporter(
    name = ExporterConstants.SLING_MODEL_EXPORTER_NAME,
    extensions = ExporterConstants.SLING_MODEL_EXTENSION
)
public class HeroSliderModel implements ComponentExporter {

    /** Resource type — must match the component folder path under /apps */
    static final String RESOURCE_TYPE = "academy-alia/components/heroslider";

    @Self
    private SlingHttpServletRequest request;

    /**
     * AEM multifield stores each item as a child node under the named node.
     * Using @ChildResource on a List<Resource> collects every child resource
     * of the "slides" node into a list automatically.
     */
    @ChildResource(name = "slides")
    private List<Resource> slideResources;

    private List<Slide> slides = new ArrayList<>();

    @PostConstruct
    private void init() {
        if (slideResources != null) {
            for (Resource res : slideResources) {
                Slide slide = res.adaptTo(Slide.class);
                if (slide != null) {
                    slides.add(slide);
                }
            }
        }
    }

    /** @return immutable list of configured slides (never null) */
    public List<Slide> getSlides() {
        return Collections.unmodifiableList(slides);
    }

    @Override
    public String getExportedType() {
        return RESOURCE_TYPE;
    }

    // -------------------------------------------------------------------------
    // Inner model for a single slide
    // -------------------------------------------------------------------------

    /**
     * Sling Model for one slide inside the multifield.
     * Each slide resource is a synthetic child node (item0, item1, …)
     * created by the multifield widget.
     */
    @Model(
        adaptables = Resource.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
    )
    public static class Slide {

        /** Path to the DAM asset selected via the image widget */
        @Inject
        private String imageReference;

        /** Alt text for the image */
        @Inject
        private String imageAlt;

        /** Main headline — required */
        @Inject
        private String title;

        /** Optional label shown above the main title */
        @Inject
        private String pretitle;

        /** Optional supporting text shown below the title */
        @Inject
        private String subtitle;

        /** CTA button label */
        @Inject
        private String ctaLabel;

        /** CTA button href */
        @Inject
        private String ctaLink;

        /** CTA link target (_self | _blank) */
        @Inject
        private String ctaTarget;

        // -- Getters ----------------------------------------------------------

        public String getImageReference() { return imageReference; }
        public String getImageAlt()       { return imageAlt;       }
        public String getTitle()          { return title;          }
        public String getPretitle()       { return pretitle;       }
        public String getSubtitle()       { return subtitle;       }
        public String getCtaLabel()       { return ctaLabel;       }
        public String getCtaLink()        { return ctaLink;        }
        public String getCtaTarget()      { return ctaTarget != null ? ctaTarget : "_self"; }
    }
}
