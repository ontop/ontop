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
import it.unibz.inf.ontop.model.term.functionsymbol.ExpressionOperation;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.spec.mapping.utils.MappingTools;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;
import org.apache.commons.rdf.api.IRI;

import java.util.*;
import java.util.stream.Stream;


public class TMappingRule {

	private final MappingTools.RDFPredicateInfo predicateInfo;

	private final DistinctVariableOnlyDataAtom projectionAtom;
	private final ImmutableSubstitution substitution;

	private final ImmutableList<ExtensionalDataNode> databaseAtoms;
	// an OR-connected list of AND-connected atomic filters
	private final ImmutableList<ImmutableList<ImmutableExpression>> filterAtoms;

	private final TermFactory termFactory;

	public TMappingRule(IQ iq, TermFactory termFactory, AtomFactory atomFactory) {

        this.termFactory = termFactory;

		this.predicateInfo = MappingTools.extractRDFPredicate(iq);

		this.projectionAtom = iq.getProjectionAtom();
		ConstructionNode cn = (ConstructionNode)iq.getTree().getRootNode();

		ImmutableSet<ImmutableExpression> joinConditions;
		ImmutableList<ExtensionalDataNode> dataAtoms;

		IQTree tree = iq.getTree().getChildren().get(0);
		if (tree.getRootNode() instanceof FilterNode) {
			joinConditions = ((FilterNode)tree.getRootNode()).getOptionalFilterCondition().get().flattenAND();
			dataAtoms = ImmutableList.of((ExtensionalDataNode)tree.getChildren().get(0));
		}
		else if (tree.getRootNode() instanceof ExtensionalDataNode) {
			joinConditions = ImmutableSet.of();
			dataAtoms = ImmutableList.of((ExtensionalDataNode)tree);
		}
		else if (tree.getRootNode() instanceof TrueNode) {
			joinConditions = ImmutableSet.of();
			dataAtoms = ImmutableList.of();
		}
		else {
			joinConditions = ((InnerJoinNode)tree.getRootNode()).getOptionalFilterCondition().map(e -> e.flattenAND()).orElseGet(ImmutableSet::of);
			dataAtoms = tree.getChildren().stream().map(n -> (ExtensionalDataNode)n).collect(ImmutableCollectors.toList());
		}

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
				.collect(ImmutableCollectors.toSet()).stream()
				.collect(ImmutableCollectors.toMap(t -> t, t -> variableGenerator.generateNewVariable()));

		this.databaseAtoms = dataAtoms.stream()
					.map(n -> n.newAtom(atomFactory.getDataAtom(
							n.getProjectionAtom().getPredicate(),
							n.getProjectionAtom().getArguments().stream()
									.map(term -> valueMap.getOrDefault(term, term))
									.collect(ImmutableCollectors.toList()))))
					.collect(ImmutableCollectors.toList());

		ImmutableList<ImmutableExpression> f = Stream.concat(
				joinConditions.stream(),
				valueMap.entrySet().stream()
					.map(e -> termFactory.getImmutableExpression(ExpressionOperation.EQ, e.getKey(), e.getValue())))
				.collect(ImmutableCollectors.toList());

		this.filterAtoms = f.isEmpty() ? ImmutableList.of() : ImmutableList.of(f);

		this.substitution = cn.getSubstitution();
	}



	TMappingRule(TMappingRule baseRule, ImmutableList<ImmutableList<ImmutableExpression>> filterAtoms) {
        this.termFactory = baseRule.termFactory;

        this.predicateInfo = baseRule.predicateInfo;
        this.projectionAtom = baseRule.projectionAtom;

		this.databaseAtoms = baseRule.databaseAtoms;
		this.substitution = baseRule.substitution;

		this.filterAtoms = filterAtoms;
	}
	
	
	TMappingRule(ImmutableList<ImmutableTerm> headTerms, MappingTools.RDFPredicateInfo predicateInfo, TMappingRule baseRule, SubstitutionFactory substitutionFactory) {
        this.termFactory = baseRule.termFactory;

		this.predicateInfo = predicateInfo;
		this.projectionAtom = baseRule.projectionAtom;

		this.databaseAtoms = baseRule.databaseAtoms;
		if (predicateInfo.isClass()) {
			substitution = substitutionFactory.getSubstitution(
					projectionAtom.getTerm(0), headTerms.get(0),
					projectionAtom.getTerm(1), getConstantIRI(RDF.TYPE),
					projectionAtom.getTerm(2), getConstantIRI(predicateInfo.getIri()));
		}
		else if (headTerms.get(1) instanceof Variable) {
			if (!headTerms.get(1).equals(projectionAtom.getTerm(1)))
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

		this.filterAtoms = baseRule.filterAtoms;
	}

	private ImmutableFunctionalTerm getConstantIRI(IRI iri) {
		return termFactory.getImmutableUriTemplate(
				termFactory.getConstantLiteral(iri.getIRIString()));
	}



	public IRI getIri() { return predicateInfo.getIri(); }

	public MappingTools.RDFPredicateInfo getPredicateInfo() { return predicateInfo; }


	public IQ asIQ(IntermediateQueryFactory iqFactory) {

		// assumes that filterAtoms is a possibly empty list of non-empty lists
		Optional<ImmutableExpression> mergedConditions = filterAtoms.stream()
				.map(list -> list.stream()
						.reduce((r, e) -> termFactory.getImmutableExpression(ExpressionOperation.AND, e, r)).get())
				.reduce((r, e) -> termFactory.getImmutableExpression(ExpressionOperation.OR, e, r));

		IQTree tree;
		switch (databaseAtoms.size()) {
			case 0:
				tree = iqFactory.createTrueNode();
				break;
			case 1:
				tree = (mergedConditions.isPresent()
						? iqFactory.createUnaryIQTree(
								iqFactory.createFilterNode(mergedConditions.get()),
								databaseAtoms.get(0))
						: databaseAtoms.get(0));
				break;
			default:
				tree = iqFactory.createNaryIQTree(
						iqFactory.createInnerJoinNode(mergedConditions),
						databaseAtoms.stream().collect(ImmutableCollectors.toList()));
		}

		return iqFactory.createIQ(projectionAtom,
				iqFactory.createUnaryIQTree(
						iqFactory.createConstructionNode(projectionAtom.getVariables(), substitution), tree));
	}

	public ImmutableList<ImmutableTerm> getHeadTerms() {
        return substitution.apply(predicateInfo.isClass()
				? ImmutableList.of(projectionAtom.getTerm(0))
				: ImmutableList.of(projectionAtom.getTerm(0), projectionAtom.getTerm(2)));
    }

	public ImmutableList<ExtensionalDataNode> getDatabaseAtoms() {
		return databaseAtoms;
	}

	public ImmutableList<ImmutableList<ImmutableExpression>> getConditions() { return filterAtoms; }

	@Override
	public int hashCode() {
		return predicateInfo.getIri().hashCode() ^ substitution.hashCode() ^ databaseAtoms.hashCode() ^ filterAtoms.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof TMappingRule) {
			TMappingRule otherRule = (TMappingRule)other;
			return (projectionAtom.getArguments().equals(otherRule.projectionAtom.getArguments()) &&
					substitution.equals(otherRule.substitution) &&
					databaseAtoms.equals(otherRule.databaseAtoms) && 
					filterAtoms.equals(otherRule.filterAtoms));
		}
		return false;
	}

	@Override 
	public String toString() {
		return predicateInfo.getIri() + "(" + substitution.apply(projectionAtom.getArguments()) + ") <- " + databaseAtoms + " AND " + filterAtoms;
	}
}
