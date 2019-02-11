package it.unibz.inf.ontop.iq.node.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.exception.OntopInternalBugException;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQProperties;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.exception.InvalidIntermediateQueryException;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.*;
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

public class StrictFlattenNodeImpl extends FlattenNodeImpl<StrictFlattenNode> implements StrictFlattenNode {

    private static final String STRICT_FLATTEN_PREFIX = "STRICT-FLATTEN";


    @AssistedInject
    private StrictFlattenNodeImpl(@Assisted Variable arrayVariable,
                                  @Assisted int arrayIndexIndex,
                                  @Assisted DataAtom<RelationPredicate> dataAtom,
                                  SubstitutionFactory substitutionFactory,
                                  IntermediateQueryFactory intermediateQueryFactory) {
        super(arrayVariable, arrayIndexIndex, dataAtom, substitutionFactory, intermediateQueryFactory);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode queryNode) {
        if(queryNode instanceof StrictFlattenNode){
            StrictFlattenNode castNode = (StrictFlattenNode) queryNode;
            return castNode.getArrayVariable().equals(getArrayVariable()) &&
                    castNode.getDataAtom().equals(getDataAtom());
        }
        return false;
    }

    @Override
    public StrictFlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        return isSyntacticallyEquivalentTo(queryNode);
    }

    @Override
    public StrictFlattenNode clone() {
        return iqFactory.createStrictFlattenNode(arrayVariable, arrayIndexIndex, dataAtom);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformStrictFlatten(tree, this, child);
    }

    @Override
    public StrictFlattenNode newNode(Variable arrayVariable, int arrayIndexIndex, DataAtom dataAtom) {
        return iqFactory.createStrictFlattenNode(arrayVariable, arrayIndexIndex, dataAtom);
    }

    @Override
    public String toString() {
        return toString(STRICT_FLATTEN_PREFIX);
    }
}
