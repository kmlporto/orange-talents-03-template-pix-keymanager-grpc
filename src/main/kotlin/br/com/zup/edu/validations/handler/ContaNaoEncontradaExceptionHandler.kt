package br.com.zup.edu.validations.handler

import br.com.zup.edu.exceptions.ContaNaoEncontradaException
import br.com.zup.edu.validations.handler.config.ExceptionHandler
import br.com.zup.edu.validations.handler.config.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ContaNaoEncontradaExceptionHandler : ExceptionHandler<ContaNaoEncontradaException> {

    override fun handle(e: ContaNaoEncontradaException): StatusWithDetails {
        return StatusWithDetails(Status.NOT_FOUND
            .withDescription(e.message)
            .withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return e is ContaNaoEncontradaException
    }
}