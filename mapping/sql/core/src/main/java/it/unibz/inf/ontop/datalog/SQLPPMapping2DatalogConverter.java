package it.unibz.inf.ontop.datalog;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.dbschema.*;
import it.unibz.inf.ontop.exception.InvalidMappingSourceQueriesException;
import it.unibz.inf.ontop.model.term.*;
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

import static it.unibz.inf.ontop.model.OntopModelSingletons.DATALOG_FACTORY;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;


public class SQLPPMapping2DatalogConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SQLPPMapping2DatalogConverter.class);

    /**
     * returns a Datalog representation of the mappings
     */
    public static ImmutableMap<CQIE, PPMappingAssertionProvenance> convert(Collection<SQLPPTriplesMap> triplesMaps,
                                                                     RDBMetadata metadata) throws InvalidMappingSourceQueriesException {
        Map<CQIE, PPMappingAssertionProvenance> mutableMap = new HashMap<>();

        List<String> errorMessages = new ArrayList<>();

        QuotedIDFactory idfac = metadata.getQuotedIDFactory();

        for (SQLPPTriplesMap mappingAxiom : triplesMaps) {
            try {
                OBDASQLQuery sourceQuery = mappingAxiom.getSourceQuery();

                List<Function> body;
                ImmutableMap<QualifiedAttributeID, Term> lookupTable;

                try {
                    SelectQueryParser sqp = new SelectQueryParser(metadata);
                    RAExpression re = sqp.parse(sourceQuery.toString());
                    lookupTable = re.getAttributes();

                    body = new ArrayList<>(re.getDataAtoms().size() + re.getFilterAtoms().size());
                    body.addAll(re.getDataAtoms());
                    body.addAll(re.getFilterAtoms());
                }
                catch (UnsupportedSelectQueryException e) {
                    ImmutableList<QuotedID> attributes = new SelectQueryAttributeExtractor(metadata)
                            .extract(sourceQuery.toString());
                    ParserViewDefinition view = metadata.createParserView(sourceQuery.toString(), attributes);

                    // this is required to preserve the order of the variables
                    ImmutableList<Map.Entry<QualifiedAttributeID,Variable>> list = view.getAttributes().stream()
                            .map(att -> new AbstractMap.SimpleEntry<>(
                                    new QualifiedAttributeID(null, att.getID()), // strip off the ParserViewDefinitionName
                                    TERM_FACTORY.getVariable(att.getID().getName())))
                            .collect(ImmutableCollectors.toList());

                    lookupTable = list.stream().collect(ImmutableCollectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                    List<Term> arguments = list.stream().map(Map.Entry::getValue).collect(ImmutableCollectors.toList());

                    body = new ArrayList<>(1);
                    body.add(TERM_FACTORY.getFunction(Relation2Predicate.createPredicateFromRelation(view), arguments));
                }

                for (ImmutableFunctionalTerm atom : mappingAxiom.getTargetAtoms()) {
                    PPMappingAssertionProvenance provenance = mappingAxiom.getMappingAssertionProvenance(atom);
                    try {

                        Function head = renameVariables(atom, lookupTable, idfac);
                        CQIE rule = DATALOG_FACTORY.getCQIE(head, body);

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
    private static Function renameVariables(Function function, ImmutableMap<QualifiedAttributeID, Term> attributes,
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

        return TERM_FACTORY.getFunction(function.getFunctionSymbol(), newTerms);
    }


    private static class AttributeNotFoundException extends Exception {
        AttributeNotFoundException(String message) {
            super(message);
        }
    }
}
