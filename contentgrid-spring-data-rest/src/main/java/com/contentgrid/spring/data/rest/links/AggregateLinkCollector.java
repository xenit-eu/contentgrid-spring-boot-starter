package com.contentgrid.spring.data.rest.links;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.util.LambdaSafe;
import org.springframework.data.rest.webmvc.mapping.LinkCollector;
import org.springframework.hateoas.Links;

@RequiredArgsConstructor
class AggregateLinkCollector implements LinkCollector {
    private final LinkCollector delegate;
    private final Iterable<ContentGridLinkCollector<?>> collectors;

    @Override
    public Links getLinksFor(Object object) {
        return getLinksFor(object, Links.NONE);
    }

    @Override
    public Links getLinksFor(Object object, Links existing) {
        existing = delegate.getLinksFor(object, existing);
        for (var collector : collectors) {
            final Links finalExisting = existing;
            existing = LambdaSafe.callback(ContentGridLinkCollector.class, collector, object, finalExisting)
                    .invokeAnd(c -> c.getLinksFor(object, finalExisting))
                    .get(finalExisting);
        }
        return existing;
    }

    @Override
    public Links getLinksForNested(Object object, Links existing) {
        existing = delegate.getLinksForNested(object, existing);
        for (var collector : collectors) {
            final Links finalExisting = existing;
            existing = LambdaSafe.callback(ContentGridLinkCollector.class, collector, object, finalExisting)
                    .invokeAnd(c -> c.getLinksForNested(object, finalExisting))
                    .get(finalExisting);
        }
        return existing;
    }
}
