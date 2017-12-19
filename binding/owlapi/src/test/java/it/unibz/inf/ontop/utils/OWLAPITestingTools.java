package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorUtility;

public class OWLAPITestingTools {

    public static final OWLAPITranslatorUtility OWLAPI_TRANSLATOR_UTILITY;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
        Injector injector = defaultConfiguration.getInjector();

        OWLAPI_TRANSLATOR_UTILITY = injector.getInstance(OWLAPITranslatorUtility.class);
    }
}
