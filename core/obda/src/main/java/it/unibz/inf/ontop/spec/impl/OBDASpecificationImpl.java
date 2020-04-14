package it.unibz.inf.ontop.spec.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.OBDASpecification;


public class OBDASpecificationImpl implements OBDASpecification {

    private final Mapping mapping;
    private final DBParameters dbParameters;
    private final ClassifiedTBox saturatedTBox;

    @Inject
    private OBDASpecificationImpl(@Assisted Mapping saturatedMapping,
                                  @Assisted DBParameters dbParameters,
                                  @Assisted ClassifiedTBox saturatedTBox) {
        this.mapping = saturatedMapping;
        this.dbParameters = dbParameters;
        this.saturatedTBox = saturatedTBox;
    }

    @Override
    public Mapping getSaturatedMapping() { return mapping; }

    @Override
    public DBParameters getDBParameters() {
        return dbParameters;
    }

    @Override
    public ClassifiedTBox getSaturatedTBox() {
        return saturatedTBox;
    }
}
