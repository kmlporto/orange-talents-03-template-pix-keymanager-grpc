package br.com.zup.edu.chave.listagem

import br.com.zup.edu.DetalheChaveResponse
import br.com.zup.edu.KeyManagerListagemServiceGrpc
import br.com.zup.edu.ListaChaveRequest
import br.com.zup.edu.chave.*
import br.com.zup.edu.util.toTimesTemp
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.*
import java.util.*

@MicronautTest(transactional = false)
internal class ListagemChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerListagemServiceGrpc.KeyManagerListagemServiceBlockingStub){

    private lateinit var CLIENT_ID: String
    private lateinit var CHAVE_ALEATORIA: ChavePix
    private lateinit var CHAVE_CELULAR: ChavePix
    private lateinit var CHAVE_CPF: ChavePix
    private lateinit var CHAVE_EMAIL: ChavePix

    @BeforeEach
    internal fun setUp() {
        CLIENT_ID = "2ac09233-21b2-4276-84fb-d83dbd9f8bab"
        CHAVE_ALEATORIA = repository.save(criaChave(CLIENT_ID, TipoChave.ALEATORIA, UUID.randomUUID().toString()))
        CHAVE_CELULAR = repository.save(criaChave(CLIENT_ID, TipoChave.CELULAR, "+5565984272588"))
        CHAVE_CPF = repository.save(criaChave(CLIENT_ID, TipoChave.CPF, "83082363083"))
        CHAVE_EMAIL = repository.save(criaChave(CLIENT_ID, TipoChave.EMAIL, "algumemail@zup.com.br"))

    }
    @AfterEach
    internal fun clean(){
        repository.deleteAll()
    }


    @Test
    fun `listagem para clientId que nao possui chaves`(){
        repository.deleteAll()

        val response = grpcClient.listagem(criaRequest(CLIENT_ID))
        assertEquals(0, response.chavesCount)
    }

    @Test
    fun `listagem para clientId que possui chaves`(){
        val response = grpcClient.listagem(criaRequest(CLIENT_ID))

        assertEquals(4, response.chavesCount)
        assertTrue(comparaResponse(response.chavesList[0], criaResponse(CHAVE_ALEATORIA)))
        assertTrue(comparaResponse(response.chavesList[1], criaResponse(CHAVE_CELULAR)))
        assertTrue(comparaResponse(response.chavesList[2], criaResponse(CHAVE_CPF)))
        assertTrue(comparaResponse(response.chavesList[3], criaResponse(CHAVE_EMAIL)))
    }

    private fun comparaResponse(expect: DetalheChaveResponse, atual: DetalheChaveResponse):Boolean{
        assertEquals(expect.clientId, atual.clientId)
        assertEquals(expect.pixId, atual.pixId)
        assertEquals(expect.valorChave, atual.valorChave)
        assertEquals(expect.criadoEm.seconds, atual.criadoEm.seconds)

        return true
    }

    @Test
    fun `listagem para clientId com request invalido`(){
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.listagem(criaRequest(""))
        }

        with(thrown){
            Assertions.assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            Assertions.assertEquals("ClientId não pode ser nulo ou vazio", status.description)
        }
    }

    private fun criaRequest(clientId: String): ListaChaveRequest{
        return ListaChaveRequest
            .newBuilder()
            .setClientId(clientId)
            .build()
    }

    private fun criaResponse(chavePix: ChavePix): DetalheChaveResponse{
        return DetalheChaveResponse.newBuilder()
            .setPixId(chavePix.id.toString())
            .setClientId(chavePix.clientId)
            .setTipoChave(br.com.zup.edu.TipoChave.valueOf(chavePix.tipoChave.name))
            .setValorChave(chavePix.chave)
            .setTipoConta(br.com.zup.edu.TipoConta.valueOf(chavePix.conta.tipoConta.name))
            .setCriadoEm(chavePix.criadoEm.toTimesTemp())
            .build()
    }

    private fun criaChave(clientId: String, tipoChave: TipoChave, valorChave: String): ChavePix {
        return ChavePix(
            clientId = clientId,
            tipoChave = tipoChave,
            chave = valorChave,
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

    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListagemServiceGrpc.KeyManagerListagemServiceBlockingStub{
            return KeyManagerListagemServiceGrpc.newBlockingStub(channel)
        }
    }

}