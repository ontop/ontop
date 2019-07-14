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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.answering.reformulation.rewriting.ExistentialQueryRewriter;
import it.unibz.inf.ontop.constraints.ImmutableCQ;
import it.unibz.inf.ontop.constraints.impl.ImmutableCQContainmentCheckUnderLIDs;
import it.unibz.inf.ontop.datalog.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.transform.impl.DefaultRecursiveIQTreeVisitingTransformer;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.atom.TriplePredicate;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.Predicate;
import it.unibz.inf.ontop.model.term.impl.ImmutabilityTools;
import it.unibz.inf.ontop.model.term.impl.TermUtils;
import it.unibz.inf.ontop.spec.ontology.*;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Edge;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.QueryConnectedComponent.Loop;
import it.unibz.inf.ontop.answering.reformulation.rewriting.impl.TreeWitnessSet.CompatibleTreeWitnessSetIterator;

import java.util.*;

import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.impl.SubstitutionUtilities;
import it.unibz.inf.ontop.substitution.impl.UnifierUtilities;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 */

public class TreeWitnessRewriter extends DummyRewriter implements ExistentialQueryRewriter {

	private static final Logger log = LoggerFactory.getLogger(TreeWitnessRewriter.class);

	private ClassifiedTBox reasoner;
	private Collection<TreeWitnessGenerator> generators;
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
	public void setTBox(ClassifiedTBox reasoner) {
		double startime = System.currentTimeMillis();

		this.reasoner = reasoner;
		super.setTBox(reasoner);

        containmentCheckUnderLIDs = new ImmutableCQContainmentCheckUnderLIDs(getSigma());

		generators = TreeWitnessGenerator.getTreeWitnessGenerators(reasoner);
		
		double endtime = System.currentTimeMillis();
		double tm = (endtime - startime) / 1000;
		time += tm;
		log.debug(String.format("setTBox time: %.3f s (total %.3f s)", tm, time));		
	}
	
	
	/*
	 * returns an atom with given arguments and the predicate name formed by the given URI basis and string fragment
	 */
	
	private Function getHeadAtom(String base, String suffix, ImmutableList<Variable> arguments) {
		Predicate predicate = datalogFactory.getSubqueryPredicate(base + suffix, arguments.size());
		return termFactory.getFunction(predicate, new ArrayList(arguments));
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

	private ImmutableList<Function> getAtomsForGenerators(Collection<TreeWitnessGenerator> gens, VariableOrGroundTerm r0)  {
		return TreeWitnessGenerator.getMaximalBasicConcepts(gens, reasoner).stream()
				.map(con -> {
					log.debug("  BASIC CONCEPT: {}", con);
					if (con instanceof OClass) {
						return atomFactory.getMutableTripleBodyAtom(immutabilityTools.convertToMutableTerm(r0), ((OClass) con).getIRI());
					}
					else if (con instanceof ObjectSomeValuesFrom) {
						ObjectPropertyExpression ope = ((ObjectSomeValuesFrom)con).getProperty();
						return (!ope.isInverse())
								? atomFactory.getMutableTripleBodyAtom(immutabilityTools.convertToMutableTerm(r0), ope.getIRI(), getFreshVariable())
								: atomFactory.getMutableTripleBodyAtom(getFreshVariable(), ope.getIRI(), immutabilityTools.convertToMutableTerm(r0));
					}
					else {
						DataPropertyExpression dpe = ((DataSomeValuesFrom)con).getProperty();
						return atomFactory.getMutableTripleBodyAtom(immutabilityTools.convertToMutableTerm(r0), dpe.getIRI(), getFreshVariable());
					}
				})
				.collect(ImmutableCollectors.toList());
	}
	
	
	
	/*
	 * rewrites a given connected CQ with the rules put into output
	 */
	
	private Collection<CQIE> rewriteCC(QueryConnectedComponent cc, Function headAtom,  Multimap<Predicate, CQIE> edgeDP) {
		
		List<CQIE> outputRules = new LinkedList<>();	

		TreeWitnessSet tws = TreeWitnessSet.getTreeWitnesses(cc, reasoner, generators);

		if (cc.hasNoFreeTerms()) {  
			if (!cc.isDegenerate() || cc.getLoop() != null) 
				for (Function a : getAtomsForGenerators(tws.getGeneratorsOfDetachedCC(), getFreshVariable())) {
					outputRules.add(datalogFactory.getCQIE(headAtom, a));
				}
		}

		Map<TreeWitness.TermCover, List<List<Function>>> treeWitnessFormulas = new HashMap<>();

		// COMPUTE AND STORE TREE WITNESS FORMULAS
		for (TreeWitness tw : tws.getTWs()) {
			log.debug("TREE WITNESS: {}", tw);		
			List<Function> twf = new LinkedList<>();
			
			// equality atoms
			Iterator<VariableOrGroundTerm> i = tw.getRoots().iterator();
			VariableOrGroundTerm r0 = i.next();
			while (i.hasNext()) 
				twf.add(termFactory.getFunctionEQ(immutabilityTools.convertToMutableTerm(i.next()), immutabilityTools.convertToMutableTerm(r0)));
			
			// root atoms
			for (DataAtom<RDFAtomPredicate> a : tw.getRootAtoms()) {
				if (!(a.getPredicate() instanceof TriplePredicate))
					throw new MinorOntopInternalBugException("A triple atom was expected: " + a);

				twf.add(a.getPredicate().getClassIRI(a.getArguments()).isPresent()
						? atomFactory.getMutableTripleAtom(immutabilityTools.convertToMutableTerm(r0), immutabilityTools.convertToMutableTerm(a.getTerm(1)), immutabilityTools.convertToMutableTerm(a.getTerm(2)))
						: atomFactory.getMutableTripleAtom(immutabilityTools.convertToMutableTerm(r0), immutabilityTools.convertToMutableTerm(a.getTerm(1)), immutabilityTools.convertToMutableTerm(r0)));
			}
			
			List<Function> genAtoms = getAtomsForGenerators(tw.getGenerators(), r0);			
			boolean subsumes = false;
//			for (Function a : genAtoms) 				
//				if (twf.subsumes(a)) {
//					subsumes = true;
//					log.debug("TWF {} SUBSUMES {}", twf.getAllAtoms(), a);
//					break;
//				}

			List<List<Function>> twfs = new ArrayList<>(subsumes ? 1 : genAtoms.size());
//			if (!subsumes) {
				for (Function a : genAtoms) {				
					LinkedList<Function> twfa = new LinkedList<>(twf);
					twfa.add(a); // 
					twfs.add(twfa);
				}
//			}
//			else
//				twfs.add(twf.getAllAtoms());
			
			treeWitnessFormulas.put(tw.getTerms(), twfs);
		}

		final String headURI = headAtom.getFunctionSymbol().getName();
		if (!cc.isDegenerate()) {
			if (tws.hasConflicts()) { 
				// there are conflicting tree witnesses
				// use compact exponential rewriting by enumerating all compatible subsets of tree witnesses
				CompatibleTreeWitnessSetIterator iterator = tws.getIterator();
				while (iterator.hasNext()) {
					Collection<TreeWitness> compatibleTWs = iterator.next();
					log.debug("COMPATIBLE: {}", compatibleTWs);
					LinkedList<Function> mainbody = new LinkedList<Function>(); 
					
					for (Edge edge : cc.getEdges()) {
						boolean contained = false;
						for (TreeWitness tw : compatibleTWs)
							if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
								contained = true;
								log.debug("EDGE {} COVERED BY {}", edge, tw);
								break;
							}
						if (!contained) {
							log.debug("EDGE {} NOT COVERED BY ANY TW",  edge);
							mainbody.addAll(edge.getAtoms(immutabilityTools));
						}
					}
					for (TreeWitness tw : compatibleTWs) {
						Function twAtom = getHeadAtom(headURI, "_TW_" + (edgeDP.size() + 1), cc.getVariables());
						mainbody.add(twAtom);				
						for (List<Function> twfa : treeWitnessFormulas.get(tw.getTerms()))
							edgeDP.put(twAtom.getFunctionSymbol(), datalogFactory.getCQIE(twAtom, twfa));
					}	
					mainbody.addAll(cc.getNonDLAtoms());					
					outputRules.add(datalogFactory.getCQIE(headAtom, mainbody));
				}
			}
			else {
				// no conflicting tree witnesses
				// use polynomial tree witness rewriting by treating each edge independently 
				LinkedList<Function> mainbody = new LinkedList<>();
				for (Edge edge : cc.getEdges()) {
					log.debug("EDGE {}", edge);
					
					Function edgeAtom = null;
					for (TreeWitness tw : tws.getTWs())
						if (tw.getDomain().contains(edge.getTerm0()) && tw.getDomain().contains(edge.getTerm1())) {
							if (edgeAtom == null) {
								//IRI atomURI = edge.getBAtoms().iterator().next().getIRI().getName();
								edgeAtom = getHeadAtom(headURI, 
										"_EDGE_" + (edgeDP.size() + 1) /*+ "_" + atomURI.getRawFragment()*/, cc.getVariables());
								mainbody.add(edgeAtom);				
								
								LinkedList<Function> edgeAtoms = new LinkedList<>();
								edgeAtoms.addAll(edge.getAtoms(immutabilityTools));
								edgeDP.put(edgeAtom.getFunctionSymbol(), datalogFactory.getCQIE(edgeAtom, edgeAtoms));
							}
							
							for (List<Function> twfa : treeWitnessFormulas.get(tw.getTerms()))
								edgeDP.put(edgeAtom.getFunctionSymbol(), datalogFactory.getCQIE(edgeAtom, twfa));
						}
					
					if (edgeAtom == null) // no tree witnesses -- direct insertion into the main body
						mainbody.addAll(edge.getAtoms(immutabilityTools));
				}
				mainbody.addAll(cc.getNonDLAtoms());
				outputRules.add(datalogFactory.getCQIE(headAtom, mainbody));
			}
		}
		else {
			// degenerate connected component
			LinkedList<Function> loopbody = new LinkedList<>();
			Loop loop = cc.getLoop();
			log.debug("LOOP {}", loop);
			if (loop != null)
				loopbody.addAll(loop.getAtoms().stream().map(a -> immutabilityTools.convertToMutableFunction(a)).collect(ImmutableCollectors.toList()));
			loopbody.addAll(cc.getNonDLAtoms());
			outputRules.add(datalogFactory.getCQIE(headAtom, loopbody));
		}
		return outputRules;
	}
	
	private double time = 0;
	
	@Override
    public IQ rewrite(IQ query) throws EmptyQueryException {
		
		double startime = System.currentTimeMillis();

		DatalogProgram program = iqConverter.translate(query);

		List<CQIE> outputRules = new LinkedList<>();
		Multimap<Predicate, CQIE> ccDP = null;
		Multimap<Predicate, CQIE> edgeDP = ArrayListMultimap.create();

		for (CQIE cqie : program.getRules()) {
			List<QueryConnectedComponent> ccs = QueryConnectedComponent.getConnectedComponents(reasoner, cqie,
					atomFactory, immutabilityTools);
			Function cqieAtom = cqie.getHead();
		
			if (ccs.size() == 1) {
				QueryConnectedComponent cc = ccs.iterator().next();
				log.debug("CONNECTED COMPONENT ({}) EXISTS {}", cc.getFreeVariables(), cc.getQuantifiedVariables());
				log.debug("     WITH EDGES {} AND LOOP {}", cc.getEdges(), cc.getLoop());
				log.debug("     NON-DL ATOMS {}", cc.getNonDLAtoms());
				outputRules.addAll(rewriteCC(cc, cqieAtom, edgeDP)); 				
			}
			else {
				if (ccDP == null)
					ccDP = ArrayListMultimap.create();
				String cqieURI = cqieAtom.getFunctionSymbol().getName();
				List<Function> ccBody = new ArrayList<>(ccs.size());
				for (QueryConnectedComponent cc : ccs) {
					log.debug("CONNECTED COMPONENT ({}) EXISTS {}", cc.getFreeVariables(), cc.getQuantifiedVariables());
					log.debug("     WITH EDGES {} AND LOOP {}", cc.getEdges(), cc.getLoop());
					log.debug("     NON-DL ATOMS {}", cc.getNonDLAtoms());
					Function ccAtom = getHeadAtom(cqieURI, "_CC_" + (ccDP.size() + 1), cc.getFreeVariables());
					for (CQIE cq : rewriteCC(cc, ccAtom, edgeDP))
					    ccDP.put(cq.getHead().getFunctionSymbol(), cq);
					ccBody.add(ccAtom);
				}
				outputRules.add(datalogFactory.getCQIE(cqieAtom, ccBody));
			}
		}
		
		log.debug("REWRITTEN PROGRAM\n{}CC DEFS\n{}", outputRules, ccDP);
		if (!edgeDP.isEmpty()) {
			log.debug("EDGE DEFS\n{}", edgeDP);			
			outputRules = plugInDefinitions(outputRules, edgeDP);
			if (ccDP != null) {
			    Multimap<Predicate, CQIE> ccDP2 = ArrayListMultimap.create();
			    for (Predicate p : ccDP.keys())
			        for (CQIE cq : plugInDefinitions(ccDP.get(p), edgeDP))
                        ccDP2.put(p, cq);
			    ccDP = ccDP2;
            }
			log.debug("INLINE EDGE PROGRAM\n{}CC DEFS\n{}", outputRules, ccDP);
		}
		if (ccDP != null) {
			outputRules = plugInDefinitions(outputRules, ccDP);
			log.debug("INLINE CONNECTED COMPONENTS PROGRAM\n{}", outputRules);
		}

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


    private List<CQIE> plugInDefinitions(Collection<CQIE> rules, Multimap<Predicate, CQIE> defs) {

        PriorityQueue<CQIE> queue = new PriorityQueue<>(rules.size(),
                Comparator.comparingInt(cq -> cq.getBody().size()));

        queue.addAll(rules);

        List<CQIE> output = new LinkedList<>();

        while (!queue.isEmpty()) {
            CQIE query = queue.poll();

            List<Function> body = query.getBody();
            int chosenAtomIdx = 0;
            Collection<CQIE> chosenDefinitions = null;
            ListIterator<Function> bodyIterator = body.listIterator();
            while (bodyIterator.hasNext()) {
                Function currentAtom = bodyIterator.next();
                Collection<CQIE> definitions = defs.get(currentAtom.getFunctionSymbol());
                if (!definitions.isEmpty()) {
                    if ((chosenDefinitions == null) || (chosenDefinitions.size() < definitions.size())) {
                        chosenDefinitions = definitions;
                        chosenAtomIdx = bodyIterator.previousIndex();
                    }
                }
            }

            boolean replaced = false;
            if (chosenDefinitions != null) {
                Collection<Variable> vars = new ArrayList<>();
                for (Function atom : body)
                    TermUtils.addReferencedVariablesTo(vars, atom);
                int maxlen = vars.stream()
                        .map(v -> v.getName().length())
                        .max(Integer::compareTo)
                        .orElse(0);
                String suffix = Strings.repeat("t", maxlen);

                for (CQIE rule : chosenDefinitions) {
                    Substitution mgu = unifierUtilities.getMGU(getFreshAtom(rule.getHead(), suffix),
                            query.getBody().get(chosenAtomIdx));
                    if (mgu != null) {
                        CQIE newquery = query.clone();
                        List<Function> newbody = newquery.getBody();
                        newbody.remove(chosenAtomIdx);
                        for (Function a : rule.getBody())
                            newbody.add(getFreshAtom(a, suffix));

						// apply substitutions in-place
						Function head = newquery.getHead();
						SubstitutionUtilities.applySubstitution(head, mgu);
						for (Function bodyatom : newquery.getBody())
							SubstitutionUtilities.applySubstitution(bodyatom, mgu);

                        // REDUCE
                        eqNormalizer.enforceEqualities(newquery);
                        removeRundantAtoms(newquery);

                        queue.add(newquery);
                        replaced = true;
                    }

                }
            }
            if (!replaced) {
                boolean found = false;
                ListIterator<CQIE> i = output.listIterator();
                while (i.hasNext()) {
                    CQIE q2 = i.next();
                    if (isContainedIn(query, q2)) {
                        found = true;
                        break;
                    }
                    else if (isContainedIn(q2, query)) {
                        i.remove();
                        log.debug("   PRUNED {} BY {}", q2, query);
                    }
                }

                if (!found) {
                    log.debug("ADDING TO THE RESULT {}", query);
                    output.add(query.clone());
                    Collections.sort(output, Comparator.comparingInt(cq -> cq.getBody().size()));
                }
            }
        }

        return output;
    }

    private void removeRundantAtoms(CQIE q) {
	    // TODO: use sets instead
	    for (int i = 0; i < q.getBody().size(); i++)
	        for (int j = i + 1; j < q.getBody().size(); j++) {
	            if (q.getBody().get(i).equals(q.getBody().get(j))) {
                    q.getBody().remove(j);
                    j--;
                }
            }
    }

    private Function getFreshAtom(Function a, String suffix) {
        List<Term> termscopy = new ArrayList<>(a.getArity());

        for (Term t : a.getTerms()) {
            if (t instanceof Variable) {
                Variable v = (Variable)t;
                termscopy.add(termFactory.getVariable(v.getName() + suffix));
            }
            else
                termscopy.add(t.clone());
        }
        return termFactory.getFunction(a.getFunctionSymbol(), termscopy);
    }

    /**
     * Check if query cq1 is contained in cq2, syntactically. That is, if the
     * head of cq1 and cq2 are equal according to toString().equals and each
     * atom in cq2 is also in the body of cq1 (also by means of toString().equals().
     */

    private static boolean isContainedIn(CQIE cq1, CQIE cq2) {
        if (!cq2.getHead().equals(cq1.getHead()))
            return false;

        for (Function atom : cq2.getBody())
            if (!cq1.getBody().contains(atom))
                return false;

        return true;
    }

}
