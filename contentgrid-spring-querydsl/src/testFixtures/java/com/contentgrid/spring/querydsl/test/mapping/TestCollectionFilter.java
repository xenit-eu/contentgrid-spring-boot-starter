package com.contentgrid.spring.querydsl.test.mapping;

import com.contentgrid.spring.querydsl.mapping.CollectionFilter;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class TestCollectionFilter<T> implements CollectionFilter<T> {

    @NonNull
    private final String filterName;
    private final String filterType;
    @Builder.Default
    private final boolean documented = true;

    @NonNull
    private final Path<T> path;

    private Collection<T> lastParameters;

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return path.getAnnotatedElement();
    }

    @Override
    public Class<T> getParameterType() {
        return (Class<T>) path.getType();
    }

    @Override
    public Optional<Predicate> createPredicate(Collection<T> parameters) {
        lastParameters = parameters;
        return Optional.empty();
    }

    @Override
    public Optional<OrderSpecifier<?>> createOrderSpecifier(Order order) {
        return Optional.empty();
    }

    public static class TestCollectionFilterBuilder<T> {

        private TestCollectionFilterBuilder<T> lastParameters(Collection<T> lastParameters) {
            throw new UnsupportedOperationException();
        }
    }
}
