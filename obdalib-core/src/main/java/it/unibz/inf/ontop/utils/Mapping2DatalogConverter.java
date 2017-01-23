package it.unibz.inf.ontop.utils;

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
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Function;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.sql.*;
import it.unibz.inf.ontop.sql.parser.RelationalExpression;
import it.unibz.inf.ontop.sql.parser.SelectQueryAttributeExtractor;
import it.unibz.inf.ontop.sql.parser.SelectQueryParser;
import it.unibz.inf.ontop.sql.parser.exceptions.UnsupportedSelectQueryException;

import java.util.*;

import com.google.common.collect.ImmutableMap;

/**
 * @author Roman Kontchakov
 */

public class Mapping2DatalogConverter {

	private static final OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	/**
	 * returns a Datalog representation of the mappings
	 */
	public static List<CQIE> constructDatalogProgram(Collection<OBDAMappingAxiom> mappings, DBMetadata dbMetadata) {
		
		List<CQIE> datalogProgram = new LinkedList<>();
		List<String> errorMessages = new ArrayList<>();
		
		QuotedIDFactory idfac = dbMetadata.getQuotedIDFactory();
		
		for (OBDAMappingAxiom mappingAxiom : mappings) {
            try {
                OBDASQLQuery sourceQuery = mappingAxiom.getSourceQuery();

                SelectQueryParser sqp = new SelectQueryParser(dbMetadata);
                List<Function> body;
                ImmutableMap<QualifiedAttributeID, Variable> lookupTable;

                try {
                    RelationalExpression re = sqp.parse(sourceQuery.toString());
                    lookupTable = re.getAttributes();

                    body = new ArrayList<>(re.getDataAtoms().size() + re.getFilterAtoms().size());
                    body.addAll(re.getDataAtoms());
                    body.addAll(re.getFilterAtoms());
                }
                catch (UnsupportedSelectQueryException e) {
                    // WRAP UP
                    //ImmutableSet<QuotedID> variableNames = mappingAxiom.getTargetQuery().stream()
                    //        .map(f -> collectVariableNames(idfac, f))
                    //        .reduce((s1, s2) -> ImmutableSet.<QuotedID>builder().addAll(s1).addAll(s2).build())
                    //        .get();
                    ImmutableList<QuotedID> variableNames =
                            new SelectQueryAttributeExtractor(dbMetadata).extract(sourceQuery.toString());

                    ParserViewDefinition view = dbMetadata.createParserView(sourceQuery.toString());
                    // TODO: clean up
                    boolean needsCreating = view.getAttributes().isEmpty();
                    ImmutableMap.Builder<QualifiedAttributeID, Variable> builder = ImmutableMap.builder();
                    List<Term> arguments = new ArrayList<>(variableNames.size());
                    variableNames.forEach(id -> {
                        QualifiedAttributeID qId = new QualifiedAttributeID(null, id);
                        if (needsCreating)
                            view.addAttribute(qId);
                        Variable var = fac.getVariable(id.getName());
                        builder.put(qId, var);
                        arguments.add(var);
                    });

                    lookupTable = builder.build();

                    body = new ArrayList<>(1);
                    body.add(fac.getFunction(Relation2DatalogPredicate.createPredicateFromRelation(view), arguments));
                }

                for (Function atom : mappingAxiom.getTargetQuery()) {
                    Function head = renameVariables(atom, lookupTable, idfac);
                    CQIE rule = fac.getCQIE(head, body);
                    datalogProgram.add(rule);
                }
            }
            catch (Exception e) { // in particular, InvalidSelectQueryException
                errorMessages.add("Error in mapping with id: " + mappingAxiom.getId()
                        + "\nDescription: " + e.getMessage()
                        + "\nMapping: [" + mappingAxiom.toString() + "]");
            }
		}

		if (!errorMessages.isEmpty())
			throw new IllegalArgumentException(
			        "There were errors analyzing the following mappings. " +
                    "Please correct the issues to continue.\n\n" +
                            Joiner.on("\n\n").join(errorMessages));

		return datalogProgram;
	}


    /**
     * Returns a new function by renaming variables occurring in the {@code function}
     *  according to the {@code attributes} lookup table
     */
    private static Function renameVariables(Function function, ImmutableMap<QualifiedAttributeID, Variable> attributes, QuotedIDFactory idfac) {
        List<Term> terms = function.getTerms();
        List<Term> newTerms = new ArrayList<>(terms.size());
        for (Term t : terms)
            newTerms.add(renameTermVariables(t, attributes, idfac));

        return fac.getFunction(function.getFunctionSymbol(), newTerms);
    }

    /**
     * Returns a new term by renaming variables occurring in the {@code term}
     *  according to the {@code attributes} lookup table
     */
    private static Term renameTermVariables(Term term, ImmutableMap<QualifiedAttributeID, Variable> attributes, QuotedIDFactory idfac) {

        if (term instanceof Variable) {
            Variable var = (Variable) term;
            String varName = var.getName();
            // TODO: remove this code
            // chop off the qualifying table name
            if (varName.contains("."))
                varName = varName.substring(varName.indexOf(".") + 1);
            QuotedID attribute = idfac.createAttributeID(varName);
            Variable newVar = attributes.get(new QualifiedAttributeID(null, attribute));

            if (newVar == null) {
                QuotedID quotedAttribute = QuotedID.createIdFromDatabaseRecord(idfac, varName);
                newVar = attributes.get(new QualifiedAttributeID(null, quotedAttribute));

                if (newVar == null)
                    throw new IllegalArgumentException("Column " + attribute + " ( " + var.getName() + " ) not found in " + attributes);
            }

            return newVar;
        }
        else if (term instanceof Function)
            return renameVariables((Function) term, attributes, idfac);

        else if (term instanceof Constant)
            return term.clone();

        throw new RuntimeException("Unknown term type: " + term);
    }
}
