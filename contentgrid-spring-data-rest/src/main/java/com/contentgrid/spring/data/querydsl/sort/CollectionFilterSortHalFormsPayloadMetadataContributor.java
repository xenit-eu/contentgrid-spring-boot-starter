package com.contentgrid.spring.data.querydsl.sort;

import com.contentgrid.spring.data.rest.webmvc.HalFormsPayloadMetadataContributor;
import com.contentgrid.spring.querydsl.mapping.CollectionFilter;
import com.contentgrid.spring.querydsl.mapping.CollectionFiltersMapping;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.web.SortHandlerMethodArgumentResolverSupport;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.mediatype.MediaTypeConfigurationCustomizer;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions;
import org.springframework.hateoas.mediatype.html.HtmlInputType;

@RequiredArgsConstructor
public class CollectionFilterSortHalFormsPayloadMetadataContributor extends
        SortHandlerMethodArgumentResolverSupport implements HalFormsPayloadMetadataContributor,
        MediaTypeConfigurationCustomizer<HalFormsConfiguration> {

    private final CollectionFiltersMapping collectionFiltersMapping;
    private final Repositories repositories;
    private final MessageResolver messageResolver;

    @Override
    public Stream<PropertyMetadata> contributeToCreateForm(Class<?> domainType) {
        return Stream.empty();
    }

    @Override
    public Stream<PropertyMetadata> contributeToUpdateForm(Class<?> domainType) {
        return Stream.empty();
    }

    @Override
    public Stream<PropertyMetadata> contributeToSearchForm(Class<?> domainType) {
        var sortOptions = collectionFiltersMapping.forDomainType(domainType)
                .forSorting()
                .documented()
                .filters()
                .flatMap(filter -> {
                    return Stream.of(
                            Direction.ASC,
                            Direction.DESC
                    ).map(dir -> new SortOption(
                            filter.getFilterName(),
                            dir,
                            messageResolver.resolve(new DirectionMessageSourceResolvable(domainType, filter, dir)),
                            foldIntoExpressions(Sort.by(dir, filter.getFilterName()))
                    ));
                }).toList();

        if (sortOptions.isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(new SortPropertyMetadata(sortOptions));
        }
    }

    @Override
    public HalFormsConfiguration customize(HalFormsConfiguration configuration) {
        var sortParamName = getSortParameter(null);
        for (Class<?> domainType : repositories) {
            configuration = configuration.withOptions(domainType, sortParamName, pm -> {
                if (pm instanceof SortPropertyMetadata sortPropertyMetadata) {
                    return HalFormsOptions.inline(sortPropertyMetadata.getSortOptions())
                            .withMinItems(0L)
                            .withValueField("value")
                            .withPromptField("prompt");
                }

                // Returning null here will just not add any options to this field.
                // That is the expected fallback when the sortParam does turn out not to actually be SortPropertyMetadata,
                // since that means the sort parameter was actually an existing field name on the domain type
                return null;
            });
        }
        return configuration;
    }

    @Value
    public static class SortOption {

        String property;

        @Getter(value = AccessLevel.NONE)
        Sort.Direction direction;

        String prompt;

        @Getter(value = AccessLevel.NONE)
        List<String> expressions;

        public String getValue() {
            return expressions.get(0);
        }

        public String getDirection() {
            return direction.name().toLowerCase(Locale.ROOT);
        }
    }

    @Getter
    @RequiredArgsConstructor
    public class SortPropertyMetadata implements PropertyMetadata {

        private final List<SortOption> sortOptions;

        @Override
        public String getName() {
            return getSortParameter(null);
        }

        @Override
        public boolean isRequired() {
            return false;
        }

        @Override
        public boolean isReadOnly() {
            return false;
        }

        @Override
        public Optional<String> getPattern() {
            return Optional.empty();
        }

        @Override
        public ResolvableType getType() {
            return ResolvableType.forClass(Sort.Order.class);
        }

        @Override
        public String getInputType() {
            return HtmlInputType.TEXT_VALUE;
        }

    }

    @RequiredArgsConstructor
    private static class I18nPropertyMessageSourceResolvable implements MessageSourceResolvable {

        private final Class<?> domainType;
        private final CollectionFilter<?> filter;

        @Override
        public String[] getCodes() {
            String globalCode = "%s.%s".formatted(domainType.getName(), filter.getFilterName());
            return new String[]{
                    globalCode + "._prompt",
                    globalCode
            };
        }

        @Override
        public String getDefaultMessage() {
            return filter.getFilterName();
        }
    }

    @RequiredArgsConstructor
    private static class DirectionMessageSourceResolvable implements MessageSourceResolvable {

        private static final String ORDER_CLASS = Sort.Order.class.getName();
        private final Class<?> domainType;
        private final CollectionFilter<?> filter;
        private final Direction direction;

        @Override
        public String[] getCodes() {
            var globalCode = ORDER_CLASS + '.' + direction.name().toLowerCase(Locale.ROOT);
            return Stream.concat(
                    classNamesStream(filter.getParameterType()).map(c -> globalCode + '.' + c),
                    Stream.of(globalCode)
            ).toArray(String[]::new);
        }

        private Stream<String> classNamesStream(Class<?> clazz) {
            return Stream.<Class<?>>iterate(clazz, c -> c != Object.class, Class::getSuperclass)
                    .flatMap(c -> Stream.of(
                            c.getName(),
                            c.getSimpleName()
                    ));
        }

        @Override
        public Object[] getArguments() {
            // MessageSourceResolvable(s) used for arguments are also automatically resolved through the MessageSource
            return new Object[]{new I18nPropertyMessageSourceResolvable(domainType, filter)};
        }

        @Override
        public String getDefaultMessage() {
            return "{0} " + direction.name();
        }
    }
}
