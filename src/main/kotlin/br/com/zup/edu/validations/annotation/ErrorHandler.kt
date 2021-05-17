package br.com.zup.edu.validations.annotation

import br.com.zup.edu.validations.handler.config.ExceptionHandlerGrpcServerInterceptor
import io.micronaut.aop.Around
import io.micronaut.context.annotation.Type
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.TYPE

@MustBeDocumented
@Target(CLASS, TYPE)
@Retention(RUNTIME)
@Type(ExceptionHandlerGrpcServerInterceptor::class)
@Around
annotation class ErrorHandler()
