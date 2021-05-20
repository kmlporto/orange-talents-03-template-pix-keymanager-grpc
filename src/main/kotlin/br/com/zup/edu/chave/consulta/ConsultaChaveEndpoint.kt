package br.com.zup.edu.chave.consulta

import br.com.zup.edu.ConsultaChaveRequest
import br.com.zup.edu.ConsultaChaveResponse
import br.com.zup.edu.KeyManagerConsultaServiceGrpc
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.chave.toModel
import br.com.zup.edu.externo.bcb.BCBClient
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
        val consultaDetail = filtro.filtra(repository, bcbClient)

        responseObserver.onNext(consultaDetail.convertToResponse())

        responseObserver.onCompleted()
    }

}