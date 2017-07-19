package it.unibz.inf.ontop.owlrefplatform.core.mappingprocessing;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.impl.VariableDefinitionExtractorImpl;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.impl.PredicateImpl;
import it.unibz.inf.ontop.model.predicate.AtomPredicate;
import it.unibz.inf.ontop.model.predicate.Predicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATA_FACTORY;

/**
 * Partially refactored, in order to support some unconventional mapping assertions.
 * <p>
 * TODO: Make it more robust: support ternary predicates
 */
public class MappingSameAsPredicateExtractor {

    private final Mapping mapping;
    private final ImmutableSet<ImmutableTerm> sameAsMappingIRIs;
    private Set<Predicate> subjectOnlySameAsRewritingTargets;
    private Set<Predicate> twoArgumentsSameAsRewritingTargets;
    private final AtomPredicate sameAsAtomPredicate = DATA_FACTORY.getAtomPredicate(DATA_FACTORY.getOWLSameAsPredicate());

    public MappingSameAsPredicateExtractor(Mapping mapping) throws IllegalArgumentException {

        this.mapping = mapping;

        subjectOnlySameAsRewritingTargets = new HashSet<>();
        twoArgumentsSameAsRewritingTargets = new HashSet<>();

        sameAsMappingIRIs = retrieveSameAsMappingsURIs();

        retrievePredicatesWithSameAs();
    }


    public boolean isSubjectOnlySameAsRewritingTarget(Predicate pred) {
        return subjectOnlySameAsRewritingTargets.contains(pred);
    }

    public boolean isTwoArgumentsSameAsRewritingTarget(Predicate pred) {
        return twoArgumentsSameAsRewritingTargets.contains(pred);
    }

    /**
     * method that gets the uris from the mappings of same as and store it in a map
     */
    private ImmutableSet<ImmutableTerm> retrieveSameAsMappingsURIs() {

        Optional<IntermediateQuery> definition = mapping.getDefinition(sameAsAtomPredicate);
        return definition.isPresent() ?
                getIRIs(definition.get()) :
                ImmutableSet.of();
    }

    private ImmutableSet<ImmutableTerm> getIRIs(IntermediateQuery definition) {
        return getIRIs(definition.getRootConstructionNode(), definition)
                .collect(ImmutableCollectors.toSet());
    }

    /**
     * Recursive
     */
    private Stream<ImmutableTerm> getIRIs(QueryNode currentNode, IntermediateQuery query) {
        return Stream.concat(
                query.getChildren(currentNode).stream()
                        .flatMap(n -> getIRIs(
                                n,
                                query
                        )),
                extractIRIs(currentNode)
        );
    }

    /**
     * Extract the IRIs from a construction node, searching through its bindings and getting only the IRI
     */
    private Stream<ImmutableTerm> extractIRIs(QueryNode currentNode) {

        if (currentNode instanceof ConstructionNode) {
            ConstructionNode constructionNode = (ConstructionNode) currentNode;
            ImmutableCollection<ImmutableTerm> localBindings = constructionNode.getSubstitution()
                    .getImmutableMap().values();

            return localBindings.stream().map(v -> ((ImmutableFunctionalTerm) v).getTerm(0))
                    //filter out the variables
                    .filter(v -> v instanceof ValueConstant);
        }
        return Stream.of();
    }


    /**
     * Get class data and object predicate that could refer to a same as
     */
    private void retrievePredicatesWithSameAs() {

        mapping.getPredicates().stream()
                .filter(p -> !(p.equals(sameAsAtomPredicate)) &&
                        !p.getName().equals(PredicateImpl.QUEST_TRIPLE_PRED.getName()))
                .forEach(p -> categorizePredicate(p));
    }

    private void categorizePredicate(AtomPredicate pred) {

        IntermediateQuery definition = mapping.getDefinition(pred)
                .orElseThrow(() -> new IllegalStateException("The mapping contains a predicate without a definition " +
                        "(-> inconsistent)"));
        if (getIRIs(definition).stream().anyMatch(i -> sameAsMappingIRIs.contains(i))) {
            ImmutableSet<Variable> projectedVariables = definition.getProjectionAtom().getVariables();

                    /* If all projected variables may return URIs */
            if (projectedVariables.size() == 2 &&
                    projectedVariables.stream()
                            .allMatch(v -> isURIValued(v, definition))) {
                twoArgumentsSameAsRewritingTargets.add(pred);
            } else {
                    /* Otherwise, the subject only may return a URI */
                subjectOnlySameAsRewritingTargets.add(pred);
            }
        }
    }

    private boolean isURIValued(Variable variable, IntermediateQuery definition) {
        return new VariableDefinitionExtractorImpl().extract(variable, definition).stream()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .anyMatch(t -> ((ImmutableFunctionalTerm) t).isDataFunction());
    }
}

