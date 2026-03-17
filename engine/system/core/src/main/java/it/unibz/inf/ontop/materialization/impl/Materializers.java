package it.unibz.inf.ontop.materialization.impl;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.materialization.MaterializationParams;
import it.unibz.inf.ontop.materialization.OntopRDFMaterializer;

public class Materializers {

    public static OntopRDFMaterializer create(OntopSystemConfiguration configuration,
                                              MaterializationParams materializationParams) throws OBDASpecificationException {
        return materializationParams.useLegacyMaterializer()
                ? new DefaultOntopRDFMaterializer(configuration, materializationParams)
                : new OnePassRDFMaterializer(configuration, materializationParams);
    }
}
