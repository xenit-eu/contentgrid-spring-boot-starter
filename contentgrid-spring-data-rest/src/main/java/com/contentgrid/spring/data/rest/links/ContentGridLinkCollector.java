package com.contentgrid.spring.data.rest.links;

import org.springframework.hateoas.Links;

public interface ContentGridLinkCollector<T> {

    Links getLinksFor(T object, Links existing);

    default Links getLinksForNested(T object, Links existing) {
        return existing;
    }
}
