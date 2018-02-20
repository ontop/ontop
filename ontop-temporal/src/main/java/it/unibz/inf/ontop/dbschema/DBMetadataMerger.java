package it.unibz.inf.ontop.dbschema;

import it.unibz.inf.ontop.dbschema.RDBMetadata;

public interface DBMetadataMerger {

    RDBMetadata mergeDBMetadata(RDBMetadata temporalDBMetadata, RDBMetadata staticDBMetadata);
}
