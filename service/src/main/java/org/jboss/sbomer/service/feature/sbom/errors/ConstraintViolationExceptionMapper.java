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
package org.jboss.sbomer.service.feature.sbom.errors;

import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper extends AbstractExceptionMapper<ConstraintViolationException> {
    @Override
    Status getStatus(ConstraintViolationException ex) {
        return Status.BAD_REQUEST;
    }

    @Override
    String errorMessage(ConstraintViolationException ex) {
        return "Invalid request provided, some constraints validations failed";
    }

    @Override
    List<String> customErrors(ConstraintViolationException ex) {
        return ex.getConstraintViolations()
                .stream()
                .map(
                        v -> String.format(
                                "%s: %s, provided: '%s'",
                                v.getPropertyPath(),
                                v.getMessage(),
                                v.getInvalidValue()))
                .toList();
    }

}
