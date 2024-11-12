/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.sbomer.service.test.integ.feature.sbom.generation;

import static io.restassured.RestAssured.given;

import org.hamcrest.CoreMatchers;
import org.jboss.sbomer.service.feature.sbom.service.SbomService;
import org.jboss.sbomer.service.test.utils.umb.TestUmbProfile;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.quarkus.test.kubernetes.client.WithKubernetesTestServer;
import io.restassured.http.ContentType;

@QuarkusTest
@WithKubernetesTestServer
@TestProfile(TestUmbProfile.class)
class SyftImageResourceIT {

    @InjectSpy
    SbomService sbomService;

    @Nested
    class V1Alpha3 {
        @Test
        void shouldAllowNoConfig() {
            given().when()
                    .contentType(ContentType.JSON)
                    .request("POST", "/api/v1alpha3/generator/syft/image/image-name")
                    .then()
                    .statusCode(202)
                    .body("id", CoreMatchers.any(String.class))
                    .and()
                    .body("identifier", CoreMatchers.equalTo("image-name"))
                    .and()
                    .body("type", CoreMatchers.equalTo("CONTAINERIMAGE"))
                    .and()
                    .body("status", CoreMatchers.is("NEW"));
        }

        @Test
        void shouldNotAllowEmptyConfig() {
            given().body("{}")
                    .when()
                    .contentType(ContentType.JSON)
                    .request("POST", "/api/v1alpha3/generator/syft/image/image-name")
                    .then()
                    .statusCode(400)
                    .body(
                            "message",
                            CoreMatchers.is(
                                    "An error occurred while deserializing provided content, please check your body 🤼"))
                    .and()
                    .body("errorId", CoreMatchers.isA(String.class))
                    .and()
                    .body("error", CoreMatchers.equalTo("Bad Request"));
        }

        @Test
        void shouldForbidInvalidConfig() {
            given().body("{\"type\": \"syft-image\", \"wrong\": []}")
                    .when()
                    .contentType(ContentType.JSON)
                    .request("POST", "/api/v1alpha3/generator/syft/image/image-name")
                    .then()
                    .statusCode(400)
                    .body(
                            "message",
                            CoreMatchers.is(
                                    "An error occurred while deserializing provided content, please check your body 🤼"))
                    .and()
                    .body("errorId", CoreMatchers.isA(String.class))
                    .and()
                    .body("error", CoreMatchers.equalTo("Bad Request"));
        }

        @Test
        void shouldAllowCustomConfig() {
            given().body("{\"type\": \"syft-image\", \"paths\": [ \"/opt\", \"/etc\"]}")
                    .when()
                    .contentType(ContentType.JSON)
                    .request("POST", "/api/v1alpha3/generator/syft/image/image-name")
                    .then()
                    .statusCode(202)
                    .body("id", CoreMatchers.any(String.class))
                    .and()
                    .body("identifier", CoreMatchers.equalTo("image-name"))
                    .and()
                    .body("type", CoreMatchers.equalTo("CONTAINERIMAGE"))
                    .and()
                    .body("status", CoreMatchers.is("NEW"));
        }
    }

    @Nested
    class V1Beta1 {
        @Test
        void shouldNotAllowEmptyConfig() {
            given().body("{}")
                    .when()
                    .contentType(ContentType.JSON)
                    .request("POST", "/api/v1beta1/generations")
                    .then()
                    .statusCode(400)
                    .body(
                            "message",
                            CoreMatchers.is(
                                    "An error occurred while deserializing provided content, please check your body 🤼"))
                    .and()
                    .body("errorId", CoreMatchers.isA(String.class))
                    .and()
                    .body("error", CoreMatchers.equalTo("Bad Request"));
        }

        @Test
        void shouldForbidInvalidConfig() {
            given().body("{\"type\": \"syft-image\", \"wrong\": []}")
                    .when()
                    .contentType(ContentType.JSON)
                    .request("POST", "/api/v1beta1/generations")
                    .then()
                    .statusCode(400)
                    .body(
                            "message",
                            CoreMatchers.is(
                                    "An error occurred while deserializing provided content, please check your body 🤼"))
                    .and()
                    .body("errorId", CoreMatchers.isA(String.class))
                    .and()
                    .body("error", CoreMatchers.equalTo("Bad Request"));
        }

        @Test
        void shouldAllowCustomConfig() {
            given().body("{\"type\": \"image\", \"image\": \"registry.com/image:tag\"}")
                    .when()
                    .contentType(ContentType.JSON)
                    .request("POST", "/api/v1beta1/generations")
                    .then()
                    .statusCode(202)
                    .body("size()", CoreMatchers.is(1))
                    .body("[0].id", CoreMatchers.any(String.class))
                    .and()
                    .body("[0].identifier", CoreMatchers.equalTo("registry.com/image:tag"))
                    .and()
                    .body("[0].type", CoreMatchers.equalTo("CONTAINERIMAGE"))
                    .and()
                    .body("[0].status", CoreMatchers.is("NEW"));
        }
    }
}
