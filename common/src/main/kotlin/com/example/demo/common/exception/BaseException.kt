package com.example.demo.common.exception

import org.springframework.http.HttpStatus

open class BaseException(
    val messageStr: String,
    val status: HttpStatus
) : RuntimeException(messageStr)
