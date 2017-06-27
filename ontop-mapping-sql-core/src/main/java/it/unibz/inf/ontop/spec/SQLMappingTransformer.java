package it.unibz.inf.ontop.spec;

import it.unibz.inf.ontop.exception.MappingException;
import it.unibz.inf.ontop.exception.OntologyException;
import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.model.DBMetadata;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;

import java.util.Optional;

public class SQLMappingTransformer implements MappingTransformer{
    @Override
    public OBDASpecification transform(Mapping mapping, DBMetadata dbMetadata, Optional<TBoxReasoner> tBox) throws
            MappingException, OntologyException {
    }
}
