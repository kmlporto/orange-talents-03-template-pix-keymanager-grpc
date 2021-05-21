package br.com.zup.edu.chave.remove

import br.com.zup.edu.KeyManagerRemoveServiceGrpc
import br.com.zup.edu.RemoveChaveRequest
import br.com.zup.edu.chave.*
import br.com.zup.edu.externo.bcb.BCBClient
import br.com.zup.edu.externo.bcb.DeletePixKeyRequest
import br.com.zup.edu.externo.bcb.DeletePixKeyResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub){

    @Inject
    lateinit var bcbClient: BCBClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }


    @Test
    fun `exclui chave existente`() {
        val chaveCadastrada = repository.save(chave())

        val request = RemoveChaveRequest
            .newBuilder()
            .setClientId(chaveCadastrada.clientId)
            .setPixId(chaveCadastrada.id.toString())
            .build()

        Mockito.`when`(bcbClient.removePix(chave().chave, requestBCB()))
            .thenReturn(HttpResponse.ok(responseBCB()))

        val response = grpcClient.remove(request)

        assertNotNull(response)
        assertEquals(request.pixId, response.pixId)
        assertEquals(request.clientId, response.clientId)
    }

    @Test
    fun `falha em excluir chave inexistente`() {
        Mockito.`when`(bcbClient.removePix(requestBCB().key, requestBCB()))
            .thenReturn(HttpResponse.ok(responseBCB()))

        val thrown = assertThrows<StatusRuntimeException>{
            grpcClient.remove(request())
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Objeto não encontrado no banco de dados", status.description)
        }

    }

    @Test
    fun `falha em excluir chave inexistente no BCB`() {
        Mockito.`when`(bcbClient.removePix(requestBCB().key, requestBCB()))
            .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException>{
            grpcClient.remove(request())
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

    @MockBean(BCBClient::class)
    fun bcbClient(): BCBClient?{
        return Mockito.mock(BCBClient::class.java)
    }

    private fun chave(): ChavePix{
        return ChavePix(
            clientId = "2ac09233-21b2-4276-84fb-d83dbd9f8bab",
            tipoChave = TipoChave.CPF,
            chave = "83082363083",
            conta = Conta(
                agencia = "0001",
                numero = "483201",
                instituicao = "ITAÚ UNIBANCO S.A.",
                nomeTitular = "Alefh Silva",
                cpfTitular = "83082363083",
                tipoConta = TipoConta.CONTA_CORRENTE
            )
        )
    }

    private fun request() : RemoveChaveRequest{
        return RemoveChaveRequest
            .newBuilder()
            .setClientId(chave().clientId)
            .setPixId(UUID.randomUUID().toString())
            .build()
    }

    private fun requestBCB(): DeletePixKeyRequest{
        return DeletePixKeyRequest(chave().chave, Conta.ITAU_ISPB)
    }

    private fun responseBCB(): DeletePixKeyResponse{
        return DeletePixKeyResponse(chave().chave, Conta.ITAU_ISPB, LocalDateTime.now())
    }
}