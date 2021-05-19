package br.com.zup.edu.externo.bcb

import br.com.zup.edu.ContaResponse
import br.com.zup.edu.TitularResponse
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.Conta
import br.com.zup.edu.chave.TipoChave
import br.com.zup.edu.chave.TipoConta
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client("\${BCB.url}")
interface BCBClient {

    @Post(value = "/pix/keys")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun cadastraPix(@Body request: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun removePix(@PathVariable key: String, @Body request: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>

    @Get(value = "/pix/keys/{key}")
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    fun consultaPix(@PathVariable key:String): HttpResponse<PixKeyDetailsResponse>
}

data class PixKeyDetailsResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

data class DeletePixKeyRequest(
    val key: String,
    val participant: String
)

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime
)

data class CreatePixKeyRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
){
    constructor(chave: ChavePix):
            this(
                keyType = PixKeyType.translate(chave.tipoChave),
                key = chave.chave,
                bankAccount = BankAccount(chave.conta),
                owner = Owner(
                    Owner.OwnerType.NATURAL_PERSON,
                    chave.conta.nomeTitular,
                    chave.conta.cpfTitular
                )
            )
}

data class CreatePixKeyResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
)

enum class PixKeyType {
    CPF,
    PHONE,
    EMAIL,
    RANDOM;

    fun toTipoChave(): br.com.zup.edu.TipoChave {
        return when(this){
            CPF ->  br.com.zup.edu.TipoChave.CPF
            PHONE ->  br.com.zup.edu.TipoChave.CELULAR
            EMAIL ->  br.com.zup.edu.TipoChave.EMAIL
            RANDOM ->  br.com.zup.edu.TipoChave.ALEATORIA
        }
    }

    companion object{
        fun translate(tipoChave: TipoChave): PixKeyType{
            return when(tipoChave){
                TipoChave.CPF -> CPF
                TipoChave.CELULAR -> PHONE
                TipoChave.EMAIL -> EMAIL
                TipoChave.ALEATORIA -> RANDOM
            }
        }
    }
}

enum class AccontType {
    CACC, SVGS;

    fun toTipoConta(): br.com.zup.edu.TipoConta {
        return when(this){
            CACC -> br.com.zup.edu.TipoConta.CONTA_POUPANCA
            SVGS -> br.com.zup.edu.TipoConta.CONTA_CORRENTE
        }
    }

    companion object{
        fun translate(tipoConta: TipoConta): AccontType {
            return when(tipoConta){
                TipoConta.CONTA_POUPANCA -> SVGS
                TipoConta.CONTA_CORRENTE -> CACC
            }
        }

    }
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccontType
){
    fun toContaResponse(): ContaResponse {
        return ContaResponse.newBuilder()
            .setInstituicao(Conta.ITAU)
            .setAgencia(branch)
            .setNumero(accountNumber)
            .setTipoConta(accountType.toTipoConta())
            .build()
    }

    constructor(conta: Conta):
            this(
                participant = Conta.ITAU_ISPB,
                branch = conta.agencia,
                accountNumber = conta.numero,
                AccontType.translate(conta.tipoConta)
            )
}

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {
    fun toTitularResponse(): TitularResponse {
        return TitularResponse.newBuilder()
            .setCpf(taxIdNumber)
            .setNome(name)
            .build()
    }

    enum class OwnerType {
        NATURAL_PERSON;
    }
}
