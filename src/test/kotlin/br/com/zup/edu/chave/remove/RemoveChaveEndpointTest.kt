package br.com.zup.edu.chave.remove

import br.com.zup.edu.KeyManagerRemoveServiceGrpc
import br.com.zup.edu.RemoveChaveRequest
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.Conta
import br.com.zup.edu.chave.TipoChave
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub){


    lateinit var request: RemoveChaveRequest
    lateinit var chaveCPF : ChavePix

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()

        chaveCPF = ChavePix(
            clientId = "2ac09233-21b2-4276-84fb-d83dbd9f8bab",
            tipoChave = TipoChave.CPF,
            chave = "83082363083",
            conta = Conta(
                agencia = "0001",
                numero = "483201",
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeTitular = "Alefh Silva",
                cpfTitular = "83082363083"
            )
        )
        request = RemoveChaveRequest
            .newBuilder()
            .setClientId(chaveCPF.clientId)
            .setPixId("1")
            .build()
    }


    @Test
    fun `exclui chave existente`() {

        repository.save(chaveCPF)

        request = RemoveChaveRequest
            .newBuilder()
            .setClientId(chaveCPF.clientId)
            .setPixId(chaveCPF.id.toString())
            .build()

        val response = grpcClient.remove(request)


        assertNotNull(response)
        assertEquals(request.pixId, response.pixId)
        assertEquals(request.clientId, response.clientId)
    }

    @Test
    fun `falha em excluir chave inexistente`() {
        val thrown = assertThrows<StatusRuntimeException>{
            grpcClient.remove(request)
        }
        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Objeto não encontrado no banco de dados", status.description)
        }

    }


    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub{
            return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }

}