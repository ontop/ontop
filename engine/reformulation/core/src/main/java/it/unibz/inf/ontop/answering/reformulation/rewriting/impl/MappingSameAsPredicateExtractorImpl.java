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

import com.google.common.collect.*;
import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.tools.VariableDefinitionExtractor;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.Constant;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * TODO: Merge it with the SameAsRewriter ?
 */
public class MappingSameAsPredicateExtractorImpl implements MappingSameAsPredicateExtractor {

    private final VariableDefinitionExtractor definitionExtractor;
    public class SameAsTargetsImpl implements SameAsTargets {
        private final ImmutableSet<IRI> subjectOnlySameAsRewritingTargets;
        private final ImmutableSet<IRI> twoArgumentsSameAsRewritingTargets;

        public SameAsTargetsImpl(ImmutableSet<IRI> subjectOnlySameAsRewritingTargets, ImmutableSet<IRI>
                twoArgumentsSameAsRewritingTargets) {
            this.subjectOnlySameAsRewritingTargets = subjectOnlySameAsRewritingTargets;
            this.twoArgumentsSameAsRewritingTargets = twoArgumentsSameAsRewritingTargets;
        }

        @Override
        public boolean isSubjectOnlySameAsRewritingTarget(IRI pred) {
            return subjectOnlySameAsRewritingTargets.contains(pred);
        }

        @Override
        public boolean isTwoArgumentsSameAsRewritingTarget(IRI pred) {
            return twoArgumentsSameAsRewritingTargets.contains(pred);
        }
    }

    @Inject
    public MappingSameAsPredicateExtractorImpl(VariableDefinitionExtractor definitionExtractor) throws IllegalArgumentException {
        this.definitionExtractor = definitionExtractor;
    }

    /**
     * TODO: Generalise it to quads and so on
     */
    @Override
    public SameAsTargets extract(Mapping mapping) {
        Optional<RDFAtomPredicate> triplePredicate = mapping.getRDFAtomPredicates().stream()
                .filter(p -> p instanceof TriplePredicate)
                .findFirst();

        return triplePredicate
                .map(p -> {
                    ImmutableSet<Constant> iriTemplates = extractSameAsIRITemplates(mapping, p);
                    return extractSameAsTargets(iriTemplates, mapping, p);
                })
                .orElseGet(() -> new SameAsTargetsImpl(ImmutableSet.of(), ImmutableSet.of()));
    }


    private ImmutableSet<Constant> extractSameAsIRITemplates(Mapping mapping, RDFAtomPredicate rdfAtomPredicate) {

        Optional<IQ> definition = mapping.getRDFPropertyDefinition(rdfAtomPredicate, OWL.SAME_AS);
        return definition
                    .map(this::extractIRITemplates)
                    .orElseGet(ImmutableSet::of);
    }

    private ImmutableSet<Constant> extractIRITemplates(IQ definition) {
        return Optional.of(definition.getProjectionAtom().getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> (RDFAtomPredicate) p)
                .map(p -> extractIRITemplates(p, definition.getProjectionAtom().getArguments(), definition.getTree()))
                .orElseGet(ImmutableSet::of);
    }

    private ImmutableSet<Constant> extractIRITemplates(RDFAtomPredicate predicate, ImmutableList<Variable> projectedVariables,
                                                       IQTree tree) {
        return Stream.of(predicate.getSubject(projectedVariables), predicate.getObject(projectedVariables))
                .flatMap(v -> extractIRITemplates(v, tree))
                .collect(ImmutableCollectors.toSet());
    }


    private Stream<Constant> extractIRITemplates(Variable variable, IQTree tree) {
        return definitionExtractor.extract(variable, tree).stream()
                .map(this::tryToExtractIRITemplateString)
                .filter(t -> t instanceof Constant)
                .map(t -> (Constant) t);
    }


    /**
     * TODO: refactor this weak code!!!
     */
    private ImmutableTerm tryToExtractIRITemplateString(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            return functionalTerm.getArity() > 0
                    ? tryToExtractIRITemplateString(functionalTerm.getTerm(0))
                    : term;
        }
        return term;
    }

    private SameAsTargets extractSameAsTargets(ImmutableSet<Constant> sameAsIriTemplates, Mapping mapping,
                                               RDFAtomPredicate rdfAtomPredicate) {
        if (sameAsIriTemplates.isEmpty())
            return new SameAsTargetsImpl(ImmutableSet.of(), ImmutableSet.of());

        Stream<IRI> classIris = mapping.getRDFClasses(rdfAtomPredicate).stream()
                .filter(classIri -> classify(sameAsIriTemplates,
                                mapping.getRDFClassDefinition(rdfAtomPredicate, classIri).get(),
                                rdfAtomPredicate, true)
                        == PredicateClassification.SUBJECT_ONLY);

        ImmutableMultimap<PredicateClassification, IRI> propertyIris = mapping.getRDFProperties(rdfAtomPredicate).stream()
                .filter(iri -> !iri.equals(OWL.SAME_AS))
                .map(iri -> Maps.immutableEntry(
                        classify(sameAsIriTemplates,
                                mapping.getRDFPropertyDefinition(rdfAtomPredicate, iri).get(),
                                rdfAtomPredicate, false),
                            iri
                        ))
                .collect(ImmutableCollectors.toMultimap());

        return new SameAsTargetsImpl(
                Stream.concat(
                        classIris,
                        propertyIris.get(PredicateClassification.SUBJECT_ONLY).stream())
                .collect(ImmutableCollectors.toSet()),
                ImmutableSet.copyOf(propertyIris.get(PredicateClassification.AT_LEAST_OBJECT)));
    }

    private PredicateClassification classify(ImmutableSet<Constant> sameAsIriTemplates, IQ definition,
                                             RDFAtomPredicate rdfAtomPredicate, boolean isClass) {
        ImmutableList<Variable> variables = definition.getProjectionAtom().getArguments();

        /*
         * Current limitation: is the object is concerned about SameAs rewriting, the subject is supposed also
         * concerned.
         * TODO: enrich the classification
         */
        if (!isClass &&
            extractIRITemplates(rdfAtomPredicate.getObject(variables), definition.getTree())
                    .anyMatch(sameAsIriTemplates::contains)) {
            return PredicateClassification.AT_LEAST_OBJECT;
        }

        return extractIRITemplates(rdfAtomPredicate.getSubject(variables), definition.getTree())
                .filter(sameAsIriTemplates::contains)
                .findAny()
                .map(t -> PredicateClassification.SUBJECT_ONLY)
                .orElse(PredicateClassification.NONE);
    }

    private enum  PredicateClassification {
        NONE,
        SUBJECT_ONLY,
        AT_LEAST_OBJECT
    }

}

