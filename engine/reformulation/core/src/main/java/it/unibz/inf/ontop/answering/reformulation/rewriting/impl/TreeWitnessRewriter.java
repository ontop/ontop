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
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Edge;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Loop;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.function.Function.identity;


/**
 * 
 */

public class TreeWitnessRewriter extends DummyRewriter implements ExistentialQueryRewriter {

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private TreeWitnessRewriterReasoner reasoner;
	private ImmutableCQContainmentCheckUnderLIDs containmentCheckUnderLIDs;

	private final DatalogFactory datalogFactory;
    private final EQNormalizer eqNormalizer;
	private final UnifierUtilities unifierUtilities;
	private final ImmutabilityTools immutabilityTools;
    private final IQ2DatalogTranslator iqConverter;
    private final DatalogProgram2QueryConverter datalogConverter;

    @Inject
	private TreeWitnessRewriter(AtomFactory atomFactory,
								TermFactory termFactory,
								DatalogFactory datalogFactory,
                                EQNormalizer eqNormalizer,
								UnifierUtilities unifierUtilities,
                                ImmutabilityTools immutabilityTools,
                                DatalogProgram2QueryConverter datalogConverter,
                                IntermediateQueryFactory iqFactory,
                                IQ2DatalogTranslator iqConverter,
								CoreUtilsFactory coreUtilsFactory) {
        super(iqFactory, atomFactory, termFactory, coreUtilsFactory);

		this.datalogFactory = datalogFactory;
        this.eqNormalizer = eqNormalizer;
		this.unifierUtilities = unifierUtilities;
		this.immutabilityTools = immutabilityTools;
        this.iqConverter = iqConverter;
        this.datalogConverter = datalogConverter;
    }

	@Override
	public void setTBox(ClassifiedTBox classifiedTBox) {
		double startime = System.currentTimeMillis();

		this.reasoner = new TreeWitnessRewriterReasoner(classifiedTBox);
		super.setTBox(classifiedTBox);

        containmentCheckUnderLIDs = new ImmutableCQContainmentCheckUnderLIDs(getSigma());

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

	private ImmutableList<DataAtom<RDFAtomPredicate>> getAtomsForGenerators(ImmutableList<TreeWitnessGenerator> gens, VariableOrGroundTerm r0)  {
		return gens.stream()
				.flatMap(g -> g.getMaximalGeneratorRepresentatives().stream())
				.distinct()
				.map(ce -> getAtomForGenerator(ce, r0))
				.collect(ImmutableCollectors.toList());
	}

	private DataAtom<RDFAtomPredicate> getAtomForGenerator(ClassExpression con, VariableOrGroundTerm r0) {
		log.debug("  BASIC CONCEPT: {}", con);
		if (con instanceof OClass) {
			return (DataAtom)atomFactory.getIntensionalTripleAtom(r0, ((OClass) con).getIRI());
		}
		else if (con instanceof ObjectSomeValuesFrom) {
			ObjectPropertyExpression ope = ((ObjectSomeValuesFrom)con).getProperty();
			return !ope.isInverse()
					? (DataAtom)atomFactory.getIntensionalTripleAtom(r0, ope.getIRI(), getFreshVariable())
					: (DataAtom)atomFactory.getIntensionalTripleAtom(getFreshVariable(), ope.getIRI(), r0);
		}
		else {
			DataPropertyExpression dpe = ((DataSomeValuesFrom)con).getProperty();
			return (DataAtom)atomFactory.getIntensionalTripleAtom(r0, dpe.getIRI(), getFreshVariable());
		}
	}
	
	private class CQ {
		private final ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> equalities;
		private final ImmutableSet<DataAtom<RDFAtomPredicate>> atoms;

		CQ(ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> equalities, ImmutableSet<DataAtom<RDFAtomPredicate>> atoms) {
		    this.equalities = equalities;
		    this.atoms = atoms;
        }

        CQ reduce() {
		    return new CQ(equalities, atoms.stream()
                    .map(a -> atomFactory.getDataAtom(a.getPredicate(),
                            a.getArguments().stream()
                                    .map(t -> equalities.getOrDefault(t, t))
                                    .collect(ImmutableCollectors.toList())))
                    .collect(ImmutableCollectors.toSet()));
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
            return new CQ(
                    mm.entries().stream().collect(ImmutableCollectors.toMap()),
                    Sets.union(atoms, cq.atoms).immutableCopy());
        }

        ImmutableList<Function> as() {
		    return Stream.concat(
		            equalities.entrySet().stream()
                            .filter(e -> e.getKey() != e.getValue())
                            .map(e -> termFactory.getFunctionEQ(immutabilityTools.convertToMutableTerm(e.getKey()), immutabilityTools.convertToMutableTerm(e.getValue()))),
                    atoms.stream()
                            .map(a -> immutabilityTools.convertToMutableFunction(a))).collect(ImmutableCollectors.toList());
        }

        @Override
        public String toString() {
		    return equalities + " AND " + atoms;
        }
	}

	private static class UnionOfCQs {
	    private final ImmutableList<CQ> cqs;

        UnionOfCQs(ImmutableList<CQ> cqs) { this.cqs = cqs; }

        UnionOfCQs join(CQ cq) {
            return new UnionOfCQs(cqs.stream()
                    .map(c -> c.join(cq).reduce())
                    .collect(ImmutableCollectors.toList()));
        }

        UnionOfCQs join(Stream<CQ> ucq) {
            List<CQ> list = ucq
                    .flatMap(cq2 -> cqs.stream().map(cq1 -> cq1.join(cq2).reduce()))
                    .collect(Collectors.toList());

            System.out.println("START REDUCING: " + list);

            for (int i = 0; i < list.size(); i++) {
                CQ cq = list.get(i);
                for (int j = i + 1; j < list.size(); j++) {
                    CQ cqp = list.get(j);
                    if (cqp.atoms.containsAll(cq.atoms)) {
                        System.out.println("REMOVE " + cqp + " COVERED BY " + cq);
                        list.remove(j);
                        j--;
                    }
                    else if (cq.atoms.containsAll(cqp.atoms)) {
                        System.out.println("REMOVE2 " + cq + " COVERED BY " + cqp);
                        list.remove(i);
                        i--;
                        break;
                    }
                }
            }

            System.out.println("RESULT: " + list);

            return new UnionOfCQs(ImmutableList.copyOf(list));
        }
	}

    private ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> getReps(ImmutableSet<VariableOrGroundTerm> equalities) {
//        ImmutableMap<VariableOrGroundTerm, VariableOrGroundTerm> reps = equalities.stream()
//                .map(s -> new AbstractMap.SimpleEntry<>(s, s.iterator().next()))
//                .flatMap(e -> e.getKey().stream().map(s -> new AbstractMap.SimpleEntry<>(s, e.getValue())))
//                .collect(ImmutableCollectors.toMap());
        // get canonical representative
        List<VariableOrGroundTerm> list = new ArrayList<>(equalities);
        list.sort(Comparator.comparing(Object::toString));
        VariableOrGroundTerm rep = list.get(0);
        return equalities.stream()
                .collect(ImmutableCollectors.toMap(identity(), s -> rep));
    }

    ImmutableList<CQ> getTreeWitnessFormula(TreeWitness tw) {
	    CQ rootAtoms = new CQ(getReps(tw.getRoots()), tw.getRootAtoms());
        return new UnionOfCQs(
                getAtomsForGenerators(tw.getGenerators(), rootAtoms.equalities.entrySet().iterator().next().getValue()).stream()
                        .map(a -> new CQ(ImmutableMap.of(), ImmutableSet.of(a)))
                        .collect(ImmutableCollectors.toList())).join(rootAtoms).cqs;
    }

	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private ImmutableList<CQ> rewriteCC(QueryConnectedComponent cc) {

		List<CQ> bodies = new LinkedList<>();

		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner);

		if (cc.hasNoFreeTerms()) {  
			if (!cc.isDegenerate() || cc.getLoop().isPresent())
				getAtomsForGenerators(ImmutableList.copyOf(tws.getGeneratorsOfDetachedCC()), getFreshVariable()).stream()
                        .forEach(a -> bodies.add(new CQ(ImmutableMap.of(), ImmutableSet.of(a))));
		}

		if (!cc.isDegenerate()) {
			if (!TreeWitness.isCompatible(tws.getTWs())) {
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				for (ImmutableCollection<TreeWitness> compatibleTWs: tws) {
					log.debug("COMPATIBLE: {}", compatibleTWs);

					CQ edges = new CQ(ImmutableMap.of(), cc.getEdges().stream()
                            .filter(edge -> compatibleTWs.stream().noneMatch(edge::isCoveredBy))
                            .flatMap(edge -> edge.getAtoms().stream())
                            .collect(ImmutableCollectors.toSet()));

					UnionOfCQs ucq = new UnionOfCQs(ImmutableList.of(edges));
					for (TreeWitness tw : compatibleTWs) {
					    ucq = ucq.join(getTreeWitnessFormula(tw).stream());
					}
					bodies.addAll(ucq.cqs);
				}
			}
			else {
				// no conflicting tree witnesses
				// use polynomial tree witness rewriting by treating each edge independently
				UnionOfCQs ucq = new UnionOfCQs(ImmutableList.of(new CQ(ImmutableMap.of(), ImmutableSet.of())));
                for (Edge edge : cc.getEdges()) {
					log.debug("EDGE {}", edge);
                    ucq = ucq.join(Stream.concat(
                            Stream.of(new CQ(ImmutableMap.of(), ImmutableSet.copyOf(edge.getAtoms()))),
                            tws.getTWs().stream()
                                    .filter(edge::isCoveredBy)
                                    .flatMap(tw -> getTreeWitnessFormula(tw).stream())));
                }
				bodies.addAll(ucq.cqs);
            }
		}
		else {
			// degenerate connected component
			log.debug("LOOP {}", cc.getLoop());
			bodies.add(new CQ(ImmutableMap.of(), cc.getLoop()
					.map(l -> ImmutableSet.copyOf(l.getAtoms()))
					.orElse(ImmutableSet.of())));
		}
		System.out.println("BODIES " + bodies);
		return ImmutableList.copyOf(bodies);
	}
	
	private double time = 0;
	
	@Override
    public IQ rewrite(IQ query) throws EmptyQueryException {
		
		double startime = System.currentTimeMillis();

		DatalogProgram program = iqConverter.translate(query);

		List<CQIE> outputRules = new LinkedList<>();
		for (CQIE cqie : program.getRules()) {
			List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(reasoner, cqie, atomFactory);

            UnionOfCQs ucq = new UnionOfCQs(ImmutableList.of(new CQ(ImmutableMap.of(), ImmutableSet.of())));
            for (QueryConnectedComponent cc : ccs) {
                log.debug("CONNECTED COMPONENT ({}) EXISTS {}", cc.getFreeVariables(), cc.getQuantifiedVariables());
                log.debug("     WITH EDGES {} AND LOOP {}", cc.getEdges(), cc.getLoop());
                log.debug("     NON-DL ATOMS {}", cc.getNonDLAtoms());
                ucq = ucq.join(rewriteCC(cc).stream());
            }
            for (CQ b : ucq.cqs) {
                CQIE cq = datalogFactory.getCQIE((Function) cqie.getHead().clone(),
                        Stream.concat(b.as().stream(),
                                ccs.stream().flatMap(cc -> cc.getNonDLAtoms().stream()))
                                .collect(ImmutableCollectors.toList()));
                eqNormalizer.enforceEqualities(cq);
                outputRules.add(cq);
            }
		}

        System.out.println("REWRITTEN PROGRAM\n" +  outputRules);

        DatalogProgram programAfterRewriting = datalogFactory.getDatalogProgram(program.getQueryModifiers(), outputRules);
        IQ convertedIQ =  datalogConverter.convertDatalogProgram(programAfterRewriting,
				query.getProjectionAtom().getArguments());

        IQTree optimisedTree = convertedIQ.getTree().acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformUnion(IQTree tree, UnionNode rootNode, ImmutableList<IQTree> children) {
                Map<ImmutableCQ, IQTree> map = new HashMap<>();
                // fix some order on variables
                ImmutableList<Variable> avs = ImmutableList.copyOf(rootNode.getVariables());
                for (IQTree child : children) {
                    ImmutableCQ cq;
                    if (child.getRootNode() instanceof InnerJoinNode
                            && !child.getChildren().stream()
                                .anyMatch(c -> !(c.getRootNode() instanceof IntensionalDataNode))) {
                        cq = new ImmutableCQ(avs, child.getChildren().stream()
                                .map(c -> ((IntensionalDataNode)c.getRootNode()).getProjectionAtom())
                                .collect(ImmutableCollectors.toList()));
                    }
                    else if (child.getRootNode() instanceof IntensionalDataNode) {
                        cq = new ImmutableCQ(avs, ImmutableList.of(
                                ((IntensionalDataNode)child.getRootNode()).getProjectionAtom()));
                    }
                    else if (child.getRootNode() instanceof ConstructionNode) {
                        ImmutableSubstitution<ImmutableTerm> substitution = ((ConstructionNode)child.getRootNode()).getSubstitution();
                        ImmutableList<Variable> avs1 = (ImmutableList<Variable>) substitution.apply(avs);
                        IQTree subtree = child.getChildren().get(0);
                        if ((subtree.getRootNode() instanceof InnerJoinNode)
                                && !subtree.getChildren().stream()
                                    .anyMatch(c -> !(c.getRootNode() instanceof IntensionalDataNode))) {
                            cq = new ImmutableCQ(avs1,
                                        subtree.getChildren().stream()
                                            .map(c -> ((IntensionalDataNode)c.getRootNode()).getProjectionAtom())
                                            .collect(ImmutableCollectors.toList()));
                        }
                        else if (subtree.getRootNode() instanceof IntensionalDataNode) {
                            cq = new ImmutableCQ(avs1, ImmutableList.of(
                                        ((IntensionalDataNode)subtree.getRootNode()).getProjectionAtom()));
                        }
                        else {
                            return transformNaryCommutativeNode(rootNode, children); // straight away
                        }
                    }
                    else {
                        return transformNaryCommutativeNode(rootNode, children);  // straight away
                    }
                    // .put returns the previous value
                    if (map.put(cq, child) != null)
                        return tree;
                }

                List<ImmutableCQ> ucq = new LinkedList<>(map.keySet());
                containmentCheckUnderLIDs.removeContainedQueries(ucq);
                if (ucq.size() == 1)
                    return map.get(ucq.get(0));
                else
                    return iqFactory.createNaryIQTree(rootNode, ucq.stream().map(cq -> map.get(cq))
                                .collect(ImmutableCollectors.toList()));
            }
        });

        IQ result = iqFactory.createIQ(convertedIQ.getProjectionAtom(), optimisedTree);

		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("Rewriting time: %.3f s (total %.3f s)", tm, time));
		log.debug("Final rewriting:\n{}", result);

		return super.rewrite(result);
	}

}
