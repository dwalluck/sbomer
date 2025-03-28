/*
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
package org.jboss.sbomer.service.test.integ.feature.sbom.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.jboss.sbomer.core.SchemaValidator.ValidationResult;
import org.jboss.sbomer.core.features.sbom.enums.GenerationRequestType;
import org.jboss.sbomer.service.feature.sbom.config.features.ProductConfig;
import org.jboss.sbomer.service.feature.sbom.config.features.ProductConfig.ErrataProductConfig;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.GenerationFinishedMessageBodyValidator;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Build;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Build.BuildSystem;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.GenerationFinishedMessageBody;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Operation;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Sbom;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Sbom.Bom;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Sbom.BomFormat;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Sbom.OperationGenerationRequest;
import org.jboss.sbomer.service.feature.sbom.features.umb.producer.model.Sbom.PncBuildGenerationRequest;
import org.jboss.sbomer.service.test.utils.umb.TestUmbProfile;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(TestUmbProfile.class)
class GenerationFinishedMessageBodyTest {

    @Inject
    GenerationFinishedMessageBodyValidator validator;

    private GenerationFinishedMessageBody genValidOperationMessageBody() {
        Operation operation = Operation.builder()
                .id("AWY5AHXBLAQAA")
                .buildSystem(Operation.BuildSystem.PNC)
                .link("http://orch.psi.redhat.com/pnc-rest/v2/operations/deliverable-analyzer/AWY5AHXBLAQAA")
                .deliverable("http://my-first-deliverable.zip")
                .build();

        return GenerationFinishedMessageBody.builder()
                .purl("as")
                .productConfig(
                        ProductConfig.builder()
                                .errataTool(
                                        ErrataProductConfig.builder()
                                                .productName("BLAH")
                                                .productVersion("AABB")
                                                .productVariant("DDDD")
                                                .build())
                                .build())
                .sbom(
                        Sbom.builder()
                                .id("429305915731435500")
                                .link("https://sbomer/api/v1alpha3/sboms/429305915731435500")
                                .bom(
                                        Bom.builder()
                                                .format(BomFormat.CYCLONEDX)
                                                .version("1.6")
                                                .link("https://sbomer/api/v1alpha3/sboms/429305915731435500/bom")
                                                .build())
                                .generationRequest(
                                        OperationGenerationRequest.builder()
                                                .id("AAA")
                                                .type(GenerationRequestType.OPERATION)
                                                .operation(operation)
                                                .build())
                                .build())
                .operation(operation)
                .build();
    }

    private GenerationFinishedMessageBody genValidMessageBody() {
        Build build = Build.builder()
                .id("AWY5AHXBLAQAA")
                .buildSystem(BuildSystem.PNC)
                .link("https://orch.psi.redhat.com/pnc-rest/v2/builds/AWY5AHXBLAQAA")
                .build();

        return GenerationFinishedMessageBody.builder()
                .purl("as")
                .productConfig(
                        ProductConfig.builder()
                                .errataTool(
                                        ErrataProductConfig.builder()
                                                .productName("BLAH")
                                                .productVersion("AABB")
                                                .productVariant("DDDD")
                                                .build())
                                .build())
                .sbom(
                        Sbom.builder()
                                .id("429305915731435500")
                                .link("https://sbomer/api/v1alpha3/sboms/429305915731435500")
                                .bom(
                                        Bom.builder()
                                                .format(BomFormat.CYCLONEDX)
                                                .version("1.6")
                                                .link("https://sbomer/api/v1alpha3/sboms/429305915731435500/bom")
                                                .build())
                                .generationRequest(
                                        PncBuildGenerationRequest.builder()
                                                .id("AAA")
                                                .type(GenerationRequestType.BUILD)
                                                .build(build)
                                                .build())
                                .build())
                .build(build)
                .build();
    }

    @Test
    void testValidityOfEmptyObject() {
        GenerationFinishedMessageBody message = GenerationFinishedMessageBody.builder().build();
        ValidationResult result = validator.validate(message);

        assertEquals(3, result.getErrors().size());
        assertFalse(result.isValid());

        MatcherAssert.assertThat(
                result.getErrors(),
                CoreMatchers.hasItems(
                        "#: Instance does not have required property \"purl\"",
                        "#: Instance does not have required property \"productConfig\"",
                        "#: Instance does not have required property \"sbom\""));

    }

    @Test
    void testJustWithPurl() {
        GenerationFinishedMessageBody message = GenerationFinishedMessageBody.builder().purl("as").build();
        ValidationResult result = validator.validate(message);

        assertEquals(2, result.getErrors().size());
        assertFalse(result.isValid());

        MatcherAssert.assertThat(
                result.getErrors(),
                CoreMatchers.hasItems(
                        "#: Instance does not have required property \"productConfig\"",
                        "#: Instance does not have required property \"sbom\""));
    }

    @Test
    void testValidBuild() {
        GenerationFinishedMessageBody message = genValidMessageBody();
        ValidationResult result = validator.validate(message);

        assertEquals(Collections.emptyList(), result.getErrors());
        assertTrue(result.isValid());
    }

    @Test
    void testValidOperation() {
        GenerationFinishedMessageBody message = genValidOperationMessageBody();
        ValidationResult result = validator.validate(message);

        assertEquals(Collections.emptyList(), result.getErrors());
        assertTrue(result.isValid());
    }

    @Test
    void testPartialErrataPoductConfig() {
        GenerationFinishedMessageBody message = genValidMessageBody();
        message.getProductConfig().getErrataTool().setProductVersion(null);

        ValidationResult result = validator.validate(message);

        assertFalse(result.isValid());

        MatcherAssert.assertThat(
                result.getErrors(),

                CoreMatchers.hasItems(
                        "#/productConfig: Property \"productConfig\" does not match schema",
                        "#/productConfig: Property \"productConfig\" does not match additional properties schema",
                        "#/productConfig/errataTool: Property \"errataTool\" does not match schema",
                        "#/productConfig/errataTool: Instance does not have required property \"productVersion\""));
    }

    @Test
    void testSbomMissingLink() {
        GenerationFinishedMessageBody message = genValidMessageBody();
        message.getSbom().setLink(null);

        ValidationResult result = validator.validate(message);

        assertFalse(result.isValid());

        MatcherAssert.assertThat(
                result.getErrors(),

                CoreMatchers.hasItems(
                        "#/sbom: Property \"sbom\" does not match schema",
                        "#/sbom: Property \"sbom\" does not match additional properties schema",
                        "#/sbom: Instance does not have required property \"link\""));
    }
}
