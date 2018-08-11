package com.example;

import com.google.common.io.Resources;
import com.gurps.avro.Person;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.Collections.singletonMap;

public class Consumer {
    final static String TOPIC = "people";
    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();

        try (InputStream props = Resources.getResource("consumer.properties").openStream()) {
            properties.load(props);
        }

        // Define the processing topology
        StreamsBuilder builder = new StreamsBuilder();
        final SpecificAvroSerde<Person> specificAvroSerde = new SpecificAvroSerde<>();
        specificAvroSerde.configure(
            singletonMap(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081"),
            false);

        final KStream<String, Person> personStream = builder.stream(TOPIC, Consumed.with(Serdes.serdeFrom(String.class), specificAvroSerde));

        personStream.foreach((key, value) -> System.out.println(key + ": " + value));

        //COULD NOT GET Printed.toSysOut() TO WORK!
//        stream
//            .print(Printed.toSysOut());

        KafkaStreams streams = new KafkaStreams(builder.build(), properties);

        streams.cleanUp();
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
