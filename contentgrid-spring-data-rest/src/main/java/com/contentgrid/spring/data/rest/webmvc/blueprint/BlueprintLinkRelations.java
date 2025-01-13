package com.contentgrid.spring.data.rest.webmvc.blueprint;

import lombok.experimental.UtilityClass;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.HalLinkRelation;

@UtilityClass
public class BlueprintLinkRelations {

    static final String CURIE = "blueprint";
    static final UriTemplate TEMPLATE = UriTemplate.of("https://contentgrid.cloud/rels/blueprint/{rel}");

    public static final LinkRelation ENTITY = HalLinkRelation.curied(CURIE, "entity");
    public static final String ENTITY_STRING = CURIE + ":entity";
    public static final LinkRelation ATTRIBUTE = HalLinkRelation.curied(CURIE, "attribute");
    public static final String ATTRIBUTE_STRING = CURIE + ":attribute";
    public static final LinkRelation RELATION = HalLinkRelation.curied(CURIE, "relation");
    public static final String RELATION_STRING = CURIE + ":relation";
    public static final LinkRelation TARGET_ENTITY = HalLinkRelation.curied(CURIE, "target-entity");
    public static final String TARGET_ENTITY_STRING = CURIE + ":target-entity";
    public static final LinkRelation CONSTRAINT = HalLinkRelation.curied(CURIE, "constraint");
    public static final String CONSTRAINT_STRING = CURIE + ":constraint";
    public static final LinkRelation SEARCH_PARAM = HalLinkRelation.curied(CURIE, "search-param");
    public static final String SEARCH_PARAM_STRING = CURIE + ":search-param";
    public static final LinkRelation SORT_PARAM = HalLinkRelation.curied(CURIE, "sort-param");
    public static final String SORT_PARAM_STRING = CURIE + ":sort-param";
}
