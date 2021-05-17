package br.com.zup.edu.chave.cadastra

import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.Conta
import br.com.zup.edu.chave.TipoChave
import br.com.zup.edu.chave.TipoConta
import br.com.zup.edu.validations.annotation.ValidPixKey
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:NotBlank
    val clientId: String?,
    @field:NotNull
    val tipoChave: TipoChave?,
    @field:Size(max=77)
    val chave: String?,
    @field:NotNull
    val tipoConta: TipoConta?
){
    fun toChavePix(conta: Conta): ChavePix {

        return ChavePix(
            clientId = clientId!!,
            tipoChave = TipoChave.valueOf(tipoChave!!.name),
            chave = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            conta = conta
        )
    }
}
