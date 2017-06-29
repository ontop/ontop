package it.unibz.inf.ontop.pivotalrepr.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.MissingRelationForExtensionalDataNodeException;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.pivotalrepr.*;
import it.unibz.inf.ontop.pivotalrepr.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.pivotalrepr.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.sql.Attribute;
import it.unibz.inf.ontop.sql.DatabaseRelationDefinition;
import it.unibz.inf.ontop.sql.Relation2DatalogPredicate;
import it.unibz.inf.ontop.sql.RelationID;

import java.util.stream.IntStream;

/**
 * "Default" implementation for an extensional data node.
 *
 * Most likely (but not necessarily) will be overwritten by native query language specific implementations.
 */
public class ExtensionalDataNodeImpl extends DataNodeImpl implements ExtensionalDataNode {

    private static final String EXTENSIONAL_NODE_STR = "EXTENSIONAL";

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted DataAtom atom) {
        super(atom);
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExtensionalDataNode clone() {
        return new ExtensionalDataNodeImpl(getProjectionAtom());
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
        return applySubstitution((ExtensionalDataNode) this, substitution);
    }

    @Override
    public SubstitutionResults<ExtensionalDataNode> applyDescendingSubstitution(
            ImmutableSubstitution<? extends ImmutableTerm> substitution, IntermediateQuery query) {
        return applySubstitution((ExtensionalDataNode)this, substitution);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        if (!getVariables().contains(variable))
            throw new IllegalArgumentException("The variable " + variable + " is not projected by " + this);

        DBMetadata metadata = query.getDBMetadata();
        DataAtom atom = getProjectionAtom();

        RelationID relationId = Relation2DatalogPredicate.createRelationFromPredicateName(
                metadata.getQuotedIDFactory(),
                atom.getPredicate());
        DatabaseRelationDefinition relation = metadata.getDatabaseRelation(relationId);

        if (relation == null)
            throw new MissingRelationForExtensionalDataNodeException("Bug: required relation for " + this + " not found");

        ImmutableList<? extends VariableOrGroundTerm> arguments = atom.getArguments();

        // NB: DB column indexes start at 1.
        return IntStream.range(1, arguments.size() + 1)
                .filter(i -> arguments.get(i - 1).equals(variable))
                .mapToObj(relation::getAttribute)
                .allMatch(Attribute::canNull);


    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        return (node instanceof ExtensionalDataNode)
                && ((ExtensionalDataNode) node).getProjectionAtom().equals(this.getProjectionAtom());
    }


    @Override
    public String toString() {
        return EXTENSIONAL_NODE_STR + " " + getProjectionAtom();
    }

    @Override
    public ExtensionalDataNode newAtom(DataAtom newAtom) {
        return new ExtensionalDataNodeImpl(newAtom);
    }
}
