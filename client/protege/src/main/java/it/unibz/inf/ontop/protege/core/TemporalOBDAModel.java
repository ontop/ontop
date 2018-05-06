package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.protege.gui.component.PropertyMappingPanel;
import it.unibz.inf.ontop.spec.datalogmtl.parser.DatalogMTLSyntaxParser;
import it.unibz.inf.ontop.spec.datalogmtl.parser.impl.DatalogMTLSyntaxParserImpl;
import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.temporal.model.impl.DatalogMTLFactoryImpl;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public TemporalOBDAModel(SpecificationFactory specificationFactory,
                             SQLPPMappingFactory ppMappingFactory,
                             PrefixDocumentFormat owlPrefixManager,
                             AtomFactory atomFactory, TermFactory termFactory,
                             TypeFactory typeFactory, DatalogFactory datalogFactory,
                             Relation2Predicate relation2Predicate, JdbcTypeMapper jdbcTypeMapper) {
        super(specificationFactory, ppMappingFactory, owlPrefixManager, atomFactory, termFactory, typeFactory, datalogFactory, relation2Predicate, jdbcTypeMapper);
    }

    public void setDatalogMTLProgram(DatalogMTLProgram datalogMTLProgram){
        prefixManager.addPrefixes(ImmutableMap.copyOf(datalogMTLProgram.getPrefixes()));
        this.ruleList = new ArrayList<>();
        this.ruleList.addAll(datalogMTLProgram.getRules());
        this.base = datalogMTLProgram.getBase();
    }

    public List<DatalogMTLRule> getRules() {
        return ruleList;
    }

    public void addRule(DatalogMTLRule parsedRule) {
        ruleList.add(parsedRule);
    }

    public void removeRule(DatalogMTLRule rule){
        ruleList.remove(rule);
    }

    public DatalogMTLProgram getDatalogMTLProgram(){
        Map<String, String> prefixMap = Maps.newHashMap();
        prefixManager.getPrefixMap().forEach(prefixMap::put);
        return DatalogMTLFactoryImpl.getInstance().createProgram(prefixMap, base, ruleList);
    }

    public DatalogMTLRule parse(String rule) {
        //TODO prefixes and base are added, must think about if one rule parsing is needed
        StringBuilder header = new StringBuilder();
        PrefixManager prefixManager = getMutablePrefixManager();
        prefixManager.getPrefixMap().forEach((key,value)->{
            header.append("PREFIX ").append(key).append("\t<").append(value).append(">\n");
        });
        header.append("BASE <").append(getNamespace()).append(">");
        DatalogMTLSyntaxParser datalogMTLSyntaxParser = new DatalogMTLSyntaxParserImpl(getAtomFactory(), getTermFactory());
        LOGGER.info("Parsing rule:\n"+header+"\n"+rule+"\n");
        return datalogMTLSyntaxParser.parse(header+"\n"+rule).getRules().get(0);
    }

    public void updateNamespace(String namespace) {
        base = namespace;
    }

    public String getNamespace() {
        return base;
    }

    public void parseMapping(File mappingFile, Properties properties) throws DuplicateMappingException,
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
}
