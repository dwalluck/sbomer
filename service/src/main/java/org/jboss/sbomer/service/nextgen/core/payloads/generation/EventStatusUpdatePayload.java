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
package org.jboss.sbomer.service.nextgen.core.payloads.generation;

import org.jboss.sbomer.service.nextgen.core.enums.EventStatus;
import org.slf4j.helpers.MessageFormatter;

import jakarta.validation.constraints.NotNull;

/**
 * <p>
 * Payload used to update event status.
 * </p>
 *
 * <p>
 * This endpoint is used only by workers (generators).
 * </p>
 *
 *
 * @param status The status identifier.
 * @param reason A human-readable description of the current status.
 */
public record EventStatusUpdatePayload(@NotNull EventStatus status, String reason) {

    public static EventStatusUpdatePayload of(EventStatus status, String reason, Object... params) {
        return new EventStatusUpdatePayload(status, MessageFormatter.arrayFormat(reason, params).getMessage());
    }
}