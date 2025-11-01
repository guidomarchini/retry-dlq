package com.sample.gmar.retrydlq.model

sealed class ProcessResult {
    data class Success(val retryCount: Int) : ProcessResult()
    data class SentToDLQ(val retryCount: Int) : ProcessResult()
    data class Failed(val retryCount: Int, val lastException: Exception) : ProcessResult()
}