package com.contentgrid.spring.querydsl.hibernate;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

public class PostgresNormalizeFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        var returnType = functionContributions.getTypeConfiguration().getBasicTypeForJavaType(String.class);
        functionContributions.getFunctionRegistry().registerNamed("normalize", returnType);
    }
}
