package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

/**
 * "Default" implementation for an extensional data node.
 *
 * Most likely (but not necessarily) will be overwritten by native query language specific implementations.
 */
public class ExtensionalDataNodeImpl extends DataNodeImpl<RelationPredicate> implements ExtensionalDataNode {

    private static final String EXTENSIONAL_NODE_STR = "EXTENSIONAL";

    // LAZY
    @Nullable
    private ImmutableSet<Variable> nullableVariables;

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted DataAtom<RelationPredicate> atom,
                                    IQTreeTools iqTreeTools, IntermediateQueryFactory iqFactory) {
        super(atom, iqTreeTools, iqFactory);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExtensionalDataNode clone() {
        return iqFactory.createExtensionalDataNode(getProjectionAtom());
    }

    @Override
    public ExtensionalDataNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer) throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public NodeTransformationProposal acceptNodeTransformer(HeterogeneousQueryNodeTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public SubstitutionResults<ExtensionalDataNode> applyAscendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution,
            QueryNode childNode, IntermediateQuery query) {
        return applySubstitution(this, substitution);
    }

    @Override
    public SubstitutionResults<ExtensionalDataNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        return applySubstitution(this, substitution);
    }

    @Override
    public ExtensionalDataNode newAtom(DataAtom<RelationPredicate> newAtom) {
        return iqFactory.createExtensionalDataNode(newAtom);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (!getVariables().contains(variable))
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        DataAtom<RelationPredicate> atom = getProjectionAtom();

        RelationDefinition relation = atom.getPredicate().getRelationDefinition();

        ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

        // NB: DB column indexes start at 1.
        return IntStream.range(1, arguments.size() + 1)
                .filter(i -> arguments.get(i - 1).equals(variable))
                .mapToObj(relation::getAttribute)
                .allMatch(Attribute::canNull);
    }

    @Override
    public ImmutableSet<Variable> getNullableVariables() {
        if (nullableVariables == null) {
            DataAtom<RelationPredicate> atom = getProjectionAtom();
            RelationDefinition relation = atom.getPredicate().getRelationDefinition();

            ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

            // NB: DB column indexes start at 1.
            nullableVariables = IntStream.range(0, arguments.size())
                    .filter(i -> arguments.get(i) instanceof Variable)
                    .filter(i -> relation.getAttribute(i + 1).canNull())
                    .mapToObj(arguments::get)
                    .map(a -> (Variable) a)
                    .collect(ImmutableCollectors.toSet());
        }
        return nullableVariables;
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ExtensionalDataNode)
                && ((ExtensionalDataNode) node).getProjectionAtom().equals(this.getProjectionAtom());
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return (queryNode instanceof ExtensionalDataNode)
                && getProjectionAtom().equals(((ExtensionalDataNode) queryNode).getProjectionAtom());
    }


    @Override
    public String toString() {
        return EXTENSIONAL_NODE_STR + " " + getProjectionAtom();
    }
}
