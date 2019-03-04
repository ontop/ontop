package it.unibz.inf.ontop.si.impl;

import com.google.inject.Injector;
import it.unibz.inf.ontop.injection.OntopSQLCoreConfiguration;
import it.unibz.inf.ontop.model.atom.TargetAtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.ontology.owlapi.OWLAPITranslatorOWL2QL;
import org.apache.commons.rdf.api.RDF;

import java.util.UUID;

/**
 * Mainly in charge of creating the "default" configuration that is used
 * both for SIRepository and the ontology loader
 */
public class LoadingConfiguration {

    private final OWLAPITranslatorOWL2QL translatorOWL2QL;
    private final String jdbcUrl;
    private static final String H2_DRIVER = "org.h2.Driver";
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final RDF rdfFactory;

    public LoadingConfiguration() {
        this.jdbcUrl = "jdbc:h2:mem:questrepository:" + UUID.randomUUID() + ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0";

        OntopSQLCoreConfiguration defaultConfiguration = OntopSQLCoreConfiguration.defaultBuilder()
                .jdbcDriver(H2_DRIVER)
                .jdbcUrl(jdbcUrl)
                .build();

        Injector injector = defaultConfiguration.getInjector();
        termFactory = defaultConfiguration.getTermFactory();
        typeFactory = defaultConfiguration.getTypeFactory();
        translatorOWL2QL = injector.getInstance(OWLAPITranslatorOWL2QL.class);
        targetAtomFactory = defaultConfiguration.getInjector().getInstance(TargetAtomFactory.class);
        rdfFactory = injector.getInstance(RDF.class);
    }

    public OWLAPITranslatorOWL2QL getTranslatorOWL2QL() {
        return translatorOWL2QL;
    }

    public TermFactory getTermFactory() {
        return termFactory;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public TargetAtomFactory getTargetAtomFactory() {
        return targetAtomFactory;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getJdbcDriver() {
        return H2_DRIVER;
    }

    public RDF getRdfFactory() {
        return rdfFactory;
    }
}
