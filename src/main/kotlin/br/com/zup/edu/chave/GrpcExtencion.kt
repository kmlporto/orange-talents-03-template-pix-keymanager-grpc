package br.com.zup.edu.chave

import br.com.zup.edu.NovaChaveRequest
import br.com.zup.edu.RemoveChaveRequest
import br.com.zup.edu.TipoChave.UNKNOWN_TIPO_CHAVE
import br.com.zup.edu.TipoConta.UNKNOWN_TIPO_CONTA
import br.com.zup.edu.chave.cadastra.NovaChavePix
import br.com.zup.edu.chave.remove.RemoveChavePix

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
    return RemoveChavePix(clientId = this.clientId, pixId = this.pixId)
}
