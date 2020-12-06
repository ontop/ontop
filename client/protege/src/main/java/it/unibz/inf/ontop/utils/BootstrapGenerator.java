package it.unibz.inf.ontop.utils;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.protege.core.DuplicateMappingException;
import it.unibz.inf.ontop.exception.MetadataExtractionException;
import it.unibz.inf.ontop.injection.OntopSQLOWLAPIConfiguration;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.impl.DirectMappingEngine;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.protege.core.OBDAModel;
import it.unibz.inf.ontop.protege.core.OBDAModelManager;
import it.unibz.inf.ontop.protege.utils.JDBCConnectionManager;
import it.unibz.inf.ontop.spec.mapping.util.MappingOntologyUtils;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * TODO: fully refactor this class. Protégé should not be exposed to these internal classes of Ontop.
 */
public class BootstrapGenerator {

    private final JDBCConnectionManager connManager;
    private final OntopSQLOWLAPIConfiguration configuration;
    private final OBDAModel activeOBDAModel;
    private final OWLModelManager owlManager;
    private final TypeFactory typeFactory;
    private final DirectMappingEngine directMappingEngine;
    private final JDBCMetadataProviderFactory metadataProviderFactory;

    public BootstrapGenerator(OBDAModelManager obdaModelManager, String baseUri,
                              OWLModelManager owlManager) throws DuplicateMappingException, MetadataExtractionException {

        connManager = JDBCConnectionManager.getJDBCConnectionManager();
        this.owlManager =  owlManager;
        configuration = obdaModelManager.getConfigurationManager().buildOntopSQLOWLAPIConfiguration(owlManager.getActiveOntology());
        activeOBDAModel = obdaModelManager.getActiveOBDAModel();
        typeFactory = obdaModelManager.getTypeFactory();
        Injector injector = configuration.getInjector();
        directMappingEngine = injector.getInstance(DirectMappingEngine.class);
        metadataProviderFactory = injector.getInstance(JDBCMetadataProviderFactory.class);

        bootstrapMappingAndOntologyProtege(baseUri);
    }

    private void bootstrapMappingAndOntologyProtege(String baseUri) throws DuplicateMappingException, MetadataExtractionException {

        List<SQLPPTriplesMap> sqlppTriplesMaps = bootstrapMapping(activeOBDAModel.generatePPMapping(), baseUri);

        // update protege ontology
        OWLOntologyManager manager = owlManager.getActiveOntology().getOWLOntologyManager();
        Set<OWLDeclarationAxiom> declarationAxioms = MappingOntologyUtils.extractDeclarationAxioms(
                manager,
                sqlppTriplesMaps.stream()
                        .flatMap(ax -> ax.getTargetAtoms().stream()),
                typeFactory,
                true);

        List<AddAxiom> addAxioms = declarationAxioms.stream()
                .map(ax -> new AddAxiom(owlManager.getActiveOntology(), ax))
                .collect(Collectors.toList());

        owlManager.applyChanges(addAxioms);
    }

    private List<SQLPPTriplesMap> bootstrapMapping(SQLPPMapping ppMapping, String baseURI0)
            throws DuplicateMappingException, MetadataExtractionException {

        final Connection conn;
        try {
            conn = connManager.getConnection(configuration.getSettings());
        }
        catch (SQLException e) {
            throw new RuntimeException("JDBC connection is missing, have you setup Ontop Mapping properties?" +
                    " Message: " + e.getMessage());
        }

        final String baseURI = (baseURI0 == null || baseURI0.isEmpty())
            ? ppMapping.getPrefixManager().getDefaultPrefix()
            : DirectMappingEngine.fixBaseURI(baseURI0);

        MetadataProvider metadataProvider = metadataProviderFactory.getMetadataProvider(conn);
        // this operation is EXPENSIVE
        ImmutableList<NamedRelationDefinition> relations = ImmutableMetadata.extractImmutableMetadata(metadataProvider).getAllRelations();

        Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();
        AtomicInteger currentMappingIndex = new AtomicInteger(ppMapping.getTripleMaps().size() + 1);

        ImmutableList<SQLPPTriplesMap> newTriplesMap = relations.stream()
                .flatMap(td -> directMappingEngine.getMapping(td, baseURI, bnodeTemplateMap, currentMappingIndex).stream())
                .collect(ImmutableCollectors.toList());

        // add to the current model the boostrapped triples map
        for (SQLPPTriplesMap triplesMap: newTriplesMap)
            activeOBDAModel.addTriplesMap(triplesMap, true);

        return newTriplesMap;
    }
}
