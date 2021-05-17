package br.com.zup.edu.chave.remove

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
class RemoveChavePix(
    @field:NotBlank
    val clientId: String,
    @field:NotBlank
    val pixId: String
)