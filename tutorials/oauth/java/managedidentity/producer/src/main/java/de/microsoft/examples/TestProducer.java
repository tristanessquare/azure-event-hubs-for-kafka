//Copyright (c) Microsoft Corporation. All rights reserved.
//Licensed under the MIT License.
package de.microsoft.examples;

import java.io.IOException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TestProducer {
    //Change constant to send messages to the desired topic, for this example we use 'test'
    private final static String TOPIC = "test";

    private final static int NUM_THREADS = 1;


    public static void main(String... args) throws IOException {
        //Create Kafka Producer
        final Producer<Long, String> producer = createProducer();

        final ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        //Run NUM_THREADS TestDataReporters
        for (int i = 0; i < NUM_THREADS; i++)
            executorService.execute(new TestDataReporter(producer, TOPIC));
    }

    private static Producer<Long, String> createProducer() throws IOException {
        Properties properties = new Properties();
        properties.load(TestProducer.class.getResourceAsStream("/producer.config"));
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(properties);
    }
}


