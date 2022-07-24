package it.unibz.inf.ontop.injection;

import com.google.inject.Injector;
import it.unibz.inf.ontop.exception.InvalidOntopConfigurationException;
import it.unibz.inf.ontop.injection.impl.OntopModelConfigurationImpl;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import org.apache.commons.rdf.api.RDF;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Properties;

/**
 * TODO: explain
 */
public interface OntopModelConfiguration {

    OntopModelSettings getSettings();

    Injector getInjector();

    void validate() throws InvalidOntopConfigurationException;

    IntermediateQueryFactory getIQFactory();
    AtomFactory getAtomFactory();
    TermFactory getTermFactory();
    TypeFactory getTypeFactory();
    RDF getRdfFactory();

    /**
     * Default builder
     */
    static Builder defaultBuilder() {
        return new OntopModelConfigurationImpl.BuilderImpl<>();
    }

    /**
     * TODO: explain
     */
    interface OntopModelBuilderFragment<B extends Builder<B>> {

        B properties(@Nonnull Properties properties);
        B propertyFile(String propertyFilePath);
        B propertyFile(File propertyFile);
        B enableTestMode();

        // TODO: enable it later
        // B cardinalityPreservationMode(OntopModelProperties.CardinalityPreservationMode mode);
    }

    /**
     * TODO: explain
     */
    interface Builder<B extends Builder<B>> extends OntopModelBuilderFragment<B> {

        OntopModelConfiguration build();
    }
}
