package it.unibz.inf.ontop.protege.core;

import com.google.common.collect.ImmutableList;
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
import it.unibz.inf.ontop.spec.mapping.parser.SQLMappingParser;
import it.unibz.inf.ontop.spec.mapping.parser.TemporalMappingParser;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPMapping;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.temporal.model.DatalogMTLProgram;
import it.unibz.inf.ontop.temporal.model.DatalogMTLRule;
import it.unibz.inf.ontop.utils.UriTemplateMatcher;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;

import java.io.File;
import java.io.Reader;
import java.net.URI;
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

    private DatalogMTLProgram datalogMTLProgram;

    public TemporalOBDAModel(SpecificationFactory specificationFactory,
                             SQLPPMappingFactory ppMappingFactory,
                             PrefixDocumentFormat owlPrefixManager,
                             AtomFactory atomFactory, TermFactory termFactory,
                             TypeFactory typeFactory, DatalogFactory datalogFactory,
                             Relation2Predicate relation2Predicate, JdbcTypeMapper jdbcTypeMapper) {
        super(specificationFactory, ppMappingFactory, owlPrefixManager, atomFactory, termFactory, typeFactory, datalogFactory, relation2Predicate, jdbcTypeMapper);
    }

    public void setDatalogMTLProgram(DatalogMTLProgram datalogMTLProgram){
        this.datalogMTLProgram = datalogMTLProgram;
    }

    public DatalogMTLProgram getDatalogMTLProgram(){
        return datalogMTLProgram;
    }

    public void addRule(DatalogMTLRule rule){
        datalogMTLProgram.addRule(rule);
    }

    public void updateRule(DatalogMTLRule oldRule, DatalogMTLRule newRule){
        datalogMTLProgram.removeRule(oldRule);
        datalogMTLProgram.addRule(newRule);
    }

    public void removeRule(DatalogMTLRule rule) {
        datalogMTLProgram.removeRule(rule);
    }

    public void updateNamespace(String namespace) {
        //TODO update namespace of the datalog MTL program
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
        // New map
        triplesMapMap = ppMapping.getTripleMaps().stream()
                .collect(collectTriplesMaps(
                        SQLPPTriplesMap::getId,
                        m -> m));
    }

}
