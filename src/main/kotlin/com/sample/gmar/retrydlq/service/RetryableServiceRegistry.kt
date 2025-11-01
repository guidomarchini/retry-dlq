package com.sample.gmar.retrydlq.service

class RetryableServiceRegistry(services: List<RetryableService<*>>) {
    private val serviceMap: Map<String, RetryableService<*>> = services.associateBy { it.getServiceName() }

    fun getService(serviceName: String): RetryableService<*>? = serviceMap[serviceName]
    fun getAllServiceNames(): Set<String> = serviceMap.keys
}
