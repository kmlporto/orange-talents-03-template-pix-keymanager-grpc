package br.com.zup.edu.chave.consulta

import br.com.zup.edu.ConsultaChaveRequest
import br.com.zup.edu.ConsultaChaveResponse
import br.com.zup.edu.KeyManagerConsultaServiceGrpc
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.toModel
import br.com.zup.edu.externo.bcb.BCBClient
import br.com.zup.edu.util.toTimesTemp
import br.com.zup.edu.validations.annotation.ErrorHandler
import io.grpc.stub.StreamObserver
import io.micronaut.validation.validator.Validator
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@ErrorHandler
class ConsultaChaveEndpoint(@Inject val repository: ChavePixRepository,
                            @Inject val bcbClient: BCBClient,
                            @Inject val validator: Validator
) : KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceImplBase(){

    override fun consulta(request: ConsultaChaveRequest, responseObserver: StreamObserver<ConsultaChaveResponse>) {

        val filtro : Filtro = request.toModel(validator)
        val detailsResponse = filtro.filtra(repository, bcbClient)

        responseObserver.onNext(ConsultaChaveResponse
            .newBuilder()
            .setClientId(request.byClient.clientId)
            .setPixId(request.byClient.pixId)
            .setTipoChave(detailsResponse.keyType.toTipoChave())
            .setValorChave(detailsResponse.key)
            .setTitular(detailsResponse.owner.toTitularResponse())
            .setConta(detailsResponse.bankAccount.toContaResponse())
            .setCriadoEm(detailsResponse.createdAt.toTimesTemp())
            .build())

        responseObserver.onCompleted()
    }

}