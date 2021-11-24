package com.example.cloud.stream.documentstore

import ch.qos.logback.classic.Logger
import com.example.cloud.stream.data.DocumentStoredAvro
import org.apache.commons.lang3.RandomStringUtils
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.RecordMetadata
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicLong

@EnableScheduling
@SpringBootApplication
class DocumentStoreApplication {
    @Bean
    fun documentStoredTopic(): NewTopic {
        return TopicBuilder
            .name("document.stored")
            .partitions(5)
            .replicas(1)
            .build()
    }
}

@Component
class Producer(private val kafkaTemplate: KafkaTemplate<String, DocumentStoredAvro>, private val topic: NewTopic) {
    private val recordNumber = AtomicLong(0)

    @Scheduled(fixedRate = 1, timeUnit = SECONDS)
    fun run() {
        val docName = RandomStringUtils.randomAlphabetic(12)
        val record = DocumentStoredAvro(recordNumber.getAndIncrement(), docName, Math.random().toLong())

        log("Producing record: ${docName}\t${record}")
        kafkaTemplate.send(topic.name(), docName, record).addCallback(
            { result: SendResult<String, DocumentStoredAvro>? ->
                val m: RecordMetadata
                if (result != null) {
                    m = result.recordMetadata
                    log("Produced record to topic ${m.topic()} partition ${m.partition()} @ offset ${m.offset()}")
                }
            }
        ) { exception: Throwable? -> log("Failed to produce to kafka", exception) }
    }
}

fun Any.log(message: String, exception: Throwable? = null) =
    LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).info(message, exception)

fun main(args: Array<String>) {
    runApplication<DocumentStoreApplication>(*args)
}
