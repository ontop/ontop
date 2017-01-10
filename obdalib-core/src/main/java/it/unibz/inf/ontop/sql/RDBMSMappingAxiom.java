package it.unibz.inf.ontop.sql;

import it.unibz.inf.ontop.model.OBDAMappingAxiom;
import it.unibz.inf.ontop.model.OBDASQLQuery;

/**
 * For RDBMS data sources
 */
public interface RDBMSMappingAxiom extends OBDAMappingAxiom {

    @Override
    OBDASQLQuery getSourceQuery();
}
