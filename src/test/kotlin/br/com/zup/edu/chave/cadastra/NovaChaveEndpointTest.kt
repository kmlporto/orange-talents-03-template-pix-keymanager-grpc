package br.com.zup.edu.chave.cadastra

import br.com.zup.edu.KeyManagerServiceGrpc
import br.com.zup.edu.NovaChaveRequest
import br.com.zup.edu.chave.ChavePix
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.Conta
import br.com.zup.edu.chave.TipoChave
import br.com.zup.edu.chave.cadastra.NovaChaveEndpointTest.Requests.*
import br.com.zup.edu.util.violations
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
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class NovaChaveEndpointTest(val grpcClient: KeyManagerServiceGrpc.KeyManagerServiceBlockingStub){

    @Inject
    lateinit var repository: ChavePixRepository

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @Test
    fun `registra chave tipo cpf valida`() {
        val response = grpcClient.cadastra(CPF_VALIDO.get)

        assertNotNull(response)
        assertNotNull(response.pixId)
    }

    @Test
    fun `falha em registrar chave repetida`() {
        val chaveCPF = ChavePix(
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

        repository.save(chaveCPF)

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(CPF_VALIDO.get)
        }
        with(thrown){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave já cadastrada", status.description)
        }
    }

    @Test
    fun `falha em registrar chave tipo cpf invalida`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(CPF_INVALIDO.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().contains(Pair("chave", "chave Pix inválida (CPF)")))
        }
    }

    @Test
    fun `registra chave tipo aleatoria valida`() {
        val response = grpcClient.cadastra(ALEATORIA_VALIDA.get)

        assertNotNull(response)
        assertNotNull(response.pixId)
    }

    @Test
    fun `falha em registrar chave tipo aleatoria invalida`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(ALEATORIA_INVALIDA.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().contains(Pair("chave", "chave Pix inválida (ALEATORIA)")))
        }
    }

    @Test
    fun `registra chave tipo celular valido`() {
        val response = grpcClient.cadastra(CELULAR_VALIDO.get)

        assertNotNull(response)
        assertNotNull(response.pixId)
    }

    @Test
    fun `falha em registrar chave tipo celular invalido`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(CELULAR_INVALIDO.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().contains(Pair("chave", "chave Pix inválida (CELULAR)")))
        }
    }

    @Test
    fun `registra chave tipo email valido`() {
        val response = grpcClient.cadastra(EMAIL_VALIDO.get)

        assertNotNull(response)
        assertNotNull(response.pixId)
    }

    @Test
    fun `falha em registrar chave tipo email invalido`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(EMAIL_INVALIDO.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().contains(Pair("chave", "chave Pix inválida (EMAIL)")))
        }
    }

    @Test
    fun `falha em registrar chave com conta que nao existe`(){
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(COM_CONTA_INVALIDA.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Conta inválida", status.description)
        }
    }

    @Test
    fun `falha em registrar chave quando cliente itau retorna 500`(){
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(COM_CLIENTID_INEXISTENTE.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Argumentos inválidos", status.description)
        }
    }

    @Test
    fun `falha em registrar chave passando conta null`(){
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(COM_CONTA_NULL.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().contains(Pair("clientId", "não deve estar em branco")))
        }
    }


    @Test
    fun `falha em registrar chave com tipo chave null`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(COM_TIPO_CHAVE_NULL.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().contains(Pair("tipoChave", "não deve ser nulo")))
        }
    }

    @Test
    fun `falha em registrar chave com tipo conta null`() {
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.cadastra(COM_TIPO_CONTA_NULL.get)
        }
        with(thrown){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
            assertTrue(violations().contains(Pair("tipoConta", "não deve ser nulo")))
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
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerServiceGrpc.KeyManagerServiceBlockingStub{
            return KeyManagerServiceGrpc.newBlockingStub(channel)
        }
    }


    enum class Requests{
        CPF_VALIDO{
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
        CPF_INVALIDO{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.CPF)
                        .setChave("830823083")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        ALEATORIA_VALIDA{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.ALEATORIA)
                        .setChave("")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        ALEATORIA_INVALIDA{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.ALEATORIA)
                        .setChave("091294AK")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        CELULAR_VALIDO{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.CELULAR)
                        .setChave("+5585988714077")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        CELULAR_INVALIDO{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.CELULAR)
                        .setChave("5585988714077")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        EMAIL_VALIDO{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.EMAIL)
                        .setChave("alefh@zup.com.br")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        EMAIL_INVALIDO{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.EMAIL)
                        .setChave("")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        COM_TIPO_CHAVE_NULL{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.UNKNOWN_TIPO_CHAVE)
                        .setChave("+5585988714077")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_CORRENTE)
                        .build()
                }
        },
        COM_TIPO_CONTA_NULL{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("2ac09233-21b2-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.CELULAR)
                        .setChave("+5585988714077")
                        .setTipoConta(br.com.zup.edu.TipoConta.UNKNOWN_TIPO_CONTA)
                        .build()
                }
        },
        COM_CONTA_INVALIDA{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("de95a228-1f27-4ad2-907e-e5a2d816e9bc")
                        .setTipoChave(br.com.zup.edu.TipoChave.CELULAR)
                        .setChave("+5585988714077")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_POUPANCA)
                        .build()
                }
        },
        COM_CLIENTID_INEXISTENTE{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setClientId("21b2-2ac09233-4276-84fb-d83dbd9f8bab")
                        .setTipoChave(br.com.zup.edu.TipoChave.CELULAR)
                        .setChave("+5585988714077")
                        .setTipoConta(br.com.zup.edu.TipoConta.CONTA_POUPANCA)
                        .build()
                }
        },
        COM_CONTA_NULL{
            override val get: NovaChaveRequest
                get() {
                    return NovaChaveRequest.newBuilder()
                        .setTipoChave(br.com.zup.edu.TipoChave.CELULAR)
                        .setChave("+5585988714077")
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