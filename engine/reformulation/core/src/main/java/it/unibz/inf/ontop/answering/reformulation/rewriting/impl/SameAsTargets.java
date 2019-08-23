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
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
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

public class SameAsTargets {

    private final ImmutableSet<IRI> subjectOnlySameAsRewritingTargets, twoArgumentsSameAsRewritingTargets;

    public SameAsTargets(ImmutableSet<IRI> subjectOnlySameAsRewritingTargets, ImmutableSet<IRI>
            twoArgumentsSameAsRewritingTargets) {
        this.subjectOnlySameAsRewritingTargets = subjectOnlySameAsRewritingTargets;
        this.twoArgumentsSameAsRewritingTargets = twoArgumentsSameAsRewritingTargets;
    }

    public boolean isSubjectOnlySameAsRewritingTarget(IRI pred) {
        return subjectOnlySameAsRewritingTargets.contains(pred);
    }

    public boolean isTwoArgumentsSameAsRewritingTarget(IRI pred) {
        return twoArgumentsSameAsRewritingTargets.contains(pred);
    }


    /**
     * TODO: Generalise it to quads and so on
     */
    public static SameAsTargets extract(Mapping mapping) {
        Optional<RDFAtomPredicate> triplePredicate = mapping.getRDFAtomPredicates().stream()
                .filter(p -> p instanceof TriplePredicate)
                .findFirst();

        return triplePredicate
                .map(p -> {
                    ImmutableSet<Constant> iriTemplates = extractSameAsIRITemplates(mapping, p);
                    return extractSameAsTargets(iriTemplates, mapping, p);
                })
                .orElseGet(() -> new SameAsTargets(ImmutableSet.of(), ImmutableSet.of()));
    }


    private static ImmutableSet<Constant> extractSameAsIRITemplates(Mapping mapping, RDFAtomPredicate rdfAtomPredicate) {

        Optional<IQ> definition = mapping.getRDFPropertyDefinition(rdfAtomPredicate, OWL.SAME_AS);
        return definition
                    .map(SameAsTargets::extractIRITemplates)
                    .orElseGet(ImmutableSet::of);
    }

    private static ImmutableSet<Constant> extractIRITemplates(IQ definition) {
        return Optional.of(definition.getProjectionAtom().getPredicate())
                .filter(p -> p instanceof RDFAtomPredicate)
                .map(p -> extractIRITemplates((RDFAtomPredicate) p, definition))
                .orElseGet(ImmutableSet::of);
    }

    private static ImmutableSet<Constant> extractIRITemplates(RDFAtomPredicate predicate, IQ definition) {
        ImmutableList<Variable> projectedVariables = definition.getProjectionAtom().getArguments();
        return Stream.of(predicate.getSubject(projectedVariables), predicate.getObject(projectedVariables))
                .flatMap(v -> extractIRITemplates(v, definition.getTree()))
                .collect(ImmutableCollectors.toSet());
    }

    private static Stream<Constant> extractIRITemplates(Variable variable, IQTree tree) {
        return tree.getPossibleVariableDefinitions().stream()
                .map(s -> s.get(variable))
                .filter(t -> !t.equals(variable))
                .map(SameAsTargets::tryToExtractIRITemplateString)
                .filter(t -> t instanceof Constant)
                .map(t -> (Constant) t);
    }


    /**
     * TODO: refactor this weak code!!!
     */
    private static ImmutableTerm tryToExtractIRITemplateString(ImmutableTerm term) {
        if (term instanceof ImmutableFunctionalTerm) {
            ImmutableFunctionalTerm functionalTerm = (ImmutableFunctionalTerm) term;
            return functionalTerm.getArity() > 0
                    ? tryToExtractIRITemplateString(functionalTerm.getTerm(0))
                    : term;
        }
        return term;
    }

    private static SameAsTargets extractSameAsTargets(ImmutableSet<Constant> sameAsIriTemplates,
                                                      Mapping mapping,
                                                      RDFAtomPredicate rdfAtomPredicate) {
        if (sameAsIriTemplates.isEmpty())
            return new SameAsTargets(ImmutableSet.of(), ImmutableSet.of());

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

        return new SameAsTargets(
                Stream.concat(
                        classIris,
                        propertyIris.get(PredicateClassification.SUBJECT_ONLY).stream())
                .collect(ImmutableCollectors.toSet()),
                ImmutableSet.copyOf(propertyIris.get(PredicateClassification.AT_LEAST_OBJECT)));
    }

    private static PredicateClassification classify(ImmutableSet<Constant> sameAsIriTemplates, IQ definition,
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

    private enum PredicateClassification {
        NONE,
        SUBJECT_ONLY,
        AT_LEAST_OBJECT
    }
}

