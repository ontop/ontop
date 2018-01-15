package it.unibz.inf.ontop.temporal.mapping;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.unibz.inf.ontop.datalog.CQIE;
import it.unibz.inf.ontop.datalog.DatalogFactory;
import it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.spec.mapping.OBDASQLQuery;
import it.unibz.inf.ontop.spec.mapping.parser.exception.InvalidSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.exception.UnsupportedSelectQueryException;
import it.unibz.inf.ontop.spec.mapping.parser.impl.RAExpression;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.spec.mapping.parser.impl.SelectQueryParser;
import it.unibz.inf.ontop.spec.mapping.pp.PPMappingAssertionProvenance;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class TemporalSQLPPMapping2DatalogConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(it.unibz.inf.ontop.datalog.SQLPPMapping2DatalogConverter.class);

    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final DatalogFactory datalogFactory;

    @Inject
    private TemporalSQLPPMapping2DatalogConverter(TermFactory termFactory, TypeFactory typeFactory, DatalogFactory datalogFactory) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.datalogFactory = datalogFactory;
    }

    /**
     * returns a Datalog representation of the mappings
     */
    public ImmutableMap<CQIE, PPMappingAssertionProvenance> convert(Collection<SQLPPTriplesMap> triplesMaps,
                                                                    RDBMetadata metadata) throws InvalidMappingSourceQueriesException {
        Map<CQIE, PPMappingAssertionProvenance> mutableMap = new HashMap<>();

        List<String> errorMessages = new ArrayList<>();

        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        for (SQLPPTriplesMap mappingAxiom : triplesMaps) {
            try {
                OBDASQLQuery sourceQuery = mappingAxiom.getSourceQuery();

                List<Function> body;
                ImmutableMap<QualifiedAttributeID, Variable> lookupTable;

                try {
                    SelectQueryParser sqp = new SelectQueryParser(metadata, termFactory, typeFactory);
                    RAExpression re = sqp.parse(sourceQuery.toString());
                    lookupTable = re.getAttributes();

                    body = new ArrayList<>(re.getDataAtoms().size() + re.getFilterAtoms().size());
                    body.addAll(re.getDataAtoms());
                    body.addAll(re.getFilterAtoms());
                }
                catch (UnsupportedSelectQueryException e) {
                    ImmutableList<QuotedID> attributes = new SelectQueryAttributeExtractor(metadata, termFactory)
                            .extract(sourceQuery.toString());
                    ParserViewDefinition view;
                    if(metadata instanceof TemporalRDBMetadata)
                        view = ((TemporalRDBMetadata)metadata).createParserView(sourceQuery.toString(), attributes);
                    else
                        view = metadata.createParserView(sourceQuery.toString(), attributes);

                    // this is required to preserve the order of the variables
                    ImmutableList<Map.Entry<QualifiedAttributeID,Variable>> list = view.getAttributes().stream()
                            .map(att -> new AbstractMap.SimpleEntry<>(
                                    new QualifiedAttributeID(null, att.getID()), // strip off the ParserViewDefinitionName
                                    termFactory.getVariable(att.getID().getName())))
                            .collect(ImmutableCollectors.toList());

                    lookupTable = list.stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    List<Term> arguments = list.stream().map(Map.Entry::getValue).collect(ImmutableCollectors.toList());

                    body = new ArrayList<>(1);
                    body.add(termFactory.getFunction(view.getAtomPredicate(), arguments));
                }

                for (ImmutableFunctionalTerm atom : mappingAxiom.getTargetAtoms()) {
                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(atom);
                    try {
                        Function head = renameVariables(atom, lookupTable, idfac);
                        CQIE rule = datalogFactory.getCQIE(head, body);

                        PPMappingAssertionProvenance previous = mutableMap.put(rule, provenance);
                        if (previous != null)
                            LOGGER.warn("Redundant triples maps: \n" + provenance + "\n and \n" + previous);
                    }
                    catch (AttributeNotFoundException e) {
                        errorMessages.add("Error: " + e.getMessage()
                                + " \nProblem location: source query of the mapping assertion \n["
                                + provenance.getProvenanceInfo() + "]");
                    }
                }
            }
            catch (InvalidSelectQueryException e) {
                errorMessages.add("Error: " + e.getMessage()
                        + " \nProblem location: source query of triplesMap \n["
                        +  mappingAxiom.getTriplesMapProvenance().getProvenanceInfo() + "]");
            }
        }

        if (!errorMessages.isEmpty())
            throw new InvalidMappingSourceQueriesException(Joiner.on("\n\n").join(errorMessages));

        return ImmutableMap.copyOf(mutableMap);
    }


    /**
     * Returns a new function by renaming variables occurring in the {@code function}
     *  according to the {@code attributes} lookup table
     */
    private Function renameVariables(Function function, ImmutableMap<QualifiedAttributeID, Variable> attributes,
                                            QuotedIDFactory idfac) throws AttributeNotFoundException {
        List<Term> terms = function.getTerms();
        List<Term> newTerms = new ArrayList<>(terms.size());
        for (Term term : terms) {
            Term newTerm;
            if (term instanceof Variable) {
                Variable var = (Variable) term;
                QuotedID attribute = idfac.createAttributeID(var.getName());
                newTerm = attributes.get(new QualifiedAttributeID(null, attribute));

                if (newTerm == null) {
                    QuotedID quotedAttribute = QuotedID.createIdFromDatabaseRecord(idfac, var.getName());
                    newTerm = attributes.get(new QualifiedAttributeID(null, quotedAttribute));

                    if (newTerm == null)
                        throw new AttributeNotFoundException("The source query does not provide the attribute " + attribute
                                + " (variable " + var.getName() + ") required by the target atom.");
                }
            }
            else if (term instanceof Function)
                newTerm = renameVariables((Function) term, attributes, idfac);
            else if (term instanceof Constant)
                newTerm = term.clone();
            else
                throw new RuntimeException("Unknown term type: " + term);

            newTerms.add(newTerm);
        }

        return termFactory.getFunction(function.getFunctionSymbol(), newTerms);
    }


    private static class UnboundVariableException extends Exception {
        UnboundVariableException(String message) {
            super(message);
        }
    }

    private static class AttributeNotFoundException extends Exception {
        AttributeNotFoundException(String message) {
            super(message);
        }
    }
}
