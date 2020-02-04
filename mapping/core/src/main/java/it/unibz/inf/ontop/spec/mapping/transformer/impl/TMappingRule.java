package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.MappingAssertionIndex;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.stream.Stream;


public class TMappingRule {

	private final MappingAssertionIndex predicateInfo;

	private final DistinctVariableOnlyDataAtom projectionAtom;
	private final ImmutableSubstitution<ImmutableTerm> substitution;

	private final ImmutableList<ExtensionalDataNode> extensionalNodes;
	// an OR-connected list of AND-connected atomic filters
	private final ImmutableList<ImmutableList<ImmutableExpression>> filter;

	private final TermFactory termFactory;

	public TMappingRule(MappingAssertionIndex predicateInfo, IQ iq, TermFactory termFactory, AtomFactory atomFactory) {
		this.termFactory = termFactory;

		this.predicateInfo = predicateInfo;
		this.projectionAtom = iq.getProjectionAtom();
		this.substitution = ((ConstructionNode)iq.getTree().getRootNode()).getSubstitution();

		IQTree tree = iq.getTree().getChildren().get(0);
		ImmutableList<ExtensionalDataNode> dataAtoms = IQ2CQ.getExtensionalDataNodes(tree).get();
		ImmutableSet<ImmutableExpression> joinConditions = IQ2CQ.getFilterExpressions(tree);

		// maps all non-constants to fresh variables
		//    this is required for more extensive use of OR
		//    for example R(x,y) :- T(x,y,22) and R(x,y) :- T(x,y,23) will be replaced by
		//    R(x,y) :- T(x,y,z) AND ((z = 22) OR (z = 23))
		// without the replacement below, the database parts of the two assertion bodies
		// will not be homomorphically equivalent

		VariableGenerator variableGenerator = iq.getVariableGenerator();
		ImmutableMap<ImmutableTerm, VariableOrGroundTerm> valueMap = dataAtoms.stream()
				.flatMap(n -> n.getProjectionAtom().getArguments().stream())
				.filter(t -> !(t instanceof Variable))
				.distinct()
				.collect(ImmutableCollectors.toMap(t -> t, t -> variableGenerator.generateNewVariable()));

		this.extensionalNodes = dataAtoms.stream()
					.map(n -> n.newAtom(atomFactory.getDataAtom(
							n.getProjectionAtom().getPredicate(),
							n.getProjectionAtom().getArguments().stream()
									.map(term -> valueMap.getOrDefault(term, term))
									.collect(ImmutableCollectors.toList()))))
					.collect(ImmutableCollectors.toList());

		ImmutableList<ImmutableExpression> filterAtoms = Stream.concat(
				joinConditions.stream(),
				valueMap.entrySet().stream()
					.map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue())))
				.collect(ImmutableCollectors.toList());

		this.filter = filterAtoms.isEmpty() ? ImmutableList.of() : ImmutableList.of(filterAtoms);
	}



	TMappingRule(TMappingRule baseRule, ImmutableList<ImmutableList<ImmutableExpression>> filter) {
        this.termFactory = baseRule.termFactory;

        this.predicateInfo = baseRule.predicateInfo;
        this.projectionAtom = baseRule.projectionAtom;
		this.substitution = baseRule.substitution;

		this.extensionalNodes = baseRule.extensionalNodes;
		this.filter = filter;
	}


	TMappingRule(ImmutableList<ImmutableTerm> headTerms, MappingAssertionIndex predicateInfo, TMappingRule baseRule, SubstitutionFactory substitutionFactory) {
        this.termFactory = baseRule.termFactory;

		this.predicateInfo = predicateInfo;
		this.projectionAtom = baseRule.projectionAtom;
		if (predicateInfo.isClass()) {
			substitution = substitutionFactory.getSubstitution(
					projectionAtom.getTerm(0), headTerms.get(0),
					projectionAtom.getTerm(1), getConstantIRI(RDF.TYPE),
					projectionAtom.getTerm(2), getConstantIRI(predicateInfo.getIri()));
		}
		else if (headTerms.get(1) instanceof Variable) {
			if (!headTerms.get(1).equals(projectionAtom.getTerm(2)))
				throw new IllegalStateException("The last argument does not match");

			substitution = substitutionFactory.getSubstitution(
					projectionAtom.getTerm(0), headTerms.get(0),
					projectionAtom.getTerm(1), getConstantIRI(predicateInfo.getIri()));
		}
		else {
			substitution = substitutionFactory.getSubstitution(
					projectionAtom.getTerm(0), headTerms.get(0),
					projectionAtom.getTerm(1), getConstantIRI(predicateInfo.getIri()),
					projectionAtom.getTerm(2), headTerms.get(1));
		}

		this.extensionalNodes = baseRule.extensionalNodes;
		this.filter = baseRule.filter;
	}

	private IRIConstant getConstantIRI(IRI iri) {
		return termFactory.getConstantIRI(iri);
	}



	public MappingAssertionIndex getPredicateInfo() { return predicateInfo; }


	public IQ asIQ(IntermediateQueryFactory iqFactory) {

		// assumes that filterAtoms is a possibly empty list of non-empty lists
		Optional<ImmutableExpression> mergedConditions = filter.stream()
				.map(list -> list.stream()
						.reduce((r, e) -> termFactory.getConjunction(e, r)).get())
				.reduce((r, e) -> termFactory.getDisjunction(e, r));

		return iqFactory.createIQ(projectionAtom,
				iqFactory.createUnaryIQTree(
						iqFactory.createConstructionNode(projectionAtom.getVariables(), substitution),
						IQ2CQ.toIQTree(extensionalNodes, mergedConditions, iqFactory)));
	}

	public ImmutableList<ImmutableTerm> getHeadTerms() {
        return predicateInfo.isClass()
				? ImmutableList.of(substitution.applyToVariable(projectionAtom.getTerm(0)))
				: ImmutableList.of(substitution.applyToVariable(projectionAtom.getTerm(0)),
									substitution.applyToVariable(projectionAtom.getTerm(2)));
    }

	public ImmutableList<ExtensionalDataNode> getDatabaseAtoms() { return extensionalNodes; }

	public ImmutableList<ImmutableList<ImmutableExpression>> getConditions() { return filter; }

	public RDFAtomPredicate getRDFAtomPredicate() { return (RDFAtomPredicate) projectionAtom.getPredicate(); }

	@Override
	public int hashCode() {
		return predicateInfo.getIri().hashCode() ^ substitution.hashCode() ^ extensionalNodes.hashCode() ^ filter.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof TMappingRule) {
			TMappingRule otherRule = (TMappingRule)other;
			return (projectionAtom.getArguments().equals(otherRule.projectionAtom.getArguments()) &&
					substitution.equals(otherRule.substitution) &&
					extensionalNodes.equals(otherRule.extensionalNodes) &&
					filter.equals(otherRule.filter));
		}
		return false;
	}

	@Override 
	public String toString() {
		return predicateInfo.getIri() + "(" + substitution.apply(projectionAtom.getArguments()) + ") <- " + extensionalNodes + " FILTER " + filter;
	}
}
