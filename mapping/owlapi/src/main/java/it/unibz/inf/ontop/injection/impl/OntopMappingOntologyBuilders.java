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

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static class OntopMappingOntologyOptions {

        final Optional<File> ontologyFile;

        final Optional<URL> ontologyURL;

        final Optional<Reader> ontologyReader;

        final Optional<String> xmlCatalogFile;

        final OntopMappingOptions mappingOptions;

        final Optional<String> factFormat;
        final Optional<String> factsBaseIRI;
        final Optional<File> factsFile;
        final Optional<URL> factsURL;
        final Optional<Reader> factsReader;

        private OntopMappingOntologyOptions(Optional<File> ontologyFile,
                                            Optional<URL> ontologyURL,
                                            Optional<Reader> ontologyReader,
                                            Optional<String> xmlCatalogFile,
                                            OntopMappingOptions mappingOptions,
                                            Optional<String> factFormat,
                                            Optional<String> factsBaseIRI,
                                            Optional<File> factsFile,
                                            Optional<URL> factsURL,
                                            Optional<Reader> factsReader) {
            this.ontologyFile = ontologyFile;
            this.ontologyReader = ontologyReader;
            this.xmlCatalogFile = xmlCatalogFile;
            this.ontologyURL = ontologyURL;
            this.mappingOptions = mappingOptions;
            this.factsFile = factsFile;
            this.factsURL = factsURL;
            this.factsReader = factsReader;
            this.factFormat = factFormat;
            this.factsBaseIRI = factsBaseIRI;
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static abstract class StandardMappingOntologyBuilderFragment<B extends OntopMappingOntologyConfiguration.Builder<B>>
            implements OntopMappingOntologyConfiguration.OntopMappingOntologyBuilderFragment<B> {

        private Optional<File> ontologyFile = Optional.empty();
        private Optional<URL> ontologyURL = Optional.empty();
        private Optional<Reader> ontologyReader = Optional.empty();

        private Optional<String> factFormat = Optional.empty();
        private Optional<String> factsBaseIRI = Optional.empty();
        private Optional<File> factsFile = Optional.empty();
        private Optional<URL> factsURL = Optional.empty();
        private Optional<Reader> factsReader = Optional.empty();
        private Optional<String> xmlCatalogFile = Optional.empty();

        protected abstract B self();

        protected abstract void declareOntologyDefined();

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
            return self();
        }

        @Override
        public B ontologyFile(@Nonnull URL url) {
            declareOntologyDefined();
            this.ontologyURL = Optional.of(url);
            return self();
        }

        @Override
        public B ontologyFile(@Nonnull File owlFile) {
            declareOntologyDefined();
            this.ontologyFile = Optional.of(owlFile);
            return self();
        }

        @Override
        public B ontologyReader(@Nonnull Reader reader) {
            declareOntologyDefined();
            this.ontologyReader = Optional.of(reader);
            return self();
        }

        @Override
        public B factFormat(@Nonnull String factFormat) {
            this.factFormat = Optional.of(factFormat);
            return self();
        }

        @Override
        public B factsBaseIRI(@Nonnull String factsBaseIRI) {
            this.factsBaseIRI = Optional.of(factsBaseIRI);
            return self();
        }

        @Override
        public B factsFile(@Nonnull String urlOrPath) {
            try {
                URL url = new URL(urlOrPath);
                /*
                 * If no protocol, treats it as a path
                 */
                String protocol = url.getProtocol();
                if (protocol == null) {
                    return factsFile(new File(urlOrPath));
                } else if (protocol.equals("file")) {
                    return factsFile(new File(url.getPath()));
                } else {
                    return factsFile(url);
                }
            } catch (MalformedURLException e) {
                return factsFile(new File(urlOrPath));
            }
        }

        @Override
        public B factsFile(@Nonnull URL url) {
            this.factsURL = Optional.of(url);
            return self();
        }

        @Override
        public B factsFile(@Nonnull File owlFile) {
            this.factsFile = Optional.of(owlFile);
            return self();
        }

        @Override
        public B factsReader(@Nonnull Reader reader) {
            this.factsReader = Optional.of(reader);
            return self();
        }

        protected OntopMappingOntologyOptions generateMappingOntologyOptions(OntopMappingOptions mappingOptions) {
            return new OntopMappingOntologyOptions(ontologyFile, ontologyURL, ontologyReader, xmlCatalogFile, mappingOptions, factFormat, factsBaseIRI, factsFile, factsURL, factsReader);
        }
    }
}
