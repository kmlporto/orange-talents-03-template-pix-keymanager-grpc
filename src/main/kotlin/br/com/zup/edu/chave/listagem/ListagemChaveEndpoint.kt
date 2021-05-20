package br.com.zup.edu.chave.listagem

import br.com.zup.edu.*
import br.com.zup.edu.chave.ChavePixRepository
import br.com.zup.edu.util.toTimesTemp
import br.com.zup.edu.validations.annotation.ErrorHandler
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@ErrorHandler
class ListagemChaveEndpoint(@Inject val repository: ChavePixRepository): KeyManagerListagemServiceGrpc.KeyManagerListagemServiceImplBase() {

    override fun listagem(request: ListaChaveRequest, responseObserver: StreamObserver<ListaChaveResponse>) {
        val chaves = repository.findAllByClientId(request.clientId)

        val listaChaveResponse: List<DetalheChaveResponse> = chaves.map {
            chavePix -> DetalheChaveResponse.newBuilder()
                                            .setPixId(chavePix.id.toString())
                                            .setClientId(chavePix.clientId)
                                            .setTipoChave(TipoChave.valueOf(chavePix.tipoChave.name))
                                            .setValorChave(chavePix.chave)
                                            .setTipoConta(TipoConta.valueOf(chavePix.conta.tipoConta.name))
                                            .setCriadoEm(chavePix.criadoEm.toTimesTemp())
                                            .build()
        }

        responseObserver.onNext(ListaChaveResponse
            .newBuilder()
            .addAllChaves(listaChaveResponse)
            .build())
        responseObserver.onCompleted()
    }
}