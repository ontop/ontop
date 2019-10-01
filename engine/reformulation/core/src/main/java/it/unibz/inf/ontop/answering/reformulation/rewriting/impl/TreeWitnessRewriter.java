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
	private ImmutableCQContainmentCheckUnderLIDs containmentCheckUnderLIDs;

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

	private ImmutableList<DataAtom<RDFAtomPredicate>> getAtomsForGenerators(Stream<TreeWitnessGenerator> gens, VariableOrGroundTerm r0)  {
		return gens
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

	private IQ getCanonicalForm(IQ query) {
        ClassifiedTBox tbox = reasoner.getClassifiedTBox();

        IQTree canonicalTree = query.getTree().acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformIntensionalData(IntensionalDataNode dataNode) {
                // TODO: support quads
                DataAtom<AtomPredicate> atom = dataNode.getProjectionAtom();
                TriplePredicate triplePredicate = (TriplePredicate) atom.getPredicate();
                ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

                // the contains tests are inefficient, but tests fails without them
                // p.isClass etc. do not work correctly -- throw exceptions because COL_TYPE is null

                Optional<IRI> classIRI = triplePredicate.getClassIRI(arguments);
                if (classIRI.isPresent()) {
                    if (tbox.classes().contains(classIRI.get())) {
                        OClass c = tbox.classes().get(classIRI.get());
                        OClass equivalent = (OClass) tbox.classesDAG().getCanonicalForm(c);
                        return dataNode.newAtom(atomFactory.getIntensionalTripleAtom(arguments.get(0), equivalent.getIRI()));
                    }
                    return  dataNode;
                }

                Optional<IRI> propertyIRI = triplePredicate.getPropertyIRI(arguments);
                if (propertyIRI.isPresent()) {
                    if (tbox.objectProperties().contains(propertyIRI.get())) {
                        ObjectPropertyExpression ope = tbox.objectProperties().get(propertyIRI.get());
                        ObjectPropertyExpression equivalent = tbox.objectPropertiesDAG().getCanonicalForm(ope);
                        return dataNode.newAtom((!equivalent.isInverse())
                                ? atomFactory.getIntensionalTripleAtom(arguments.get(0), equivalent.getIRI(), arguments.get(2))
                                : atomFactory.getIntensionalTripleAtom(arguments.get(2), equivalent.getIRI(), arguments.get(0)));
                    }
                    else if (tbox.dataProperties().contains(propertyIRI.get())) {
                        DataPropertyExpression dpe = tbox.dataProperties().get(propertyIRI.get());
                        DataPropertyExpression equivalent = tbox.dataPropertiesDAG().getCanonicalForm(dpe);
                        return dataNode.newAtom(atomFactory.getIntensionalTripleAtom(arguments.get(0), equivalent.getIRI(), arguments.get(2)));
                    }
                    return  dataNode;
                }
                throw new MinorOntopInternalBugException("Unknown type of triple atoms");
            }
        });

        return iqFactory.createIQ(query.getProjectionAtom(), canonicalTree);
    }

    private IQTree convertCQ(CQ cq, Optional<ImmutableExpression> filter) {

        ImmutableList<IQTree> body = cq.atoms.stream()
                .map(a -> iqFactory.createIntensionalDataNode((DataAtom<AtomPredicate>)(DataAtom)a))
                .collect(ImmutableCollectors.toList());

        IQTree result = (body.size() == 1)
                ? ((filter.isPresent())
                    ? iqFactory.createUnaryIQTree(iqFactory.createFilterNode(filter.get()), body.get(0))
                    : body.get(0))
                : iqFactory.createNaryIQTree(iqFactory.createInnerJoinNode(filter), body);

        ImmutableMap<Variable, VariableOrGroundTerm> equalities = cq.equalities.entrySet().stream()
                .filter(e -> e.getKey() != e.getValue())
                .collect(ImmutableCollectors.toMap(e -> (Variable)e.getKey(), Map.Entry::getValue));

        if (equalities.isEmpty())
            return result;
        else {
            ImmutableSubstitution substitution = substitutionFactory.getSubstitution(equalities);
            return iqFactory.createUnaryIQTree(
                    iqFactory.createConstructionNode(
                            Sets.union(result.getVariables(), substitution.getDomain()).immutableCopy(),
                            substitution), result);
        }
    }

	@Override
    public IQ rewrite(IQ query) throws EmptyQueryException {
		
		double startime = System.currentTimeMillis();

		IQ canonicalQuery = getCanonicalForm(query);

        IQTree rewriting = canonicalQuery.getTree().acceptTransformer(new DefaultRecursiveIQTreeVisitingTransformer(iqFactory) {
            @Override
            public IQTree transformConstruction(IQTree tree, ConstructionNode rootNode, IQTree child) {
                // fix some order on variables
                ImmutableList<Variable> avs = ImmutableList.copyOf(rootNode.getVariables());
                ImmutableSubstitution<ImmutableTerm> substitution = rootNode.getSubstitution();
                ImmutableList<Variable> avs1 = (ImmutableList<Variable>) substitution.apply(avs);
                ImmutableList<DataAtom<RDFAtomPredicate>> bgp;
                Optional<ImmutableExpression> filter;
                if ((child.getRootNode() instanceof InnerJoinNode)
                        && child.getChildren().stream()
                        .allMatch(c -> c.getRootNode() instanceof IntensionalDataNode)) {
                    bgp = child.getChildren().stream()
                                    .map(c -> (DataAtom<RDFAtomPredicate>)(DataAtom)((IntensionalDataNode)c.getRootNode()).getProjectionAtom())
                                    .collect(ImmutableCollectors.toList());
                    filter = ((InnerJoinNode)child.getRootNode()).getOptionalFilterCondition();
                }
                else if (child.getRootNode() instanceof IntensionalDataNode) {
                    bgp = ImmutableList.of((DataAtom<RDFAtomPredicate>)(DataAtom)((IntensionalDataNode)child.getRootNode()).getProjectionAtom());
                    filter = Optional.empty();
                }
                else {
                    return super.transformConstruction(tree, rootNode, child);
                }

                List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(bgp, ImmutableSet.copyOf(avs1));

                ImmutableList<CQ> ucq = ccs.stream()
                        .map(cc -> rewriteCC(cc).stream())
                        .collect(toUCQ());

                IQTree result = (ucq.size() == 1)
                        ? convertCQ(ucq.get(0), filter)
                        : iqFactory.createNaryIQTree(
                                iqFactory.createUnionNode(rootNode.getVariables()),
                                ucq.stream().map(cq -> convertCQ(cq, filter)).collect(ImmutableCollectors.toList()));

                return iqFactory.createUnaryIQTree(rootNode, result);
            }
        });

        IQ convertedIQ = iqFactory.createIQ(canonicalQuery.getProjectionAtom(), rewriting);

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
