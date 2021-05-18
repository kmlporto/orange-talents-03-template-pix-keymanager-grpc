package br.com.zup.edu.externo.bcb

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

}

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
) {
    fun atualiza(chave: ChavePix): ChavePix {
        chave.chave = key
        return chave
    }
}

enum class PixKeyType {
    CPF,
    PHONE,
    EMAIL,
    RANDOM;

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

    constructor(conta: Conta):
            this(
                participant = conta.ITAU_ISPB,
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
    enum class OwnerType {
        NATURAL_PERSON;
    }
}
