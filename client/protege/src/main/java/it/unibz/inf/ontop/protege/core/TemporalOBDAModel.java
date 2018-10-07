package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.dbschema.JdbcTypeMapper;
import it.unibz.inf.ontop.dbschema.Relation2Predicate;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.exception.MappingIOException;
import it.unibz.inf.ontop.injection.OntopTemporalMappingSQLAllConfiguration;
import it.unibz.inf.ontop.injection.SQLPPMappingFactory;
import it.unibz.inf.ontop.injection.SpecificationFactory;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.ImmutableFunctionalTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLSyntaxParser;
import it.unibz.inf.ontop.spec.datalogmtl.parser.impl.DatalogMTLSyntaxParserImpl;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.SQLMappingFactory;
import it.unibz.inf.ontop.spec.mapping.impl.SQLMappingFactoryImpl;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDASQLParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.TemporalTargetQueryRenderer;
import it.unibz.inf.ontop.temporal.mapping.IntervalQueryParser;
import it.unibz.inf.ontop.temporal.mapping.SQLPPTemporalTriplesMap;
import it.unibz.inf.ontop.temporal.mapping.TemporalMappingInterval;
import it.unibz.inf.ontop.temporal.mapping.impl.SQLPPTemporalTriplesMapImpl;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.impl.DatalogMTLFactoryImpl;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * For the moment, this class always use the same factories
 * built according to INITIAL Quest preferences.
 * Late modified preferences are not taken into account.
 * <p>
 * <p>
 * <p>
 * <p>
 * An OBDA model contains mapping information.
 * <p>
 * An OBDA model is a container for the database and mapping declarations needed to define a
 * Virtual ABox or Virtual RDF graph. That is, this is a manager for a
 * collection of JDBC databases (when SQL is the native query language) and their corresponding mappings.
 * It is used as input to any Quest instance (either OWLAPI or Sesame).
 *
 * <p>
 * OBDAModels are also used indirectly by the Protege plugin and many other
 * utilities including the mapping materializer (e.g. to generate ABox assertions or
 * RDF triples from a .obda file and a database).
 *
 * <p>
 */
public class TemporalOBDAModel extends OBDAModel {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private List<DatalogMTLRule> ruleList = new ArrayList<>();
    private String base;
    private static final SQLMappingFactory MAPPING_FACTORY = SQLMappingFactoryImpl.getInstance();
    private final DatalogMTLSyntaxParser DATALOG_MTL_SYNTAX_PARSER;
    private final IntervalQueryParser INTERVAL_QUERY_PARSER;


    TemporalOBDAModel(SpecificationFactory specificationFactory,
                      SQLPPMappingFactory ppMappingFactory,
                      PrefixDocumentFormat owlPrefixManager,
                      AtomFactory atomFactory, TermFactory termFactory,
                      TypeFactory typeFactory, DatalogFactory datalogFactory,
                      Relation2Predicate relation2Predicate, JdbcTypeMapper jdbcTypeMapper) {
        super(specificationFactory, ppMappingFactory, owlPrefixManager, atomFactory, termFactory, typeFactory, datalogFactory, relation2Predicate, jdbcTypeMapper);
        DATALOG_MTL_SYNTAX_PARSER = new DatalogMTLSyntaxParserImpl(getAtomFactory(), getTermFactory());
        INTERVAL_QUERY_PARSER = new IntervalQueryParser(getTermFactory());
    }

    public void removeRule(DatalogMTLRule rule) {
        ruleList.remove(rule);
    }

    public List<DatalogMTLRule> getRules() {
        return ruleList;
    }

    public void addRule(DatalogMTLRule parsedRule) {
        ruleList.add(parsedRule);
    }

    public void updateRule(DatalogMTLRule previousRule, DatalogMTLRule newRule) {
        ruleList.remove(previousRule);
        ruleList.add(newRule);
    }

    DatalogMTLProgram getDatalogMTLProgram() {
        return DatalogMTLFactoryImpl.getInstance().createProgram(prefixManager.getPrefixMap(), base, ruleList);
    }

    void setDatalogMTLProgram(DatalogMTLProgram datalogMTLProgram) {
        prefixManager.addPrefixes(ImmutableMap.copyOf(datalogMTLProgram.getPrefixes()));
        this.ruleList = new ArrayList<>();
        this.ruleList.addAll(datalogMTLProgram.getRules());
        this.base = datalogMTLProgram.getBase();
    }

    public DatalogMTLRule parseRule(String rule) {
        //TODO prefixes and base are added, must think about if one rule parsing is needed
        StringBuilder header = new StringBuilder();
        PrefixManager prefixManager = getMutablePrefixManager();
        prefixManager.getPrefixMap().forEach((key, value) -> header.append("PREFIX ").append(key).append("\t<").append(value).append(">\n"));
        header.append("BASE <").append(getNamespace()).append(">\n");
        LOGGER.info("Parsing rule:\n" + header + "\n" + rule + "\n");
        return DATALOG_MTL_SYNTAX_PARSER.parse(header + "\n" + rule).getRules().get(0);
    }

    public String dmtlRuleToString(DatalogMTLRule datalogMTLRule) {
        String ruleString = datalogMTLRule.toString();
        List<String> prefixes = prefixManager.getNamespaceList();
        for (String prefixEntry : prefixes) {
            ruleString = ruleString.replaceAll(prefixEntry, prefixManager.getPrefix(prefixEntry));
        }
        return ruleString;
    }

    public void updateNamespace(String namespace) {
        base = namespace;
    }

    public String getNamespace() {
        return base;
    }

    void parseMapping(File mappingFile, Properties properties) throws DuplicateMappingException,
            InvalidMappingException, MappingIOException {

        OntopTemporalMappingSQLAllConfiguration configuration = OntopTemporalMappingSQLAllConfiguration.defaultBuilder()
                .nativeOntopTemporalMappingFile(mappingFile)
                .properties(properties)
                .build();

        SQLMappingParser mappingParser = configuration.getInjector().getInstance(TemporalMappingParser.class);

        SQLPPMapping ppMapping = mappingParser.parse(mappingFile);
        prefixManager.addPrefixes(ppMapping.getMetadata().getPrefixManager().getPrefixMap());
        triplesMapMap = ppMapping.getTripleMaps().stream()
                .collect(collectTriplesMaps(
                        SQLPPTriplesMap::getId,
                        m -> m));
    }

    public boolean insertMapping(SQLPPTemporalTriplesMap mapping, String newId, String target, String source, String interval) throws Exception {
        ImmutableList<ImmutableFunctionalTerm> targetQuery = new TurtleOBDASQLParser(getMutablePrefixManager().getPrefixMap(),
                getAtomFactory(), getTermFactory()).parse(target);
        if (targetQuery != null) {
            //List<String> invalidPredicates = TargetQueryValidator.validate(targetQuery, getCurrentVocabulary());
            //if (invalidPredicates.isEmpty()) {
            OBDASQLQuery body = MAPPING_FACTORY.getSQLQuery(source.trim());
            TemporalMappingInterval mappingInterval = INTERVAL_QUERY_PARSER.parse(interval);

            LOGGER.info("Inserting Mapping: \n" + mappingInterval + "\n" + target + "\n" + source);

            if (mapping == null) {
                addTriplesMap(new SQLPPTemporalTriplesMapImpl(newId, body, targetQuery, mappingInterval),
                        false);
            } else {
                triplesMapMap.put(mapping.getId(), new SQLPPTemporalTriplesMapImpl(newId, body, targetQuery, mappingInterval));
            }
            return true;
        }
        //}
        throw new NullPointerException("targetQuery is null");
    }

    @Override
    public String getRenderedTargetQuery(ImmutableList<ImmutableFunctionalTerm> targetQuery) {
        return TemporalTargetQueryRenderer.encode(targetQuery, prefixManager);
    }

}
