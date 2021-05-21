package br.com.zup.edu.chave.consulta

import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.exceptions.NotFoundException
import br.com.zup.edu.externo.bcb.BCBClient
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BCBClient) : ConsultaDetail;

    @Introspected
    data class ByChave(@field:NotBlank @Size(max=77) val chave: String): Filtro(){

        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): ConsultaDetail{
            val chaveOptional = repository.findByChave(chave)

            if(chaveOptional.isPresent)
                return ConsultaDetail.convert(chaveOptional.get())

            val responseBCB = bcbClient.consultaPix(chave)

            if(responseBCB.status != HttpStatus.OK || responseBCB.body() == null)
                throw NotFoundException("Chave não cadastrada no Banco Central")

            return ConsultaDetail.convert(responseBCB.body()!!)
        }

    }

    @Introspected
    data class ByClient(@field:NotBlank val clientId: String,
                        @field:NotNull val pixId: UUID): Filtro(){

        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): ConsultaDetail{
            val chaveOptional = repository.findById(pixId)

            if(!chaveOptional.isPresent)
                throw NotFoundException("Chave não cadastrada no sistema")

            if(chaveOptional.get().clientId != clientId)
                throw IllegalStateException("Chave não pertence ao clientId passado")

            return ConsultaDetail.convert(chaveOptional.get())
        }

    }

    @Introspected
    class Invalido : Filtro(){
        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): ConsultaDetail {
            throw IllegalStateException("Filtro inválido")
        }

    }
}