package br.com.zup.edu.chave

import javax.persistence.*

@Embeddable
class Conta (
    @Column(nullable = false)
    val agencia: String,
    @Column(nullable = false)
    val numero: String,
    @Column(nullable = false)
    val instituicao: String,
    @Column(nullable = false)
    val nomeTitular: String,
    @Column(nullable = false)
    val cpfTitular: String
    )