package br.com.zup.edu.exceptions

import java.lang.RuntimeException

class ChaveExistenteException(message: String? = "Chave já cadastrada") : RuntimeException(message)