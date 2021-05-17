package br.com.zup.edu.chave.cadastra

import br.com.zup.edu.KeyManagerCadastraServiceGrpc
import br.com.zup.edu.NovaChaveRequest
import br.com.zup.edu.NovaChaveResponse
import br.com.zup.edu.chave.toNovaChave
import br.com.zup.edu.validations.annotation.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorHandler
class NovaChaveEndpoint(@Inject private val service: NovaChaveService) : KeyManagerCadastraServiceGrpc.KeyManagerCadastraServiceImplBase(){

    override fun cadastra(request: NovaChaveRequest, responseObserver: StreamObserver<NovaChaveResponse>) {
        val novaChavePix = request.toNovaChave()

        val chavePix = service.registra(novaChavePix)

        responseObserver.onNext(NovaChaveResponse.newBuilder().setPixId(chavePix.id!!).build())
        responseObserver.onCompleted()
    }

}