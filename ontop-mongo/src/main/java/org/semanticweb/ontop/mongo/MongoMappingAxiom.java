package org.semanticweb.ontop.mongo;

import org.semanticweb.ontop.model.CQIE;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.impl.AbstractOBDAMappingAxiom;

public class MongoMappingAxiom extends AbstractOBDAMappingAxiom {

	private final MongoQuery sourceQuery;
	private final CQIE targetQuery;
	
    

    public MongoMappingAxiom(String id, MongoQuery sourceQuery, CQIE targetQuery) {
        super(id);
        this.sourceQuery = sourceQuery;
        this.targetQuery = targetQuery;
    }

    
    @Override
    public void setSourceQuery(OBDAQuery query) {
    	throw new IllegalAccessError("use constructor instead");
    }

    @Override
    public MongoQuery getSourceQuery() {
        return sourceQuery;
    }

    @Override
    public void setTargetQuery(OBDAQuery query) {
    	throw new IllegalAccessError("use constructor instead");
    }

    @Override
    public CQIE getTargetQuery() {
        return targetQuery;
    }

    @Override
    public Object clone() {
    	MongoMappingAxiom cloneAxiom = new MongoMappingAxiom( getId(), sourceQuery.clone() , targetQuery.clone() );
    	return cloneAxiom;
    }
}
