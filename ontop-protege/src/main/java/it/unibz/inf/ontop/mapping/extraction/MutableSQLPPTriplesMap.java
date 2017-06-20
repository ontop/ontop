package it.unibz.inf.ontop.mapping.extraction;

import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.OBDASQLQuery;
import it.unibz.inf.ontop.model.SQLPPTriplesMap;

import java.io.Serializable;
import java.util.List;

/**
 * Mutable
 */
public interface MutableSQLPPTriplesMap extends SQLPPTriplesMap, Cloneable, Serializable {

    void setSourceQuery(OBDASQLQuery query);

    void setTargetQuery(List<Function> query);

    List<Function> getTargetQuery();

    void setId(String id);

    MutableSQLPPTriplesMap clone();
}
