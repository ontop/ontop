package it.unibz.inf.ontop.si.repository.impl;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.injection.OntopMappingConfiguration;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.si.SemanticIndexException;
import it.unibz.inf.ontop.si.impl.LoadingConfiguration;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import it.unibz.inf.ontop.spec.ontology.Assertion;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Wrapper for RDBMSSIRepositoryManager
 */

public class SIRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SIRepository.class);
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "";

    private final RDBMSSIRepositoryManager dataRepository;
    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final LoadingConfiguration loadingConfiguration;

    public SIRepository(ClassifiedTBox tbox, LoadingConfiguration loadingConfiguration) {

        this.termFactory = loadingConfiguration.getTermFactory();
        this.typeFactory = loadingConfiguration.getTypeFactory();
        this.loadingConfiguration = loadingConfiguration;
        this.dataRepository = new RDBMSSIRepositoryManager(tbox, termFactory, typeFactory,
            loadingConfiguration.getTargetAtomFactory());

        LOG.warn("Semantic index mode initializing: \nString operation over URI are not supported in this mode ");
    }

    public String getJdbcUrl() { return loadingConfiguration.getJdbcUrl(); }

    public String getUser() { return DEFAULT_USER; }

    public String getPassword(){ return DEFAULT_PASSWORD; }

    public String getJdbcDriver() {
        return loadingConfiguration.getJdbcDriver();
    }

    public int insertData(Connection connection, Iterator<Assertion> iterator) throws SQLException {
        return dataRepository.insertData(connection, iterator, 5000, 500);
    }

    public SemanticIndexURIMap getUriMap() { return dataRepository.getUriMap(); }

    public Connection createConnection() throws SemanticIndexException {

        try {
            Connection localConnection = DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
            // Creating the ABox repository
            dataRepository.createDBSchemaAndInsertMetadata(localConnection);
            return localConnection;
        }
        catch (SQLException e) {
            throw new SemanticIndexException(e.getMessage());
        }
    }

    public SQLPPMapping createMappings() {

        OntopMappingConfiguration defaultConfiguration = OntopMappingConfiguration.defaultBuilder()
                .build();
        SpecificationFactory specificationFactory = defaultConfiguration.getInjector().getInstance(SpecificationFactory.class);
        PrefixManager prefixManager = specificationFactory.createPrefixManager(ImmutableMap.of());

        ImmutableList<SQLPPTriplesMap> mappingAxioms = dataRepository.getMappings();

        return new SQLPPMappingImpl(mappingAxioms, prefixManager);
    }
}
