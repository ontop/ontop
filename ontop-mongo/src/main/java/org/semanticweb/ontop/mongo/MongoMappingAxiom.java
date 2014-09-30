package org.semanticweb.ontop.mongo;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.impl.AbstractOBDAMappingAxiom;

public class MongoMappingAxiom extends AbstractOBDAMappingAxiom {

	private MongoQuery sourceQuery;
	private CQIE targetQuery;
	
    public MongoMappingAxiom(String id) {
        super(id);
    }

    @Override
    public void setSourceQuery(OBDAQuery query) {
    	this.sourceQuery = (MongoQuery)query;
    }

    @Override
    public MongoQuery getSourceQuery() {
        return sourceQuery;
    }

    @Override
    public void setTargetQuery(OBDAQuery query) {
    	this.targetQuery = (CQIE)query;
    }

    @Override
    public CQIE getTargetQuery() {
        return targetQuery;
    }

    @Override
    public Object clone() {
    	// TODO what is the correct wat to clone sourceQuery and should it be cloned or not?
    	MongoMappingAxiom cloneAxiom = new MongoMappingAxiom( getId() );
    	cloneAxiom.setSourceQuery( (OBDAQuery)sourceQuery );
    	cloneAxiom.setTargetQuery( targetQuery.clone() );
        return cloneAxiom;
    }
}
