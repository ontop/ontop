package org.semanticweb.ontop.mongo;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.impl.AbstractOBDAMappingAxiom;

public class MongoMappingAxiom extends AbstractOBDAMappingAxiom {

    public MongoMappingAxiom(String id) {
        super(id);
    }

    @Override
    public void setSourceQuery(OBDAQuery query) {

    }

    @Override
    public MongoQuery getSourceQuery() {
        return null;
    }

    @Override
    public void setTargetQuery(OBDAQuery query) {

    }

    @Override
    public CQIE getTargetQuery() {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }
}
