package it.unibz.inf.ontop.utils;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopModelConfiguration;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Ontology;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDF;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class SITestingTools {

    public static final OWLAPITranslatorOWL2QL OWLAPI_TRANSLATOR;
    public static final TermFactory TERM_FACTORY;
    public static final TypeFactory TYPE_FACTORY;
    public static final RDF RDF_FACTORY;

    static {
        OntopModelConfiguration defaultConfiguration = OntopModelConfiguration.defaultBuilder().build();
        Injector injector = defaultConfiguration.getInjector();

        OWLAPI_TRANSLATOR = injector.getInstance(OWLAPITranslatorOWL2QL.class);
        TERM_FACTORY = injector.getInstance(TermFactory.class);
        TYPE_FACTORY = injector.getInstance(TypeFactory.class);
        RDF_FACTORY = injector.getInstance(RDF.class);
    }
    /**
     * USE FOR TESTS ONLY
     *
     * @param filename
     * @return
     * @throws OWLOntologyCreationException
     */

    public static ClassifiedTBox loadOntologyFromFileAndClassify(String filename) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology owl = man.loadOntologyFromOntologyDocument(new File(filename));
        Ontology onto = OWLAPI_TRANSLATOR.translateAndClassify(owl);
        return onto.tbox();
    }

    public static IRI getIRI(String prefix, String suffix) {
        return RDF_FACTORY.createIRI(prefix + suffix);
    }
}
