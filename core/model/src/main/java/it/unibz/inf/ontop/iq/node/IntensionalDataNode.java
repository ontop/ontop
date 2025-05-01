package it.unibz.inf.ontop.iq.node;

import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.LeafIQTree;
import it.unibz.inf.ontop.iq.transform.IQTreeVisitingTransformer;
import it.unibz.inf.ontop.iq.visit.IQVisitor;
import it.unibz.inf.ontop.model.atom.AtomPredicate;
import it.unibz.inf.ontop.model.atom.DataAtom;

/**
 * TODO: explain
 * TODO: find a better name
 *
 * See IntermediateQueryFactory for creating a new instance.
 */
public interface IntensionalDataNode extends LeafIQTree {

    DataAtom<AtomPredicate> getProjectionAtom();

    IntensionalDataNode newAtom(DataAtom<AtomPredicate> newAtom);

    @Override
    default <T> T acceptVisitor(IQVisitor<T> visitor) {
        return visitor.transformIntensionalData(this);
    }
}
