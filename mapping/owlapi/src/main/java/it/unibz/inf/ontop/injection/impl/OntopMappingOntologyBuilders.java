package it.unibz.inf.ontop.injection.impl;


import it.unibz.inf.ontop.injection.OntopMappingOntologyConfiguration;
import it.unibz.inf.ontop.injection.impl.OntopMappingConfigurationImpl.OntopMappingOptions;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class OntopMappingOntologyBuilders {

    static class OntopMappingOntologyOptions {

        final Optional<File> ontologyFile;

        final Optional<URL> ontologyURL;

        final Optional<Reader> ontologyReader;

        final Optional<String> xmlCatalogFile;

        final OntopMappingOptions mappingOptions;

        private OntopMappingOntologyOptions(Optional<File> ontologyFile,
                                            Optional<URL> ontologyURL,
                                            Optional<Reader> ontologyReader, Optional<String> xmlCatalogFile,
                                            OntopMappingOptions mappingOptions) {
            this.ontologyFile = ontologyFile;
            this.ontologyReader = ontologyReader;
            this.xmlCatalogFile = xmlCatalogFile;
            this.ontologyURL = ontologyURL;
            this.mappingOptions = mappingOptions;
        }
    }

    static class StandardMappingOntologyBuilderFragment<B extends OntopMappingOntologyConfiguration.Builder<B>>
            implements OntopMappingOntologyConfiguration.OntopMappingOntologyBuilderFragment<B> {

        private final B builder;
        private final Runnable declareOntologyDefinedCB;
        private Optional<File> ontologyFile = Optional.empty();
        private Optional<URL> ontologyURL = Optional.empty();
        private Optional<Reader> ontologyReader = Optional.empty();
        private Optional<String> xmlCatalogFile = Optional.empty();

        StandardMappingOntologyBuilderFragment(B builder,
                                               Runnable declareOntologyDefinedCB) {
            this.builder = builder;
            this.declareOntologyDefinedCB = declareOntologyDefinedCB;
        }

        @Override
        public B ontologyFile(@Nonnull String urlOrPath) {
            try {
                URL url = new URL(urlOrPath);
                /*
                 * If no protocol, treats it as a path
                 */
                String protocol = url.getProtocol();
                if (protocol == null) {
                    return ontologyFile(new File(urlOrPath));
                } else if (protocol.equals("file")) {
                    return ontologyFile(new File(url.getPath()));
                } else {
                    return ontologyFile(url);
                }
            } catch (MalformedURLException e) {
                return ontologyFile(new File(urlOrPath));
            }
        }

        @Override
        public B xmlCatalogFile(@Nonnull String xmlCatalogFile) {
            this.xmlCatalogFile = Optional.of(xmlCatalogFile);
            return builder;
        }

        @Override
        public B ontologyFile(@Nonnull URL url) {
            declareOntologyDefinedCB.run();
            this.ontologyURL = Optional.of(url);
            return builder;
        }


        @Override
        public B ontologyFile(@Nonnull File owlFile) {
            declareOntologyDefinedCB.run();
            this.ontologyFile = Optional.of(owlFile);
            return builder;
        }

        @Override
        public B ontologyReader(@Nonnull Reader reader) {
            declareOntologyDefinedCB.run();
            this.ontologyReader = Optional.of(reader);
            return builder;
        }

        OntopMappingOntologyOptions generateMappingOntologyOptions(OntopMappingOptions mappingOptions) {
            return new OntopMappingOntologyOptions(ontologyFile, ontologyURL, ontologyReader, xmlCatalogFile, mappingOptions);
        }
    }
}
