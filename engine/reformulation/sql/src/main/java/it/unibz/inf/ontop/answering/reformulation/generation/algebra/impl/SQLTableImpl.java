package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLTable;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

public class SQLTableImpl implements SQLTable {
    @Override
    public DataAtom<RelationPredicate> getAtom() {
        return null;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return null;
    }
}
