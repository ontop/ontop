package it.unibz.inf.ontop.spec.mapping.transformer.impl;

import com.google.common.collect.*;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.tools.impl.IQ2CQ;
import it.unibz.inf.ontop.model.atom.*;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.*;
import java.util.stream.Stream;


public class TMappingRule {

	private final DistinctVariableOnlyDataAtom projectionAtom;
	private final Substitution<ImmutableTerm> substitution;

	private final ImmutableList<ExtensionalDataNode> extensionalNodes;
	// an OR-connected list of AND-connected atomic filters
	private final ImmutableList<ImmutableList<ImmutableExpression>> filter;

	public TMappingRule(IQ iq, CoreSingletons coreSingletons) {
		this.projectionAtom = iq.getProjectionAtom();
		this.substitution = ((ConstructionNode)iq.getTree().getRootNode()).getSubstitution();

		IQTree tree = iq.getTree().getChildren().get(0);
		ImmutableList<ExtensionalDataNode> dataAtoms = IQ2CQ.getExtensionalDataNodes(tree, coreSingletons);
		ImmutableSet<ImmutableExpression> joinConditions = IQ2CQ.getFilterExpressions(tree);

		// maps all non-variables to fresh variables
		//    this is required for more extensive use of OR
		//    for example R(x,y) :- T(x,y,22) and R(x,y) :- T(x,y,23) will be replaced by
		//    R(x,y) :- T(x,y,z) AND ((z = 22) OR (z = 23))
		// without the replacement below, the database parts of the two assertion bodies
		// will not be homomorphically equivalent

		VariableGenerator variableGenerator = iq.getVariableGenerator();
		ImmutableMap<ImmutableTerm, VariableOrGroundTerm> valueMap = dataAtoms.stream()
				.flatMap(n -> n.getArgumentMap().values().stream())
				.filter(t -> !(t instanceof Variable))
				.distinct()
				.collect(ImmutableCollectors.toMap(t -> t, t -> variableGenerator.generateNewVariable()));

		IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();

		this.extensionalNodes = dataAtoms.stream()
					.map(n -> iqFactory.createExtensionalDataNode(
							n.getRelationDefinition(),
							n.getArgumentMap().entrySet().stream()
									.collect(ImmutableCollectors.toMap(
											Map.Entry::getKey,
											e -> valueMap.getOrDefault(e.getValue(), e.getValue())))))
					.collect(ImmutableCollectors.toList());

		TermFactory termFactory = coreSingletons.getTermFactory();

		ImmutableList<ImmutableExpression> filterAtoms = Stream.concat(
				joinConditions.stream(),
				valueMap.entrySet().stream()
					.map(e -> termFactory.getStrictEquality(e.getKey(), e.getValue())))
				.collect(ImmutableCollectors.toList());

		this.filter = filterAtoms.isEmpty() ? ImmutableList.of() : ImmutableList.of(filterAtoms);
	}

	TMappingRule(TMappingRule baseRule, ImmutableList<ImmutableList<ImmutableExpression>> filter) {
		this.projectionAtom = baseRule.projectionAtom;
		this.substitution = baseRule.substitution;

		this.extensionalNodes = baseRule.extensionalNodes;
		this.filter = filter;
	}


	private TMappingRule(Substitution<ImmutableTerm> substitution, TMappingRule baseRule) {
		this.projectionAtom = baseRule.projectionAtom;
		this.substitution = substitution;

		this.extensionalNodes = baseRule.extensionalNodes;
		this.filter = baseRule.filter;
	}

	TMappingRule createCopy(Substitution<ImmutableTerm> substitution) {
		return new TMappingRule(substitution, this);
	}

	public IQ asIQ(CoreSingletons coreSingletons) {

		IntermediateQueryFactory iqFactory = coreSingletons.getIQFactory();
		TermFactory termFactory = coreSingletons.getTermFactory();
		SubstitutionFactory substitutionFactory = coreSingletons.getSubstitutionFactory();

		// assumes that filterAtoms is a possibly empty list of non-empty lists
		Optional<ImmutableExpression> mergedConditions = termFactory.getDisjunction(
				filter.stream().map(termFactory::getConjunction));

		return iqFactory.createIQ(projectionAtom,
				iqFactory.createUnaryIQTree(
						iqFactory.createConstructionNode(projectionAtom.getVariables(), substitution),
						IQ2CQ.toIQTree(extensionalNodes, mergedConditions, coreSingletons)));
	}

	public ImmutableList<ImmutableTerm> getHeadTerms() { return substitution.applyToTerms(projectionAtom.getArguments());  }

	public DistinctVariableOnlyDataAtom getProjectionAtom() { return projectionAtom; }

	public Substitution<ImmutableTerm> getSubstitution() { return substitution; }

	public ImmutableList<ExtensionalDataNode> getDatabaseAtoms() { return extensionalNodes; }

	public ImmutableList<ImmutableList<ImmutableExpression>> getConditions() { return filter; }

	// there is a .contains() method call that relies on equals

	@Override
	public int hashCode() {
		return Objects.hash(substitution, extensionalNodes, filter);
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof TMappingRule) {
			TMappingRule otherRule = (TMappingRule)other;
			return (projectionAtom.equals(otherRule.projectionAtom) &&
					substitution.equals(otherRule.substitution) &&
					extensionalNodes.equals(otherRule.extensionalNodes) &&
					filter.equals(otherRule.filter));
		}
		return false;
	}

	@Override
	public String toString() {
		return projectionAtom.getPredicate() + "(" + getHeadTerms() + ") <- " + extensionalNodes + " FILTER " + filter;
	}
}
