package br.com.zup.edu.chave.cadastra

import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.exceptions.ChaveExistenteException
import br.com.zup.edu.externo.bcb.BCBClient
import br.com.zup.edu.externo.bcb.CreatePixKeyRequest
import br.com.zup.edu.externo.itau.ItauClient
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChaveService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BCBClient
) {

    @Transactional
    fun registra(@Valid novaChavePix: NovaChavePix) : ChavePix {

        if(repository.existsByChave(novaChavePix.chave!!))
            throw ChaveExistenteException()

        val responseItau = itauClient.consulta(novaChavePix.clientId!!, novaChavePix.tipoConta!!)

        val conta = responseItau.body()?.toModel() ?: throw IllegalArgumentException("Conta inválida")

        var chave = novaChavePix.toChavePix(conta)

        val responseBcb = bcbClient.cadastraPix(CreatePixKeyRequest(chave))

        if(responseBcb.status != HttpStatus.CREATED)
            throw IllegalArgumentException("Erro ao cadastrar pix no Banco Central")

        chave.atualiza(responseBcb.body()!!.key)

        repository.save(chave)

        return chave
    }
}