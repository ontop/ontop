package it.unibz.inf.ontop.injection;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.net.URL;

/**
 * TODO: move in a Maven module independent of OWLAPI
 */
public interface OntopMappingOntologyConfiguration extends OntopMappingConfiguration {

    interface OntopMappingOntologyBuilderFragment<B extends Builder<B>> {

        B ontologyFile(@Nonnull String urlOrPath);

        B xmlCatalogFile(@Nonnull String xmlCatalogFile);

        B ontologyFile(@Nonnull URL url);

        B ontologyFile(@Nonnull File owlFile);

        B ontologyReader(@Nonnull Reader reader);
    }

    interface Builder<B extends Builder<B>> extends OntopMappingOntologyBuilderFragment<B>,
            OntopMappingConfiguration.Builder<B> {

        @Override
        OntopMappingOntologyConfiguration build();
    }
}
