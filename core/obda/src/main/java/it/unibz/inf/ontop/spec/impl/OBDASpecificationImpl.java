package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ImmutableOntologyVocabulary;
import it.unibz.inf.ontop.spec.ontology.TBoxReasoner;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.dbschema.DBMetadata;


public class OBDASpecificationImpl implements OBDASpecification {

    private final Mapping mapping;
    private final DBMetadata dbMetadata;
    private final TBoxReasoner saturatedTBox;

    @Inject
    private OBDASpecificationImpl(@Assisted Mapping saturatedMapping,
                                  @Assisted DBMetadata dbMetadata,
                                  @Assisted TBoxReasoner saturatedTBox) {
        this.mapping = saturatedMapping;
        this.dbMetadata = dbMetadata;
        this.saturatedTBox = saturatedTBox;
    }

    @Override
    public Mapping getSaturatedMapping() {
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
    public ImmutableOntologyVocabulary getVocabulary() { return saturatedTBox.getVocabulary(); }
}
