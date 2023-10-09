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
import it.unibz.inf.ontop.datalog.*;
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
import it.unibz.inf.ontop.spec.mapping.transformer.QueryUnionSplitter;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Singleton
public class TMappingSaturatorImpl implements MappingSaturator  {

	// TODO: the implementation of EXCLUDE ignores equivalent classes / properties

    private final TMappingExclusionConfig tMappingExclusionConfig;
	private final TermFactory termFactory;
    private final QueryUnionSplitter unionSplitter;
    private final UnionFlattener unionNormalizer;
    private final MappingCQCOptimizer mappingCqcOptimizer;
    private final UnionBasedQueryMerger queryMerger;
    private final CoreSingletons coreSingletons;
    private final IntermediateQueryFactory iqFactory;
    private final SubstitutionFactory substitutionFactory;

    @Inject
	private TMappingSaturatorImpl(TMappingExclusionConfig tMappingExclusionConfig,
                                  QueryUnionSplitter unionSplitter,
                                  UnionFlattener unionNormalizer,
                                  MappingCQCOptimizer mappingCqcOptimizer,
                                  UnionBasedQueryMerger queryMerger,
                                  CoreSingletons coreSingletons) {
        this.tMappingExclusionConfig = tMappingExclusionConfig;
		this.termFactory = coreSingletons.getTermFactory();
        this.unionSplitter = unionSplitter;
        this.unionNormalizer = unionNormalizer;
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

        ImmutableMap<MappingAssertionIndex, Collection<TMappingRule>> original = mapping.stream()
                .flatMap(a -> unionSplitter.splitUnion(unionNormalizer.optimize(a.getQuery()))
                        .map(IQ::normalizeForOptimization) // replaces join equalities
                        .map(q -> mappingCqcOptimizer.optimize(cqc, q))
                        .map(q -> Maps.immutableEntry(a.getIndex(), new TMappingRule(q, coreSingletons))))
                .collect(ImmutableCollectors.toMultimap()).asMap();

        ImmutableMap<MappingAssertionIndex, IQ> saturated = original.keySet().stream()
                .map(MappingAssertionIndex::getPredicate)
                .distinct()
                .map(rdfAtomPredicate -> new TMappingRuleHeadConstructorProvider(rdfAtomPredicate))
                .flatMap(provider -> Stream.concat(Stream.concat(
                    reasoner.objectPropertiesDAG().stream()
                            .filter(node -> !node.getRepresentative().isInverse() && !tMappingExclusionConfig.contains(node.getRepresentative()))
                            .map(node -> Maps.immutableEntry(node, getSaturatedRepresentative(node, reasoner.objectPropertiesDAG(), original, provider::constructor, cqc)))
                            .flatMap(e -> e.getKey().getMembers().stream()
                                .filter(d -> !d.isInverse() || d.getInverse() != e.getKey().getRepresentative())
                                .map(d -> Maps.immutableEntry(provider.constructor(d), e.getValue()))),

                    reasoner.dataPropertiesDAG().stream()
                            .filter(node -> !tMappingExclusionConfig.contains(node.getRepresentative()))
                            .map(node -> Maps.immutableEntry(node, getSaturatedRepresentative(node, reasoner.dataPropertiesDAG(), original, provider::constructor, cqc)))
                            .flatMap(e -> e.getKey().getMembers().stream()
                                    .map(d -> Maps.immutableEntry(provider.constructor(d), e.getValue())))),

                    reasoner.classesDAG().stream()
                            .filter(node -> (node.getRepresentative() instanceof OClass) && !tMappingExclusionConfig.contains((OClass)node.getRepresentative()))
                            .map(node -> Maps.immutableEntry(node, getSaturatedRepresentative(node, reasoner.classesDAG(), original, provider::constructor, cqc)))
                            .flatMap(e -> e.getKey().getMembers().stream()
                                    .filter(d -> d instanceof OClass)
                                    .map(d -> Maps.immutableEntry(provider.constructor(d), e.getValue())))))

                .filter(e -> e.getValue().isPresent())
                .map(e -> Maps.immutableEntry(e.getKey(), e.getValue().get()))

                .map(e -> Maps.immutableEntry(
                        e.getKey().indexOf(), e.getKey().updateConstructionNodeIri(e.getValue())))
                .collect(ImmutableCollectors.toMap());

        ImmutableList<IQ> combined = Stream.concat(
                saturated.values().stream(),
                original.entrySet().stream()
                        .filter(e -> !saturated.containsKey(e.getKey()))
                        .map(e -> e.getValue().stream()
                                        .collect(TMappingEntry.toTMappingEntry(cqc, coreSingletons)))
                        .map(this::toIQ)
                        .map(Optional::get))
                .collect(ImmutableCollectors.toList());

        return combined.stream()
                .map(q -> new MappingAssertion(q, null))
                .collect(ImmutableCollectors.toList());
    }

    private Optional<IQ> toIQ(Collection<TMappingRule> rules) {
        return queryMerger.mergeDefinitions(rules.stream()
                        .map(r -> r.asIQ(coreSingletons))
                        .collect(ImmutableCollectors.toList()))
                .map(IQ::normalizeForOptimization);
    }

    private <T> Optional<IQ> getSaturatedRepresentative(Equivalences<T> node,
                                                                       EquivalencesDAG<T> dag,
                                                                       ImmutableMap<MappingAssertionIndex, Collection<TMappingRule>> original,
                                                                       Function<T, TMappingRuleHeadConstructor> constructor,
                                                                       ExtensionalDataNodeListContainmentCheck cqc) {
        IRIConstant iri = constructor.apply(node.getRepresentative()).getIri();

        ImmutableList<TMappingRule> list = dag.getSub(node).stream()
                .flatMap(subnode -> subnode.getMembers().stream())
                .map(constructor)
                .flatMap(t -> Optional.ofNullable(original.get(t.indexOf()))
                        .map(l -> l.stream()
                                .map(m -> m.createCopy(t.updateConstructionNodeIri(m.getProjectionAtom(), m.getSubstitution(), iri))))
                        .orElseGet(Stream::of))
                .collect(TMappingEntry.toTMappingEntry(cqc, coreSingletons));

        return toIQ(list);
    }


    private abstract class TMappingRuleHeadConstructor {
        final MappingAssertionIndex index;
        final IRIConstant iri;
        TMappingRuleHeadConstructor(MappingAssertionIndex index) {
            this.index = index;
            this.iri = termFactory.getConstantIRI(index.getIri());
        }
        MappingAssertionIndex indexOf() { return index; }

        IRIConstant getIri() { return iri; }

        abstract ImmutableList<ImmutableTerm> updateAtomArguments(ImmutableList<ImmutableTerm> args, IRIConstant newIri);

        public Substitution<ImmutableTerm> updateConstructionNodeIri(DistinctVariableOnlyDataAtom projectionAtom, Substitution<ImmutableTerm> substitution, IRIConstant newIri) {
            ImmutableList<Variable> variables = projectionAtom.getArguments();
            return substitutionFactory.getSubstitution(variables, updateAtomArguments(substitution.apply(variables), newIri));
        }

        public IQ updateConstructionNodeIri(IQ query) {
                ConstructionNode constructionNode = (ConstructionNode) query.getTree().getRootNode();
                Substitution<ImmutableTerm> updatedSubstitution = updateConstructionNodeIri(query.getProjectionAtom(), constructionNode.getSubstitution(), iri);
                ConstructionNode updatedConstructionNode = iqFactory.createConstructionNode(constructionNode.getVariables(), updatedSubstitution);
                return iqFactory.createIQ(query.getProjectionAtom(),
                        iqFactory.createUnaryIQTree(updatedConstructionNode, ((UnaryIQTree)query.getTree()).getChild()));
        }
    }

    private class TMappingRuleHeadConstructorProvider {
	    private final RDFAtomPredicate p;
        private final IRIConstant rdfType;

        TMappingRuleHeadConstructorProvider(RDFAtomPredicate rdfAtomPredicate) {
            this.p = rdfAtomPredicate;
            this.rdfType = termFactory.getConstantIRI(RDF.TYPE);
        }

        TMappingRuleHeadConstructor constructor(ClassExpression ce) {
            if (ce instanceof OClass) {
                OClass oc = (OClass)ce;
                return new TMappingRuleHeadConstructor(MappingAssertionIndex.ofClass(p, oc.getIRI())) {
                    @Override
                    public ImmutableList<ImmutableTerm> updateAtomArguments(ImmutableList<ImmutableTerm> args, IRIConstant newIri) {
                        return p.updateSPO(args, p.getSubject(args), rdfType, newIri);
                    }
                };
            }
            else if (ce instanceof ObjectSomeValuesFrom) {
                ObjectPropertyExpression ope = ((ObjectSomeValuesFrom) ce).getProperty();
                return new TMappingRuleHeadConstructor(MappingAssertionIndex.ofProperty(p, ope.getIRI())) {
                    @Override
                    public ImmutableList<ImmutableTerm> updateAtomArguments(ImmutableList<ImmutableTerm> args, IRIConstant newIri) {
                        return p.updateSPO(args, ope.isInverse() ? p.getObject(args) : p.getSubject(args), rdfType, newIri);
                    }
                };
            }
            else if (ce instanceof DataSomeValuesFrom) {
                DataPropertyExpression dpe = ((DataSomeValuesFrom) ce).getProperty();
                return new TMappingRuleHeadConstructor(MappingAssertionIndex.ofProperty(p, dpe.getIRI())) {
                    @Override
                    public ImmutableList<ImmutableTerm> updateAtomArguments(ImmutableList<ImmutableTerm> args, IRIConstant newIri) {
                        return p.updateSPO(args, p.getSubject(args), rdfType, newIri);
                    }
                };
            }
            else
                throw new MinorOntopInternalBugException("Unexpected type" + ce);
        }

        TMappingRuleHeadConstructor constructor(ObjectPropertyExpression ope) {
            return new TMappingRuleHeadConstructor(MappingAssertionIndex.ofProperty(p, ope.getIRI())) {
                @Override
                public ImmutableList<ImmutableTerm> updateAtomArguments(ImmutableList<ImmutableTerm> args, IRIConstant newIri) {
                    return ope.isInverse()
                            ? p.updateSPO(args, p.getObject(args), newIri, p.getSubject(args))
                            : p.updateSPO(args, p.getSubject(args), newIri, p.getObject(args));
                }
            };
        }

        TMappingRuleHeadConstructor constructor(DataPropertyExpression dpe) {
            return new TMappingRuleHeadConstructor(MappingAssertionIndex.ofProperty(p, dpe.getIRI())) {
                @Override
                public ImmutableList<ImmutableTerm> updateAtomArguments(ImmutableList<ImmutableTerm> args, IRIConstant newIri) {
                    return p.updateSPO(args, p.getSubject(args), newIri, p.getObject(args));
                }
            };
        }
    }
}
