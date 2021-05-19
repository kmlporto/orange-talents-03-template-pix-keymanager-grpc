package br.com.zup.edu.exceptions

import java.lang.RuntimeException

class NotFoundException (message: String? = "Objeto não encontrado") : RuntimeException(message)