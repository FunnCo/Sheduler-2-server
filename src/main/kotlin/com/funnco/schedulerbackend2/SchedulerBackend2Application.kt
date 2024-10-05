package com.funnco.schedulerbackend2

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SchedulerBackend2Application

fun main(args: Array<String>) {
	runApplication<SchedulerBackend2Application>(*args)
}
