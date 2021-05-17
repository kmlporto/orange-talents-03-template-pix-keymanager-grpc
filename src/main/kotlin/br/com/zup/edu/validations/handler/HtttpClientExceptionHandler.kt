package br.com.zup.edu.validations.handler

import br.com.zup.edu.validations.handler.config.ExceptionHandler
import io.grpc.Status
import io.micronaut.http.client.exceptions.HttpClientResponseException
import javax.inject.Singleton

@Singleton
class HtttpClientExceptionHandler : ExceptionHandler<HttpClientResponseException> {

    override fun handle(e: HttpClientResponseException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
                Status.INVALID_ARGUMENT
                .withDescription("Argumentos inválidos")
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is HttpClientResponseException
    }
}