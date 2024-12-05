package com.devtracker.config; // Update this to match your package structure

import graphql.scalars.ExtendedScalars;
import graphql.schema.visibility.NoIntrospectionGraphqlFieldVisibility;

import org.springframework.graphql.execution.RuntimeWiringConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GraphQLConfig {
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return builder -> builder.scalar(ExtendedScalars.DateTime)
                .fieldVisibility(NoIntrospectionGraphqlFieldVisibility.NO_INTROSPECTION_FIELD_VISIBILITY);
    }

}