package br.com.zup.edu.chave

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class TipoChaveTest{
    @Test
    fun `deve ser valido quando chave aleatoria for nula ou vazia`(){
        with(TipoChave.ALEATORIA){
            assertTrue(valida(null))
            assertTrue(valida(""))
        }
    }

    @Test
    fun `nao deve ser valido quando chave aleatoria preenchida`(){
        with(TipoChave.ALEATORIA){
            assertFalse(valida("qualquer valor"))
        }
    }

    @Test
    fun `deve ser valido quando chave celuar for valido`(){
        with(TipoChave.CELULAR){
            assertTrue(valida("+5527981392208"))
        }
    }

    @Test
    fun `nao deve ser valido quando chave celular invalido`(){
        with(TipoChave.CELULAR){
            assertFalse(valida("5527981392208"))
            assertFalse(valida(""))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve ser valido quando chave CPF for valido`(){
        with(TipoChave.CPF){
            assertTrue(valida("42219284093"))
        }
    }

    @Test
    fun `nao deve ser valido quando chave CPF invalido`(){
        with(TipoChave.CPF){
            assertFalse(valida("111111a1111"))
            assertFalse(valida(""))
            assertFalse(valida(null))
        }
    }

    @Test
    fun `deve ser valido quando chave email for valido`(){
        with(TipoChave.EMAIL){
            assertTrue(valida("algumemail@gmail.com"))
        }
    }

    @Test
    fun `nao deve ser valido quando chave email invalido`(){
        with(TipoChave.EMAIL){
            assertFalse(valida("algumemailgmail.com."))
            assertFalse(valida(""))
            assertFalse(valida(null))
        }
    }

}