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

package org.qubership.atp.tdm.service.notification.projects;

import static org.qubership.atp.tdm.env.configurator.utils.CacheNames.AUTH_PROJECT_CACHE;
import static java.lang.String.format;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.qubership.atp.tdm.exceptions.kafka.TdmKafkaListenerReadEventException;
import org.qubership.atp.tdm.exceptions.kafka.TdmKafkaListenerTypeEventException;
import org.qubership.atp.tdm.mdc.MdcField;
import org.qubership.atp.tdm.model.ProjectInformation;
import org.qubership.atp.tdm.service.ProjectInformationService;
import org.qubership.atp.tdm.service.TestDataService;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.qubership.atp.integration.configuration.mdc.MdcUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectEventKafkaListener implements ProjectEventListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final TestDataService testDataService;
    private final ProjectInformationService projectInformationService;

    public ProjectEventKafkaListener(@Nonnull TestDataService testDataService,
                                     @Nonnull ProjectInformationService projectInformationService) {
        this.testDataService = testDataService;
        this.projectInformationService = projectInformationService;
    }

    @Override
    @KafkaListener(topics = "${kafka.project.topic:catalog_notification_topic}")
    @CacheEvict(value = AUTH_PROJECT_CACHE, allEntries = true)
    public void listen(String event) {
        MDC.clear();
        ProjectEvent projectEvent;
        try {
            projectEvent = objectMapper.readValue(event, ProjectEvent.class);
        } catch (IOException e) {
            log.error(String.format(TdmKafkaListenerReadEventException.DEFAULT_MESSAGE, event), e);
            throw new TdmKafkaListenerReadEventException(event);
        }
        MdcUtils.put(MdcField.PROJECT_ID.toString(), projectEvent.getProjectId());
        switch (projectEvent.getType()) {
            case CREATE: {
                log.info("Project '{}' was created", projectEvent.getProjectName());
                projectInformationService.saveProjectInformation(new ProjectInformation(
                        projectEvent.getProjectId(), projectEvent.getTimeZone(), projectEvent.getDateFormat(),
                        projectEvent.getTimeFormat(),  projectEvent.getTdmTableExpirationTime())
                );
                break;
            }
            case UPDATE: {
                log.info("Project '{}' was updated", projectEvent.getProjectName());
                projectInformationService.saveProjectInformation(new ProjectInformation(projectEvent.getProjectId(),
                        projectEvent.getTimeZone(), projectEvent.getDateFormat(), projectEvent.getTimeFormat(),
                        projectEvent.getTdmTableExpirationTime()
                ));
                break;
            }
            case DELETE: {
                log.info("Project '{}' was deleted from projects catalogue", projectEvent.getProjectName());
                testDataService.deleteProjectFromCatalogue(projectEvent.getProjectId());
                break;
            }
            default: {
                log.error(format(TdmKafkaListenerTypeEventException.DEFAULT_MESSAGE, projectEvent.getType().name()));
                throw new TdmKafkaListenerTypeEventException(projectEvent.getType().name());
            }
        }
    }
}
