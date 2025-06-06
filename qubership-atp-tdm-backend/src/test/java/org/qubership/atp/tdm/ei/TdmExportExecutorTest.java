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

import org.qubership.atp.ei.node.dto.ExportScope;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class TdmExportExecutorTest extends AbstractExportImportTest {

    private String tableNamePositiveCase;
    ExportScope exportScope = new ExportScope();

    @Autowired
    TdmExportExecutor exportExecutor;

    @BeforeEach
    public void setUp() throws Exception {
        tableNamePositiveCase = tableName + UUID.randomUUID();
        tableNamePositiveCase = tableNamePositiveCase.replaceAll("-", "_");
        exportScope.getEntities().put(ServiceScopeEntities.ENTITY_ATP_TDM_TABLES.getValue(), createSetUuid());
        runExport(exportExecutor, exportScope, tableNamePositiveCase);
    }

    @AfterEach
    public void tearDown() {
        deleteTestDataTableIfExists(tableNamePositiveCase);
        catalogRepository.deleteByTableName(tableNamePositiveCase);
    }

    @Test
    public void exportToFolder_exportProjectToSpecifiedFolder_dataExportedSuccessfullyProjectIdIsValid() {
        Assertions.assertEquals(exportImportProjectId, exportImportObject.getProjectId());
    }

    @Test
    public void exportToFolder_exportProjectToSpecifiedFolder_dataExportedSuccessfullySystemIdIsValid() {
        Assertions.assertEquals(systemId,
                exportImportObject.getTables().stream().findFirst().get().getSystemId());
    }

    @Test
    public void exportToFolder_exportProjectToSpecifiedFolder_dataExportedSuccessfullyEnvironmentIdIsValid() {
        Assertions.assertEquals(environmentId,
                exportImportObject.getTables().stream().findFirst().get().getEnvironmentId());
    }

    @Test
    public void exportToFolder_exportProjectToSpecifiedFolder_dataExportedSuccessfullyTableNameIsValid() {
        Assertions.assertEquals(tableNamePositiveCase,
                exportImportObject.getTables().stream().findFirst().get().getTableName());
    }

    @Test
    public void exportToFolder_exportProjectToSpecifiedFolder_dataExportedSuccessfullyTableTitleIsValid() {
        Assertions.assertEquals(tableTitle,
                exportImportObject.getTables().stream().findFirst().get().getTableTitle());
    }

    @Test
    public void exportToFolder_exportProjectToSpecifiedFolder_dataExportedSuccessfullyDataExists() {
        Assertions.assertFalse(exportImportObject.getTables().stream().findFirst().get().getData().isEmpty());
    }

    @Test
    public void exportToFolder_exportProjectToSpecifiedFolder_dataExportedAndFlagsSetCorrectly() {
        Assertions.assertFalse(exportImportObject.getTables().stream().findFirst().get().getFlagsTable().isUnoccupiedValidation());
    }
}
