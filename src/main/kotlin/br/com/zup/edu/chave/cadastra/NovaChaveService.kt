package br.com.zup.edu.chave.cadastra

import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.Conta
import br.com.zup.edu.exceptions.ChaveExistenteException
import br.com.zup.edu.exceptions.ContaNaoEncontradaException
import br.com.zup.edu.externo.ContaResponse
import br.com.zup.edu.externo.ItauClient
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
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

        if(response.status.name == "NOT_FOUND") {
            throw ContaNaoEncontradaException()
        }
        val conta = response.body()?.toModel()!!


        val chave = novaChavePix.toChavePix(conta)
        repository.save(chave)

        return chave
    }
}