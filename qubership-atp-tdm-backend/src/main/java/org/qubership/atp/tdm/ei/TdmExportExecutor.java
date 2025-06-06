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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.collections4.CollectionUtils;
import org.qubership.atp.ei.node.ExportExecutor;
import org.qubership.atp.ei.node.dto.ExportImportData;
import org.qubership.atp.ei.node.services.ObjectSaverToDiskService;
import org.qubership.atp.tdm.model.TestDataTableCatalog;
import org.qubership.atp.tdm.model.cleanup.TestDataCleanupConfig;
import org.qubership.atp.tdm.model.ei.ExportImportObject;
import org.qubership.atp.tdm.model.ei.ExportTable;
import org.qubership.atp.tdm.model.refresh.TestDataRefreshConfig;
import org.qubership.atp.tdm.model.table.TestDataTable;
import org.qubership.atp.tdm.repo.CatalogRepository;
import org.qubership.atp.tdm.repo.CleanupConfigRepository;
import org.qubership.atp.tdm.repo.ColumnRepository;
import org.qubership.atp.tdm.repo.ImportInfoRepository;
import org.qubership.atp.tdm.repo.RefreshConfigRepository;
import org.qubership.atp.tdm.repo.TestDataColumnFlagsRepository;
import org.qubership.atp.tdm.repo.TestDataMonitoringRepository;
import org.qubership.atp.tdm.repo.TestDataTableRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TdmExportExecutor implements ExportExecutor {

    @Value("${spring.application.name}")
    private String implementationName;

    private final ObjectSaverToDiskService objectSaverToDiskService;
    private final TestDataTableRepository testDataTableRepository;
    private final CatalogRepository catalogRepository;
    private final CleanupConfigRepository cleanupConfigRepository;
    private final ColumnRepository columnRepository;
    private final ImportInfoRepository importInfoRepository;
    private final RefreshConfigRepository refreshConfigRepository;
    private final TestDataMonitoringRepository testDataMonitoringRepository;
    private final TestDataColumnFlagsRepository testDataColumnFlagsRepository;

    @Override
    public void exportToFolder(ExportImportData exportData, Path path) throws Exception {
        Set<String> exportScopeAtpTdmTables = exportData.getExportScope().getEntities()
                .getOrDefault(ServiceScopeEntities.ENTITY_ATP_TDM_TABLES.getValue(), new HashSet<>());
        if (CollectionUtils.isNotEmpty(exportScopeAtpTdmTables)) {
            log.info("Start export for project {}", exportData.getProjectId());
            saveData(exportData.getProjectId(), path);
            log.info("Finish export for project {}", exportData.getProjectId());
        } else {
            log.error("Data hasn't been found, for project: [{}]", exportData.getProjectId());
        }
    }

    @Override
    public String getExportImplementationName() {
        return implementationName;
    }

    private void saveData(@Nonnull UUID projectId, @Nonnull Path workDir) {
        ExportImportObject exportImportObject = exportData(projectId);
        objectSaverToDiskService.exportAtpEntity(projectId, exportImportObject, workDir);
    }

    private ExportImportObject exportData(@Nonnull UUID projectId) {
        log.info("Export data for project id:[{}] started...", projectId);
        List<TestDataTableCatalog> tableCatalogs = catalogRepository.findAllByProjectId(projectId);
        List<ExportTable> exportTables = createExportTable(tableCatalogs);
        ExportImportObject exportResponse = new ExportImportObject();
        exportResponse.setProjectId(projectId);
        exportResponse.setTables(exportTables);
        testDataMonitoringRepository.findById(projectId)
                .ifPresent(exportResponse::setTestDataMonitoring);
        log.info("Export finished.");
        return exportResponse;
    }

    private List<ExportTable> createExportTable(List<TestDataTableCatalog> tableCatalogs) {
        return tableCatalogs
                .stream()
                .map(tableCatalog -> fillExportTableByData(createExportTable(tableCatalog), tableCatalog))
                .collect(Collectors.toList());
    }

    private ExportTable createExportTable(TestDataTableCatalog tableCatalog) {
        UUID environmentId = tableCatalog.getEnvironmentId();
        UUID systemId = tableCatalog.getSystemId();
        return new ExportTable(systemId, environmentId,
                tableCatalog.getTableName(), tableCatalog.getTableTitle());
    }

    private ExportTable fillExportTableByData(ExportTable exportTable, TestDataTableCatalog tableCatalog) {
        UUID cleanupConfigId = tableCatalog.getCleanupConfigId();
        if (Objects.nonNull(cleanupConfigId)) {
            exportTable.setCleanupConfig(cleanupConfigRepository.findById(cleanupConfigId)
                    .orElseGet(() -> {
                        log.warn("Cleanup config with id:[{}] was not found.", cleanupConfigId);
                        TestDataCleanupConfig testDataCleanupConfig = new TestDataCleanupConfig();
                        testDataCleanupConfig.setId(cleanupConfigId);
                        return testDataCleanupConfig;
                    }));
        }
        exportTable.setColumns(columnRepository.findAllByIdentityTableName(tableCatalog.getTableName()));
        exportTable.setImportInfo(importInfoRepository.findByTableName(tableCatalog.getTableName()));
        UUID refreshConfigId = tableCatalog.getRefreshConfigId();
        if (Objects.nonNull(refreshConfigId)) {
            exportTable.setRefreshConfig(refreshConfigRepository.findById(refreshConfigId).orElseGet(() -> {
                log.warn("Refresh config with id:[{}] was not found.", refreshConfigId);
                TestDataRefreshConfig refreshConfig = new TestDataRefreshConfig();
                refreshConfig.setId(cleanupConfigId);
                return refreshConfig;
            }));
        }
        exportTable.setFlagsTable(testDataColumnFlagsRepository.findRowByTableName(tableCatalog.getTableName()));
        TestDataTable testDataTable = testDataTableRepository.getFullTestData(tableCatalog.getTableName());
        exportTable.setData(testDataTable.getData());
        return exportTable;
    }
}
