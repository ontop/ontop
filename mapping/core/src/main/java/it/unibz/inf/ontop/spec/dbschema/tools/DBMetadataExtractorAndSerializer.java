package it.unibz.inf.ontop.spec.dbschema.tools;

import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.exception.MetadataExtractionException;

import java.util.function.Function;

public interface DBMetadataExtractorAndSerializer {

    default String extractAndSerialize() throws MetadataExtractionException {
        return extractAndSerialize(NamedRelationDefinition::getID);
    }

    String extractAndSerialize(Function<NamedRelationDefinition, RelationID> fkRelationIDExtractor) throws MetadataExtractionException;
}
