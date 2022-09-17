package it.unibz.inf.ontop.endpoint.beans;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.injection.OntopSystemConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.OntopRepository;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class OntopVirtualRepositoryBean {

    @Bean
    private OntopSystemConfiguration setupOntopConfiguration(@Value("${mapping}") String mappings,
                                                             @Value("${ontology:#{null}}") String ontology,
                                                             @Value("${xml-catalog:#{null}}") String xmlCatalog,
                                                             @Value("${properties:#{null}}") String properties,
                                                             @Value("${constraint:#{null}}") String constraint,
                                                             @Value("${db-metadata:#{null}}") String dbMetadata,
                                                             @Value("${ontop-views:#{null}}") String ontopViews,
                                                             @Value("${db-user:#{null}}") String dbUser,
                                                             @Value("${db-password:#{null}}") String dbPassword,
                                                             @Value("${db-url:#{null}}") String dbUrl,
                                                             @Value("${db-driver:#{null}}") String dbDriver) throws RepositoryException {
        OntopSQLOWLAPIConfiguration.Builder<? extends OntopSQLOWLAPIConfiguration.Builder> builder = OntopSQLOWLAPIConfiguration.defaultBuilder();

        if (properties != null && !properties.isEmpty())
            builder.propertyFile(properties);

        if (mappings.endsWith(".obda"))
            builder.nativeOntopMappingFile(mappings);
        else
            builder.r2rmlMappingFile(mappings);

        if ((ontology != null) && (!ontology.isEmpty()))
            builder.ontologyFile(ontology);

        if ((xmlCatalog != null) && (!xmlCatalog.isEmpty()))
            builder.xmlCatalogFile(xmlCatalog);

        if (constraint != null && !constraint.isEmpty())
            builder.basicImplicitConstraintFile(constraint);

        if (dbMetadata !=null && !dbMetadata.isEmpty())
            builder.dbMetadataFile(dbMetadata);

        if (ontopViews !=null && !ontopViews.isEmpty())
            builder.ontopViewFile(ontopViews);

        if (dbUser != null && !dbUser.isEmpty())
            builder.jdbcUser(dbUser);

        // NB: might be empty
        if (dbPassword != null)
            builder.jdbcPassword(dbPassword);

        if (dbUrl != null && !dbUrl.isEmpty())
            builder.jdbcUrl(dbUrl);

        //TODO Can this be empty?
        if (dbDriver != null && !dbDriver.isEmpty())
            builder.jdbcDriver(dbDriver);

        return builder.build();
    }

    @Bean
    private OntopVirtualRepository setupVirtualRepository(@Value("${lazy:false}") boolean lazy,
                                                          OntopSystemConfiguration configuration) throws RepositoryException {
        OntopVirtualRepository repository = OntopRepository.defaultRepository(configuration);

        if (!lazy) {
            repository.init();
        }

        return repository;
    }
}
