package it.unibz.inf.ontop.spec.mapping.bootstrap.engines.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.inject.Inject;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.dbschema.impl.JDBCMetadataProviderFactory;
import it.unibz.inf.ontop.exception.MappingBootstrappingException;
import it.unibz.inf.ontop.injection.OntopSQLCredentialSettings;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.BnodeStringTemplateFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.MPMappingAssertionProducer;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.Pair;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.SourceProducer;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.clusters.Cluster;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.dictionary.Dictionary;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.util.MPMappingOntologyUtils;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.LocalJDBCConnectionUtils;
import org.apache.commons.rdf.api.RDF;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.bootstrap.util.BootConf;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import org.semanticweb.owlapi.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MPEngine extends AbstractBootstrappingEngine {

    private MPMappingAssertionProducer dmap = null;

    @Inject
    private MPEngine(OntopSQLCredentialSettings settings,
                     SpecificationFactory specificationFactory,
                     SQLPPMappingFactory ppMappingFactory, TypeFactory typeFactory, TermFactory termFactory,
                     RDF rdfFactory, TargetAtomFactory targetAtomFactory,
                     DBFunctionSymbolFactory dbFunctionSymbolFactory,
                     SQLPPSourceQueryFactory sourceQueryFactory,
                     JDBCMetadataProviderFactory metadataProviderFactory){
        super(settings, specificationFactory, ppMappingFactory, typeFactory,termFactory,
                rdfFactory, targetAtomFactory, dbFunctionSymbolFactory, sourceQueryFactory, metadataProviderFactory);
    }

    @Override
    public ImmutableList<SQLPPTriplesMap> bootstrapMappings(String baseIRI,
                                                            ImmutableList<NamedRelationDefinition> tables,
                                                            SQLPPMapping mapping,
                                                            BootConf bootConf) {
        Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap = new HashMap<>();
        AtomicInteger currentMappingIndex = new AtomicInteger(mapping.getTripleMaps().size() + 1);

        Dictionary dict = bootConf.getDictionary();

        if( dict.isEmpty() ) return bootstrapMappingsEmptyDict(baseIRI, tables, mapping, bnodeTemplateMap, currentMappingIndex, bootConf);

        return Stream.concat(
                        mapping.getTripleMaps().stream(),
                        tables.stream()
                                .filter(table -> dict.containsTable(table.getID().getComponents().get(RelationID.TABLE_INDEX).getName()))
                                .flatMap(td -> getMapping(td, baseIRI, bnodeTemplateMap, currentMappingIndex, bootConf).stream()))
                .collect(ImmutableCollectors.toList());
    }

    /** Empty vocabulary  **/
    private ImmutableList<SQLPPTriplesMap> bootstrapMappingsEmptyDict(String baseIRI,
                                                                      ImmutableList<NamedRelationDefinition> tables,
                                                                      SQLPPMapping mapping,
                                                                      Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
                                                                      AtomicInteger currentMappingIndex, BootConf bootConf) {

        // Davide> I must create local variables in order to correctly increment the global currentMappingIndex
        //         (because in Java there is no guarantee on the order of execution of concatenated streams)
        List<SQLPPTriplesMap> preExistentMappings = mapping.getTripleMaps();
        List<SQLPPTriplesMap> mpMappings = tables.stream()
                .flatMap(td -> getMapping(td, baseIRI, bnodeTemplateMap, currentMappingIndex, bootConf).stream()).collect(Collectors.toList());
        List<SQLPPTriplesMap> workloadMappings = getWorkloadMapping(tables, baseIRI, bnodeTemplateMap, currentMappingIndex, bootConf);

        return Stream.concat(
                Stream.concat(preExistentMappings.stream(), mpMappings.stream()),
                workloadMappings.stream()
        ).collect(ImmutableCollectors.toList());

    }

    @Override
    public OWLOntology bootstrapOntology(String baseIRI, Optional<OWLOntology> inputOntology, SQLPPMapping newPPMapping, BootConf bootConf) throws MappingBootstrappingException {

        OWLOntology ontology;
        try {
            ontology = inputOntology.isPresent() ? inputOntology.get() : createEmptyOntology(baseIRI);
        } catch (OWLOntologyCreationException e) {
            throw new MappingBootstrappingException(e);
        }

        OWLOntologyManager manager = ontology.getOWLOntologyManager();

        // Vocabulary Declaration
        Set<OWLDeclarationAxiom> declarationAxioms = targetAtoms2OWLDeclarations(manager, newPPMapping);
        manager.addAxioms(ontology, declarationAxioms);

        // Data Properties
        Set<OWLDataPropertyAxiom> dataPropsDomainAxioms = targetAtoms2OWLDataPropertyAxioms(manager, newPPMapping);
        manager.addAxioms(ontology, dataPropsDomainAxioms);

        // Object Properties
        Set<OWLObjectPropertyAxiom> objectPropertyAxioms = targetAtoms2OWLObjectPropertyAxioms(manager, newPPMapping, bootConf);

        manager.addAxioms(ontology, objectPropertyAxioms);

        // Subclass axioms
        Set<OWLSubClassOfAxiom> subClassOfAxioms = toSubclassAxioms(manager, baseIRI, newPPMapping, bootConf);

        manager.addAxioms(ontology, subClassOfAxioms);

        // Create Annotations
        Set<OWLAxiom> annotations = createAnnotations(manager, baseIRI, newPPMapping, bootConf);

        manager.addAxioms(ontology, annotations);

        // Clusters subclass axioms
        Set<OWLSubClassOfAxiom> clustersSubclassOfAxioms = toSubclassAxiomsClusters(manager, baseIRI, bootConf.getClusters(), bootConf.getDictionary());

        manager.addAxioms(ontology, clustersSubclassOfAxioms);

        return ontology;
    }

    private Set<OWLSubClassOfAxiom> toSubclassAxiomsClusters(OWLOntologyManager manager, String baseIRI, List<Cluster> clusters, Dictionary dictionary) {
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        Set<OWLSubClassOfAxiom> axiomsSet = new HashSet<>();

        for( Cluster c : clusters ){
            if( c.getType().equals("class") ) { // TODO: Make enum
                String parent = dictionary.isEmpty() ? c.getTableName() : dictionary.getTableAlias(c.getTableName());
                for (Cluster.ClusteringMapEntry e : c.getClusteringMapEntries()) {
                    String child = e.getValue();

                    // Create axioms:
                    OWLClass subclass = dataFactory.getOWLClass(IRI.create(baseIRI, parent + "/" + child));
                    OWLClass superclass = dataFactory.getOWLClass(IRI.create(baseIRI, parent));

                    axiomsSet.add(dataFactory.getOWLSubClassOfAxiom(subclass, superclass));
                }

            }
        }
        return axiomsSet;
    }

    /**
     *
     * @param pair
     * @param tables
     * @return From a candidate joinPair, I can get up to two effective joinPairs (this situation
     *         occurs if containment is data-verified in both directions of the joinPair)
     *         If Left - Right = 0, return true (i.e., if Left contained in Right)
     *         Left - Right = v, and v < THRESHOLD * Left
     */
    private boolean isJoinPairHoldingInData(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> pair, Connection conn, ImmutableList<NamedRelationDefinition> tables) {

        boolean result = false;

        // 1) Check in both directions if fkey (on these attributes? On a subset?)
        String leftTable = pair.first().get(0).getRelation().getSQLRendering();
        String rightTable = pair.second().get(0).getRelation().getSQLRendering();

        String commaLeft = pair.first().stream().map(qualifiedAttributeID -> qualifiedAttributeID.getAttribute().getSQLRendering()).collect(Collectors.joining( "," ));
        String commaRight = pair.second().stream().map(qualifiedAttributeID -> qualifiedAttributeID.getAttribute().getSQLRendering()).collect(Collectors.joining( "," ));

        String query = "SELECT " + commaLeft + " FROM " + leftTable + " except " + "SELECT " + commaRight + " FROM " + rightTable + " LIMIT 1";

        if( isTargetIndexed(pair) ) {
            try {
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet resultSet = stmt.executeQuery();
                result = !resultSet.next();
                resultSet.close();
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Error occurred during bootstrapping: "
                        + e.getMessage());
                System.err.println("Debugging information for developers: ");
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     *
     * @param pair
     * @param tables
     * @return From a candidate joinPair, I can get up to two effective joinPairs (this situation
     *         occurs if containment is data-verified in both directions of the joinPair)
     *         If Left - Right = 0, return true (i.e., if Left contained in Right)
     *         If Left - Right = v, and v < THRESHOLD * Left, then return true
     */
    private boolean isJoinPairHoldingInDataThreshold(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> pair, Connection conn, ImmutableList<NamedRelationDefinition> tables) {

        boolean result = false;
        final double THRESHOLD=0.6d; // If containment is violated by at most THRESHOLD%, then consider it as a valid containemnt

        // 1) Check in both directions if fkey (on these attributes? On a subset?)
        String leftTable = pair.first().get(0).getRelation().getSQLRendering();
        String rightTable = pair.second().get(0).getRelation().getSQLRendering();

        String commaLeft = pair.first().stream().map(qualifiedAttributeID -> qualifiedAttributeID.getAttribute().getSQLRendering()).collect(Collectors.joining( "," ));
        String commaRight = pair.second().stream().map(qualifiedAttributeID -> qualifiedAttributeID.getAttribute().getSQLRendering()).collect(Collectors.joining( "," ));

        String countExceptQuery = "SELECT count(*) FROM ( SELECT " + commaLeft + " FROM " + leftTable + " except " + "SELECT " + commaRight + " FROM " + rightTable + " ) A";
        String countTotalQuery = "SELECT count(*) FROM " + leftTable;

        if( isTargetIndexed(pair) ) {
            try {
                PreparedStatement stmt = conn.prepareStatement(countExceptQuery);
                ResultSet resultSet = stmt.executeQuery();
                if( resultSet.next() ){
                    int countExcept = resultSet.getInt(1);
                    resultSet.close();
                    stmt.close();
                    stmt = conn.prepareStatement(countTotalQuery);
                    resultSet = stmt.executeQuery();
                    if( resultSet.next() ) {
                        int countTotal = resultSet.getInt(1);
                        resultSet.close();
                        stmt.close();

                        result = countExcept < countTotal * THRESHOLD;
                    }
                    else throw new MappingBootstrappingException(new Exception("No result in resultset.next(), a result was expected though."));
                }
                else throw new MappingBootstrappingException(new Exception("No result in resultset.next(), a result was expected though."));

            } catch (SQLException | MappingBootstrappingException e) {
                System.err.println("Error occurred during bootstrapping: "
                        + e.getMessage());
                System.err.println("Debugging information for developers: ");
                e.printStackTrace();
            }
        }
        return result;
    }

    private boolean isTargetIndexed(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> pair){
        // Check whether target is indexed.
        boolean targetIndexed = false;
        for( NamedRelationDefinition table : this.tables ){
            if( table.getID().equals(pair.second().get(0).getRelation()) ){
                Set<Set<QuotedID>> collect = table.getUniqueConstraints().stream()
                        .map(unique -> unique.getAttributes().stream()
                                .map(attribute -> attribute.getID())
                                .collect(Collectors.toSet()))
                        .collect(Collectors.toSet());

                for( Set<QuotedID> uniqueAtts : collect ){
                    if( uniqueAtts.equals(pair.second().stream().map(qA -> qA.getAttribute()).collect(Collectors.toSet())) ){
                        targetIndexed = true;
                    }
                }
            }
        }
        return targetIndexed;
    }

    private Set<OWLAxiom> createAnnotations(OWLOntologyManager manager, String baseIRI, SQLPPMapping newPPMapping, BootConf bootConf) {
        return MPMappingOntologyUtils.createAnnotations(manager, baseIRI, newPPMapping, typeFactory, true, bootConf);
    }

    private Set<OWLSubClassOfAxiom> toSubclassAxioms(OWLOntologyManager manager, String baseIRI, SQLPPMapping newPPMapping, BootConf bootConf) {
        Set<OWLSubClassOfAxiom> axioms = MPMappingOntologyUtils.extractSubclassAxioms(manager, baseIRI, tables, bootConf);
        return axioms;
    }

    private Set<OWLObjectPropertyAxiom> targetAtoms2OWLObjectPropertyAxioms(OWLOntologyManager manager, SQLPPMapping newPPMapping, BootConf bootConf) {

        Set<OWLObjectPropertyAxiom> objectPropertyAxioms = MPMappingOntologyUtils.extractObjectPropertyAxioms(manager,
                newPPMapping,
                typeFactory,
                true,
                tables,
                this.dmap,
                bootConf,
                this.metadataProvider);

        return objectPropertyAxioms;
    }

    private Set<OWLDataPropertyAxiom> targetAtoms2OWLDataPropertyAxioms(OWLOntologyManager manager, SQLPPMapping newPPMapping){
        List<ImmutableList<TargetAtom>> mappingsTargetsList = newPPMapping.getTripleMaps().stream()
                .map(ax -> ax.getTargetAtoms()).collect(Collectors.toList());

        Set<OWLDataPropertyAxiom> dataPropertyAxioms = MPMappingOntologyUtils.extractDataPropertyAxioms(
                manager,
                mappingsTargetsList,
                typeFactory,
                true
        );
        return dataPropertyAxioms;
    }

    /***
     * generate a mapping axiom from a table of a database
     *
     * @param table : the data definition from which mappings are extraced
     * @param baseIRI : the base uri needed for direct mapping axiom
     *
     * @param bnodeTemplateMap
     * @param bootConf : Bootstrapper Configuration
     * @return a List of OBDAMappingAxiom-s
     *
     */
    private ImmutableList<SQLPPTriplesMap> getMapping(NamedRelationDefinition table,
                                                      String baseIRI,
                                                      Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
                                                      AtomicInteger mappingIndex,
                                                      BootConf bootConf) {
        if(this.dmap == null)
            dmap = new MPMappingAssertionProducer(baseIRI, termFactory, targetAtomFactory,rdfFactory, dbFunctionSymbolFactory, typeFactory);

        // TODO Here I could look for clusters, in case the auto-clusters option is enabled.
        // ... how are these auto-clusters supposed to work? What if there's more than one clustering attribute?
        // Idea: Choose *all of them*.
        discoverAutoclusters();

        Optional<Stream<Map.Entry<String, ImmutableList<TargetAtom>>>> clusterDefinitionsOpt = bootConf.getClusterForTable(SourceProducer.schemaName(table), SourceProducer.tableName(table))
                .map(cluster -> shPattern(table, bnodeTemplateMap, bootConf, cluster));

        Stream<Map.Entry<String, ImmutableList<TargetAtom>>> clusterDefinitions = clusterDefinitionsOpt.isPresent() ? clusterDefinitionsOpt.get() : Stream.empty();

        return Stream.concat(clusterDefinitions,
                        Stream.concat(
                                Stream.of(Maps.immutableEntry(dmap.getSQL(table, bootConf.getNullValue()), dmap.getCQ(table, bnodeTemplateMap, bootConf))), // Class defs and data props
                                table.getForeignKeys().stream()
                                        .filter(fk -> MPMappingAssertionProducer.retrieveParentTables(table).isEmpty() || !bootConf.isEnableSH()) // If the fkey conforms to the Schema-Hierarchy pattern, ignore
                                        .filter(fk -> bootConf.getDictionary().isEmpty() || bootConf.getDictionary().containsTable(fk.getReferencedRelation().getID().getComponents().get(RelationID.TABLE_INDEX).getName()))
                                        .map(fk -> Maps.immutableEntry(dmap.getRefSQL(fk, bootConf), dmap.getRefCQ(fk, bnodeTemplateMap, bootConf))))
                )// Objet props
                .map(e -> new OntopNativeSQLPPTriplesMap("BOOTSTRAPPED-MAPPING-ID" + mappingIndex.getAndIncrement(),
                        sourceQueryFactory.createSourceQuery(e.getKey()), e.getValue()))
                .collect(ImmutableCollectors.toList());
    }

    private void discoverAutoclusters() {
        // TODO Implement
    }

    // Table is SH
    private Stream<Map.Entry<String, ImmutableList<TargetAtom>>> shPattern(NamedRelationDefinition table,
                                                                           Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
                                                                           BootConf bootConf,
                                                                           Cluster cluster){

        if( cluster.getType().equals("class") ){
            return cluster.getClusteringMapEntries().stream()
                    .map(e -> Maps.immutableEntry(
                                    dmap.getClusteringSQL(e.getKey(), cluster.getClusteringAttribute(), table, bootConf.getNullValue()),
                                    dmap.getClusteringCQ(table, bnodeTemplateMap, e.getValue(), bootConf)
                            )
                    );
        }

        assert(false); // This can never be reached
        return null;
    }

    /** Generate from a joinPair workload **/
    public ImmutableList<SQLPPTriplesMap> getWorkloadMapping(
            ImmutableList<NamedRelationDefinition> tables, String baseIRI,
            Map<NamedRelationDefinition, BnodeStringTemplateFunctionSymbol> bnodeTemplateMap,
            AtomicInteger mappingIndex,
            BootConf bootConf) {

        if(this.dmap == null )
            dmap = new MPMappingAssertionProducer(baseIRI, termFactory, targetAtomFactory,rdfFactory, dbFunctionSymbolFactory, typeFactory);

        List<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> joinPairs = bootConf.getJoinPairs(this.metadataProvider);
        ImmutableList<SQLPPTriplesMap> workloadList = null;

        List<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> invertedJoinPairs = joinPairs.stream()
                .map(pair -> new Pair<>(pair.second(), pair.first()))
                .collect(Collectors.toList());

        Set<Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>>> allPairs = Streams.concat(
                joinPairs.stream(), invertedJoinPairs.stream()).collect(Collectors.toSet());

        try (Connection conn = LocalJDBCConnectionUtils.createConnection(settings)) {

            workloadList = allPairs.stream()
                    .filter(pair -> !isPairCoveredByFkeys(pair, tables))
                    .filter(pair -> isJoinPairHoldingInDataThreshold(pair, conn, tables))
                    .map(pair -> Maps.immutableEntry(dmap.getRefSQL(pair, this.tables, bootConf), dmap.getRefCQ(pair, bnodeTemplateMap, bootConf, this.tables)))
                    .map(e -> new OntopNativeSQLPPTriplesMap("BOOTSTRAPPED-MAPPING-ID" + mappingIndex.getAndIncrement(),
                            sourceQueryFactory.createSourceQuery(e.getKey()), e.getValue()))
                    .collect(ImmutableCollectors.toList());


        } catch (SQLException e) {
            System.err.println("Error occurred during bootstrapping: "
                    + e.getMessage());
            System.err.println("Debugging information for developers: ");
            e.printStackTrace();
        }
        return workloadList;
    }

    private boolean isPairCoveredByFkeys(Pair<List<QualifiedAttributeID>, List<QualifiedAttributeID>> pair, ImmutableList<NamedRelationDefinition> tables) {

        List<Pair<QualifiedAttributeID, QualifiedAttributeID>> unfoldedPairs = new ArrayList<>();
        for( int i = 0; i < pair.first().size(); ++i ){
            unfoldedPairs.add(new Pair<>(pair.first().get(i), pair.second().get(i)));
        }

        for( NamedRelationDefinition table : tables ){
            for(ForeignKeyConstraint fk : table.getForeignKeys() ){
                List<Pair<QualifiedAttributeID, QualifiedAttributeID>> conditions = fk.getComponents().stream()
                        .map(c -> new Pair<>(
                                new QualifiedAttributeID(((NamedRelationDefinition)c.getAttribute().getRelation()).getID(),
                                        c.getAttribute().getID()),
                                new QualifiedAttributeID(((NamedRelationDefinition)c.getReferencedAttribute().getRelation()).getID(),
                                        c.getReferencedAttribute().getID())))
                        .collect(Collectors.toList());

                // .map(c -> new Pair<>(MPAxiomProducer.getSQLColumnName(c.getAttribute()), MPAxiomProducer.getSQLColumnName(c.getReferencedAttribute()))) // Commented OUT

                // Check if conditions equal to some workloadPair
                if( unfoldedPairs.equals(conditions) )
                    return true;
            }
        }
        return false;
    }
}
