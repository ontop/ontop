package it.unibz.inf.ontop.answering.reformulation.generation.algebra;

import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

/**
 * TODO: find a better name
 */
public interface SQLTable extends SQLRelation {

    DataAtom<RelationPredicate> getAtom();

}
