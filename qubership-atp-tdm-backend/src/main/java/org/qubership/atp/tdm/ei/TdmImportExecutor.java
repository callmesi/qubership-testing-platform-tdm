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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.qubership.atp.tdm.model.TestDataTableCatalog;
import org.qubership.atp.tdm.model.TestDataTableImportInfo;
import org.qubership.atp.tdm.model.cleanup.TestDataCleanupConfig;
import org.qubership.atp.tdm.model.ei.ExportImportObject;
import org.qubership.atp.tdm.model.ei.ExportTable;
import org.qubership.atp.tdm.model.refresh.TestDataRefreshConfig;
import org.qubership.atp.tdm.model.statistics.TestDataTableMonitoring;
import org.qubership.atp.tdm.model.table.TestDataFlagsTable;
import org.qubership.atp.tdm.model.table.TestDataTable;
import org.qubership.atp.tdm.model.table.column.TestDataTableColumn;
import org.qubership.atp.tdm.model.table.column.TestDataTableColumnIdentity;
import org.springframework.stereotype.Service;

import org.qubership.atp.ei.node.ImportExecutor;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.dto.ValidationResult;
import org.qubership.atp.ei.node.services.ObjectLoaderFromDiskService;
import org.qubership.atp.tdm.env.configurator.model.LazyEnvironment;
import org.qubership.atp.tdm.env.configurator.service.EnvironmentsService;

import org.qubership.atp.tdm.repo.CatalogRepository;
import org.qubership.atp.tdm.repo.CleanupConfigRepository;
import org.qubership.atp.tdm.repo.ColumnRepository;
import org.qubership.atp.tdm.repo.ImportInfoRepository;
import org.qubership.atp.tdm.repo.RefreshConfigRepository;
import org.qubership.atp.tdm.repo.TestDataColumnFlagsRepository;
import org.qubership.atp.tdm.repo.TestDataMonitoringRepository;
import org.qubership.atp.tdm.repo.TestDataTableRepository;
import org.qubership.atp.tdm.repo.impl.SystemColumns;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class TdmImportExecutor implements ImportExecutor {

    private final EnvironmentsService environmentsService;
    private final TestDataTableRepository testDataTableRepository;
    private final CatalogRepository catalogRepository;
    private final CleanupConfigRepository cleanupConfigRepository;
    private final ColumnRepository columnRepository;
    private final ImportInfoRepository importInfoRepository;
    private final RefreshConfigRepository refreshConfigRepository;
    private final TestDataMonitoringRepository testDataMonitoringRepository;
    private final TestDataColumnFlagsRepository testDataColumnFlagsRepository;
    private final ObjectLoaderFromDiskService objectLoaderFromDiskService;
    private List<ExportImportObject> exportImportObjectList;

    @Override
    public void importData(ExportImportData importData, Path path) throws Exception {
        log.info("start importData(importData: {}, workDir: {})", importData, path);
        exportImportObjectList.forEach(this::importData);
        exportImportObjectList.clear();
        log.info("end importData(importData: {}, workDir: {})", importData, path);
    }

    private void importData(@Nonnull ExportImportObject exportImportObject) {
        log.info("Import for project id:[{}] started...", exportImportObject.getProjectId());

        UUID projectId = exportImportObject.getProjectId();
        TestDataTableMonitoring testDataMonitoring = exportImportObject.getTestDataMonitoring();
        List<ExportTable> tables = exportImportObject.getTables();

        if (Objects.nonNull(testDataMonitoring)) {
            testDataMonitoringRepository.save(testDataMonitoring);
        }

        for (ExportTable table : tables) {
            TestDataCleanupConfig cleanupConfig = table.getCleanupConfig();
            TestDataRefreshConfig refreshConfig = table.getRefreshConfig();
            TestDataTableImportInfo importInfo = table.getImportInfo();
            List<TestDataTableColumn> columns = table.getColumns();
            TestDataTableCatalog catalog = createTestDataCatalog(new TestDataTableCatalog(), table, projectId,
                    cleanupConfig, refreshConfig, importInfo);
            catalogRepository.save(catalog);
            if (Objects.nonNull(cleanupConfig)) {
                cleanupConfigRepository.save(cleanupConfig);
            }
            if (Objects.nonNull(columns)) {
                columnRepository.saveAll(columns);
            }
            if (Objects.nonNull(importInfo)) {
                importInfoRepository.save(importInfo);
            }
            if (Objects.nonNull(refreshConfig)) {
                refreshConfigRepository.save(refreshConfig);
            }
            TestDataFlagsTable flags = table.getFlagsTable();
            testDataColumnFlagsRepository.save(flags);
            saveData(projectId, table, table.getTableName());
        }
        log.info("Import finished.");
    }

    private void createListExportImportObjects(Path path) {
        if (!exportImportObjectList.isEmpty()) {
            exportImportObjectList.clear();
        }
        Map<UUID, Path> listObjects = objectLoaderFromDiskService.getListOfObjects(path, ExportImportObject.class);
        listObjects.forEach((id, objectPath) ->
                exportImportObjectList.add(objectLoaderFromDiskService.loadFileAsObject(objectPath,
                ExportImportObject.class)));
    }

    private void saveData(UUID projectId, ExportTable table, String tableName) {
        List<Map<String, Object>> data = table.getData();
        if (Objects.nonNull(data)) {
            Optional<Map<String, Object>> dataFirstRow = data.stream().findFirst();
            if (dataFirstRow.isPresent()) {
                try {
                    testDataTableRepository.dropTable(tableName);
                } catch (Exception e) {
                    log.warn("Table:[{}] doesn't exist.", tableName);
                }
                List<String> columnNames = new ArrayList<>(dataFirstRow.get().keySet());
                TestDataTable testDataTable = createTestDataTable(new TestDataTable(), table, tableName,
                        generateTestDataTableColumnList(columnNames));

                testDataTableRepository.saveTestData(tableName, false, testDataTable);
            }
        } else {
            log.warn("Data was not found in export process. Project id:[{}]", projectId);
        }
    }

    private List<TestDataTableColumn> generateTestDataTableColumnList(List<String> columnNames) {
        List<String> systemColumns = SystemColumns.getColumnNames();
        return columnNames.stream()
                .filter(c -> !systemColumns.contains(c))
                .map(c -> new TestDataTableColumn(new TestDataTableColumnIdentity("", c)))
                .collect(Collectors.toList());
    }

    private TestDataTable createTestDataTable(TestDataTable testDataTable,
                                              ExportTable table,
                                              String tableName,
                                              List<TestDataTableColumn> columns) {
        testDataTable.setName(tableName);
        testDataTable.setColumns(columns);
        testDataTable.setData(table.getData());
        return testDataTable;
    }

    private TestDataTableCatalog createTestDataCatalog(TestDataTableCatalog catalog,
                                                      ExportTable table,
                                                      UUID projectId,
                                                      TestDataCleanupConfig cleanupConfig,
                                                      TestDataRefreshConfig refreshConfig,
                                                      TestDataTableImportInfo importInfo) {
        catalog.setTableName(table.getTableName());
        catalog.setProjectId(projectId);
        catalog.setEnvironmentId(table.getEnvironmentId());
        catalog.setSystemId(table.getSystemId());
        catalog.setTableTitle(table.getTableTitle());
        if (Objects.nonNull(cleanupConfig)) {
            catalog.setCleanupConfigId(cleanupConfig.getId());
        }
        if (Objects.nonNull(refreshConfig)) {
            catalog.setRefreshConfigId(refreshConfig.getId());
        }
        if (Objects.nonNull(importInfo)) {
            catalog.setImportQuery(importInfo.getTableQuery());
            catalog.setQueryTimeout(importInfo.getQueryTimeout());
        }
        return catalog;
    }

    private List<String> validateData(@Nonnull ExportImportObject exportImportObject, List<String> validations,
                                      ExportImportData exportImportData) {
        UUID projectId = exportImportObject.getProjectId();
        log.debug("validateData: {}", exportImportObject);
        if (exportImportData.isCreateNewProject() || exportImportData.isImportFirstTime()) {
            log.debug("Validation data was skipped, because new project will be created (project id = {})", projectId);
            return null;
        }
        boolean isProjectExistsInEnvs = environmentsService.getLazyProjects().stream()
                .anyMatch(p -> p.getId().equals(projectId));
        if (!isProjectExistsInEnvs) {
            validations.add("Project with id:[" + projectId + "] wasn't found in env service.");
            return validations;
        }

        List<ExportTable> tables = exportImportObject.getTables();
        List<UUID> lazyEnvironments =
                environmentsService.getLazyEnvironmentsShort(projectId)
                        .stream().map(LazyEnvironment::getId).collect(Collectors.toList());
        tables.forEach(table -> {
            UUID envId = table.getEnvironmentId();
            UUID sysId = table.getSystemId();
            if (!lazyEnvironments.contains(envId)) {
                validations.add("Environment with id:[" + envId + "] wasn't found in env service.");
            } else {
                boolean isSystemExists = environmentsService.getLazySystems(envId)
                        .stream().anyMatch(s -> s.getId().equals(sysId));
                if (!isSystemExists) {
                    validations.add("System with id:[" + sysId + "] wasn't found in env service.");
                }
            }
        });

        return validations;
    }

    @Override
    public ValidationResult validateData(ExportImportData exportImportData, Path path) {
        List<String> messages = new ArrayList<>();
        createListExportImportObjects(path);
        exportImportObjectList.forEach(exportImportObject -> validateData(exportImportObject, messages,
                exportImportData));
        return new ValidationResult(new ArrayList<>(messages));
    }

    public ValidationResult preValidateData(ExportImportData exportImportData, Path path) {
        return null;
    }
}
