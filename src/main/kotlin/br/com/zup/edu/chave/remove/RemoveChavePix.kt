package br.com.zup.edu.chave.remove

import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
class RemoveChavePix(
    @field:NotBlank
    val clientId: String,
    @field:NotNull
    val pixId: UUID
)