package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.MissingRelationForExtensionalDataNodeException;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.*;
import it.unibz.inf.ontop.iq.transform.node.HeterogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.ImmutableSubstitution;

import java.util.stream.IntStream;

/**
 * "Default" implementation for an extensional data node.
 *
 * Most likely (but not necessarily) will be overwritten by native query language specific implementations.
 */
public class ExtensionalDataNodeImpl extends DataNodeImpl implements ExtensionalDataNode {

    private static final String EXTENSIONAL_NODE_STR = "EXTENSIONAL";
    private final Relation2Predicate relation2Predicate;

    @AssistedInject
    private ExtensionalDataNodeImpl(@Assisted DataAtom atom,
                                    Relation2Predicate relation2Predicate) {
        super(atom);
        this.relation2Predicate = relation2Predicate;
    }

    @Override
    public void acceptVisitor(QueryNodeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExtensionalDataNode clone() {
        return new ExtensionalDataNodeImpl(getProjectionAtom(), relation2Predicate);
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

        RelationID relationId = relation2Predicate.createRelationFromPredicateName(
                metadata.getQuotedIDFactory(),
                atom.getPredicate());
        RelationDefinition relation = metadata.getRelation(relationId);

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
        return new ExtensionalDataNodeImpl(newAtom, relation2Predicate);
    }
}
