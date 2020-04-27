package it.unibz.inf.ontop.generation.algebra.impl;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.model.term.NonConstantTerm;

public class SQLOrderComparatorImpl implements SQLOrderComparator {

    private final boolean isAscending;
    private final NonConstantTerm term;

    @AssistedInject
    private SQLOrderComparatorImpl(@Assisted NonConstantTerm term, @Assisted Boolean isAscending) {
        this.isAscending = isAscending;
        this.term = term;
    }

    @Override
    public boolean isAscending() {
        return isAscending;
    }

    @Override
    public NonConstantTerm getTerm() {
        return term;
    }
}
