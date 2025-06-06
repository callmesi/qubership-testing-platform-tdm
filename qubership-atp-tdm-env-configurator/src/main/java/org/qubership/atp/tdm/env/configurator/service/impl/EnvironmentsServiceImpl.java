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

package org.qubership.atp.tdm.env.configurator.service.impl;

import static java.lang.String.format;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.qubership.atp.tdm.env.configurator.api.dto.environments.SystemNameViewDto;

import org.qubership.atp.tdm.env.configurator.model.Connection;
import org.qubership.atp.tdm.env.configurator.model.LazyEnvironment;
import org.qubership.atp.tdm.env.configurator.model.LazyProject;
import org.qubership.atp.tdm.env.configurator.model.LazySystem;
import org.qubership.atp.tdm.env.configurator.model.Project;
import org.qubership.atp.tdm.env.configurator.model.System;
import org.qubership.atp.tdm.env.configurator.service.DtoConvertService;
import org.qubership.atp.tdm.env.configurator.service.EnvironmentsService;
import org.qubership.atp.tdm.env.configurator.utils.CacheNames;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.qubership.atp.auth.springbootstarter.exceptions.AtpException;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.BaseSearchRequestDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.EnvironmentDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.EnvironmentFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.ConnectionFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.EnvironmentNameViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.EnvironmentResDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.ProjectFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.ProjectNameViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.SystemEnvironmentsViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertFullProjectByIdException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertFullSystemByNameException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertFullSystemBySysIdException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazyEnvironmentByEnvIdtException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazyEnvironmentByNameException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazyEnvironmentsException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazyProjectByIdException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazyProjectByNameException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazyProjectsException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazySystemBySysIdException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazySystemsByEnvIdByNameException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazySystemsByEnvIdException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvConvertLazySystemsByProjectIdException;
import org.qubership.atp.tdm.env.configurator.exceptions.internal.TdmEnvResetCachesException;
import org.qubership.atp.tdm.env.configurator.service.client.EnvironmentFeignClient;
import org.qubership.atp.tdm.env.configurator.service.client.ProjectEnvironmentFeignClient;
import org.qubership.atp.tdm.env.configurator.service.client.SystemEnvironmentFeignClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvironmentsServiceImpl implements EnvironmentsService {

    private final EnvironmentFeignClient environmentFeignClient;
    private final ProjectEnvironmentFeignClient projectEnvFeignClient;
    private final SystemEnvironmentFeignClient systemEnvFeignClient;
    private final DtoConvertService dtoConvertService;
    private final CacheManager cacheManager;

    /**
     * Project:
     * Get full project by ID.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_FULL_PROJECT_CACHE)
    public Project getFullProject(@Nonnull UUID projectId) {
        log.info("Loading project by id: [{}]", projectId);
        Project project;
        ResponseEntity<ProjectFullVer1ViewDto> fullProjectRes =
                projectEnvFeignClient.getProject(projectId, true);
        try {
            project = dtoConvertService.convert(fullProjectRes.getBody(), Project.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertFullProjectByIdException.DEFAULT_MESSAGE, projectId), e);
            throw new TdmEnvConvertFullProjectByIdException(projectId.toString());
        }
        log.info("Project successfully loaded.");
        return project;
    }

    /**
     * Get lazy project by ID.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_PROJECT_CACHE)
    public LazyProject getLazyProjectById(@Nonnull UUID projectId) {
        log.info("Loading lazy project by Id.");
        LazyProject lazyProjects;
        ResponseEntity<ProjectFullVer1ViewDto> projectsResponse =
                projectEnvFeignClient.getShortProject(projectId, false);
        try {
            lazyProjects = dtoConvertService.convert(projectsResponse.getBody(), LazyProject.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazyProjectByIdException.DEFAULT_MESSAGE, projectId), e);
            throw new TdmEnvConvertLazyProjectByIdException(projectId.toString());
        }
        log.info("Lazy project by Id successfully loaded.");
        return lazyProjects;
    }

    /**
     * Get lazy project by name.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_PROJECT_BY_NAME_CACHE)
    public LazyProject getLazyProjectByName(@Nonnull String projectName) {
        log.info("Loading lazy project by name: {}.", projectName);
        ResponseEntity<ProjectFullVer1ViewDto> projectsResponse =
                projectEnvFeignClient.getShortProjectByName(projectName, false);
        LazyProject lazyProject;
        try {
            lazyProject = dtoConvertService.convert(projectsResponse.getBody(), LazyProject.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazyProjectByNameException.DEFAULT_MESSAGE, projectName), e);
            throw new TdmEnvConvertLazyProjectByNameException(projectName);
        }
        log.info("Lazy project by name successfully loaded.");
        return lazyProject;
    }

    /**
     * Get lazy projects.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_PROJECTS_CACHE)
    public List<LazyProject> getLazyProjects() {
        log.info("Loading lazy projects.");
        List<LazyProject> lazyProjects;
        ResponseEntity<List<ProjectNameViewDto>> projectsResponse =
                projectEnvFeignClient.getAllShort(false);
        try {
            lazyProjects = dtoConvertService.convertList(projectsResponse.getBody(), LazyProject.class);
        } catch (Exception e) {
            log.error(TdmEnvConvertLazyProjectsException.DEFAULT_MESSAGE, e);
            throw new TdmEnvConvertLazyProjectsException();
        }
        log.info("Lazy projects successfully loaded.");
        return lazyProjects;
    }


    /**
     * Environment:
     * Get lazy environment by ID.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_ENVIRONMENT_BY_ID_CACHE)
    public LazyEnvironment getLazyEnvironment(@Nonnull UUID environmentId) {
        log.info("Loading lazy environment by environment id: [{}]",  environmentId);
        LazyEnvironment environment;
        ResponseEntity<EnvironmentFullVer1ViewDto> envLazyRes =
                environmentFeignClient.getEnvironment(environmentId, false);
        try {
            environment = dtoConvertService.convert(envLazyRes.getBody(), LazyEnvironment.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazyEnvironmentByEnvIdtException.DEFAULT_MESSAGE,
                    environmentId), e);
            throw new TdmEnvConvertLazyEnvironmentByEnvIdtException(environmentId.toString());
        }
        log.info("Lazy environment successfully loaded.");
        return environment;
    }

    /**
     * Get env name by environment ID.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_ENV_NAME_BY_ENVIRONMENT_ID_CACHE)
    public String getEnvNameById(@Nonnull UUID environmentId) {
        log.info("Loading environment name by environment id: [{}]",  environmentId);
        ResponseEntity<String> environmentNameById = environmentFeignClient.getEnvironmentNameById(environmentId);
        String body = environmentNameById.getBody();
        return body;
    }

    /**
     * Get lazy environments by project ID - with systems.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_ENVIRONMENTS_CACHE)
    public List<LazyEnvironment> getLazyEnvironments(@Nonnull UUID projectId) {
        log.info("Loading lazy environments by project id: [{}]", projectId);
        List<LazyEnvironment> lazyEnvironments;
        ResponseEntity<List<EnvironmentResDto>> envResponse =
                projectEnvFeignClient.getEnvironments(projectId, false);
        try {
            lazyEnvironments = dtoConvertService.convertList(envResponse.getBody(), LazyEnvironment.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazyEnvironmentsException.DEFAULT_MESSAGE,
                    projectId), e);
            throw new TdmEnvConvertLazyEnvironmentsException(projectId.toString());
        }
        log.info("Lazy environments successfully loaded.");
        return lazyEnvironments;
    }

    /**
     * Get lazy environments by project ID - without systems.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_ENVIRONMENTS_SHORT_CACHE)
    public List<LazyEnvironment> getLazyEnvironmentsShort(@Nonnull UUID projectId) {
        log.info("Loading lazy environments by project id: [{}]", projectId);
        List<LazyEnvironment> lazyEnvironments;
        ResponseEntity<List<EnvironmentNameViewDto>> envResponse =
                projectEnvFeignClient.getEnvironmentsShort(projectId);
        try {
            lazyEnvironments = dtoConvertService.convertList(envResponse.getBody(), LazyEnvironment.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazyEnvironmentsException.DEFAULT_MESSAGE,
                    projectId), e);
            throw new TdmEnvConvertLazyEnvironmentsException(projectId.toString());
        }
        log.info("Lazy environments successfully loaded.");
        return lazyEnvironments;
    }

    /**
     * Get lazy environment by project and environment name.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_ENVIRONMENT_BY_NAME_CACHE)
    public LazyEnvironment getLazyEnvironmentByName(@Nonnull UUID projectId, @Nonnull String environmentName) {
        ArrayList<String> envNames = new ArrayList<>();
        envNames.add(environmentName);
        BaseSearchRequestDto baseSearchRequestDto = new BaseSearchRequestDto();
        baseSearchRequestDto.setProjectId(projectId);
        baseSearchRequestDto.setNames(envNames);
        ResponseEntity<List<EnvironmentDto>> body = environmentFeignClient.findBySearchRequest(baseSearchRequestDto, false);
        LazyEnvironment lazyEnvironment;
        try {
            lazyEnvironment = dtoConvertService.convertList(body.getBody(), LazyEnvironment.class)
                    .stream().filter(env -> env.getName().equals(environmentName.trim())).findFirst().orElseThrow(
                            () -> new TdmEnvConvertLazyEnvironmentByNameException(environmentName, projectId.toString())
                    );
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazyEnvironmentByNameException.DEFAULT_MESSAGE,
                    environmentName, projectId), e);
            throw new TdmEnvConvertLazyEnvironmentByNameException(environmentName, projectId.toString());
        }
        return lazyEnvironment;
    }


    /**
     * System:
     * Get full system by project ID, environment ID, name.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_FULL_SYSTEM_BY_NAME_CACHE)
    public System getFullSystemByName(@Nonnull UUID projectId, @Nonnull UUID environmentId,
                                      @Nonnull String systemName) {
        log.info("Loading full systems for project id: [{}] by environment id: [{}] and systemName: [{}]", projectId,
                environmentId, systemName);
        ResponseEntity<SystemFullVer1ViewDto> systemResponse = systemEnvFeignClient
                .getSystemByName(environmentId, systemName, true);
        System system;
        try {
            system = dtoConvertService.convert(systemResponse.getBody(), System.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertFullSystemByNameException.DEFAULT_MESSAGE, systemName), e);
            throw new TdmEnvConvertFullSystemByNameException(systemName);
        }
        log.info("Full systems by name successfully loaded.");
        return system;
    }

    /**
     * Get full system by ID.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_FULL_SYSTEM_BY_ID_CACHE)
    public System getFullSystemById(UUID systemId) {
        log.info("Loading full system by system ID: {}", systemId);
        System system;
        ResponseEntity<SystemFullVer1ViewDto> systemResponse =
                systemEnvFeignClient.getSystem(systemId, true);
        try {
            system = dtoConvertService.convert(systemResponse.getBody(), System.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertFullSystemBySysIdException.DEFAULT_MESSAGE, systemId), e);
            throw new TdmEnvConvertFullSystemBySysIdException(systemId.toString());
        }
        log.info("Full systems by system ID successfully loaded.");
        return system;
    }

    /**
     * Get connections by system ID.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_CONNECTIONS_BY_SYSTEM_ID_CACHE)
    public List<Connection> getConnectionsSystemById(UUID systemId) {
        log.info("Loading connections by system ID: {}", systemId);
        List<Connection> connections;
        ResponseEntity<List<ConnectionFullVer1ViewDto>> systemConnections =
                systemEnvFeignClient.getSystemConnections(systemId, false);
        try {
            connections = dtoConvertService.convertList(systemConnections.getBody(), Connection.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertFullSystemBySysIdException.DEFAULT_MESSAGE, systemId), e);
            throw new TdmEnvConvertFullSystemBySysIdException(systemId.toString());
        }
        log.info("Full systems by system ID successfully loaded.");
        return connections;
    }

    /**
     * Get lazy system by ID.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_SYSTEM_CACHE)
    public LazySystem getLazySystemById(@Nonnull UUID systemId) {
        log.info("Loading lazy system by system ID: {}", systemId);
        LazySystem lazySystem;
        ResponseEntity<SystemFullVer1ViewDto> system = systemEnvFeignClient.getShortSystem(systemId, false);
        try {
            lazySystem = dtoConvertService.convert(system.getBody(), LazySystem.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazySystemBySysIdException.DEFAULT_MESSAGE, systemId), e);
            throw new TdmEnvConvertLazySystemBySysIdException(systemId.toString());
        }
        log.info("Lazy systems by system ID successfully loaded.");
        return lazySystem;
    }

    /**
     * Get lazy system by project ID, environment ID, name.
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_SYSTEM_BY_NAME_CACHE)
    public LazySystem getLazySystemByName(@Nonnull UUID projectId, @Nonnull UUID environmentId,
                                      @Nonnull String systemName) {
        log.info("Loading lazy systems for project id: [{}] by environment id: [{}] and systemName: [{}]", projectId,
                environmentId, systemName);
        ResponseEntity<SystemFullVer1ViewDto> systemResponse =
                systemEnvFeignClient.getSystemByName(environmentId, systemName, false);
        LazySystem lazySystem;
        try {
            lazySystem = dtoConvertService.convert(systemResponse.getBody(), LazySystem.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertFullSystemByNameException.DEFAULT_MESSAGE, systemName), e);
            throw new TdmEnvConvertFullSystemByNameException(systemName);
        }
        log.info("Full systems by name successfully loaded.");
        return lazySystem;
    }



    /**
     * Get lazy systems by env Id.
     * @param environmentId ATP projectId
     * @return list of LazySystem's
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_SYSTEMS_CACHE)
    public List<LazySystem> getLazySystems(@Nonnull UUID environmentId) {
        log.info("Loading lazy systems by env ID: [{}]", environmentId);
        ResponseEntity<List<SystemNameViewDto>> systemsRes =
                environmentFeignClient.getSystemsShort(environmentId);
        log.info("Lazy systems by envID successfully loaded.");
        try {
            return dtoConvertService.convertList(systemsRes.getBody(), LazySystem.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazySystemsByEnvIdException.DEFAULT_MESSAGE, environmentId), e);
            throw new TdmEnvConvertLazySystemsByEnvIdException(environmentId.toString());
        }
    }

    /**
     * Get lazy systems by env Id and name.
     * @param environmentId ATP environmentId
     * @param defaultSystem ATP defaultSystem
     * @return list of LazySystem's
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_SYSTEMS_BY_NAME_CACHE)
    public List<LazySystem> getLazySystems(@Nonnull UUID environmentId, @Nonnull String defaultSystem) {
        log.info("Loading lazy systems by environment id: [{}], defaultSystem: [{}]", environmentId, defaultSystem);
        List<LazySystem> lazySystems;
        ResponseEntity<List<SystemNameViewDto>> systemsRes =
                environmentFeignClient.getSystemsShort(environmentId);
        try {
            List<LazySystem> systems = dtoConvertService.convertList(systemsRes.getBody(), LazySystem.class);
            lazySystems = sortByDefault(systems, defaultSystem);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazySystemsByEnvIdByNameException.DEFAULT_MESSAGE,
                    environmentId, defaultSystem), e);
            throw new TdmEnvConvertLazySystemsByEnvIdByNameException(environmentId.toString(), defaultSystem);
        }
        log.info("Lazy systems by envId and name successfully loaded.");
        return lazySystems;
    }

    @Override
    @Cacheable(value = CacheNames.TDM_ALL_SHORT_LAZY_SYSTEMS_BY_PROJECT_CACHE)
    public List<LazySystem> getLazySystemsByProjectWithEnvIds(@Nonnull UUID projectId) {
        log.info("Loading lazy systems by project ID: [{}]", projectId);
        List<LazySystem> lazySystems;
        ResponseEntity<List<SystemEnvironmentsViewDto>> allShortName =
                projectEnvFeignClient.getAllShortSystemsOnProject(projectId);
        try {
            lazySystems = dtoConvertService.convertList(allShortName.getBody(), LazySystem.class);
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazySystemsByProjectIdException.DEFAULT_MESSAGE, projectId), e);
            throw new TdmEnvConvertLazySystemsByProjectIdException(projectId.toString());
        }
        log.info("Lazy systems by project ID successfully loaded.");
        return lazySystems;
    }

    /**
     * Get all systems from feign client by project id.
     * @param projectId ATP projectId
     * @return list of LazySystem's
     */
    @Override
    @Cacheable(value = CacheNames.TDM_LAZY_SYSTEMS_BY_PROJECT_CACHE)
    public List<LazySystem> getLazySystemsByProjectIdWithConnections(@Nonnull UUID projectId) {
        log.info("Loading lazy systems by project ID: [{}]", projectId);
        List<LazySystem> systems;
        List<org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer2ViewDto> body =
                projectEnvFeignClient.getProjectSystems(projectId, null, false).getBody();
        try {
            systems = dtoConvertService.convertList(body, LazySystem.class);
        } catch (AtpException ae) {
            throw ae;
        } catch (Exception e) {
            log.error(format(TdmEnvConvertLazySystemsByProjectIdException.DEFAULT_MESSAGE, projectId), e);
            throw new TdmEnvConvertLazySystemsByProjectIdException(projectId.toString());
        }
        log.info("Lazy systems by project ID successfully loaded");
        return systems;
    }

    @Override
    public boolean resetCaches() {
        log.info("Reset caches.");
        try {
            Field[] fields = CacheNames.class.getDeclaredFields();
            for (Field field : fields) {
                Cache cache = cacheManager.getCache(field.get(String.class).toString());
                if (Objects.nonNull(cache)) {
                    cache.clear();
                }
            }
        } catch (Exception e) {
            log.error(TdmEnvResetCachesException.DEFAULT_MESSAGE, e);
            throw new TdmEnvResetCachesException();
        }
        log.info("Environment caches have been cleared.");
        return true;
    }

    private List<LazySystem> sortByDefault(@Nonnull List<LazySystem> lazySystems, String defaultSystem) {
        LazySystem lazySystem;
        for (int i = 0; i < lazySystems.size(); i++) {
            lazySystem = lazySystems.get(i);
            if (lazySystem.getName().equals(defaultSystem)) {
                Collections.swap(lazySystems, i, 0);
                break;
            }
        }
        return lazySystems;
    }
}
