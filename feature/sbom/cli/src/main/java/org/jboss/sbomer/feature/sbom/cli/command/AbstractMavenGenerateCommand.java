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
package org.jboss.sbomer.feature.sbom.cli.command;

import java.nio.file.Path;

import lombok.Getter;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

public abstract class AbstractMavenGenerateCommand extends AbstractGenerateCommand {

    @Getter
    @Option(
            names = { "-s", "--settings" },
            description = "Path to Maven settings.xml file that should be used for this run instead of the default one",
            converter = PathConverter.class,
            defaultValue = "${env:SBOMER_SBOM_SETTINGS_XML_PATH}",
            scope = ScopeType.INHERIT)
    Path settingsXmlPath;

}
