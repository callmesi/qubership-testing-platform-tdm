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

import static org.qubership.atp.tdm.service.notification.environments.EnvironmentEventType.DELETE;
import static java.lang.String.format;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.qubership.atp.tdm.exceptions.kafka.TdmKafkaListenerReadEventException;
import org.qubership.atp.tdm.mdc.TdmMdcHelper;
import org.qubership.atp.tdm.repo.CatalogRepository;
import org.qubership.atp.tdm.service.TestDataService;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvironmentsEventKafkaListener implements EnvironmentEventListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestDataService testDataService;
    private final CatalogRepository catalogRepository;
    private final TdmMdcHelper tdmMdcHelper;

    /**
     * Environments Event Kafka Listener.
     */
    public EnvironmentsEventKafkaListener(@Nonnull TestDataService testDataService,
                                          @Nonnull CatalogRepository catalogRepository, TdmMdcHelper tdmMdcHelper) {
        this.testDataService = testDataService;
        this.catalogRepository = catalogRepository;
        this.tdmMdcHelper = tdmMdcHelper;
    }

    @Override
    @KafkaListener(topics = "${kafka.environments.topic}")
    public void listen(String event) {
        MDC.clear();
        EnvironmentEvent environmentEvent;
        log.info("Kafka environment event: {}", event);
        try {
            environmentEvent = objectMapper.readValue(event, EnvironmentEvent.class);
        } catch (IOException e) {
            log.error(String.format(TdmKafkaListenerReadEventException.DEFAULT_MESSAGE, event), e);
            throw new TdmKafkaListenerReadEventException(event);
        }
        tdmMdcHelper.putEnvironmentEventFields(environmentEvent);
        if (DELETE.equals(environmentEvent.getEventType())) {
            log.info("Environment '{}' was deleted", environmentEvent.getId());
            catalogRepository.findByEnvironmentId(environmentEvent.getId()).forEach(
                    x -> testDataService.deleteTestData(x.getTableName())
            );
        }
    }
}
