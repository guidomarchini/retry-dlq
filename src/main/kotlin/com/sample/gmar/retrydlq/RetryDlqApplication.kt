package com.sample.gmar.retrydlq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RetryDlqApplication

fun main(args: Array<String>) {
	runApplication<RetryDlqApplication>(*args)
}
