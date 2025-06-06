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

package org.qubership.atp.tdm.integration.environment;

import org.qubership.atp.tdm.integration.containers.ForEachPostgresqlContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.rules.ExternalResource;

import java.util.Optional;

@Slf4j
public class IntegrationPostgresEnvironment extends ExternalResource {

    private static IntegrationPostgresEnvironment instance;
    private String postgresJdbcUrl = "jdbc:postgresql://localhost:5433/atp2_tdm";
    private ForEachPostgresqlContainer postgresContainer;

    private IntegrationPostgresEnvironment() {
        Optional<String> startEnvironment = Optional.ofNullable(System.getProperty("LOCAL_DOCKER_START"));
        if (startEnvironment.isPresent() && Boolean.parseBoolean(startEnvironment.get())) {
            postgresContainer = ForEachPostgresqlContainer.getInstance();
        } else {
            Optional<String> urlDB = Optional.ofNullable(System.getProperty("JDBC_URL"));
            if (urlDB.isPresent()) {
                postgresJdbcUrl = System.getProperty("JDBC_URL");
            }
            log.info("Postgres JDBC URL {}", postgresJdbcUrl);
        }
    }

    public static IntegrationPostgresEnvironment getInstance() {
        if (instance == null) {
            instance = new IntegrationPostgresEnvironment();
        }
        return instance;
    }

    @Override
    protected void before() {
        if (postgresContainer != null) {
            postgresContainer.start();
            postgresJdbcUrl = postgresContainer.getPostgresIp();
        } else {
            log.info("Don't started Postgres Server container. Postgres JDBC URL {}", postgresJdbcUrl);
        }
    }

    @Override
    protected void after() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

    public String getPostgresJdbcUrl() {
        return postgresJdbcUrl;
    }

}
