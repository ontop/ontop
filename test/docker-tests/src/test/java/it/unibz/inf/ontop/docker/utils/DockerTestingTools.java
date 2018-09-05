package it.unibz.inf.ontop.docker.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;

public class DockerTestingTools {

    public static final OWLAPITranslatorOWL2QL OWLAPI_TRANSLATOR;
    public static final TermFactory TERM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
        Injector injector = defaultConfiguration.getInjector();

        OWLAPI_TRANSLATOR = injector.getInstance(OWLAPITranslatorOWL2QL.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
    }
}
