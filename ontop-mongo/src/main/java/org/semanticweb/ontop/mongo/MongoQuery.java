package org.semanticweb.ontop.mongo;

import com.google.gson.JsonObject;
import org.semanticweb.ontop.model.OBDAQuery;
import org.semanticweb.ontop.model.OBDAQueryModifiers;

/**
 * Mongo Query as a native query language for the source part of the mappings
 */
public class MongoQuery implements OBDAQuery {

    private final String collectionName;

    private final JsonObject filterCriteria;

    public MongoQuery(String collectionName, JsonObject filterCriteria) {
        this.collectionName = collectionName;
        this.filterCriteria = filterCriteria;
    }

    @Override
    public OBDAQueryModifiers getQueryModifiers() {
        return null;
    }

    @Override
    public void setQueryModifiers(OBDAQueryModifiers modifiers) {

    }

    @Override
    public boolean hasModifiers() {
        return false;
    }
    
    public String getCollectionName () {
    	return collectionName;
    }

    public JsonObject getFilterCriteria () {
    	return filterCriteria;
    }
}
