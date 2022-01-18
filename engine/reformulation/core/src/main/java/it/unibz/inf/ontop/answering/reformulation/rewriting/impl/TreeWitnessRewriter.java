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
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.function.Function.identity;


/**
 * 
 */

public class TreeWitnessRewriter extends DummyRewriter implements ExistentialQueryRewriter {

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private TreeWitnessRewriterReasoner reasoner;
	private ImmutableCQContainmentCheckUnderLIDs<RDFAtomPredicate> containmentCheckUnderLIDs;

    private final SubstitutionFactory substitutionFactory;

    @Inject
	private TreeWitnessRewriter(AtomFactory atomFactory,
								TermFactory termFactory,
                                IntermediateQueryFactory iqFactory,
								CoreUtilsFactory coreUtilsFactory,
                                SubstitutionFactory substitutionFactory) {
        super(iqFactory, atomFactory, termFactory, coreUtilsFactory);

        this.substitutionFactory = substitutionFactory;
    }

	@Override
	public void setTBox(ClassifiedTBox classifiedTBox) {
		double startime = System.currentTimeMillis();

		this.reasoner = new TreeWitnessRewriterReasoner(classifiedTBox);
		super.setTBox(classifiedTBox);

        containmentCheckUnderLIDs = new ImmutableCQContainmentCheckUnderLIDs<>(getSigma());

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

	private ImmutableList<DataAtom<RDFAtomPredicate>> getAtomsForGenerators(Stream<TreeWitnessGenerator> gens, VariableOrGroundTerm r0)  {
		return gens
				.flatMap(g -> g.getMaximalGeneratorRepresentatives().stream())
				.distinct()
				.map(ce -> getAtom(ce, r0, this::getFreshVariable))
				.collect(ImmutableCollectors.toList());
	}

	private class CQ {
		private final ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> equalities;
		private final ImmutableSet<DataAtom<RDFAtomPredicate>> atoms;

		CQ(ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> equalities, ImmutableSet<DataAtom<RDFAtomPredicate>> atoms) {
		    this.equalities = equalities;
		    this.atoms = atoms;
        }

        CQ(ImmutableSet<DataAtom<RDFAtomPredicate>> atoms) {
            this.equalities = ImmutableMap.of();
            this.atoms = atoms;
        }

        CQ join(CQ cq) {
		    ImmutableMultimap<VariableOrGroundTerm, VariableOrGroundTerm> mm =
                        Stream.concat(equalities.entrySet().stream(), cq.equalities.entrySet().stream())
                            .distinct()
                            .collect(ImmutableCollectors.toMultimap());
		    Optional<Map.Entry<VariableOrGroundTerm, Collection<VariableOrGroundTerm>>> dk;
		    // merge equivalence classes: e.g., x = y and y = z are merged into x = y = z
		    while ((dk = mm.asMap().entrySet().stream().filter(e -> e.getValue().size() > 1).findFirst()).isPresent()) {
		        Collection<VariableOrGroundTerm> c = dk.get().getValue();
                VariableOrGroundTerm r = c.iterator().next();
                mm = mm.entries().stream()
                        .distinct()
                        .collect(ImmutableCollectors.toMultimap(Map.Entry::getKey, e -> c.contains(e.getValue()) ? r : e.getValue()));
            }
            ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> eqs = mm.entries().stream().collect(ImmutableCollectors.toMap());

            return new CQ(eqs,
                    // reduce
                    Sets.union(atoms, cq.atoms).stream()
                            .map(a -> atomFactory.getDataAtom(a.getPredicate(),
                                    a.getArguments().stream()
                                            .map(t -> eqs.getOrDefault(t, t))
                                            .collect(ImmutableCollectors.toList())))
                            .collect(ImmutableCollectors.toSet()));
        }

        @Override
        public String toString() {
		    return equalities + " AND " + atoms;
        }
	}

    class UCQBuilder {
	    private List<CQ> list;

        UCQBuilder(CQ cq) {
	        list = ImmutableList.of(cq);
        }

        UCQBuilder join(Stream<CQ> cqs) {
            list = cqs
                    .flatMap(cq2 -> list.stream().map(cq1 -> cq1.join(cq2)))
                    .collect(Collectors.toList());

            for (int i = 0; i < list.size(); i++) {
                CQ cq = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    CQ cqp = list.get(j);
                    if (cqp.atoms.containsAll(cq.atoms)) {
                        list.remove(j);
                        j--;
                    }
                    else if (cq.atoms.containsAll(cqp.atoms)) {
                        list.remove(i);
                        i--;
                        break;
                    }
                }
            }

	        return this;
        }

        ImmutableList<CQ> build() { return ImmutableList.copyOf(list); }
    }

    public Collector<Stream<CQ>, UCQBuilder, ImmutableList<CQ>> toUCQ(CQ cq) {
        return Collector.of(
                () -> new UCQBuilder(cq), // Supplier
                UCQBuilder::join, // Accumulator
                (b1, b2) -> b1.join(b2.build().stream()), // Merger
                UCQBuilder::build, // Finisher
                Collector.Characteristics.UNORDERED);
    }

    public Collector<Stream<CQ>, UCQBuilder, ImmutableList<CQ>> toUCQ() {
        return toUCQ(new CQ(ImmutableSet.of()));
    }


    ImmutableList<CQ> getTreeWitnessFormula(TreeWitness tw) {
        // get canonical representative
        List<VariableOrGroundTerm> list = new ArrayList<>(tw.getRoots());
        list.sort(Comparator.comparing(Object::toString));
        VariableOrGroundTerm rep = list.get(0);
        ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> equalities = list.stream()
                .collect(ImmutableCollectors.toMap(identity(), s -> rep));

        UCQBuilder ucq = new UCQBuilder(new CQ(equalities, tw.getRootAtoms()));
        return ucq.join(getAtomsForGenerators(tw.getGenerators().stream(), rep).stream()
                        .map(a -> new CQ(ImmutableSet.of(a))))
                .build();
    }

	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private ImmutableList<CQ> rewriteCC(QueryConnectedComponent cc) {

		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner);

		ImmutableList.Builder<CQ> builder = ImmutableList.builder();
		if (cc.hasNoFreeTerms() && (!cc.isDegenerate() || cc.getLoop().isPresent())) {
            builder.addAll(getAtomsForGenerators(tws.getGeneratorsOfDetachedCC().stream(), getFreshVariable()).stream()
                        .map(a -> new CQ(ImmutableSet.of(a)))
                        .collect(ImmutableCollectors.toList()));
		}

		if (!cc.isDegenerate()) {
			if (!TreeWitness.isCompatible(tws.getTWs())) {
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				for (ImmutableCollection<TreeWitness> compatibleTWs: tws) {
					log.debug("COMPATIBLE: {}", compatibleTWs);

					CQ edges = new CQ(cc.getEdges().stream()
                            .filter(edge -> compatibleTWs.stream().noneMatch(edge::isCoveredBy))
                            .flatMap(edge -> edge.getAtoms().stream())
                            .collect(ImmutableCollectors.toSet()));

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
                                Stream.of(new CQ(ImmutableSet.copyOf(edge.getAtoms()))),
                                tws.getTWs().stream()
                                        .filter(edge::isCoveredBy)
                                        .flatMap(tw -> getTreeWitnessFormula(tw).stream())))
                            .collect(toUCQ()));
            }
		}
		else {
			// degenerate connected component
			log.debug("LOOP {}", cc.getLoop());
			builder.add(new CQ(cc.getLoop()
                    .map(l -> ImmutableSet.copyOf(l.getAtoms()))
                    .orElse(ImmutableSet.of())));
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


    ImmutableCQ<RDFAtomPredicate> convert(CQ cq, ImmutableList<Variable> vars) {
        ImmutableMap<Variable, VariableOrGroundTerm> equalities = cq.equalities.entrySet().stream()
                .filter(e -> e.getKey() != e.getValue())
                .collect(ImmutableCollectors.toMap(e -> (Variable)e.getKey(), Map.Entry::getValue));

        ImmutableSubstitution s = substitutionFactory.getSubstitution(equalities);

	    return new ImmutableCQ<RDFAtomPredicate>(s.apply(vars), ImmutableList.copyOf(cq.atoms));
    }

    private ImmutableList<IQTree> convertCQ(ImmutableCQ<RDFAtomPredicate> cq, ImmutableList<Variable> vars) {

        ImmutableList<IQTree> body = cq.getAtoms().stream()
                .map(a -> iqFactory.createIntensionalDataNode((DataAtom<AtomPredicate>)(DataAtom)a))
                .collect(ImmutableCollectors.toList());

        ImmutableMap<Variable, ImmutableTerm> map = IntStream.range(0, vars.size())
                .mapToObj(i -> Maps.<Variable, ImmutableTerm>immutableEntry(vars.get(i), cq.getAnswerVariables().get(i)))
                .filter(e -> !e.getKey().equals(e.getValue()))
                .collect(ImmutableCollectors.toMap());

        ImmutableSubstitution<ImmutableTerm> substitution = substitutionFactory.getSubstitution(map);
        if (substitution.isEmpty())
            return body;

        IQTree result = join(body);

        return ImmutableList.of(iqFactory.createUnaryIQTree(
                iqFactory.createConstructionNode(
                        Sets.union(result.getVariables(), substitution.getDomain()).immutableCopy(),
                        substitution), result));
    }

    private IQTree join(ImmutableList<IQTree> atoms) {
        return (atoms.size() == 1)
                ? atoms.get(0)
                : iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(), atoms);
    }

    @Override
    public IQ rewrite(IQ query) throws EmptyQueryException {
		
		double startime = System.currentTimeMillis();

		IQTree canonicalTree = getCanonicalForm(query.getTree());

        IQTree rewritingTree = canonicalTree.acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
                // fix some order on variables
                ImmutableList<Variable> avs = ImmutableList.copyOf(rootNode.getVariables());
                return iqFactory.createUnaryIQTree(rootNode, child.acceptTransformer(new BasicGraphPatternTransformer(iqFactory) {
                    @Override
                    protected ImmutableList<IQTree> transformBGP(ImmutableList<IntensionalDataNode> triplePatterns) {
                        ImmutableList<DataAtom<RDFAtomPredicate>> bgp = triplePatterns.stream()
                                .map(c -> (DataAtom<RDFAtomPredicate>)(DataAtom)((IntensionalDataNode)c.getRootNode()).getProjectionAtom())
                                .collect(ImmutableCollectors.toList());

                        List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(new ImmutableCQ<>(avs, bgp));

                        ImmutableList<CQ> ucq = ccs.stream()
                                .map(cc -> rewriteCC(cc).stream())
                                .collect(toUCQ());

                        List<ImmutableCQ<RDFAtomPredicate>> ucq2 = ucq.stream()
                                .map(cq -> convert(cq, avs))
                                .collect(Collectors.toList());
                        containmentCheckUnderLIDs.removeContainedQueries(ucq2);

                        return convertUCQ(ucq2.stream()
                                .map(cq -> convertCQ(cq, avs))
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
                .map(c -> c.getVariables().equals(vars)
                        ? c
                        : iqFactory.createUnaryIQTree(iqFactory.createConstructionNode(vars), c))
                .collect(ImmutableCollectors.toList());

        return ImmutableList.of(iqFactory.createNaryIQTree(iqFactory.createUnionNode(vars), unionChildren));
    }
}
