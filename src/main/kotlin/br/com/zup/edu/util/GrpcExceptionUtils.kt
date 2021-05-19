package br.com.zup.edu.util

import com.google.protobuf.Timestamp
import com.google.rpc.BadRequest
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import java.time.LocalDateTime
import java.time.ZoneOffset

import java.time.Instant

fun StatusRuntimeException.violations(): List<Pair<String, String>>{
    val details = StatusProto.fromThrowable(this)?.detailsList?.get(0)!!
        .unpack(BadRequest::class.java)

    return details.fieldViolationsList.map { it.field to it.description }
}

fun LocalDateTime.toTimesTemp(): Timestamp{
    val instant: Instant = this.toInstant(ZoneOffset.UTC)

    return Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
}