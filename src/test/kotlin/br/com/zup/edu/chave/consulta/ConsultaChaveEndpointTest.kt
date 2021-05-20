package br.com.zup.edu.chave.consulta

import br.com.zup.edu.ConsultaChaveRequest
import br.com.zup.edu.KeyManagerConsultaServiceGrpc
import br.com.zup.edu.chave.*
import br.com.zup.edu.externo.bcb.*
import br.com.zup.edu.util.toTimesTemp
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
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class ConsultaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub
){
    @Inject
    lateinit var bcbClient: BCBClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `consulta chave byClient valida`(){
        val chavePixSalva = repository.save(chave())

        val chaveResponse = grpcClient.consulta(consultaByClientRequest(chavePixSalva.clientId, chavePixSalva.id))

        assertNotNull(chaveResponse)

        with(chaveResponse){
            assertEquals(chavePixSalva.clientId, clientId)
            assertEquals(chavePixSalva.id.toString(), pixId)
            assertEquals(chavePixSalva.tipoChave, TipoChave.valueOf(tipoChave.name))
            assertEquals(chavePixSalva.chave, valorChave)
        }
    }

    @Test
    fun `falha em consultar chave byClient nao cadastrada`(){
        val thrown = assertThrows<StatusRuntimeException>{
            grpcClient.consulta(consultaByClientRequest(chave().clientId, chave().id))
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não cadastrada no sistema", status.description)
        }
    }

    @Test
    fun `falha em consultar chave byClient que nao pertence ao clientId passado`(){
        val chavePixSalva = repository.save(chave())
        val clientIdInvalido = "3fa85f64-5717-4562-b3fc-2c963f66afa6"

        val thrown = assertThrows<StatusRuntimeException>{
            grpcClient.consulta(consultaByClientRequest(clientIdInvalido, chavePixSalva.id))
        }

        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave não pertence ao clientId passado", status.description)
        }
    }

    @Test
    fun `consulta chave ByChave valida salva no sistema`(){
        val chavePixSalva = repository.save(chave())

        val chaveResponse = grpcClient.consulta(consultaByChaveRequest())

        assertNotNull(chaveResponse)

        with(chaveResponse){
            assertEquals(chavePixSalva.clientId, clientId)
            assertEquals(chavePixSalva.id.toString(), pixId)
            assertEquals(chavePixSalva.tipoChave, TipoChave.valueOf(tipoChave.name))
            assertEquals(chavePixSalva.chave, valorChave)
            assertEquals(chavePixSalva.criadoEm.toTimesTemp().seconds, criadoEm.seconds)
        }
    }

    @Test
    fun `consulta chave ByChave valida nao salva no sistema`(){
        Mockito.`when`(bcbClient.consultaPix(chave().chave))
            .thenReturn(HttpResponse.ok(responseBCB()))

        val chaveResponse = grpcClient.consulta(consultaByChaveRequest())

        assertNotNull(chaveResponse)

        with(chaveResponse){
            assertEquals("", clientId)
            assertEquals("", pixId)
            assertEquals(chave().tipoChave, TipoChave.valueOf(tipoChave.name))
            assertEquals(chave().chave, valorChave)
        }
    }

    @Test
    fun `falha ao consultar chave ByChave nao salva no BCB`(){
        Mockito.`when`(bcbClient.consultaPix(chave().chave))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(consultaByChaveRequest())
        }

        with(thrown){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave não cadastrada no Banco Central", status.description)
        }
    }
    @Test
    fun `falha ao consultar chave com filtro invalido`(){
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.consulta(consultaFiltroInvalidoRequest())
        }

        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Filtro inválido", status.description)
        }
    }


    private fun responseBCB(): PixKeyDetailsResponse{
        return PixKeyDetailsResponse(
            keyType = PixKeyType.valueOf(chave().tipoChave.name),
            key = chave().chave,
            bankAccount = BankAccount(chave().conta),
            owner = Owner(chave().conta),
            createdAt = chave().criadoEm
        )
    }

    private fun consultaByClientRequest(clientId: String, pixId: Long?): ConsultaChaveRequest{
        return ConsultaChaveRequest.newBuilder()
            .setByClient(ConsultaChaveRequest.ByClient.newBuilder()
                                                        .setClientId(clientId)
                                                        .setPixId(pixId?.toString() ?: "1")
                                                        .build())
            .build()

    }

    private fun consultaByChaveRequest(): ConsultaChaveRequest{
        return ConsultaChaveRequest.newBuilder()
            .setByChave(chave().chave)
            .build()
    }

    private fun consultaFiltroInvalidoRequest(): ConsultaChaveRequest{
        return ConsultaChaveRequest.newBuilder()
            .build()
    }

    private fun chave(): ChavePix {
        return ChavePix(
            clientId = "2ac09233-21b2-4276-84fb-d83dbd9f8bab",
            tipoChave = TipoChave.CPF,
            chave = "83082363083",
            conta = conta()
        )
    }

    private fun conta(): Conta {
        return Conta(
            agencia = "0001",
            numero = "483201",
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeTitular = "Alefh Silva",
            cpfTitular = "83082363083",
            tipoConta = TipoConta.CONTA_CORRENTE
        )
    }


    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub{
            return KeyManagerConsultaServiceGrpc.newBlockingStub(channel)
        }
    }


    @MockBean(BCBClient::class)
    fun bcbClient(): BCBClient?{
        return Mockito.mock(BCBClient::class.java)
    }

}