package it.unibz.inf.ontop.rdf4j.repository;

import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.rdf4j.repository.impl.OntopVirtualRepository;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

public class H2RDF4JTestTools {

    private static final String URL_PREFIX = "jdbc:h2:mem:";

    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static String generateJdbcUrl() {
        return URL_PREFIX + UUID.randomUUID();
    }

    public static Connection createH2Instance(String jdbcUrl, String dbScriptRelativePath) throws SQLException, IOException {
        Connection sqlConnection = DriverManager.getConnection(jdbcUrl, USER, PASSWORD);

        java.sql.Statement st = sqlConnection.createStatement();

        FileReader reader = new FileReader(H2RDF4JTestTools.class.getResource(dbScriptRelativePath).getPath());
        BufferedReader in = new BufferedReader(reader);
        StringBuilder bf = new StringBuilder();
        String line = in.readLine();
        while (line != null) {
            bf.append(line);
            line = in.readLine();
        }
        in.close();

        st.executeUpdate(bf.toString());
        sqlConnection.commit();
        return sqlConnection;
    }

    public static OntopRepositoryConnection initR2RML(String jdbcUrl, String r2rmlRelativePath,
                                                      @Nullable String ontologyRelativePath, @Nullable String propertyFile) {
        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .r2rmlMappingFile(AbstractRDF4JTest.class.getResource(r2rmlRelativePath).getPath())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(USER)
                .jdbcPassword(PASSWORD)
                .enableTestMode();

        if (ontologyRelativePath != null)
            builder.ontologyFile(AbstractRDF4JTest.class.getResource(ontologyRelativePath).getPath());

        if (propertyFile != null)
            builder.propertyFile(AbstractRDF4JTest.class.getResource(propertyFile).getPath());

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();
        /*
         * Prepare the data connection for querying.
         */
        return repo.getConnection();
    }

    public static OntopRepositoryConnection initOBDA(String jdbcUrl, String obdaRelativePath,
                                                     @Nullable String ontologyRelativePath, @Nullable String propertyFile,
                                                     @Nullable String lensesFile, @Nullable String dbMetadataFile,
                                                     @Nullable String sparqlRulesRelativePath) {
        return initOBDA(jdbcUrl, obdaRelativePath, ontologyRelativePath, propertyFile, lensesFile, dbMetadataFile, sparqlRulesRelativePath, null, null);
    }

    public static OntopRepositoryConnection initOBDA(String jdbcUrl, String obdaRelativePath,
                                                     @Nullable String ontologyRelativePath, @Nullable String propertyFile,
                                                     @Nullable String lensesFile, @Nullable String dbMetadataFile,
                                                     @Nullable String sparqlRulesRelativePath, @Nullable String factsFile,
                                                     @Nullable String factsBaseIRI) {
        OntopSQLOWLAPIConfiguration.Builder<?> builder = OntopSQLOWLAPIConfiguration.defaultBuilder()
                .nativeOntopMappingFile(AbstractRDF4JTest.class.getResource(obdaRelativePath).getPath())
                .jdbcUrl(jdbcUrl)
                .enableTestMode();

        if (ontologyRelativePath != null)
            builder.ontologyFile(AbstractRDF4JTest.class.getResource(ontologyRelativePath).getPath());

        if (propertyFile != null) {
            builder.propertyFile(AbstractRDF4JTest.class.getResource(propertyFile).getPath())
                    .jdbcUser(USER)
                    .jdbcPassword(PASSWORD);
        }
        else {
            // Test for supporting arbitrary JDBC properties.
            // FOR TEST PURPOSES ONLY! Use the regular methods in your code.
            Properties properties = new Properties();
            properties.put("jdbc.property.user", USER);
            properties.put("jdbc.property.password", PASSWORD);
            builder.properties(properties);
        }

        if (lensesFile != null)
            builder.lensesFile(AbstractRDF4JTest.class.getResource(lensesFile).getPath());

        if (dbMetadataFile != null)
            builder.dbMetadataFile(AbstractRDF4JTest.class.getResource(dbMetadataFile).getPath());

        if (sparqlRulesRelativePath != null)
            builder.sparqlRulesFile(AbstractRDF4JTest.class.getResource(sparqlRulesRelativePath).getPath());

        if (factsFile != null)
            builder.factsFile(AbstractRDF4JTest.class.getResource(factsFile).getPath());

        if (factsBaseIRI != null)
            builder.factsBaseIRI(factsBaseIRI);

        OntopSQLOWLAPIConfiguration config = builder.build();

        OntopVirtualRepository repo = OntopRepository.defaultRepository(config);
        repo.init();
        /*
         * Prepare the data connection for querying.
         */
        return repo.getConnection();
    }


}
