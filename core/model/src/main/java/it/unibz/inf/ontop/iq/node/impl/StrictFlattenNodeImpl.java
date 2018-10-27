package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.StrictFlattenNode;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;

public class StrictFlattenNodeImpl extends FlattenNodeImpl<StrictFlattenNode> implements StrictFlattenNode{

    private static final String STRICT_FLATTEN_PREFIX = "STRICT-FLATTEN";


    @Inject
    private StrictFlattenNodeImpl(@Assisted Variable arrayVariable,
                                  @Assisted int arrayIndexIndex,
                                  @Assisted DataAtom dataAtom,
                                  @Assisted ImmutableList<Boolean> argumentNullability) {
        super(arrayVariable, arrayIndexIndex, dataAtom, argumentNullability);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof StrictFlattenNode) {
            StrictFlattenNode flattenNode = (StrictFlattenNode) node;
            if (!(getArrayVariable().equals(flattenNode.getArrayVariable())
                    && getProjectionAtom().equals(flattenNode.getProjectionAtom()))) {
                return false;
            }
            else if (flattenNode instanceof StrictFlattenNodeImpl) {
                return argumentNullability.equals(((StrictFlattenNodeImpl)flattenNode).argumentNullability);
            }
            else {
                throw new RuntimeException("TODO: check the nullabilityMap against another implementation of StrictFlattenNode");
            }
        }
        return false;
    }

    @Override
    public ImmutableSet<Variable> getRequiredVariables(IntermediateQuery query) {
        return getProjectionAtom().getVariables();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        if(queryNode instanceof StrictFlattenNode){
            StrictFlattenNode castNode = (StrictFlattenNode) queryNode;
            return castNode.getArrayVariable().equals(getArrayVariable()) &&
            castNode.getArrayIndexTerm().equals(getArrayIndexTerm()) &&
            castNode.getArgumentNullability().equals(getArgumentNullability()) &&
            castNode.getProjectionAtom().equals(getProjectionAtom());
        }
        return false;
    }

    @Override
    public StrictFlattenNode clone() {
        return new StrictFlattenNodeImpl(getArrayVariable(), getArrayIndexIndex(), getProjectionAtom(), argumentNullability);
    }

    @Override
    protected StrictFlattenNode newFlattenNode(Variable newArrayVariable, DataAtom newAtom) {
        return new StrictFlattenNodeImpl(newArrayVariable, getArrayIndexIndex(), newAtom, argumentNullability);
    }

    @Override
    public boolean isStrict() {
        return true;
    }

    @Override
    public StrictFlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }


    @Override
    public String toString() {
        return toString(STRICT_FLATTEN_PREFIX);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        ImmutableList<? extends VariableOrGroundTerm> atomArguments = getProjectionAtom().getArguments();
        if (variable.equals(getArrayVariable()))
            return false;
        else if (atomArguments.contains(variable)) {
            int firstIndex = atomArguments.indexOf(variable);
            if (!argumentNullability.get(firstIndex))
                return false;
                /*
                 * Look for a second occurrence of the variable --> implicit filter condition and thus not nullable
                 */
            else {
                int arity = atomArguments.size();
                if (firstIndex >= (arity - 1))
                    return true;
                else {
                    int secondIndex = atomArguments.subList(firstIndex + 1, arity).indexOf(variable);
                    return secondIndex < 0;
                }
            }
        }
        else {
            return query.getFirstChild(this)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("A FlattenNode must have a child"))
                    .isVariableNullable(query, variable);
        }
    }

    @Override
    public StrictFlattenNode newAtom(DataAtom newAtom) {
        return new StrictFlattenNodeImpl(getArrayVariable(), getArrayIndexIndex(), newAtom, argumentNullability);
    }

}
