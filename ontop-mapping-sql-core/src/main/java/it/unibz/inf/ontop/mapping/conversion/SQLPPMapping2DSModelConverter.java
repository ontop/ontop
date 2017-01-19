package it.unibz.inf.ontop.mapping.conversion;


import it.unibz.inf.ontop.mapping.extraction.DataSourceModel;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.nativeql.DBException;
import it.unibz.inf.ontop.ontology.Ontology;

import java.util.Optional;

public interface SQLPPMapping2DSModelConverter {

    DataSourceModel convert(OBDAModel ppMapping, Optional<DBMetadata> dbMetadata, Optional<Ontology> ontology) throws DBException;
}
