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
import org.qubership.atp.tdm.env.configurator.api.dto.project.*;
import org.qubership.atp.tdm.env.configurator.service.client.ProjectEnvironmentFeignClient;

@RunWith(SpringRunner.class)
@EnableFeignClients(clients = {ProjectEnvironmentFeignClient.class})
@ContextConfiguration(classes = {ProjectEnvironmentFeignClientPactUnitTest.TestApp.class})
@Import({JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class, FeignConfiguration.class,
        FeignAutoConfiguration.class})
@TestPropertySource(
        properties = {"feign.atp.environments.name=atp-environments", "feign.atp.environments.route=", "feign.atp.environments.url=http://localhost:8888"})
public class ProjectEnvironmentFeignClientPactUnitTest {

    @Configuration
    public static class TestApp {

    }

    @Autowired
    ProjectEnvironmentFeignClient projectEnvFeignClient;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("atp-environments", "localhost", 8888, this);

    @Test
    @PactVerification()
    public void allPass() {
        UUID projectId = UUID.fromString("7c9dafe9-2cd1-4ffc-ae54-45867f2b9701");

        ResponseEntity<List<ProjectFullVer2ViewDto>> result1 = projectEnvFeignClient.getAllProjects(null, false);
        Assert.assertEquals(result1.getStatusCode().value(), 200);
        Assert.assertTrue(result1.getHeaders().get("Content-Type").contains("application/json"));

        ResponseEntity<ProjectFullVer1ViewDto> result2 = projectEnvFeignClient.getProject(projectId, true);
        Assert.assertEquals(result2.getStatusCode().value(), 200);
        Assert.assertTrue(result2.getHeaders().get("Content-Type").contains("application/json"));

        ResponseEntity<List<EnvironmentResDto>> result3 = projectEnvFeignClient.getEnvironments(projectId, false);
        Assert.assertEquals(result3.getStatusCode().value(), 200);
        Assert.assertTrue(result3.getHeaders().get("Content-Type").contains("application/json"));

        ResponseEntity<List<SystemEnvironmentsViewDto>> result4 = projectEnvFeignClient.getAllShortSystemsOnProject(projectId);
        Assert.assertEquals(result4.getStatusCode().value(), 200);
        Assert.assertTrue(result4.getHeaders().get("Content-Type").contains("application/json"));

        ResponseEntity<List<SystemFullVer2ViewDto>> result5 = projectEnvFeignClient.getProjectSystems(projectId, null, false);
        Assert.assertEquals(result5.getStatusCode().value(), 200);
        Assert.assertTrue(result5.getHeaders().get("Content-Type").contains("application/json"));
    }

    @Pact(consumer = "atp-tdm")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        DslPart object = new PactDslJsonBody()
                .integerType("created")
                .uuid("createdBy")
                .stringType("description")
                .uuid("id")
                .integerType("modified")
                .uuid("modifiedBy")
                .stringType("name")
                .stringType("shortName")
                .array("environments").object().closeArray();

        DslPart projectFullVer2Res = new PactDslJsonArray().template(object);


        DslPart projectFullVer1Res = new PactDslJsonBody()
                .integerType("created")
                .uuid("createdBy")
                .stringType("description")
                .uuid("id")
                .integerType("modified")
                .uuid("modifiedBy")
                .stringType("name")
                .stringType("shortName")
                .array("environments").object().closeArray();

        DslPart object1 = new PactDslJsonBody()
                .integerType("created")
                .uuid("createdBy")
                .stringType("description")
                .uuid("id")
                .integerType("modified")
                .uuid("modifiedBy")
                .stringType("name")
                .stringType("graylogName")
                .uuid("projectId")
                .array("systems").object().closeArray();

        DslPart environmentRes = new PactDslJsonArray().template(object1);

        DslPart object2 = new PactDslJsonBody()
                .stringType("name")
                .uuid("id")
                .array("environmentIds").object().closeArray();

        DslPart systemEnvironmentsViewDtoList = new PactDslJsonArray().template(object2);

        DslPart object3 = new PactDslJsonBody()
                .integerType("created")
                .uuid("createdBy")
                .integerType("dateOfCheckVersion")
                .integerType("dateOfLastCheck")
                .stringType("description")
                .uuid("externalId")
                .stringType("externalName")
                .uuid("id")
                .uuid("linkToSystemId")
                .booleanType("mergeByName")
                .integerType("modified")
                .uuid("modifiedBy")
                .stringType("name")
                .uuid("parentSystemId")
                .stringType("status", SystemFullVer1ViewDto.StatusEnum.FAIL.toString())
                .object("serverITF")
                .object("parametersGettingVersion").closeObject()
                .array("environments").object().closeArray()
                .array("connections").object().closeArray();

        DslPart systemFullVer2ViewDto = new PactDslJsonArray().template(object3);

        PactDslResponse response = builder
                .given("all ok")
                .uponReceiving("GET /api/projects OK")
                .path("/api/projects")
                .matchQuery("full", "true|false", "false")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(projectFullVer2Res)

                .given("all ok")
                .uponReceiving("GET /api/projects/{projectId} OK")
                .path("/api/projects/7c9dafe9-2cd1-4ffc-ae54-45867f2b9701")
                .matchQuery("full", "true|false", "false")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(projectFullVer1Res)

                .given("all ok")
                .uponReceiving("GET /api/projects/{projectId}/environments OK")
                .path("/api/projects/7c9dafe9-2cd1-4ffc-ae54-45867f2b9701/environments")
                .matchQuery("full", "true|false", "false")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(environmentRes)

                .given("all ok")
                .uponReceiving("GET /api/projects/{projectId}/environments/systems/short OK")
                .path("/api/projects/7c9dafe9-2cd1-4ffc-ae54-45867f2b9701/environments/systems/short")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(systemEnvironmentsViewDtoList)

                .given("all ok")
                .uponReceiving("GET /api/projects/{projectId}/environments/systems?category=category&full=false OK")
                .path("/api/projects/7c9dafe9-2cd1-4ffc-ae54-45867f2b9701/environments/systems")
                .query("category=category")
                .query("full=false")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(systemFullVer2ViewDto)

                ;

        return response.toPact();
    }
}
