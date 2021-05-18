package br.com.zup.edu.chave.remove

import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.externo.bcb.BCBClient
import br.com.zup.edu.externo.bcb.DeletePixKeyRequest
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BCBClient
) {

    @Transactional
    fun remove(@Valid remove: RemoveChavePix){
        val chavePix: ChavePix = repository.findByIdAndClientId(remove.pixId.toLong(), remove.clientId)

        val responseBCB = bcbClient.removePix(chavePix.chave, DeletePixKeyRequest(chavePix.chave, chavePix.conta.ITAU_ISPB))

        if(responseBCB.status != HttpStatus.OK)
            throw IllegalArgumentException("Erro ao deletar pix no Banco Central")

        repository.delete(chavePix)
    }

}