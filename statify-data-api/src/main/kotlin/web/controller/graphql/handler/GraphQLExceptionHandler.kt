package org.danila.web.controller.graphql.handler

import com.netflix.graphql.dgs.exceptions.DgsBadRequestException
import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.execution.ResultPath
import graphql.language.SourceLocation
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class GraphQLExceptionHandler : DataFetcherExceptionHandler {

    override fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters?): CompletableFuture<DataFetcherExceptionHandlerResult> {
        val error = if (handlerParameters != null)
            toGraphQLError(
                exception = handlerParameters.exception,
                location = handlerParameters.sourceLocation,
                path = handlerParameters.path
            )
        else
            GraphqlErrorBuilder.newError()
                .message("Unexpected error")
                .errorType(ErrorType.DataFetchingException)
                .build()

        return CompletableFuture.completedFuture(
            DataFetcherExceptionHandlerResult.newResult()
                .error(error)
                .build()
        )
    }

    private fun toGraphQLError(
        exception: Throwable,
        location: SourceLocation?,
        path: ResultPath?
    ): GraphQLError {
        return when (exception) {
            is DgsBadRequestException -> GraphqlErrorBuilder.newError()
                .message(exception.message)
                .location(location)
                .path(path)
                .errorType(exception.errorType)
                .build()

            is IllegalArgumentException -> GraphqlErrorBuilder.newError()
                .message("Invalid input: ${exception.message}")
                .location(location)
                .path(path)
                .errorType(ErrorType.ValidationError)
                .build()

            else -> GraphqlErrorBuilder.newError()
                .message("Unexpected error: ${exception.message}")
                .location(location)
                .path(path)
                .errorType(ErrorType.DataFetchingException)
                .build()
        }
    }

}