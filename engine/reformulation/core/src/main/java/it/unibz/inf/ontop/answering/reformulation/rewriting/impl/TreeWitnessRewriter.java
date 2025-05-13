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
import it.unibz.inf.ontop.answering.reformulation.rewriting.ExistentialQueryRewriter;
import it.unibz.inf.ontop.constraints.HomomorphismFactory;
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.ImmutableCQContainmentCheck;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.ontology.*;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */

public class TreeWitnessRewriter extends DummyRewriter implements ExistentialQueryRewriter {

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private TreeWitnessRewriterReasoner reasoner;
	private ImmutableCQContainmentCheck<RDFAtomPredicate> containmentCheckUnderLIDs;

    private final SubstitutionFactory substitutionFactory;

    @Inject
	private TreeWitnessRewriter(CoreSingletons coreSingletons) {
        super(coreSingletons);
        this.substitutionFactory = coreSingletons.getSubstitutionFactory();
    }

	@Override
	public void setTBox(ClassifiedTBox classifiedTBox) {
		double startime = System.currentTimeMillis();

		this.reasoner = new TreeWitnessRewriterReasoner(classifiedTBox);
		super.setTBox(classifiedTBox);

        containmentCheckUnderLIDs = homomorphismFactory.getCQContainmentCheck(getSigma());

		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("setTBox time: %.3f s (total %.3f s)", tm, time));		
	}
	
	
	private int freshVarIndex = 0;
	
	private Variable getFreshVariable() {
		freshVarIndex++;
		return termFactory.getVariable("twr" + freshVarIndex);
	}
	
	/*
	 * returns atoms E of a given collection of tree witness generators; 
	 * the `free' variable of the generators is replaced by the term r0;
	 */

	private ImmutableSet<DataAtom<RDFAtomPredicate>> getAtomsForGenerators(Stream<TreeWitnessGenerator> gens, VariableOrGroundTerm r0)  {
		return gens
				.flatMap(g -> g.getMaximalGeneratorRepresentatives().stream())
				.map(ce -> getAtom(ce, r0, this::getFreshVariable))
				.collect(ImmutableCollectors.toSet());
	}

    private static ImmutableCQ<RDFAtomPredicate> createCQ(ImmutableSet<Variable> answerVariables, Substitution<VariableOrGroundTerm> substitution, ImmutableList<DataAtom<RDFAtomPredicate>> atoms) {
        return new ImmutableCQ<>(answerVariables, substitution, atoms);
    }

    private ImmutableCQ<RDFAtomPredicate> createCQ(ImmutableList<DataAtom<RDFAtomPredicate>> atoms) {
        return new ImmutableCQ<>(atoms.stream()
                        .flatMap(a -> a.getVariables().stream())
                        .collect(ImmutableCollectors.toSet()),
                substitutionFactory.getSubstitution(), atoms);
    }


    class UCQBuilder {
	    private List<ImmutableCQ<RDFAtomPredicate>> list;

        UCQBuilder(ImmutableCQ<RDFAtomPredicate> cq) {
	        list = ImmutableList.of(cq);
        }

        UCQBuilder join(Stream<ImmutableCQ<RDFAtomPredicate>> cqs) {
            list = cqs
                    .flatMap(cq2 -> list.stream()
                            .flatMap(cq1 -> joinCQs(cq1, cq2).stream()))
                    .collect(Collectors.toList());

            for (int i = 0; i < list.size(); i++) {
                ImmutableCQ<RDFAtomPredicate> cq = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    ImmutableCQ<RDFAtomPredicate> cqp = list.get(j);
                    if (cqp.getAtoms().containsAll(cq.getAtoms())) {
                        list.remove(j);
                        j--;
                    }
                    else if (cq.getAtoms().containsAll(cqp.getAtoms())) {
                        list.remove(i);
                        i--;
                        break;
                    }
                }
            }

	        return this;
        }

        private ImmutableSet<Set<VariableOrGroundTerm>> mergeOnePairOfClasses(ImmutableSet<Set<VariableOrGroundTerm>> equivalenceClasses) {
            for (Set<VariableOrGroundTerm> s1 : equivalenceClasses)
                for (Set<VariableOrGroundTerm> s2 : equivalenceClasses)
                    if (s1 != s2) {
                        if (!Sets.intersection(s1, s2).isEmpty())
                            return Sets.union(Sets.difference(equivalenceClasses, ImmutableSet.of(s1, s2)), ImmutableSet.of(Sets.union(s1, s2))).immutableCopy();
                    }
            return null;
        }

        private Optional<ImmutableCQ<RDFAtomPredicate>> joinCQs(ImmutableCQ<RDFAtomPredicate> cq1, ImmutableCQ<RDFAtomPredicate> cq2) {

            ImmutableSet<Set<VariableOrGroundTerm>> equivalenceClasses = Stream.of(cq1, cq2)
                    .map(ImmutableCQ::getSubstitution)
                    .map(Substitution::inverseMap)
                    .map(ImmutableMap::entrySet)
                    .flatMap(Collection::stream)
                    .map(e -> Sets.union(ImmutableSet.of(e.getKey()), ImmutableSet.copyOf(e.getValue())))
                    .collect(ImmutableCollectors.toSet());

            while (true) {
                ImmutableSet<Set<VariableOrGroundTerm>> newEquivalenceClasses = mergeOnePairOfClasses(equivalenceClasses);
                if (newEquivalenceClasses == null)
                    break;
                equivalenceClasses = newEquivalenceClasses;
            }

            ImmutableMap<Set<VariableOrGroundTerm>, VariableOrGroundTerm> map = equivalenceClasses.stream()
                    .map(c -> Maps.immutableEntry(c, getEquivalenceClassRepresentative(c)))
                    .collect(ImmutableCollectors.toMap());
            if (map.containsValue(null))
                return Optional.empty();

            Substitution<VariableOrGroundTerm> substitution = getSubstitution(map);

            return Optional.of(createCQ(
                    Sets.union(cq1.getAnswerVariables(), cq2.getAnswerVariables()).immutableCopy(),
                    substitution,
                    Stream.concat(cq1.getAtoms().stream(), cq2.getAtoms().stream())
                            .distinct()
                            .map(a -> atomFactory.getDataAtom(a.getPredicate(), substitutionFactory.onVariableOrGroundTerms().applyToTerms(substitution, a.getArguments())))
                            .collect(ImmutableCollectors.toList())));
        }

        ImmutableList<ImmutableCQ<RDFAtomPredicate>> build() { return ImmutableList.copyOf(list); }
    }

    public Collector<Stream<ImmutableCQ<RDFAtomPredicate>>, UCQBuilder, ImmutableList<ImmutableCQ<RDFAtomPredicate>>> toUCQ(ImmutableCQ<RDFAtomPredicate> cq) {
        return Collector.of(
                () -> new UCQBuilder(cq), // Supplier
                UCQBuilder::join, // Accumulator
                (b1, b2) -> b1.join(b2.build().stream()), // Merger
                UCQBuilder::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    public Collector<Stream<ImmutableCQ<RDFAtomPredicate>>, UCQBuilder, ImmutableList<ImmutableCQ<RDFAtomPredicate>>> toUCQ() {
        return toUCQ(createCQ(ImmutableList.of()));
    }

    private VariableOrGroundTerm getEquivalenceClassRepresentative(Set<VariableOrGroundTerm> equivalenceClass) {
        ImmutableSet<Variable> equivalenceClassVariables = equivalenceClass.stream()
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toSet());

        switch (equivalenceClass.size() - equivalenceClassVariables.size()) {
            case 0: // all variables
                return equivalenceClassVariables.stream()
                        .min(Comparator.comparing(Object::toString))
                        .get();
            case 1: // a single constant
                return Sets.difference(equivalenceClass, equivalenceClassVariables).stream().findFirst().get();
            default: // more than one constant
                return null;
        }
    }

    private Substitution<VariableOrGroundTerm> getSubstitution(ImmutableMap<Set<VariableOrGroundTerm>, VariableOrGroundTerm> map) {
        return map.entrySet().stream()
                .flatMap(e -> e.getKey().stream()
                        .filter(v -> v != e.getValue())
                        .map(v -> Maps.immutableEntry((Variable)v, e.getValue())))
                .collect(substitutionFactory.toSubstitution());
    }

    ImmutableList<ImmutableCQ<RDFAtomPredicate>> getTreeWitnessFormula(TreeWitness tw) {
        Set<VariableOrGroundTerm> roots = tw.getRoots();

        // get canonical representative
        VariableOrGroundTerm representative = getEquivalenceClassRepresentative(roots);
        if (representative == null)
                return ImmutableList.of();

        Substitution<VariableOrGroundTerm> substitution = getSubstitution(ImmutableMap.of(roots, representative));

        ImmutableSet<Variable> rootVariables = roots.stream()
                .filter(t -> t instanceof Variable)
                .map(t -> (Variable)t)
                .collect(ImmutableCollectors.toSet());

        UCQBuilder ucq = new UCQBuilder(createCQ(rootVariables, substitution, ImmutableList.copyOf(tw.getRootAtoms())));
        return ucq.join(getAtomsForGenerators(tw.getGenerators().stream(), representative).stream()
                        .map(a -> createCQ(ImmutableList.of(a))))
                .build();
    }

	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private ImmutableList<ImmutableCQ<RDFAtomPredicate>> rewriteCC(QueryConnectedComponent cc) {

		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner);

		ImmutableList.Builder<ImmutableCQ<RDFAtomPredicate>> builder = ImmutableList.builder();
		if (cc.hasNoFreeTerms() && (!cc.isDegenerate() || cc.getLoop().isPresent())) {
            builder.addAll(getAtomsForGenerators(tws.getGeneratorsOfDetachedCC().stream(), getFreshVariable()).stream()
                        .map(a -> createCQ(ImmutableList.of(a)))
                        .collect(ImmutableCollectors.toList()));
		}

		if (!cc.isDegenerate()) {
			if (!TreeWitness.isCompatible(tws.getTWs())) {
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				for (ImmutableCollection<TreeWitness> compatibleTWs: tws) {
					log.debug("COMPATIBLE: {}", compatibleTWs);

                    ImmutableCQ<RDFAtomPredicate> edges = createCQ(cc.getEdges().stream()
                            .filter(edge -> compatibleTWs.stream().noneMatch(edge::isCoveredBy))
                            .flatMap(edge -> edge.getAtoms().stream())
                            .collect(ImmutableCollectors.toList()));

					builder.addAll(
					        compatibleTWs.stream()
                                .map(tw -> getTreeWitnessFormula(tw).stream())
                                .collect(toUCQ(edges)));
				}
			}
			else {
				// no conflicting tree witnesses
				// use polynomial tree witness rewriting by treating each edge independently
				builder.addAll(
				        cc.getEdges().stream()
                            .map(edge -> Stream.concat(
                                Stream.of(createCQ(edge.getAtoms())),
                                tws.getTWs().stream()
                                        .filter(edge::isCoveredBy)
                                        .map(this::getTreeWitnessFormula)
                                        .flatMap(Collection::stream)))
                            .collect(toUCQ()));
            }
		}
		else {
			// degenerate connected component
			log.debug("LOOP {}", cc.getLoop());
			builder.add(createCQ(cc.getLoop()
                    .map(l -> l.getAtoms())
                    .orElse(ImmutableList.of())));
		}
		return builder.build();
	}
	
	private double time = 0;

	private IQTree getCanonicalForm(IQTree tree) {
        ClassifiedTBox tbox = reasoner.getClassifiedTBox();

        return tree.acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
                // TODO: support quads
                DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();
                TriplePredicate triplePredicate = (TriplePredicate) atom.getPredicate();
                ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

                Optional<IRI> classIRI = triplePredicate.getClassIRI(arguments);
                if (classIRI.isPresent()) {
                    IRI iri = classIRI.get();
                    if (tbox.classes().contains(iri)) {
                        OClass c = tbox.classes().get(iri);
                        OClass equivalent = (OClass) tbox.classesDAG().getCanonicalForm(c);
                        return dataNode.newAtom(getAtom(arguments.get(0), equivalent));
                    }
                }
                else {
                    Optional<IRI> propertyIRI = triplePredicate.getPropertyIRI(arguments);
                    if (propertyIRI.isPresent()) {
                        IRI iri = propertyIRI.get();
                        if (tbox.objectProperties().contains(iri)) {
                            ObjectPropertyExpression ope = tbox.objectProperties().get(iri);
                            ObjectPropertyExpression equivalent = tbox.objectPropertiesDAG().getCanonicalForm(ope);
                            return dataNode.newAtom(getAtom(arguments.get(0), equivalent, arguments.get(2)));
                        }
                        else if (tbox.dataProperties().contains(iri)) {
                            DataPropertyExpression dpe = tbox.dataProperties().get(iri);
                            DataPropertyExpression equivalent = tbox.dataPropertiesDAG().getCanonicalForm(dpe);
                            return dataNode.newAtom(getAtom(arguments.get(0), equivalent, arguments.get(2)));
                        }
                    }
                }
                return  dataNode;
                //throw new MinorOntopInternalBugException("Unknown type of triple atoms");
            }
        });
    }


    private ImmutableList<IQTree> convertCQ(ImmutableCQ<RDFAtomPredicate> cq) {

        ImmutableList<IQTree> body = cq.getAtoms().stream()
                .map(a -> iqFactory.createIntensionalDataNode((DataAtom<AtomPredicate>)(DataAtom)a))
                .collect(ImmutableCollectors.toList());

        Substitution<?> substitution = cq.getSubstitution();
        if (substitution.isEmpty())
            return body;

        IQTree result = join(body);

        return ImmutableList.of(iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        Sets.union(result.getVariables(), substitution.getDomain()).immutableCopy(),
                        substitution), result));
    }

    private IQTree join(ImmutableList<IQTree> atoms) {
        return iqTreeTools.createJoinTree(Optional.empty(), atoms)
                .orElseThrow(() -> new MinorOntopInternalBugException("Joining tree failed"));
    }

    @Override
    public IQ rewrite(IQ query) throws EmptyQueryException {
		
		double startime = System.currentTimeMillis();

		IQTree canonicalTree = getCanonicalForm(query.getTree());

        IQTree rewritingTree = canonicalTree.acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformConstruction(UnaryIQTree tree, ConstructionNode rootNode, IQTree child) {
                // fix some order on variables
                ImmutableSet<Variable> avs = rootNode.getVariables();
                return iqFactory.createUnaryIQTree(rootNode, child.acceptTransformer(new BasicGraphPatternTransformer(iqFactory, iqTreeTools) {
                    @Override
                    protected ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns) {
                        ImmutableList<DataAtom<RDFAtomPredicate>> bgp = triplePatterns.stream()
                                .map(IntensionalDataNode::getProjectionAtom)
                                .map(a -> (DataAtom<RDFAtomPredicate>)(DataAtom)a)
                                .collect(ImmutableCollectors.toList());

                        List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(new ImmutableCQ<>(avs, substitutionFactory.getSubstitution(), bgp));

                        ImmutableList<ImmutableCQ<RDFAtomPredicate>> ucq = ccs.stream()
                                .map(cc -> rewriteCC(cc).stream())
                                .collect(toUCQ());

                        List<ImmutableCQ<RDFAtomPredicate>> ucq2 = new ArrayList<>(ucq);
                        containmentCheckUnderLIDs.removeContainedQueries(ucq2);

                        return convertUCQ(ucq2.stream()
                                .map(cq -> convertCQ(cq))
                                .collect(ImmutableCollectors.toList()));
                    }
                }));
            }
        });

		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("Rewriting time: %.3f s (total %.3f s)", tm, time));
		log.debug("Final rewriting:\n{}", rewritingTree);

        IQ result = iqFactory.createIQ(query.getProjectionAtom(), rewritingTree);
        return super.rewrite(result);
	}

    private ImmutableList<IQTree> convertUCQ(ImmutableList<ImmutableList<IQTree>> ucq) {
        if (ucq.size() == 1)
            return ucq.get(0);

        ImmutableList<IQTree> joined = ucq.stream()
                .map(this::join)
                .collect(ImmutableCollectors.toList());

        // intersection
        ImmutableSet<Variable> vars = joined.get(0).getVariables().stream()
                .filter(v -> joined.stream().allMatch(j -> j.getVariables().contains(v)))
                .collect(ImmutableCollectors.toSet());

        ImmutableList<IQTree> unionChildren = joined.stream()
                .map(c -> iqTreeTools.createConstructionNodeTreeIfNontrivial(c, vars))
                .collect(ImmutableCollectors.toList());

        return ImmutableList.of(iqTreeTools.createUnionTree(vars, unionChildren));
    }
}
