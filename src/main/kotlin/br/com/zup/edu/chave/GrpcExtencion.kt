package br.com.zup.edu.chave

import br.com.zup.edu.ConsultaChaveRequest
import br.com.zup.edu.ConsultaChaveRequest.FiltroCase.BYCHAVE
import br.com.zup.edu.ConsultaChaveRequest.FiltroCase.BYCLIENT
import br.com.zup.edu.NovaChaveRequest
import br.com.zup.edu.RemoveChaveRequest
import br.com.zup.edu.TipoChave.UNKNOWN_TIPO_CHAVE
import br.com.zup.edu.TipoConta.UNKNOWN_TIPO_CONTA
import br.com.zup.edu.chave.cadastra.NovaChavePix
import br.com.zup.edu.chave.consulta.Filtro
import br.com.zup.edu.chave.remove.RemoveChavePix
import io.micronaut.validation.validator.Validator
import java.util.*
import javax.validation.ConstraintViolationException

fun NovaChaveRequest.toNovaChave(): NovaChavePix {
    return NovaChavePix(
        clientId = clientId,
        tipoChave = when(tipoChave){
            UNKNOWN_TIPO_CHAVE -> null
            else -> TipoChave.valueOf(tipoChave.name)
        },
        chave = chave,
        tipoConta = when(tipoConta){
            UNKNOWN_TIPO_CONTA -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }
    )
}

fun RemoveChaveRequest.toRemove(): RemoveChavePix {
    return RemoveChavePix(clientId = this.clientId, pixId = UUID.fromString(this.pixId))
}

fun ConsultaChaveRequest.toModel(validator: Validator): Filtro {
    val filtro = when(filtroCase){
        BYCHAVE -> Filtro.ByChave(byChave)
        BYCLIENT -> byClient.let { Filtro.ByClient(byClient.clientId, UUID.fromString(byClient.pixId) ) }
        else -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if(violations.isNotEmpty())
        throw ConstraintViolationException(violations)

    return filtro
}