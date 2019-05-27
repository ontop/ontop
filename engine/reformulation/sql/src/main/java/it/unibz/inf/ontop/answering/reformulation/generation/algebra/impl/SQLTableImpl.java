package it.unibz.inf.ontop.answering.reformulation.generation.algebra.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLRelationVisitor;
import it.unibz.inf.ontop.answering.reformulation.generation.algebra.SQLTable;
import it.unibz.inf.ontop.model.atom.DataAtom;
import it.unibz.inf.ontop.model.atom.RelationPredicate;

public class SQLTableImpl implements SQLTable {

    private final DataAtom<RelationPredicate> atom;

    @AssistedInject
    private SQLTableImpl(@Assisted DataAtom<RelationPredicate> atom) {
        this.atom = atom;
    }

    @Override
    public DataAtom<RelationPredicate> getAtom() {
        return atom;
    }

    @Override
    public <T> T acceptVisitor(SQLRelationVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
