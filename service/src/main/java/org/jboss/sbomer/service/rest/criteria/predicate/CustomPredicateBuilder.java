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
package org.jboss.sbomer.service.rest.criteria.predicate;

import java.util.ArrayList;
import java.util.List;

import org.jboss.sbomer.service.feature.sbom.model.Sbom;
import org.jboss.sbomer.service.feature.sbom.model.SbomGenerationRequest;
import org.jboss.sbomer.service.rest.criteria.AbstractCriteriaAwareRepository;

import com.github.tennaito.rsql.builder.BuilderTools;
import com.github.tennaito.rsql.jpa.PredicateBuilder;
import com.github.tennaito.rsql.misc.EntityManagerAdapter;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.LogicalNode;
import cz.jirutka.rsql.parser.ast.Node;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomPredicateBuilder {

    private CustomPredicateBuilder() {
        throw new IllegalStateException("This is a utility class that should not be instantiated");
    }

    public static <T> Predicate createPredicate(
            LogicalNode logical,
            From<?, ?> root,
            Class<T> entity,
            EntityManagerAdapter ema,
            BuilderTools misc) {

        log.debug("Creating Predicate for logical node: {}", logical);

        CriteriaBuilder builder = ema.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();

        for (Node node : logical.getChildren()) {

            log.debug("Creating Predicates from all children nodes.");

            if (node instanceof LogicalNode ln) {
                predicates.add(createPredicate(ln, root, entity, ema, misc));
            } else if (node instanceof ComparisonNode cn) {
                predicates.add(createPredicate(cn, root, entity, ema, misc));
            } else {
                throw new IllegalArgumentException("Unknown expression type: " + node.getClass());
            }
        }

        return switch (logical.getOperator()) {
            case AND -> builder.and(predicates.toArray(new Predicate[0]));
            case OR -> builder.or(predicates.toArray(new Predicate[0]));
        };

    }

    /**
     * Create a Predicate from the RSQL AST comparison node.
     *
     * @param comparison RSQL AST comparison node.
     * @param startRoot From that predicate expression paths depends on.
     * @param entity The main entity of the query.
     * @param entityManager JPA EntityManager.
     * @param misc Facade with all necessary tools for predicate creation.
     * @return Predicate a predicate representation of the Node.
     */
    public static <T> Predicate createPredicate(
            ComparisonNode comparison,
            From<?, ?> startRoot,
            Class<T> entity,
            EntityManagerAdapter entityManager,
            BuilderTools misc) {

        log.debug("Creating Predicate for comparison node: {}", comparison);

        if (startRoot == null) {
            String msg = "From root node was undefined.";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (startRoot.getJavaType().equals(Sbom.class) && comparison.getSelector().equals("sbom")) {
            String msg = "RSQL on field Sbom.sbom with type JsonNode is not supported!";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        if (startRoot.getJavaType().equals(SbomGenerationRequest.class) && comparison.getSelector().equals("config")) { // TODO
                                                                                                                        // change
                                                                                                                        // to
                                                                                                                        // Config
            String msg = "RSQL on field SbomGenerationRequest.config with type JsonNode is not supported!";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        // This is to make the model change backward compatible, so older buildId selectors can still be used
        if ((startRoot.getJavaType().equals(Sbom.class) || startRoot.getJavaType().equals(SbomGenerationRequest.class))
                && comparison.getSelector().equals("buildId")) {

            log.debug("Converting legacy selector 'buildId' into the new selector 'identifier'");

            comparison = comparison.withSelector("identifier");
        }

        if ((startRoot.getJavaType().equals(Sbom.class)) && comparison.getSelector().startsWith("generation.")) {
            log.debug("Ensuring new 'generation' selector works with 'generationRequest'");
            comparison = comparison.withSelector(comparison.getSelector().replace("generation.", "generationRequest."));
        }

        Path<?> propertyPath = PredicateBuilder
                .findPropertyPath(comparison.getSelector(), startRoot, entityManager, misc);

        if ((AbstractCriteriaAwareRepository.IS_NULL.equals(comparison.getOperator())
                && Enum.class.isAssignableFrom(propertyPath.getJavaType())
                || (AbstractCriteriaAwareRepository.IS_EQUAL.equals(comparison.getOperator())
                        && Enum.class.isAssignableFrom(propertyPath.getJavaType())))) {

            log.debug(
                    "Detected type '{}' for selector '{}' with custom comparison operator '{}'. Delegating to custom PredicateBuilderStrategy!",
                    propertyPath.getJavaType(),
                    comparison.getSelector(),
                    comparison.getOperator().getSymbol());

            if (misc.getPredicateBuilder() != null) {
                return misc.getPredicateBuilder().createPredicate(comparison, startRoot, entity, entityManager, misc);
            }
        }

        return PredicateBuilder.createPredicate(comparison, startRoot, entity, entityManager, misc);
    }

}
