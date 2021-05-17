package br.com.zup.edu.chave.cadastra

import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.exceptions.ChaveExistenteException
import br.com.zup.edu.externo.ItauClient
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ItauClient) {

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix) : ChavePix {

        if(repository.existsByChave(novaChavePix.chave!!))
            throw ChaveExistenteException()

        val response = itauClient.consulta(novaChavePix.clientId!!, novaChavePix.tipoConta!!)

        val conta = response.body()?.toModel() ?: throw IllegalArgumentException("Conta inválida")

        val chave = novaChavePix.toChavePix(conta)
        repository.save(chave)

        return chave
    }
}