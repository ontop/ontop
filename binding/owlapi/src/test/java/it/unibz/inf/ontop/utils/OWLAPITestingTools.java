package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;

public class OWLAPITestingTools {

    public static final OWLAPITranslatorOWL2QL OWLAPI_TRANSLATOR;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
        Injector injector = defaultConfiguration.getInjector();

        OWLAPI_TRANSLATOR = injector.getInstance(OWLAPITranslatorOWL2QL.class);
    }
}
