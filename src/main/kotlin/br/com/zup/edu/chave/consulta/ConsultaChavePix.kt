package br.com.zup.edu.chave.consulta

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class ConsultaChavePix (
    @field:NotBlank
    val clientId: String,
    @field:NotBlank
    @field:Size(max = 77)
    val pixId: String
)