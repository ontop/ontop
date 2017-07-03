package it.unibz.inf.ontop.spec.trans;

import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.mapping.Mapping;

public interface MappingSameAsRewriter {

    Mapping rewrite(Mapping mapping, DBMetadata dbMetadata);
}
