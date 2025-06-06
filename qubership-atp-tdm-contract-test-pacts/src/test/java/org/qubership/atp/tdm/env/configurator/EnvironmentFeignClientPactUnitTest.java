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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslResponse;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.qubership.atp.auth.springbootstarter.config.FeignConfiguration;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.EnvironmentFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.SystemFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.api.dto.environments.SystemFullVer2ViewDto;
import org.qubership.atp.tdm.env.configurator.service.client.EnvironmentFeignClient;

@RunWith(SpringRunner.class)
@EnableFeignClients(clients = {EnvironmentFeignClient.class})
@ContextConfiguration(classes = {EnvironmentFeignClientPactUnitTest.TestApp.class})
@Import({JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class, FeignConfiguration.class,
        FeignAutoConfiguration.class})
@TestPropertySource(
        properties = {"feign.atp.environments.name=atp-environments", "feign.atp.environments.route=", "feign.atp.environments.url=http://localhost:8888"})
public class EnvironmentFeignClientPactUnitTest {

    @Configuration
    public static class TestApp {
    }

    @Autowired
    EnvironmentFeignClient environmentFeignClient;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("atp-environments", "localhost", 8888, this);

    @Test
    @PactVerification()
    public void allPass() {
        UUID envId = UUID.fromString("7c9dafe9-2cd1-4ffc-ae54-45867f2b9701");

        ResponseEntity<EnvironmentFullVer1ViewDto> result1 = environmentFeignClient.getEnvironment(envId, true);
        Assert.assertEquals(result1.getStatusCode().value(), 200);
        Assert.assertTrue(result1.getHeaders().get("Content-Type").contains("application/json"));

        ResponseEntity<List<SystemFullVer2ViewDto>> result2 = environmentFeignClient.getSystemV2(envId, "system_type", false);
        Assert.assertEquals(result2.getStatusCode().value(), 200);
        Assert.assertTrue(result2.getHeaders().get("Content-Type").contains("application/json"));

    }

    @Pact(consumer = "atp-tdm")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        DslPart environmentFullVer1ViewRes = new PactDslJsonBody()
                .integerType("created")
                .integerType("modified")
                .uuid("createdBy")
                .uuid("modifiedBy")
                .uuid("projectId")
                .uuid("id")
                .stringType("description")
                .stringType("name")
                .array("systems").object().closeArray();

        DslPart object1 = new PactDslJsonBody()
                .integerType("created")
                .integerType("dateOfCheckVersion")
                .integerType("dateOfLastCheck")
                .integerType("modified")
                .uuid("modifiedBy")
                .uuid("createdBy")
                .uuid("externalId")
                .uuid("id")
                .uuid("linkToSystemId")
                .uuid("parentSystemId")

                .stringType("description")
                .stringType("name")
                .stringType("status", SystemFullVer1ViewDto.StatusEnum.FAIL.toString())
                .stringType("version")
                .booleanType("mergeByName")
                .object("serverITF").closeObject()
                .object("parametersGettingVersion").closeObject()
                .object("systemCategory").closeObject()
                .array("environments").object().closeArray()
                .array("connections").object().closeArray();

        DslPart systemFullVer2ViewRes = new PactDslJsonArray().template(object1);

        PactDslResponse response = builder
                .given("all ok")
                .uponReceiving("GET /api/environments/{environmentId} OK")
                .path("/api/environments/7c9dafe9-2cd1-4ffc-ae54-45867f2b9701")
                .matchQuery("full", "true|false", "true")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(environmentFullVer1ViewRes)

                .given("all ok")
                .uponReceiving("GET /api/v2/environments/{environmentId}/systems OK")
                .path("/api/v2/environments/7c9dafe9-2cd1-4ffc-ae54-45867f2b9701/systems")
                .matchQuery("system_type", "\\w*", "type")
                .matchQuery("full", "true|false", "false")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(systemFullVer2ViewRes);

        return response.toPact();
    }
}
