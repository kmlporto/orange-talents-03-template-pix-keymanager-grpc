package br.com.zup.edu.chave

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {
    fun existsByChave(chave: String): Boolean
    fun findByIdAndClientId(id: UUID, clientId:String): ChavePix
    fun findByChave(chave:String): Optional<ChavePix>
    fun findAllByClientId(clientId: String): List<ChavePix>
}