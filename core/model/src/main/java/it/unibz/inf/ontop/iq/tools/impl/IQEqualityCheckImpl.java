package it.unibz.inf.ontop.iq.tools.impl;

import com.google.inject.Inject;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IntermediateQuery;
import it.unibz.inf.ontop.iq.equivalence.IQSyntacticEquivalenceChecker;
import it.unibz.inf.ontop.iq.exception.EmptyQueryException;
import it.unibz.inf.ontop.iq.tools.IQConverter;
import it.unibz.inf.ontop.iq.tools.IQEqualityCheck;

public class IQEqualityCheckImpl implements IQEqualityCheck {
    private final IQConverter iqConverter;

    @Inject
    IQEqualityCheckImpl(IQConverter iqConverter) {
        this.iqConverter = iqConverter;
    }

    @Override
    public boolean equal(IQ iq1, IQ iq2) throws EmptyQueryException {
        IntermediateQuery i1 = iqConverter.convert(iq1);
        IntermediateQuery i2 = iqConverter.convert(iq2);
        return IQSyntacticEquivalenceChecker.areEquivalent(i1, i2);
    }
}
