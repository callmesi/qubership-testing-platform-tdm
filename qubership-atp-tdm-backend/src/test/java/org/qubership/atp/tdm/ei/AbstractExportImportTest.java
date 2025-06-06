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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.qubership.atp.tdm.model.TestDataTableCatalog;
import org.qubership.atp.tdm.model.ei.ExportImportObject;

import com.google.gson.Gson;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.dto.ExportFormat;
import org.qubership.atp.ei.node.dto.ExportScope;
import org.qubership.atp.tdm.AbstractTestDataTest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractExportImportTest extends AbstractTestDataTest {

    protected StringBuilder exportJson = new StringBuilder();
    protected String query = "select sim from tdm_test_data_refresh_unoccupied_table";
    protected String tableName = "tdm_test_data_export_import";
    protected String tableTitle = "TDM Test Data Run Export Import";
    protected String tablePrefix = "tdm_";
    protected Gson json = new Gson();
    protected ExportImportObject exportImportObject;
    protected static final UUID exportImportProjectId = UUID.randomUUID();

    protected Set<String> createSetUuid() {
        return Stream.of(tablePrefix + UUID.randomUUID(), tablePrefix + UUID.randomUUID()).collect(Collectors.toSet());
    }

    public void runExport(TdmExportExecutor exportExecutor, ExportScope exportScope, String tableName) throws Exception {
        TestDataTableCatalog
                table = createTestDataTableCatalog(exportImportProjectId, systemId, environmentId, tableTitle, tableName, query);
        createTestDataTable(tableName);
        createTestDataTableColumns(tableName);
        createDateCleanupConfig(table);
        createFlagsTable(tableName);
        exportExecutor.exportToFolder(new ExportImportData(exportImportProjectId, exportScope, ExportFormat.ATP),
                Paths.get(""));
        File file = new File("ExportImportObject/" + exportImportProjectId + ".json");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                exportJson.append(line);
            }
            exportImportObject = json.fromJson(exportJson.toString(), ExportImportObject.class);
        } catch (IOException e) {
            log.error("File hasn't been found. ", e);
        }
    }

}
