package it.unibz.inf.ontop.executor.renaming;

import it.unibz.inf.ontop.model.DataAtom;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.pivotalrepr.impl.ConstructionNodeImpl;
import it.unibz.inf.ontop.model.AtomPredicate;
import it.unibz.inf.ontop.pivotalrepr.*;

/**
 * TODO: explain
 *
 * Immutable
 */
public class PredicateRenamer implements HomogeneousQueryNodeTransformer {

    private final static OBDADataFactory DATA_FACTORY = OBDADataFactoryImpl.getInstance();
    private final AtomPredicate formerPredicate;
    private final AtomPredicate newPredicate;


    public PredicateRenamer(AtomPredicate formerPredicate, AtomPredicate newPredicate) {
        this.formerPredicate = formerPredicate;
        this.newPredicate = newPredicate;
    }

    @Override
    public FilterNode transform(FilterNode filterNode) {
        return filterNode;
    }

    @Override
    public ExtensionalDataNode transform(ExtensionalDataNode extensionalDataNode) {
        return extensionalDataNode;
    }

    @Override
    public LeftJoinNode transform(LeftJoinNode leftJoinNode) {
        return leftJoinNode;
    }

    @Override
    public UnionNode transform(UnionNode unionNode) {
        return unionNode;
    }

    @Override
    public IntensionalDataNode transform(IntensionalDataNode intensionalDataNode) {
        return intensionalDataNode;
    }

    @Override
    public InnerJoinNode transform(InnerJoinNode innerJoinNode){
        return innerJoinNode;
    }

    @Override
    public ConstructionNode transform(ConstructionNode formerNode) {
        DataAtom currentAtom = formerNode.getProjectionAtom();
        AtomPredicate currentPredicate = currentAtom.getPredicate();

        /**
         * Makes a replacement proposal for the construction node
         */
        if (currentPredicate.equals(formerPredicate)) {
            DataAtom newDataAtom = DATA_FACTORY.getDataAtom(newPredicate,
                    formerNode.getProjectionAtom().getArguments());

            return new ConstructionNodeImpl(newDataAtom, formerNode.getSubstitution(),
                    formerNode.getOptionalModifiers());
        }
        else {
            return formerNode;
        }
    }

    @Override
    public GroupNode transform(GroupNode groupNode) {
        return groupNode;
    }

}
