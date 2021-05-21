package br.com.zup.edu.chave.cadastra

import br.com.zup.edu.KeyManagerCadastraServiceGrpc
import br.com.zup.edu.NovaChaveRequest
import br.com.zup.edu.chave.*
import br.com.zup.edu.chave.cadastra.NovaChaveEndpointTest.Requests.*
import br.com.zup.edu.externo.bcb.*
import br.com.zup.edu.externo.itau.ContaResponse
import br.com.zup.edu.externo.itau.InstituicaoResponse
import br.com.zup.edu.externo.itau.ItauClient
import br.com.zup.edu.externo.itau.TitularResponse
import br.com.zup.edu.util.violations
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
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class NovaChaveEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceBlockingStub){

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BCBClient

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `registra chave tipo cpf valida`() {
        Mockito.`when`(itauClient.consulta(chave().clientId, conta().tipoConta.name))
            .thenReturn(HttpResponse.ok(itauResponse()))

        Mockito.`when`(bcbClient.cadastraPix(CreatePixKeyRequest(chave())))
            .thenReturn(HttpResponse.created(bcbResponse()))

        val response = grpcClient.cadastra(CHAVE_VALIDA.get)

        assertNotNull(response)
        assertNotNull(response.pixId)
    }

    @Test
    fun `falha em registrar chave repetida`() {
        Mockito.`when`(itauClient.consulta(chave().clientId, conta().tipoConta.name))
            .thenReturn(HttpResponse.ok(itauResponse()))

        Mockito.`when`(bcbClient.cadastraPix(CreatePixKeyRequest(chave())))
            .thenReturn(HttpResponse.created(bcbResponse()))

        repository.save(chave())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(CHAVE_VALIDA.get)
        }

        with(thrown){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave já cadastrada", status.description)
        }
    }

    @Test
    fun `falha em registrar chave com conta que nao existe`(){
        Mockito.`when`(itauClient.consulta(chave().clientId, conta().tipoConta.name))
            .thenReturn(HttpResponse.notFound())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(CHAVE_VALIDA.get)
        }
        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Conta inválida", status.description)
        }
    }

    @Test
    fun `falha em registrar chave quando cliente itau retorna 500`(){
        Mockito.`when`(itauClient.consulta(chave().clientId, conta().tipoConta.name))
            .thenReturn(HttpResponse.ok(itauResponse()))

        Mockito.`when`(bcbClient.cadastraPix(CreatePixKeyRequest(chave())))
            .thenReturn(HttpResponse.badRequest())

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(CHAVE_VALIDA.get)
        }
        with(thrown){
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao cadastrar pix no Banco Central", status.description)
        }
    }

    @Test
    fun `falha em registrar chave com dados null`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(COM_DADOS_NULOS.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().containsAll(
                listOf(
                    Pair("clientId", "não deve estar em branco"),
                    Pair("tipoConta", "não deve ser nulo"),
                    Pair("tipoChave", "não deve ser nulo"),
                    Pair("chave", "chave Pix inválida ()")
                )
            ))
        }
    }

    @Factory
    class Clients{
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceBlockingStub{
            return KeyManagerCadastraServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient?{
        return Mockito.mock(ItauClient::class.java)
    }

    @MockBean(BCBClient::class)
    fun bcbClient(): BCBClient?{
        return Mockito.mock(BCBClient::class.java)
    }

    private fun conta(): Conta{
        return Conta(
            agencia = "0001",
            numero = "483201",
            instituicao = "ITAÚ UNIBANCO S.A.",
            nomeTitular = "Alefh Silva",
            cpfTitular = "83082363083",
            tipoConta = TipoConta.CONTA_CORRENTE
        )
    }

    private fun chave(): ChavePix{
        return ChavePix(
            clientId = "2ac09233-21b2-4276-84fb-d83dbd9f8bab",
            tipoChave = TipoChave.CPF,
            chave = "83082363083",
            conta = conta()
        )
    }

    private fun itauResponse(): ContaResponse {
        return ContaResponse(
            tipo = TipoConta.CONTA_CORRENTE,
            instituicao = InstituicaoResponse( nome = "Itau", ispb = Conta.ITAU_ISPB),
            agencia = conta().agencia,
            numero = conta().numero,
            titular = TitularResponse(nome = conta().nomeTitular, id = chave().clientId, cpf = conta().cpfTitular)
        )
    }

    private fun bcbResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = PixKeyType.CPF,
            key = "83082363083",
            bankAccount = BankAccount(conta()),
            owner = owner(),
            createdAt = LocalDateTime.now()
        )
    }

    private fun owner(): Owner{
        return Owner(Owner.OwnerType.NATURAL_PERSON, conta().nomeTitular, conta().cpfTitular)
    }

    enum class Requests{
        CHAVE_VALIDA{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.CPF)
                        .setChave("83082363083")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        COM_DADOS_NULOS{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setTipoChave(br.com.zup.edu.TipoChave.UNKNOWN_TIPO_CHAVE)
                        .setTipoConta(br.com.zup.edu.TipoConta.UNKNOWN_TIPO_CONTA)
                        .build()
                }
        };

        abstract val get: NovaChaveRequest
    }
}