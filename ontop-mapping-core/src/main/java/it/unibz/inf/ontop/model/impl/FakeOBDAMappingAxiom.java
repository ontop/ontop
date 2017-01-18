package it.unibz.inf.ontop.model.impl;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.SourceQuery;

import java.util.List;


public class FakeOBDAMappingAxiom implements OBDAMappingAxiom {

    private final static String MESSAGE = "This OBDAMappingAxiom is fake (not expected to be used)";

    @Override
    public void setSourceQuery(SourceQuery query) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public SourceQuery getSourceQuery() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void setTargetQuery(List<Function> query) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public List<Function> getTargetQuery() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException(MESSAGE);
    }

    @Override
    public OBDAMappingAxiom clone() {
        throw new UnsupportedOperationException(MESSAGE);
    }
}
