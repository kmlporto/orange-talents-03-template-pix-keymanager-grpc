package br.com.zup.edu.chave.remove

import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveChaveService(
    @Inject private val repository: ChavePixRepository) {

    @Transactional
    fun remove(@Valid remove: RemoveChavePix){
        val chavePix: ChavePix = repository.findByIdAndClientId(remove.pixId.toLong(), remove.clientId)

        repository.delete(chavePix)
    }

}