package it.unibz.inf.ontop.spec.trans;


import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;

public interface MappingCanonicalRewriter {

    Mapping rewrite(Mapping mapping, DBMetadata dbMetadata);
}
