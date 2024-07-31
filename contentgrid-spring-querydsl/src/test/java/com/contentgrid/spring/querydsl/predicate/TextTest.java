package com.contentgrid.spring.querydsl.predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.contentgrid.spring.querydsl.annotation.QuerydslPredicateFactory;
import com.contentgrid.spring.querydsl.mapping.UnsupportedCollectionFilterPredicatePathTypeException;
import com.contentgrid.spring.querydsl.test.fixtures.QTestObject;
import com.contentgrid.spring.querydsl.test.predicate.PredicateFactoryTester;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TextTest {
    private final PredicateFactoryTester<QTestObject> TESTER = new PredicateFactoryTester<>(new QTestObject(
            PathMetadataFactory.forVariable("o")));

    static Stream<Class<? extends QuerydslPredicateFactory>> factories() {
        return Stream.of(
                Text.EqualsIgnoreCase.class,
                Text.StartsWith.class,
                Text.StartsWithIgnoreCase.class
        );
    }

    static Stream<Arguments> boundPredicates() {
        BiFunction<StringPath, String, BooleanExpression> equalsIgnoreCase = StringExpression::equalsIgnoreCase;
        BiFunction<StringPath, String, BooleanExpression> startsWith = StringExpression::startsWith;
        BiFunction<StringPath, String, BooleanExpression> startsWithIgnoreCase = StringExpression::startsWithIgnoreCase;

        return Stream.of(
                Arguments.of(new Text.EqualsIgnoreCase(), equalsIgnoreCase),
                Arguments.of(new Text.StartsWith(), startsWith),
                Arguments.of(new Text.StartsWithIgnoreCase(), startsWithIgnoreCase)
        );
    }

    @ParameterizedTest
    @MethodSource("factories")
    void rejectsNonStringPath(Class<QuerydslPredicateFactory<Path<?>, String>> type)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        var factory = type.getDeclaredConstructor().newInstance();
        var tests = TESTER.evaluateAll(factory, Stream.of(
                QTestObject::booleanValue,
                QTestObject::embeddedObject,
                QTestObject::embeddedItems,
                QTestObject::timeValue,
                QTestObject::uuidValue
        ));

        assertThat(tests).allSatisfy(test -> {
            assertThatThrownBy(test::boundPaths)
                    .isInstanceOf(UnsupportedCollectionFilterPredicatePathTypeException.class);
            assertThatThrownBy(() -> test.bind(List.of("abc")))
                    .isInstanceOf(UnsupportedCollectionFilterPredicatePathTypeException.class);
        });
    }

    @ParameterizedTest
    @MethodSource("boundPredicates")
    void bindsStringPath(QuerydslPredicateFactory<StringPath, String> predicateFactory, BiFunction<StringPath, String, BooleanExpression> mapper) {
        var factory = TESTER.evaluate(predicateFactory, QTestObject::stringValue);

        assertThat(factory.boundPaths()).containsExactly(TESTER.getPathBase().stringValue);
        assertThat(factory.bind(List.of())).isEmpty();
        assertThat(factory.bind(List.of("abc"))).hasValueSatisfying(predicate -> {
            assertThat(predicate).isEqualTo(mapper.apply(TESTER.getPathBase().stringValue, "abc"));
        });
        assertThat(factory.bind(List.of("ABCdef"))).hasValueSatisfying(predicate -> {
            assertThat(predicate).isEqualTo(mapper.apply(TESTER.getPathBase().stringValue, "ABCdef"));
        });
        assertThat(factory.bind(List.of("ABCdef", "GHI"))).hasValueSatisfying(predicate -> {
            assertThat(predicate).isEqualTo(mapper.apply(TESTER.getPathBase().stringValue, "ABCdef")
                    .or(mapper.apply(TESTER.getPathBase().stringValue, "GHI")));
        });
    }

}