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

package org.qubership.atp.tdm.configuration;

import javax.annotation.Nonnull;

import org.qubership.atp.tdm.mdc.TdmMdcHelper;
import org.qubership.atp.tdm.repo.CatalogRepository;
import org.qubership.atp.tdm.service.ProjectInformationService;
import org.qubership.atp.tdm.service.TestDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

import org.qubership.atp.tdm.service.notification.environments.EnvironmentEventListener;
import org.qubership.atp.tdm.service.notification.environments.EnvironmentsEventKafkaListener;
import org.qubership.atp.tdm.service.notification.projects.ProjectEventKafkaListener;
import org.qubership.atp.tdm.service.notification.projects.ProjectEventListener;
import org.qubership.atp.tdm.service.notification.systems.SystemEventListener;
import org.qubership.atp.tdm.service.notification.systems.SystemsEventKafkaListener;

@EnableKafka
@Configuration
public class KafkaEventListenerConfig {
    @Value("${kafka.enable:false}")
    private boolean kafkaEnable;
    private final TestDataService testDataService;
    private final ProjectInformationService projectInformationService;
    private final CatalogRepository catalogRepository;
    private final TdmMdcHelper tdmMdcHelper;

    /**
     * Configure listener config.
     * @param testDataService TDM tables service
     * @param projectInformationService Info about projects (timestamp..)
     * @param catalogRepository General info about tables
     * @param helper helper
     */
    public KafkaEventListenerConfig(@Nonnull TestDataService testDataService,
                                    @Nonnull ProjectInformationService projectInformationService,
                                    @Nonnull CatalogRepository catalogRepository,
                                    TdmMdcHelper helper) {
        this.testDataService = testDataService;
        this.projectInformationService = projectInformationService;
        this.catalogRepository = catalogRepository;
        tdmMdcHelper = helper;
    }

    /**
     * If Kafka enabled then listen.
     *
     * @return event
     */
    @Bean
    public ProjectEventListener projectEventNotificationService() {
        if (kafkaEnable) {
            return new ProjectEventKafkaListener(testDataService, projectInformationService);
        } else {
            return event -> {
            };
        }
    }

    /**
     * If Kafka enabled then listen.
     *
     * @return event
     */
    @Bean
    public EnvironmentEventListener environmentEventNotificationService() {
        if (kafkaEnable) {
            return new EnvironmentsEventKafkaListener(testDataService, catalogRepository, tdmMdcHelper);
        } else {
            return event -> {
            };
        }
    }

    /**
     * If Kafka enabled then listen.
     *
     * @return event
     */
    @Bean
    public SystemEventListener systemEventNotificationService() {
        if (kafkaEnable) {
            return new SystemsEventKafkaListener(testDataService, catalogRepository, tdmMdcHelper);
        } else {
            return event -> {
            };
        }
    }
}
