package br.com.zup.edu.chave.consulta


import br.com.zup.edu.*
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.externo.bcb.PixKeyDetailsResponse
import br.com.zup.edu.util.toTimesTemp
import java.time.LocalDateTime

data class ConsultaDetail(
    val clientId: String?,
    val pixId: String?,
    val tipoChave: TipoChave,
    val valorChave: String,
    val conta: ContaResponse,
    val titular: TitularResponse,
    val criadoEm: LocalDateTime
){
    companion object{
        fun convert(chave: ChavePix): ConsultaDetail{

            return with(chave){
                ConsultaDetail(
                    clientId = clientId,
                    pixId = id.toString(),
                    tipoChave = TipoChave.valueOf(tipoChave.name),
                    valorChave = this.chave,
                    conta = ContaResponse.newBuilder()
                        .setTipoConta(TipoConta.valueOf(conta.tipoConta.name))
                        .setInstituicao(conta.instituicao)
                        .setAgencia(conta.agencia)
                        .setNumero(conta.numero)
                        .build(),
                    titular = TitularResponse.newBuilder()
                        .setNome(conta.nomeTitular)
                        .setCpf(conta.cpfTitular)
                        .build(),
                    criadoEm = criadoEm
                )
            }
        }
        fun convert(responseBCB: PixKeyDetailsResponse): ConsultaDetail{
            return with(responseBCB){
                ConsultaDetail(
                    clientId = "",
                    pixId = "",
                    tipoChave = keyType.toTipoChave(),
                    valorChave = key,
                    conta = bankAccount.toContaResponse(),
                    titular = owner.toTitularResponse(),
                    criadoEm = createdAt
                )
            }
        }
    }

    fun convertToResponse(): ConsultaChaveResponse {
        return ConsultaChaveResponse
            .newBuilder()
            .setClientId(clientId)
            .setPixId(pixId)
            .setTipoChave(tipoChave)
            .setValorChave(valorChave)
            .setTitular(titular)
            .setConta(conta)
            .setCriadoEm(criadoEm.toTimesTemp())
            .build()
    }
}
