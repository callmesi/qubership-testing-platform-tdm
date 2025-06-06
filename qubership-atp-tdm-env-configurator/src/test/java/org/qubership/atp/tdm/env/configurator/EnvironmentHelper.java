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

package org.qubership.atp.tdm.env.configurator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.qubership.atp.tdm.env.configurator.model.Connection;
import org.qubership.atp.tdm.env.configurator.model.Environment;
import org.qubership.atp.tdm.env.configurator.model.LazyEnvironment;
import org.qubership.atp.tdm.env.configurator.model.LazyProject;
import org.qubership.atp.tdm.env.configurator.model.LazySystem;
import org.qubership.atp.tdm.env.configurator.model.Project;
import org.qubership.atp.tdm.env.configurator.model.System;

import org.qubership.atp.tdm.env.configurator.api.dto.environments.EnvironmentDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.EnvironmentFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.SystemFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.SystemFullVer2ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.SystemNameViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.project.*;

public class EnvironmentHelper {

    private static final UUID projectId = UUID.randomUUID();

    private static final UUID lazyEnvId = UUID.randomUUID();
    private static final UUID systemId = UUID.randomUUID();
    private static final UUID rbmSystemId = UUID.randomUUID();
    private static final UUID tomsSystemId = UUID.randomUUID();
    private static final UUID envId = UUID.randomUUID();
    private static final UUID lazyProjectId = UUID.randomUUID();

    public static final LazyProject lazyProject = new LazyProject() {{
        setName("Lazy Project Name");
        setId(lazyProjectId);
    }};

    public static final LazySystem lazySystem = new LazySystem() {{
        setName("System Name");
        setId(systemId);
    }};

    private static final LazySystem lazySystemToms = new LazySystem() {{
        setName("Default");
        setId(tomsSystemId);
    }};

    private static final LazySystem lazySystemRbm = new LazySystem() {{
        setName("SystemName");
        setId(rbmSystemId);
    }};

    public static final LazyEnvironment lazyEnvironment = new LazyEnvironment() {{
        setName("Lazy Environment Name");
        setId(lazyEnvId);
        setProjectId(lazyProjectId);
        setSystems(Collections.singletonList(systemId.toString()));
    }};

    public static final LazyEnvironment lazyEnvironmentShort = new LazyEnvironment() {{
        setName("Lazy Environment Name");
        setId(lazyEnvId);
    }};

    public static final List<LazySystem> lazySystems = new ArrayList<LazySystem>() {{
        add(lazySystemToms);
        add(lazySystemRbm);
    }};

    public static final SystemEnvironmentsViewDto systemEnvironmentsViewDtoTOMS = new SystemEnvironmentsViewDto(){{
        setName("Default");
        setId(tomsSystemId);
    }};

    public static final SystemEnvironmentsViewDto systemEnvironmentsViewDtoRBM = new SystemEnvironmentsViewDto(){{
        setName("SystemName");
        setId(rbmSystemId);
    }};

    public static final org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer2ViewDto
            systemFullVer2ViewDtoTOMS =
            new org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer2ViewDto() {{
                setName("Default");
                setId(tomsSystemId);
    }};


    public static final org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer2ViewDto
            systemFullVer2ViewDtoRBM =
            new org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer2ViewDto() {{
                setName("SystemName");
                setId(rbmSystemId);
            }};

    public static final List<org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer2ViewDto>
            systemsFullVer2ViewDtoP =
            new ArrayList<org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer2ViewDto>() {{
        add(systemFullVer2ViewDtoTOMS);
        add(systemFullVer2ViewDtoRBM);
    }};

    public static final List<SystemEnvironmentsViewDto> systemEnvironmentsViewDto
            = new ArrayList<SystemEnvironmentsViewDto>(){{
        add(systemEnvironmentsViewDtoTOMS);
        add(systemEnvironmentsViewDtoRBM);
    }};

    public static final System system = new System() {{
        setName("System Name");
        setId(systemId);
    }};

    public static final Environment environment = new Environment() {{
        setName("Environment Name");
        setId(envId);
        setProjectId(projectId);
        setSystems(Collections.singletonList(system));
    }};

    public static final Project project = new Project() {{
        setName("Project Name");
        setId(projectId);
        setEnvironments(Collections.singletonList(environment));
    }};


    public static final ProjectFullVer2ViewDto projectFullVer2ViewDto = new ProjectFullVer2ViewDto() {{
        setName("Lazy Project Name");
        setId(lazyProjectId);
    }};

    public static final ProjectNameViewDto projectNameViewDto = new ProjectNameViewDto() {{
        setName("Lazy Project Name");
        setId(lazyProjectId);
    }};

    public static final EnvironmentResDto environmentResDto = new EnvironmentResDto() {{
        setName("Lazy Environment Name");
        setId(lazyEnvId);
        setProjectId(lazyProjectId);
        setSystems(Collections.singletonList(systemId.toString()));
    }};

    public static final EnvironmentDto environmentDto = new EnvironmentDto() {{
        setName("Lazy Environment Name");
        setId(lazyEnvId);
        setProjectId(lazyProjectId);
        setSystems(Collections.singletonList(systemId.toString()));
    }};

    private static final SystemNameViewDto systemNameViewDtoToms = new SystemNameViewDto() {{
        setName("Default");
        setId(tomsSystemId);
    }};

    private static final SystemNameViewDto systemNameViewDtoRbm = new SystemNameViewDto() {{
        setName("SystemName");
        setId(rbmSystemId);
    }};

    public static final List<SystemNameViewDto> systemFullVer2ViewDto = new ArrayList<SystemNameViewDto>() {{
        add(systemNameViewDtoToms);
        add(systemNameViewDtoRbm);
    }};


    private static final Connection systemConnectionHttp = new Connection() {{
        setName("http");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("url", "http://localhost:8080/");
        setParameters(parameters);
    }};

    private static final Connection systemConnectionDb = new Connection() {{
        setName("DB");
        Map<String, String> parameters = new HashMap<>();
        parameters.put("db_type", "postgresql");
        parameters.put("jdbc_url", "localhost");
        parameters.put("db_login", "tdmadmin");
        parameters.put("db_password", "tdmadmin");
        setParameters(parameters);
    }};

    public static final List<Connection> systemConnections = new ArrayList<Connection>() {{
        add(systemConnectionDb);
        add(systemConnectionHttp);
    }};

    public static final SystemFullVer1ViewDto systemFullVer1ViewDto = new SystemFullVer1ViewDto() {{
        setName("System Name");
        setId(systemId);
    }};

    public static final EnvironmentFullVer1ViewDto environmentFullVer1ViewDto = new EnvironmentFullVer1ViewDto() {{
        setName("Environment Name");
        setId(envId);
        setProjectId(projectId);
        setSystems(Collections.singletonList(systemFullVer1ViewDto));
    }};

    public static final EnvironmentFullVer1ViewDto environmentLazyVer1ViewDto = new EnvironmentFullVer1ViewDto() {{
        setName("Lazy Environment Name");
        setId(lazyEnvId);
        setProjectId(lazyProjectId);
        setSystems(Collections.singletonList(systemId.toString()));
    }};

    public static final org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer1ViewDto
            systemProjectFullVer1ViewDto =
            new org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer1ViewDto() {{
                setName("System Name");
                setId(systemId);
    }};

    public static final org.qubership.atp.tdm.env.configurator.api.dto.project.EnvironmentFullVer1ViewDto
            environmentProjectFullVer1ViewDto =
            new org.qubership.atp.tdm.env.configurator.api.dto.project.EnvironmentFullVer1ViewDto() {{
                setName("Environment Name");
                setId(envId);
                setProjectId(projectId);
                setSystems(Collections.singletonList(systemProjectFullVer1ViewDto));
    }};

    public static final ProjectFullVer1ViewDto projectFullVer1ViewDto = new ProjectFullVer1ViewDto() {{
        setName("Project Name");
        setId(projectId);
        setEnvironments(Collections.singletonList(environmentProjectFullVer1ViewDto));
    }};

    public static final ProjectFullVer1ViewDto projectLazyVer1ViewDto = new ProjectFullVer1ViewDto() {{
        setName("Lazy Project Name");
        setId(lazyProjectId);
    }};

    public static final ProjectFullVer1ViewDto projectFullVer1ViewDtoLazyProjectByName = new ProjectFullVer1ViewDto() {{
        setName("Lazy Project Name");
        setId(lazyProjectId);
    }};
}
