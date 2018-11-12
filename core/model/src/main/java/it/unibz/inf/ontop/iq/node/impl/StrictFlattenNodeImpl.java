package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.StrictFlattenNode;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.ImmutableExpression;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.VariableOrGroundTerm;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StrictFlattenNodeImpl extends FlattenNodeImpl<StrictFlattenNode> implements StrictFlattenNode{

    private static final String STRICT_FLATTEN_PREFIX = "STRICT-FLATTEN";


    @AssistedInject
    private StrictFlattenNodeImpl(@Assisted Variable arrayVariable,
                                  @Assisted int arrayIndexIndex,
                                  @Assisted DataAtom<RelationPredicate> dataAtom,
                                  @Assisted ImmutableList<Boolean> argumentNullability,
                                  SubstitutionFactory substitutionFactory,
                                  IntermediateQueryFactory intermediateQueryFactory) {
        super(arrayVariable, arrayIndexIndex, dataAtom, argumentNullability, substitutionFactory, intermediateQueryFactory);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof StrictFlattenNode) {
            StrictFlattenNode flattenNode = (StrictFlattenNode) node;
            if (!(getArrayVariable().equals(flattenNode.getArrayVariable())
                    && getDataAtom().equals(flattenNode.getDataAtom()))) {
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
        return getDataAtom().getVariables();
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        if(queryNode instanceof StrictFlattenNode){
            StrictFlattenNode castNode = (StrictFlattenNode) queryNode;
            return castNode.getArrayVariable().equals(getArrayVariable()) &&
            castNode.getArrayIndexTerm().equals(getArrayIndexTerm()) &&
            castNode.getArgumentNullability().equals(getArgumentNullability()) &&
            castNode.getDataAtom().equals(getDataAtom());
        }
        return false;
    }

    @Override
    public StrictFlattenNode clone() {
        return iqFactory.createStrictFlattenNode(arrayVariable, arrayIndexIndex, dataAtom, argumentNullability);
    }

//    @Override
//    protected StrictFlattenNode newFlattenNode(Variable newArrayVariable, DataAtom<RelationPredicate> newAtom) {
//        return new StrictFlattenNodeImpl(newArrayVariable, getArrayIndexIndex(), newAtom, argumentNullability);
//    }

    @Override
    public IQTree liftBinding(IQTree childIQTree, VariableGenerator variableGenerator, IQProperties currentIQProperties) {
        return ;
    }

    @Override
    public VariableNullability getVariableNullability(IQTree child) {
        ImmutableList<? extends VariableOrGroundTerm> atomArguments = dataAtom.getArguments();

        ImmutableSet<Variable> localVars = atomArguments.stream()
                .filter(t -> t instanceof Variable)
                .map (v -> (Variable)v)
                .collect(ImmutableCollectors.toSet());

        ImmutableSet<Variable> childVariables = child.getVariables();
        Stream<Variable> nullableLocalVars = localVars.stream()
                .filter(v -> !v.equals(arrayVariable) && !isRepeatedIn(v, dataAtom) && !childVariables.contains(v) ||
                        !isDeclaredNonNullable(v, atomArguments));

        return new VariableNullabilityImpl(Stream.concat(
                child.getVariableNullability().getNullableGroups().stream()
                        .filter(g -> filterNullabilityGroup(g, localVars)),
                nullableLocalVars
                        .map(v -> ImmutableSet.of(v)))
                .collect(ImmutableCollectors.toSet())
        );
    }

    private boolean isDeclaredNonNullable(Variable v, ImmutableList<? extends VariableOrGroundTerm> atomArguments) {
        return !argumentNullability.get(atomArguments.indexOf(v));
    }

    private boolean isRepeatedIn(Variable v, DataAtom<RelationPredicate> dataAtom) {
        return dataAtom.getArguments().stream()
                .filter(t -> t.equals(v))
                .count() > 1;
    }

    private boolean filterNullabilityGroup(ImmutableSet<Variable> group, ImmutableSet<Variable> localVars) {
       return group.stream()
               .anyMatch(v -> localVars.contains(v));
    }

    @Override
    public IQTree propagateDownConstraint(ImmutableExpression constraint, IQTree child) {
        return ;
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformStrictFlatten(tree, this, child);
    }

    @Override
    public StrictFlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public StrictFlattenNode newNode(Variable arrayVariable, int arrayIndexIndex, DataAtom dataAtom, ImmutableList argumentNullability) {
        return iqFactory.createStrictFlattenNode(arrayVariable, arrayIndexIndex, dataAtom, argumentNullability);
    }

    @Override
    public String toString() {
        return toString(STRICT_FLATTEN_PREFIX);
    }

    @Override
    public boolean isVariableNullable(IntermediateQuery query, Variable variable) {
        ImmutableList<? extends VariableOrGroundTerm> atomArguments = getDataAtom().getArguments();
        if (variable.equals(getArrayVariable()))
            return false;
        else if (atomArguments.contains(variable)) {
            /*Look for a second occurrence among the variables projected by the child --> implicit filter condition and thus not nullable */
            if(query.getVariables(query.getFirstChild(this)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("A FlattenNode must have a child")))
                .contains(variable))
                return false;

            /*
             *Look for a second occurrence of the variable in the array --> implicit filter condition and thus not nullable
             */
            int firstIndex = atomArguments.indexOf(variable);
            if (!argumentNullability.get(firstIndex))
                return false;
            int arity = atomArguments.size();
            if (firstIndex >= (arity - 1))
                return true;
            int secondIndex = atomArguments.subList(firstIndex + 1, arity).indexOf(variable);
            return secondIndex < 0;
        }
        else {
            return query.getFirstChild(this)
                    .orElseThrow(() -> new InvalidIntermediateQueryException("A FlattenNode must have a child"))
                    .isVariableNullable(query, variable);
        }
    }
}
