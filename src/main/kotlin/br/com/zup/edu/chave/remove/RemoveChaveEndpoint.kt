package br.com.zup.edu.chave.remove

import br.com.zup.edu.KeyManagerRemoveServiceGrpc
import br.com.zup.edu.RemoveChaveRequest
import br.com.zup.edu.RemoveChaveResponse
import br.com.zup.edu.chave.toRemove
import br.com.zup.edu.validations.annotation.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChaveEndpoint(@Inject private val service: RemoveChaveService) : KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceImplBase(){

    override fun remove(request: RemoveChaveRequest, responseObserver: StreamObserver<RemoveChaveResponse>) {
        val removeChavePix: RemoveChavePix = request.toRemove()

        service.remove(removeChavePix)

        responseObserver.onNext(RemoveChaveResponse
                                    .newBuilder()
                                    .setClientId(request.clientId)
                                    .setPixId(request.pixId)
                                    .build())
        responseObserver.onCompleted()
    }

}