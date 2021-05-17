package br.com.zup.edu.validations.handler

import br.com.zup.edu.validations.handler.config.ExceptionHandler
import io.grpc.Status
import io.micronaut.data.exceptions.EmptyResultException
import javax.inject.Singleton

@Singleton
class EmptyResultExceptionHandler : ExceptionHandler<EmptyResultException> {
    override fun handle(e: EmptyResultException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription("Objeto não encontrado no banco de dados")
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is EmptyResultException
    }
}