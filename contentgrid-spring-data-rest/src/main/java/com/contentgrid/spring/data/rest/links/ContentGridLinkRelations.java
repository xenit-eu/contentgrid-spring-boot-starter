package com.contentgrid.spring.data.rest.links;

import lombok.experimental.UtilityClass;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;

@UtilityClass
public class ContentGridLinkRelations {
    final static String CURIE = "cg";
    final static UriTemplate TEMPLATE = UriTemplate.of("https://contentgrid.com/rels/contentgrid/{rel}");

    public final static LinkRelation RELATION = HalLinkRelation.curied(CURIE, "relation");
    public final static LinkRelation CONTENT = HalLinkRelation.curied(CURIE, "content");

}
