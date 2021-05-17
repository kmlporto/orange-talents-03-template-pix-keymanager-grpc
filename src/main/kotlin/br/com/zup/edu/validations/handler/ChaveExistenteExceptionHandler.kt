package br.com.zup.edu.validations.handler

import br.com.zup.edu.exceptions.ChaveExistenteException
import br.com.zup.edu.validations.handler.config.ExceptionHandler
import br.com.zup.edu.validations.handler.config.ExceptionHandler.StatusWithDetails
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChaveExistenteExceptionHandler : ExceptionHandler<ChaveExistenteException> {

    override fun handle(e: ChaveExistenteException): StatusWithDetails {
        return StatusWithDetails(Status.ALREADY_EXISTS
            .withDescription(e.message)
            .withCause(e))

    }

    override fun supports(e: Exception): Boolean {
        return e is ChaveExistenteException
    }

}