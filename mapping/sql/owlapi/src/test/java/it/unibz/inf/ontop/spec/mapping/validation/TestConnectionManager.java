package it.unibz.inf.ontop.spec.mapping.validation;

import it.unibz.inf.ontop.exception.OBDASpecificationException;
import it.unibz.inf.ontop.injection.OntopMappingSQLAllOWLAPIConfiguration;
import it.unibz.inf.ontop.iq.node.ConstructionNode;
import it.unibz.inf.ontop.model.atom.RDFAtomPredicate;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.spec.mapping.Mapping;
import it.unibz.inf.ontop.spec.OBDASpecification;
import org.apache.commons.rdf.api.IRI;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.stream.Stream;

public class TestConnectionManager implements Closeable {

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String createScriptFilePath;
    private final String dropScriptFilePath;
    private Connection connection;

    public TestConnectionManager(String jdbcUrl, String dbUser, String dbPassword, String createScriptFilePath,
                                 String dropScriptFilePath) throws IOException, SQLException {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.createScriptFilePath = createScriptFilePath;
        this.dropScriptFilePath = dropScriptFilePath;
        createTables();
    }

    private void createTables() throws IOException, SQLException {
        connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(readFile(MappingOntologyMismatchTest.class.getResource(createScriptFilePath).getFile()));
        }
        connection.commit();
    }

    private void dropTables() throws SQLException, IOException {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate(readFile(MappingOntologyMismatchTest.class.getResource(dropScriptFilePath).getFile()));
        }
        connection.commit();
        connection.close();
    }

    private static String readFile(String file) throws IOException {
        try (FileReader reader = new FileReader(file);
        BufferedReader in = new BufferedReader(reader)) {
            StringBuilder bf = new StringBuilder();
            String line = in.readLine();
            while (line != null) {
                bf.append(line);
                line = in.readLine();
            }
            return bf.toString();
        }
    }

    public OBDASpecification extractSpecification(String owlFile, String obdaFile) throws OBDASpecificationException {
        OntopMappingSQLAllOWLAPIConfiguration configuration = OntopMappingSQLAllOWLAPIConfiguration.defaultBuilder()
                .ontologyFile(getClass().getResource(owlFile).getFile())
                .nativeOntopMappingFile(getClass().getResource(obdaFile).getFile())
                .jdbcUrl(jdbcUrl)
                .jdbcUser(dbUser)
                .jdbcPassword(dbPassword)
                .build();
        return configuration.loadSpecification();
    }


    public static Optional<IRI> getDatatype(Mapping mapping) {
        RDFAtomPredicate triplePredicate = mapping.getRDFAtomPredicates().stream()
                .findFirst().get();

        return mapping.getRDFProperties(triplePredicate).stream()
                .map(i -> mapping.getRDFPropertyDefinition(triplePredicate, i))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(query -> Optional.of(query.getTree().getRootNode())
                        .filter(r -> r instanceof ConstructionNode)
                        .map(r -> (ConstructionNode)r)
                        .map(r -> r.getSubstitution().getImmutableMap().values().stream())
                        .orElseGet(Stream::empty))
                .filter(t -> t instanceof ImmutableFunctionalTerm)
                .map(t -> (ImmutableFunctionalTerm) t)
                .flatMap(t-> t.inferType()
                        .flatMap(TermTypeInference::getTermType)
                        .map(Stream::of)
                        .orElseGet(Stream::empty))
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype)t)
                .map(RDFDatatype::getIRI)
                .findFirst();
    }

    @Override
    public void close() throws IOException {
        try {
            dropTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
