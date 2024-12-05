// package com.devtracker.exception;

// import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
// import org.springframework.stereotype.Component;

// import graphql.GraphQLError;
// import graphql.GraphqlErrorBuilder;
// import graphql.schema.DataFetchingEnvironment;


// @Component
// public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

//     @Override
//     protected GraphQLError resolveToSingleError(Throwable exception, DataFetchingEnvironment env) {
//         GraphQLError error = transformException(exception);

//         return error;
//     }

//     private GraphQLError transformException(Throwable exception) {
//         if (exception instanceof NotFoundException) {
//             return GraphqlErrorBuilder.newError()
//                     .message(exception.getMessage())
//                     .build();
//         }

//         if (exception instanceof ValidationException) {
//             return GraphqlErrorBuilder.newError()
//                     .message(exception.getMessage())
//                     .build();
//         }

//         // Default error
//         return GraphqlErrorBuilder.newError()
//                 .message("Unexpected error occurred")
//                 .build();
//     }
// }