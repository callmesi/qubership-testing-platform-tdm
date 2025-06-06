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
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

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

import au.com.dius.pact.consumer.dsl.*;
import au.com.dius.pact.consumer.junit.PactProviderRule;
import au.com.dius.pact.consumer.junit.PactVerification;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.qubership.atp.auth.springbootstarter.config.FeignConfiguration;
import org.qubership.atp.tdm.env.configurator.api.dto.project.SystemFullVer1ViewDto;
import org.qubership.atp.tdm.env.configurator.service.client.SystemEnvironmentFeignClient;

@RunWith(SpringRunner.class)
@EnableFeignClients(clients = {SystemEnvironmentFeignClient.class})
@ContextConfiguration(classes = {SystemEnvironmentFeignClientPactUnitTest.TestApp.class})
@Import({JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class, FeignConfiguration.class,
        FeignAutoConfiguration.class})
@TestPropertySource(
        properties = {"feign.atp.environments.name=atp-environments", "feign.atp.environments.route=", "feign.atp.environments.url=http://localhost:8888"})
public class SystemEnvironmentFeignClientPactUnitTest {

    @Configuration
    public static class TestApp {

    }

    @Autowired
    SystemEnvironmentFeignClient systemEnvFeignClient;

    @Rule
    public PactProviderRule mockProvider = new PactProviderRule("atp-environments", "localhost", 8888, this);

    @Test
    @PactVerification()
    public void allPass() {
        UUID id = UUID.fromString("7c9dafe9-2cd1-4ffc-ae54-45867f2b9701");

        ResponseEntity<SystemFullVer1ViewDto> result1 = systemEnvFeignClient.getSystem(id, true);
        Assert.assertEquals(result1.getStatusCode().value(), 200);
        Assert.assertTrue(result1.getHeaders().get("Content-Type").contains("application/json"));


    }

    @Pact(consumer = "atp-tdm")
    public RequestResponsePact createPact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        DslPart object = new PactDslJsonBody()
                .integerType("created")
                .integerType("dateOfCheckVersion")
                .integerType("dateOfLastCheck")
                .integerType("modified")
                .uuid("createdBy")
                .uuid("externalId")
                .uuid("id")
                .uuid("linkToSystemId")
                .uuid("modifiedBy")
                .uuid("parentSystemId")
                .stringType("description")
                .stringType("externalName")
                .stringType("name")
                .booleanType("mergeByName")
                .object("parametersGettingVersion").closeObject()
                .object("serverITF").closeObject()
                .array("environmentIds").object().closeArray()
                .array("connections").object().closeArray();

        PactDslResponse response = builder
                .given("all ok")
                .uponReceiving("GET /api/systems/{systemId} OK")
                .path("/api/systems/7c9dafe9-2cd1-4ffc-ae54-45867f2b9701")
                .matchQuery("full", "true|false", "true")
                .method("GET")
                .willRespondWith()
                .status(200)
                .headers(headers)
                .body(object)
                ;

        return response.toPact();
    }
}
