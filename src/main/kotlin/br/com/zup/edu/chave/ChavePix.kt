package br.com.zup.edu.chave

import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(name = "uk_chave_pix", columnNames = ["chave"])]
)
class ChavePix(

    @field:NotBlank
    @Column(nullable = false)
    val clientId: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotBlank
    @Column(nullable = false, unique = true)
    var chave: String,

    @Embedded
    val conta: Conta,

    ){

    @Id
    @GeneratedValue
    var id: UUID? = null

    @Column(nullable = false)
    val criadoEm = LocalDateTime.now()

    fun atualiza(chave: String){
        this.chave = chave
    }
}