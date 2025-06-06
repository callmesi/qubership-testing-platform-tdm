/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.tdm.service.notification.environments;

import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.qubership.atp.tdm.AbstractTestDataTest;
import org.qubership.atp.tdm.model.TestDataTableCatalog;
import org.apache.kafka.clients.producer.ProducerConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Disabled // only with local kafka
public class EnvironmentEventKafkaListenerTest extends AbstractTestDataTest {

    private final CountDownLatch latch = new CountDownLatch(1);
    private final int delayForReceiveNotification = 5;

    @SpyBean
    EnvironmentsEventKafkaListener kafkaListener;

    @Value("${kafka.environments.topic}")
    private String topic;

    @Autowired
    public KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void envKafkaListener_sendSomeMessage_catchSentMessage() {
        kafkaTemplate.send(topic,createMessageToDeleteTable(
                EnvironmentEventType.DELETE
        ));
        Mockito.verify(kafkaListener, Mockito.timeout(5000)).listen(createMessageToDeleteTable(
                EnvironmentEventType.DELETE
        ));
    }

    @Test
    public void envKafkaListener_deleteTablesByEnvironment_catalogRepoHasNoDeletedTable() throws InterruptedException {
        createTestDataTableCatalog(UUID.randomUUID(), systemId, environmentId,
                "Test Table", "table_name_project");

        long elementsBeforeDeleting = catalogRepository.findByEnvironmentId(environmentId).size();

        kafkaTemplate.send(topic,createMessageToDeleteTable(
                EnvironmentEventType.DELETE));

        latch.await(delayForReceiveNotification, TimeUnit.SECONDS);
        List<TestDataTableCatalog> catalogList = catalogRepository.findByEnvironmentId(environmentId);
        long elementsAfterDeleting = catalogList.size();

        Assertions.assertEquals(1,elementsBeforeDeleting-elementsAfterDeleting);
    }

    @Test
    public void envKafkaListener_deleteTablesBySystem_catalogRepoHasNoDeletedTable() throws InterruptedException {
        createTestDataTableCatalog(UUID.randomUUID(), systemId, environmentId,
                "Test Table1", "table_name_project1");
        createTestDataTableCatalog(UUID.randomUUID(), systemId, environmentId,
                "Test Table2", "table_name_project2");

        long elementsBeforeDeleting = catalogRepository.findBySystemId(systemId).size();

        kafkaTemplate.send(topic,createMessageToDeleteTable(
                EnvironmentEventType.DELETE));

        latch.await(delayForReceiveNotification, TimeUnit.SECONDS);
        List<TestDataTableCatalog> catalogList = catalogRepository.findBySystemId(systemId);
        long elementsAfterDeleting = catalogList.size();

        Assertions.assertEquals(2,elementsBeforeDeleting-elementsAfterDeleting);
    }

    @Test
    public void envKafkaListener_deleteTableBySystem_testDataTableRepoHasNoDeletedTable() {
        Assertions.assertThrows(BadSqlGrammarException.class, () -> {
            TestDataTableCatalog catalog1 = createTestDataTableCatalog(UUID.randomUUID(), systemId, environmentId,
                    "Test Table1", "table_name_project1");
            createTestDataTable(catalog1.getTableName());

            Assertions.assertNotNull(testDataService.getTestData(catalog1.getTableName()));

            kafkaTemplate.send(topic,createMessageToDeleteTable(
                    EnvironmentEventType.DELETE));

            latch.await(delayForReceiveNotification, TimeUnit.SECONDS);
            testDataService.getTestData(catalog1.getTableName());
        });
    }

    private String createMessageToDeleteTable( EnvironmentEventType eventType) {
        UUID entityId;
        entityId = environmentId;

        String data = "{\"id\": \"" + entityId + "\",\"eventType\": \"" + eventType + "\"}";
        return data;
    }

    @TestConfiguration
    static class KafkaTestContainersConfiguration {

        @Value("${spring.kafka.consumer.bootstrap-servers}")
        String bootstrapServer;

        @Bean
        public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        public ProducerFactory<String, String> producerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }
    }
}
