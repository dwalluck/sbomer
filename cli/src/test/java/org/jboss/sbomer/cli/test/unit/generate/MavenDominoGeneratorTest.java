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
package org.jboss.sbomer.cli.test.unit.generate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.jboss.sbomer.cli.feature.sbom.generate.MavenDominoGenerator;
import org.jboss.sbomer.cli.feature.sbom.generate.ProcessRunner;
import org.jboss.sbomer.core.errors.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

class MavenDominoGeneratorTest {

    final Path dominoDir = Path.of("/path/to/domino/dir");
    final Path workDir = Path.of("work/dir");

    @Test
    void testFailedWhenNoDominoDirProvided(@TempDir Path projectDir) {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> MavenDominoGenerator.builder().withDominoDir(null).build().run(projectDir));

        assertEquals("Domino validation failed", thrown.getLocalizedMessage());
        assertEquals(1, thrown.getErrors().size());
        assertEquals("No Domino directory provided", thrown.getErrors().get(0));
    }

    @Test
    void testFailedWhenDominoDirDoesntExist(@TempDir Path projectDir) {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> MavenDominoGenerator.builder().withDominoDir(Path.of("some/dir")).build().run(projectDir));

        assertEquals("Domino validation failed", thrown.getLocalizedMessage());
        assertEquals(1, thrown.getErrors().size());
        assertEquals(
                "Provided domino directory '" + "some/dir".replace('/', File.separatorChar) + "' doesn't exist",
                thrown.getErrors().get(0));
    }

    @Test
    void testFailedWhenDominoDoesntExistForDefaultVersion(@TempDir Path projectDir, @TempDir Path wrongDir) {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> MavenDominoGenerator.builder().withDominoDir(wrongDir).build().run(projectDir));

        assertEquals("Domino validation failed", thrown.getLocalizedMessage());
        assertEquals(1, thrown.getErrors().size());
        assertEquals(
                String.format("Domino could not be found on path '%s%cdomino.jar'", wrongDir, File.separatorChar),
                thrown.getErrors().get(0));
    }

    @Test
    void testFailedWhenDominoDirDoesntExistWithCustomVersion(@TempDir Path projectDir, @TempDir Path dominoDir) {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> MavenDominoGenerator.builder()
                        .withDominoDir(dominoDir)
                        .withDominoVersion("1.2.3")
                        .build()
                        .run(projectDir));

        assertEquals("Domino validation failed", thrown.getLocalizedMessage());
        assertEquals(1, thrown.getErrors().size());
        assertEquals(
                String.format(
                        "Domino could not be found on path '%s%cdomino-1.2.3.jar'",
                        dominoDir,
                        File.separatorChar),
                thrown.getErrors().get(0));
    }

    @Test
    void testGenerate() {
        MavenDominoGenerator generator = MavenDominoGenerator.builder().withDominoDir(dominoDir).build();

        List<String> cmd = generate(generator, dominoDir, workDir);

        assertThat(
                cmd,
                contains(
                        "java",
                        "-XX:InitialRAMPercentage=75.0",
                        "-XX:MaxRAMPercentage=75.0",
                        "-XX:+ExitOnOutOfMemoryError",
                        "-XX:+PrintCommandLineFlags",
                        "-XshowSettings:vm",
                        "-Dquarkus.args=\"\"",
                        "-jar",
                        "/path/to/domino/dir/domino.jar".replace('/', File.separatorChar),
                        "report",
                        "--project-dir=" + "work/dir".replace('/', File.separatorChar),
                        "--output-file=bom.json",
                        "--manifest"));
    }

    @Test
    void testGenerateWithSettingsXml() {
        var settingsXmlPath = Path.of("settings.xml");

        MavenDominoGenerator generator = MavenDominoGenerator.builder()
                .withDominoDir(dominoDir)
                .withSettingsXmlPath(settingsXmlPath)
                .build();

        List<String> cmd = generate(generator, dominoDir, workDir);

        assertThat(
                cmd,
                contains(
                        "java",
                        "-XX:InitialRAMPercentage=75.0",
                        "-XX:MaxRAMPercentage=75.0",
                        "-XX:+ExitOnOutOfMemoryError",
                        "-XX:+PrintCommandLineFlags",
                        "-XshowSettings:vm",
                        "-Dquarkus.args=\"\"",
                        "-jar",
                        "/path/to/domino/dir/domino.jar".replace('/', File.separatorChar),
                        "report",
                        "--project-dir=" + "work/dir".replace('/', File.separatorChar),
                        "--output-file=bom.json",
                        "--manifest",
                        "-s",
                        "settings.xml"));
    }

    @Test
    void testGenerateWithCustomArgs() {
        MavenDominoGenerator generator = MavenDominoGenerator.builder().withDominoDir(dominoDir).build();

        List<String> cmd = generate(generator, dominoDir, workDir, "one-arg", "1", "--test");

        assertThat(
                cmd,
                contains(
                        "java",
                        "-XX:InitialRAMPercentage=75.0",
                        "-XX:MaxRAMPercentage=75.0",
                        "-XX:+ExitOnOutOfMemoryError",
                        "-XX:+PrintCommandLineFlags",
                        "-XshowSettings:vm",
                        "-Dquarkus.args=\"\"",
                        "-jar",
                        "/path/to/domino/dir/domino.jar".replace('/', File.separatorChar),
                        "report",
                        "--project-dir=" + "work/dir".replace('/', File.separatorChar),
                        "--output-file=bom.json",
                        "--manifest",
                        "one-arg",
                        "1",
                        "--test"));
    }

    @Test
    void testGenerateWithEmptyCustomArgs() {
        MavenDominoGenerator generator = MavenDominoGenerator.builder().withDominoDir(dominoDir).build();

        List<String> cmd = generate(generator, dominoDir, workDir, "");

        assertThat(
                cmd,
                contains(
                        "java",
                        "-XX:InitialRAMPercentage=75.0",
                        "-XX:MaxRAMPercentage=75.0",
                        "-XX:+ExitOnOutOfMemoryError",
                        "-XX:+PrintCommandLineFlags",
                        "-XshowSettings:vm",
                        "-Dquarkus.args=\"\"",
                        "-jar",
                        "/path/to/domino/dir/domino.jar".replace('/', File.separatorChar),
                        "report",
                        "--project-dir=" + "work/dir".replace('/', File.separatorChar),
                        "--output-file=bom.json",
                        "--manifest"));
    }

    private List<String> generate(MavenDominoGenerator generator, Path dominoDir, Path workDir, String... args) {
        ArgumentCaptor<Path> workDirCaptor = ArgumentCaptor.forClass(Path.class);
        ArgumentCaptor<String[]> commandCaptor = ArgumentCaptor.forClass(String[].class);

        try (MockedStatic<ProcessRunner> runnerMock = Mockito.mockStatic(ProcessRunner.class)) {
            runnerMock.when(() -> ProcessRunner.run(workDirCaptor.capture(), commandCaptor.capture()))
                    .thenAnswer((Answer<Void>) invocation -> null);

            try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
                filesMock.when(() -> Files.exists(workDir)).thenReturn(true);
                filesMock.when(() -> Files.isDirectory(workDir)).thenReturn(true);

                filesMock.when(() -> Files.exists(dominoDir)).thenReturn(true);
                filesMock.when(() -> Files.exists(Path.of(dominoDir.toString(), "domino.jar"))).thenReturn(true);

                var outputPath = generator.run(workDir, args);

                assertEquals(Path.of("work/dir/bom.json"), outputPath);
            }

        }

        assertEquals(workDir, workDirCaptor.getValue());

        return Arrays.asList(commandCaptor.getValue());
    }
}
