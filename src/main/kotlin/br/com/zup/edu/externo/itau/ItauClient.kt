package br.com.zup.edu.externo.itau

import br.com.zup.edu.chave.Conta
import br.com.zup.edu.chave.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${itauERP.url}")
interface ItauClient {

    @Get("/clientes/{clientId}/contas")
    fun consulta(@PathVariable clientId: String, @QueryValue tipo: TipoConta) : HttpResponse<ContaResponse>

}

data class ContaResponse(val tipo: TipoConta, val instituicao: InstituicaoResponse, val agencia: String, val numero: String, val titular: TitularResponse){

    fun toModel(): Conta {
        return Conta(
            agencia = agencia,
            numero = numero,
            instituicao = instituicao.nome,
            nomeTitular = titular.nome,
            cpfTitular = titular.cpf,
            tipoConta = tipo
        )
    }
}

data class InstituicaoResponse(val nome: String, val ispb: String)

data class TitularResponse(val id: String, val nome: String, val cpf: String)
