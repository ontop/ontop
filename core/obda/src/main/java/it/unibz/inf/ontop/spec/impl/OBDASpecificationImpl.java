package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.OBDASpecification;
import it.unibz.inf.ontop.dbschema.DBMetadata;


public class OBDASpecificationImpl implements OBDASpecification {

    private final Mapping mapping;
    private final DBMetadata dbMetadata;
    private final ClassifiedTBox saturatedTBox;

    @Inject
    private OBDASpecificationImpl(@Assisted Mapping saturatedMapping,
                                  @Assisted DBMetadata dbMetadata,
                                  @Assisted ClassifiedTBox saturatedTBox) {
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
    public ClassifiedTBox getSaturatedTBox() {
        return saturatedTBox;
    }
}
