package br.com.zup.edu.validations.handler

import br.com.zup.edu.validations.handler.config.ExceptionHandler
import br.com.zup.edu.validations.handler.config.ExceptionHandler.StatusWithDetails
import io.grpc.Status

class DefaultExceptionHandler: ExceptionHandler<Exception> {
    override fun handle(e: Exception): StatusWithDetails {
        val status = when(e){
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)

            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)

            else -> Status.UNKNOWN
        }

        return StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}