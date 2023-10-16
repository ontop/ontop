package it.unibz.inf.ontop.spec.dbschema.tools.impl;

import it.unibz.inf.ontop.dbschema.NamedRelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.dbschema.tools.DBMetadataExtractorAndSerializer;

import java.util.function.Function;

public class FakeDBMetadataExtractorAndSerializer implements DBMetadataExtractorAndSerializer {

    @Override
    public String extractAndSerialize(Function<NamedRelationDefinition, RelationID> fkRelationIDExtractor) {
        throw new UnsupportedOperationException("Fake implementation. Please use a real implementation");
    }
}
