package it.unibz.inf.ontop.answering.reformulation.rewriting.impl;

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
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.tools.VariableDefinitionExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.ValueConstant;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 19/07/2017: partially refactored, in order to support some (unconventional) mapping assertions.
 * <p>
 * TODO: Make it more robust: support ternary predicates
 * TODO: Merge it with the SameAsRewriter ?
 */
public class MappingSameAsPredicateExtractorImpl implements MappingSameAsPredicateExtractor{

    private final VariableDefinitionExtractor definitionExtractor;
    private final AtomPredicate sameAsAtomPredicate;

    public class ResultImpl implements Result {
        private final ImmutableSet<Predicate> subjectOnlySameAsRewritingTargets;
        private final ImmutableSet<Predicate> twoArgumentsSameAsRewritingTargets;

        public ResultImpl(ImmutableSet<Predicate> subjectOnlySameAsRewritingTargets, ImmutableSet<Predicate>
                twoArgumentsSameAsRewritingTargets) {
            this.subjectOnlySameAsRewritingTargets = subjectOnlySameAsRewritingTargets;
            this.twoArgumentsSameAsRewritingTargets = twoArgumentsSameAsRewritingTargets;
        }

        @Override
        public boolean isSubjectOnlySameAsRewritingTarget(Predicate pred) {
            return subjectOnlySameAsRewritingTargets.contains(pred);
        }

        @Override
        public boolean isTwoArgumentsSameAsRewritingTarget(Predicate pred) {
            return twoArgumentsSameAsRewritingTargets.contains(pred);
        }
    }

    @Inject
    public MappingSameAsPredicateExtractorImpl(VariableDefinitionExtractor definitionExtractor,
                                               AtomFactory atomFactory) throws IllegalArgumentException {
        this.definitionExtractor = definitionExtractor;
        this.sameAsAtomPredicate = atomFactory.getAtomPredicate(atomFactory.getOWLSameAsPredicate());
    }

    @Override
    public Result extract(Mapping mapping) {

        ImmutableSet<ImmutableTerm> sameAsMappingIRIs = retrieveSameAsMappingsURIs(mapping);
        return extractPredicates(sameAsMappingIRIs, mapping);
    }


    private ImmutableSet<ImmutableTerm> retrieveSameAsMappingsURIs(Mapping mapping) {

        Optional<IntermediateQuery> definition = mapping.getDefinition(sameAsAtomPredicate);
        return definition.isPresent() ?
                getIRIs(definition.get()) :
                ImmutableSet.of();
    }

    private ImmutableSet<ImmutableTerm> getIRIs(IntermediateQuery definition) {
        return getIRIs(definition.getRootNode(), definition)
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


    private Result extractPredicates(ImmutableSet<ImmutableTerm> sameAsMappingIRIs, Mapping mapping) {

        ImmutableMultimap<Boolean, Predicate> category2TargetPred = mapping.getPredicates().stream()
                .filter(p -> !(p.equals(sameAsAtomPredicate)))
                .filter(p -> !p.isTriplePredicate())
                .filter(p -> isRewritingTarget(p, mapping, sameAsMappingIRIs))
                .collect(ImmutableCollectors.toMultimap(
                        p -> isSubjectOnlyRewritingTarget(mapping, p),
                        p -> p
                ));
        return new ResultImpl(
                ImmutableSet.copyOf(category2TargetPred.get(true)),
                ImmutableSet.copyOf(category2TargetPred.get(false))
        );
    }

    private boolean isRewritingTarget(AtomPredicate pred, Mapping mapping, ImmutableSet<ImmutableTerm> sameAsMappingIRIs) {
        IntermediateQuery definition = mapping.getDefinition(pred)
                .orElseThrow(() -> new IllegalStateException("The mapping contains a predicate without a definition " +
                        "(-> inconsistent)"));
        return getIRIs(definition).stream()
                .anyMatch(i -> sameAsMappingIRIs.contains(i));
    }

    private boolean isSubjectOnlyRewritingTarget(Mapping mapping, AtomPredicate pred) {
        IntermediateQuery definition = mapping.getDefinition(pred)
                .orElseThrow(() -> new IllegalStateException("The mapping contains a predicate without a definition " +
                        "(-> inconsistent)"));

        ImmutableSet<Variable> projectedVariables = definition.getProjectionAtom().getVariables();

        /* If all projected variables may return URIs */
        if (projectedVariables.size() == 2 &&
                projectedVariables.stream()
                        .allMatch(v -> isURIValued(v, definition))) {
            return false;
        }
        /* Otherwise, the subject only may return a URI */
        return true;
    }

    private boolean isURIValued(Variable variable, IntermediateQuery definition) {
        return definitionExtractor.extract(variable, definition).stream()
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .anyMatch(t -> ((ImmutableFunctionalTerm) t).isDataFunction());
    }
}

