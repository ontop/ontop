package it.unibz.inf.ontop.spec.mapping.transformer.impl;

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
import com.google.inject.Singleton;
import it.unibz.inf.ontop.constraints.impl.ExtensionalDataNodeListContainmentCheck;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.IRIConstant;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertion;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.spec.mapping.TMappingExclusionConfig;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingCQCOptimizer;
import it.unibz.inf.ontop.spec.mapping.transformer.MappingSaturator;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

@Singleton
public class TMappingSaturatorImpl implements MappingSaturator  {

	// TODO: the implementation of EXCLUDE ignores equivalent classes / properties

    private final TMappingExclusionConfig tMappingExclusionConfig;
	private final TermFactory termFactory;
    private final MappingCQCOptimizer mappingCqcOptimizer;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
	private TMappingSaturatorImpl(TMappingExclusionConfig tMappingExclusionConfig,
                                  MappingCQCOptimizer mappingCqcOptimizer,
                                  UnionBasedQueryMerger queryMerger,
                                  CoreSingletons coreSingletons) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
		this.termFactory = coreSingletons.getTermFactory();
        this.mappingCqcOptimizer = mappingCqcOptimizer;
        this.queryMerger = queryMerger;
        this.coreSingletons = coreSingletons;
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
        this.iqFactory = coreSingletons.getIQFactory();
    }

    @Override
    public ImmutableList<MappingAssertion> saturate(ImmutableList<MappingAssertion> mapping, ClassifiedTBox reasoner) {

        ExtensionalDataNodeListContainmentCheck cqc = new ExtensionalDataNodeListContainmentCheck(coreSingletons.getHomomorphismFactory(), coreSingletons.getCoreUtilsFactory());

	    // index mapping assertions by the predicate type
        //     same IRI can be a class name and a property name
        //     but the same IRI cannot be an object and a data or annotation property name at the same time
        // see https://www.w3.org/TR/owl2-new-features/#F12:_Punning

        ImmutableMultimap<MappingAssertionIndex, MappingAssertion> original = mapping.stream()
                .flatMap(a -> Stream.of(a.getQuery())
                        .map(IQ::normalizeForOptimization) // replaces join equalities
                        .map(q -> mappingCqcOptimizer.optimize(cqc, q))
                        .map(q -> Maps.immutableEntry(a.getIndex(), a.copyOf(q))))
                .collect(ImmutableCollectors.toMultimap());

        ImmutableMap<MappingAssertionIndex, MappingAssertion> saturated = original.keySet().stream()
                .map(MappingAssertionIndex::getPredicate)
                .distinct()
                .map(MappingAssertionConstructionNodeTransformerProvider::new)
                .flatMap(provider -> Stream.concat(Stream.concat(
                    reasoner.objectPropertiesDAG().stream()
                            .filter(node -> !node.getRepresentative().isInverse() && !tMappingExclusionConfig.contains(node.getRepresentative()))
                            .flatMap(node -> saturate(node.getRepresentative(), getSubsumees(reasoner.objectPropertiesDAG(), node), original, provider::getTransformer, cqc).stream()
                                    .flatMap(ma -> node.getMembers().stream()
                                            .filter(d -> !d.isInverse() || d.getInverse() != node.getRepresentative())
                                            .map(d -> Maps.immutableEntry(provider.getTransformer(node.getRepresentative(), d), ma)))),

                    reasoner.dataPropertiesDAG().stream()
                            .filter(node -> !tMappingExclusionConfig.contains(node.getRepresentative()))
                            .flatMap(node -> saturate(node.getRepresentative(), getSubsumees(reasoner.dataPropertiesDAG(), node), original, provider::getTransformer, cqc).stream()
                                    .flatMap(ma -> node.getMembers().stream()
                                            .map(d -> Maps.immutableEntry(provider.getTransformer(node.getRepresentative(), d), ma))))),

                    reasoner.classesDAG().stream()
                            .filter(node -> (node.getRepresentative() instanceof OClass) && !tMappingExclusionConfig.contains((OClass)node.getRepresentative()))
                            .flatMap(node -> saturate(node.getRepresentative(), getSubsumees(reasoner.classesDAG(), node), original, provider::getTransformer, cqc).stream()
                                    .flatMap(ma -> node.getMembers().stream()
                                            .filter(d -> d instanceof OClass)
                                            .map(d -> Maps.immutableEntry(provider.getTransformer(node.getRepresentative(), d), ma))))))

                .map(e -> Maps.immutableEntry(
                        e.getKey().getToIndex(), e.getKey().updateConstructionNodeIri(e.getValue())))
                .collect(ImmutableCollectors.toMap());

        return Stream.concat(
                saturated.values().stream(),
                original.asMap().entrySet().stream()
                        .filter(e -> !saturated.containsKey(e.getKey()))
                        .map(e -> e.getValue().stream()
                                        .collect(TMappingEntry.toMappingAssertion(cqc, coreSingletons, queryMerger)))
                        .map(Optional::get))
                .collect(ImmutableCollectors.toList());
    }


    private <T> Optional<MappingAssertion> saturate(T representative, Stream<T> subsumees,
                                                    ImmutableMultimap<MappingAssertionIndex, MappingAssertion> original,
                                                    BiFunction<T, T, MappingAssertionConstructionNodeTransformer> transformerProvider,
                                                    ExtensionalDataNodeListContainmentCheck cqc) {

        return subsumees
                .map(s -> transformerProvider.apply(s, representative))
                .flatMap(u -> original.get(u.getFromIndex()).stream()
                        .map(u::updateConstructionNodeIri))
                .collect(TMappingEntry.toMappingAssertion(cqc, coreSingletons, queryMerger));
    }

    private static <T> Stream<T> getSubsumees(EquivalencesDAG<T> dag, Equivalences<T> node) {
        return dag.getSub(node).stream()
                .flatMap(n -> n.getMembers().stream());
    }

    private class MappingAssertionConstructionNodeTransformer {
        private final MappingAssertionIndex fromIndex, toIndex;
        private final Function<ImmutableList<ImmutableTerm>, ImmutableList<ImmutableTerm>> termTransformer;
        MappingAssertionConstructionNodeTransformer(MappingAssertionIndex fromIndex, MappingAssertionIndex toIndex, Function<ImmutableList<ImmutableTerm>, ImmutableList<ImmutableTerm>> termTransformer) {
            this.fromIndex = fromIndex;
            this.toIndex = toIndex;
            this.termTransformer = termTransformer;
        }
        MappingAssertionIndex getFromIndex() { return fromIndex; }
        MappingAssertionIndex getToIndex() { return toIndex; }

        MappingAssertion updateConstructionNodeIri(MappingAssertion assertion) {
            IQ query = assertion.getQuery();
            ConstructionNode constructionNode = (ConstructionNode) query.getTree().getRootNode();
            DistinctVariableOnlyDataAtom projectionAtom = query.getProjectionAtom();
            ImmutableList<Variable> variables = projectionAtom.getArguments();
            ImmutableList<ImmutableTerm> args = constructionNode.getSubstitution().apply(variables);
            Substitution<ImmutableTerm> updatedSubstitution = substitutionFactory.getSubstitution(variables, termTransformer.apply(args));
            ConstructionNode updatedConstructionNode = iqFactory.createConstructionNode(constructionNode.getVariables(), updatedSubstitution);
            IQ updatedQuery = iqFactory.createIQ(projectionAtom,
                    iqFactory.createUnaryIQTree(updatedConstructionNode, ((UnaryIQTree)query.getTree()).getChild()));
            return assertion.copyOf(updatedQuery);
        }

        @Override
        public String toString() {
            return fromIndex + " -> " + toIndex;
        }
    }

    private class MappingAssertionConstructionNodeTransformerProvider {
	    private final RDFAtomPredicate rdfAtomPredicate;
        private final IRIConstant rdfType;

        MappingAssertionConstructionNodeTransformerProvider(RDFAtomPredicate rdfAtomPredicate) {
            this.rdfAtomPredicate = rdfAtomPredicate;
            this.rdfType = termFactory.getConstantIRI(RDF.TYPE);
        }

        MappingAssertionConstructionNodeTransformer getTransformer(ClassExpression from, ClassExpression to) {
            if (!(to instanceof OClass))
                throw new MinorOntopInternalBugException("Cannot get a transformer to a property restriction: " + from + " " + to);

            OClass toClass = (OClass)to;
            IRIConstant newIri = termFactory.getConstantIRI(toClass.getIRI());
            if (from instanceof OClass) {
                OClass oc = (OClass)from;
                return new MappingAssertionConstructionNodeTransformer(
                        MappingAssertionIndex.ofClass(rdfAtomPredicate, oc.getIRI()),
                        MappingAssertionIndex.ofClass(rdfAtomPredicate, toClass.getIRI()),
                        args ->  rdfAtomPredicate.updateSPO(args, rdfAtomPredicate.getSubject(args), rdfType, newIri));
            }
            else if (from instanceof ObjectSomeValuesFrom) {
                ObjectPropertyExpression ope = ((ObjectSomeValuesFrom) from).getProperty();
                return new MappingAssertionConstructionNodeTransformer(
                        MappingAssertionIndex.ofProperty(rdfAtomPredicate, ope.getIRI()),
                        MappingAssertionIndex.ofClass(rdfAtomPredicate, toClass.getIRI()),
                        ope.isInverse()
                            ? args -> rdfAtomPredicate.updateSPO(args, rdfAtomPredicate.getObject(args), rdfType, newIri)
                            : args -> rdfAtomPredicate.updateSPO(args, rdfAtomPredicate.getSubject(args), rdfType, newIri));
            }
            else if (from instanceof DataSomeValuesFrom) {
                DataPropertyExpression dpe = ((DataSomeValuesFrom) from).getProperty();
                return new MappingAssertionConstructionNodeTransformer(
                        MappingAssertionIndex.ofProperty(rdfAtomPredicate, dpe.getIRI()),
                        MappingAssertionIndex.ofClass(rdfAtomPredicate, toClass.getIRI()),
                        args -> rdfAtomPredicate.updateSPO(args, rdfAtomPredicate.getSubject(args), rdfType, newIri));
            }
            else
                throw new MinorOntopInternalBugException("Unexpected type" + from);
        }

        MappingAssertionConstructionNodeTransformer getTransformer(ObjectPropertyExpression from, ObjectPropertyExpression to) {
            IRIConstant newIri = termFactory.getConstantIRI(to.getIRI());
            return new MappingAssertionConstructionNodeTransformer(
                    MappingAssertionIndex.ofProperty(rdfAtomPredicate, from.getIRI()),
                    MappingAssertionIndex.ofProperty(rdfAtomPredicate, to.getIRI()),
                    from.isInverse() != to.isInverse()
                            ? args -> rdfAtomPredicate.updateSPO(args, rdfAtomPredicate.getObject(args), newIri, rdfAtomPredicate.getSubject(args))
                            : args -> rdfAtomPredicate.updateSPO(args, rdfAtomPredicate.getSubject(args), newIri, rdfAtomPredicate.getObject(args)));
        }

        MappingAssertionConstructionNodeTransformer getTransformer(DataPropertyExpression from, DataPropertyExpression to) {
            IRIConstant newIri = termFactory.getConstantIRI(to.getIRI());
            return new MappingAssertionConstructionNodeTransformer(
                    MappingAssertionIndex.ofProperty(rdfAtomPredicate, from.getIRI()),
                    MappingAssertionIndex.ofProperty(rdfAtomPredicate, to.getIRI()),
                    args -> rdfAtomPredicate.updateSPO(args, rdfAtomPredicate.getSubject(args), newIri, rdfAtomPredicate.getObject(args)));
        }
    }
}
