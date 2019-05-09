package it.unibz.inf.ontop.iq.node.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.exception.QueryNodeTransformationException;
import it.unibz.inf.ontop.iq.node.QueryNode;
import it.unibz.inf.ontop.iq.node.RelaxedFlattenNode;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.transform.node.HomogeneousQueryNodeTransformer;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;

public class RelaxedFlattenNodeImpl extends FlattenNodeImpl<RelaxedFlattenNode> implements RelaxedFlattenNode {

    private static final String RELAXED_FLATTEN_PREFIX = "RELAXED-FLATTEN";

    @AssistedInject
    private RelaxedFlattenNodeImpl(@Assisted Variable arrayVariable,
                                  @Assisted int arrayIndexIndex,
                                  @Assisted DataAtom<RelationPredicate> dataAtom,
                                  SubstitutionFactory substitutionFactory,
                                  IntermediateQueryFactory intermediateQueryFactory) {
        super(arrayVariable, arrayIndexIndex, dataAtom, substitutionFactory, intermediateQueryFactory);
    }

    @Override
    public boolean isSyntacticallyEquivalentTo(QueryNode node) {
        if (node instanceof RelaxedFlattenNode) {
            RelaxedFlattenNode flattenNode = (RelaxedFlattenNode) node;
            if (!(getArrayVariable().equals(flattenNode.getArrayVariable())
                    && getDataAtom().equals(flattenNode.getDataAtom()))) {
                return false;
            }
//            else if (flattenNode instanceof RelaxedFlattenNodeImpl) {
//                return argumentNullability.equals(((RelaxedFlattenNodeImpl)flattenNode).argumentNullability);
//            }
//            else {
//                throw new RuntimeException("TODO: check the nullabilityMap against another implementation of RelaxedFlattenNode");
//            }
        }
        return false;
    }

    @Override
    public RelaxedFlattenNode acceptNodeTransformer(HomogeneousQueryNodeTransformer transformer)
            throws QueryNodeTransformationException {
        return transformer.transform(this);
    }

    @Override
    public boolean isEquivalentTo(QueryNode queryNode) {
        if(queryNode instanceof RelaxedFlattenNode){
            RelaxedFlattenNode castNode = (RelaxedFlattenNode) queryNode;
            return castNode.getArrayVariable().equals(getArrayVariable()) &&
                    castNode.getDataAtom().equals(getDataAtom());
        }
        return false;
    }

    @Override
    public RelaxedFlattenNode clone() {
        return iqFactory.createRelaxedFlattenNode(arrayVariable, arrayIndexIndex, dataAtom);
    }

    @Override
    public IQTree acceptTransformer(IQTree tree, IQTreeVisitingTransformer transformer, IQTree child) {
        return transformer.transformRelaxedFlatten(tree, this, child);
    }

    @Override
    public RelaxedFlattenNode newNode(Variable arrayVariable, int arrayIndexIndex, DataAtom dataAtom) {
        return iqFactory.createRelaxedFlattenNode(arrayVariable, arrayIndexIndex, dataAtom);
    }

    @Override
    public String toString() {
        return toString(RELAXED_FLATTEN_PREFIX);
    }
}
