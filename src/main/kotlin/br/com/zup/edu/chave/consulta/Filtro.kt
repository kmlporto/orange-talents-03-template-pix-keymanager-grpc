package br.com.zup.edu.chave.consulta

import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.exceptions.NotFoundException
import br.com.zup.edu.externo.bcb.BCBClient
import br.com.zup.edu.externo.bcb.PixKeyDetailsResponse
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BCBClient) : PixKeyDetailsResponse;

    @Introspected
    data class ByChave(@field:NotBlank @Size(max=77) val chave: String): Filtro(){

        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): PixKeyDetailsResponse{
            val chaveOptional = repository.findByChave(chave)

            if(!chaveOptional.isPresent)
                throw NotFoundException("Chave não cadastrada no sistema")

            val responseBCB = bcbClient.consultaPix(chaveOptional.get().chave)

            if(responseBCB.status != HttpStatus.OK || responseBCB.body() == null)
                throw NotFoundException("Chave não cadastrada no Banco Central")

            return responseBCB.body()!!
        }

    }

    @Introspected
    data class ByClient(@field:NotBlank val clientId: String, @field:NotBlank val pixId: String): Filtro(){

        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): PixKeyDetailsResponse{
            val chaveOptional = repository.findById(pixId.toLong())

            if(!chaveOptional.isPresent)
                throw NotFoundException("Chave não cadastrada no sistema")

            if(chaveOptional.get().clientId != clientId)
                throw IllegalStateException("Chave não pertence ao clientId passado")

            val responseBCB = bcbClient.consultaPix(chaveOptional.get().chave)

            if(responseBCB.status != HttpStatus.OK || responseBCB.body() == null)
                throw NotFoundException("Chave não cadastrada no Banco Central")

            return responseBCB.body()!!
        }

    }

    @Introspected
    class Invalido : Filtro(){
        override fun filtra(repository: ChavePixRepository, bcbClient: BCBClient): PixKeyDetailsResponse {
            throw IllegalStateException("Filtro inválido")
        }

    }
}