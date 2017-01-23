package it.unibz.inf.ontop.spec.impl;

import it.unibz.inf.ontop.mapping.Mapping;
import it.unibz.inf.ontop.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.dagjgrapht.TBoxReasoner;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.model.DBMetadata;


public class OBDASpecificationImpl implements OBDASpecification {

    private final Mapping mapping;
    private final DBMetadata dbMetadata;
    private final TBoxReasoner saturatedTBox;
    private final ImmutableOntologyVocabulary vocabulary;

    public OBDASpecificationImpl(Mapping mapping, DBMetadata dbMetadata,
                                 TBoxReasoner saturatedTBox,
                                 ImmutableOntologyVocabulary vocabulary) {
        this.mapping = mapping;
        this.dbMetadata = dbMetadata;
        this.saturatedTBox = saturatedTBox;
        this.vocabulary = vocabulary;
    }

    @Override
    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public DBMetadata getDBMetadata() {
        return dbMetadata;
    }

    @Override
    public TBoxReasoner getSaturatedTBox() {
        return saturatedTBox;
    }

    @Override
    public ImmutableOntologyVocabulary getVocabulary() {
        return vocabulary;
    }
}
