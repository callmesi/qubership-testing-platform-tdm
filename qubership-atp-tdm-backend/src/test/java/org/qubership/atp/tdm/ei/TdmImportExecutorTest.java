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

package org.qubership.atp.tdm.ei;

import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.dto.ExportFormat;
import org.qubership.atp.ei.node.dto.ExportScope;
import org.qubership.atp.ei.node.dto.ValidationResult;
import org.qubership.atp.tdm.env.configurator.model.LazyEnvironment;
import org.qubership.atp.tdm.env.configurator.model.LazyProject;
import org.qubership.atp.tdm.env.configurator.model.LazySystem;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TdmImportExecutorTest extends AbstractExportImportTest {

    private final String validImportFile = "src/test/resources/ei-resources/valid-import";
    private final String thereIsNoProjectInEnvServiceImportFile = "src/test/resources/ei-resources/incorrect-imports" +
            "/thereIsNoProject";
    private final String thereIsNoEnvironmentInEnvServiceImportFile = "src/test/resources/ei-resources/incorrect" +
            "-imports/thereIsNoEnvironment";
    private final String thereIsNoSystemInEnvServiceImportFile = "src/test/resources/ei-resources/incorrect-imports" +
            "/thereIsNoSystem";

    private static final UUID projectId = UUID.fromString("6b90558d-f0c7-448a-b701-f04d95ad655d");
    private static final UUID environmentId = UUID.fromString("9fd33044-d693-4b24-8d9b-67277e120631");
    private static final UUID systemId = UUID.fromString("eef70313-1ccc-4be7-ba3c-787654abf863");
    private static final List<LazyProject> projectsList = new ArrayList<>();
    private static final List<LazyEnvironment> environmentsList = new ArrayList<>();
    private static final List<LazySystem> systemsList = new ArrayList<>();

    @Autowired
    TdmImportExecutor importExecutor;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final LazyEnvironment lazyEnvironment = new LazyEnvironment() {{
        setName("Lazy Environment");
        setId(environmentId);
    }};

    private static final LazyProject lazyProject = new LazyProject() {{
        setName("Lazy Project");
        setId(projectId);
    }};

    private static final LazySystem lazySystem = new LazySystem() {{
        setName("Lazy System");
        setId(systemId);
    }};

    @BeforeEach
    public void setUp() {
        projectsList.add(lazyProject);
        environmentsList.add(lazyEnvironment);
        systemsList.add(lazySystem);
        when(environmentsService.getLazyProjects()).thenReturn(projectsList);
        when(environmentsService.getLazyEnvironments(any())).thenReturn(environmentsList);
        when(environmentsService.getLazySystems(any())).thenReturn(systemsList);

        when(environmentsService.getEnvNameById(any())).thenReturn("test");
        when(environmentsService.getLazyEnvironmentsShort(any())).thenReturn(Collections.singletonList(lazyEnvironment));
        when(environmentsService.getConnectionsSystemById(any())).thenReturn(Collections.singletonList(dbConnection));
    }

    @Test
    public void importData_importFile_importedSuccessfully() throws Exception {
        importExecutor.validateData(new ExportImportData(exportImportProjectId, new ExportScope(), ExportFormat.ATP),
                Paths.get(validImportFile));
        importExecutor.importData(new ExportImportData(exportImportProjectId, new ExportScope(), ExportFormat.ATP),
                Paths.get(validImportFile));
        Assertions.assertEquals(6,
                jdbcTemplate.queryForList("select * from tdm_test_data_export_import").size());
    }

    @Test
    public void validateData_validateImportingFile_fileIsValid() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(validImportFile));
        Assertions.assertTrue(validationResult.isValid());
    }

    @Test
    public void validateData_validateImportingFile_thereAreNoMessagesInValidationResult() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(validImportFile));
        Assertions.assertTrue(validationResult.getMessages().isEmpty());
    }

    @Test
    public void validateData_validateImportingFile_thereIsNoProjectInEnvsService() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(thereIsNoProjectInEnvServiceImportFile));
        Assertions.assertEquals("Project with id:[13fec4c4-9c27-42e2-a4b3-250415525603] wasn't found in env service.",
                validationResult.getMessages().get(0));
    }

    @Test
    public void validateData_validateImportingFile_thereIsNoProjectInEnvsServiceFileIsIncorrect() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(thereIsNoProjectInEnvServiceImportFile));
        Assertions.assertFalse(validationResult.isValid());
    }

    @Test
    public void validateData_validateImportingFile_thereIsNoEnvironmentInEnvsService() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(thereIsNoEnvironmentInEnvServiceImportFile));
        Assertions.assertEquals("Environment with id:[da027247-bb3b-41cf-a8e8-247a368065e1] wasn't found in env service.",
                validationResult.getMessages().get(0));
    }

    @Test
    public void validateData_validateImportingFile_thereIsNoEnvironmentInEnvsServiceFileIsIncorrect() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(thereIsNoEnvironmentInEnvServiceImportFile));
        Assertions.assertFalse(validationResult.isValid());
    }

    @Test
    public void validateData_validateImportingFile_thereIsNoSystemInEnvsService() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(thereIsNoSystemInEnvServiceImportFile));
        Assertions.assertEquals("System with id:[dc42601e-54c5-4511-bd5b-bc588b343051] wasn't found in env service.",
                validationResult.getMessages().get(0));
    }

    @Test
    public void validateData_validateImportingFile_thereIsNoSystemInEnvsServiceFileIsIncorrect() throws Exception {
        ValidationResult validationResult = importExecutor.validateData(new ExportImportData(exportImportProjectId,
                        new ExportScope(),
                        ExportFormat.ATP),
                Paths.get(thereIsNoSystemInEnvServiceImportFile));
        Assertions.assertFalse(validationResult.isValid());
    }
}
