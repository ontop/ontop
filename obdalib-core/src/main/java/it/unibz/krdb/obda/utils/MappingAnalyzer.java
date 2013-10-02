/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.BooleanAlgebraPredicate;
import it.unibz.krdb.sql.api.BooleanLiteral;
import it.unibz.krdb.sql.api.BooleanOperator;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.ComparisonPredicate.Operator;
import it.unibz.krdb.sql.api.DecimalLiteral;
import it.unibz.krdb.sql.api.ICondition;
import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.IntegerLiteral;
import it.unibz.krdb.sql.api.LeftParenthesis;
import it.unibz.krdb.sql.api.Literal;
import it.unibz.krdb.sql.api.NullPredicate;
import it.unibz.krdb.sql.api.OrOperator;
import it.unibz.krdb.sql.api.Parenthesis;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.RightParenthesis;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.StringLiteral;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

//import com.hp.hpl.jena.iri.IRI;

public class MappingAnalyzer {

	private List<OBDAMappingAxiom> mappingList;
	private DBMetadata dbMetaData;

	private SQLQueryTranslator translator;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	/**
	 * Creates a mapping analyzer by taking into account the OBDA model.
	 */
	public MappingAnalyzer(List<OBDAMappingAxiom> mappingList, DBMetadata dbMetaData) {
		this.mappingList = mappingList;
		this.dbMetaData = dbMetaData;

		translator = new SQLQueryTranslator(dbMetaData);
	}

	public DatalogProgram constructDatalogProgram() {
		DatalogProgram datalog = dfac.getDatalogProgram();
		LinkedList<String> errorMessage = new LinkedList<String>();
		for (OBDAMappingAxiom axiom : mappingList) {
			try {
				// Obtain the target and source query from each mapping axiom in
				// the model.
				CQIE targetQuery = (CQIE) axiom.getTargetQuery();
				
				// This is the new way to get the parsed sql, since it is already parsed by the mapping parser
				// Currently disabled, to prevent interference with the MetaMappingExpander
				//QueryTree queryTree = axiom.getSourceQueryTree();

				
				OBDASQLQuery sourceQuery = (OBDASQLQuery) axiom.getSourceQuery();

				// Construct the SQL query tree from the source query
				QueryTree queryTree = translator.contructQueryTree(sourceQuery.toString());
				 
				
				// Create a lookup table for variable swapping
				LookupTable lookupTable = createLookupTable(queryTree);

				// We can get easily the table from the SQL query tree
				ArrayList<Relation> tableList = queryTree.getTableSet();

				// Construct the body from the source query
				ArrayList<Function> atoms = new ArrayList<Function>();
				for (Relation table : tableList) {
					// Construct the URI from the table name
					String tableName = table.getGivenName();
					String predicateName = tableName;

					// Construct the predicate using the table name
					int arity = dbMetaData.getDefinition(tableName).countAttribute();
					Predicate predicate = dfac.getPredicate(predicateName, arity);

					// Swap the column name with a new variable from the lookup table
					List<Term> terms = new ArrayList<Term>();
					for (int i = 1; i <= arity; i++) {
						String columnName = dbMetaData.getFullQualifiedAttributeName(tableName, table.getAlias(), i);
						String termName = lookupTable.lookup(columnName);
						if (termName == null) {
							throw new RuntimeException("Column '" + columnName + "'was not found in the lookup table: ");
						}
						Term term = dfac.getVariable(termName);
						terms.add(term);
					}
					// Create an atom for a particular table
					Function atom = dfac.getFunction(predicate, terms);
					atoms.add(atom);
				}

				// For the join conditions
				ArrayList<String> joinConditions = queryTree.getJoinCondition();
				for (String predicate : joinConditions) {
					String[] value = predicate.split("=");
					String leftValue = value[0];
					String rightValue = value[1];
					String lookup1 = lookupTable.lookup(leftValue);
					String lookup2 = lookupTable.lookup(rightValue);
					if (lookup1 == null)
						throw new RuntimeException("Unable to get column name for variable: " + leftValue);
					if (lookup2 == null)
						throw new RuntimeException("Unable to get column name for variable: " + rightValue);

					Term t1 = dfac.getVariable(lookup1);
					Term t2 = dfac.getVariable(lookup2);

					Function atom = dfac.getFunctionEQ(t1, t2);
					atoms.add(atom);
				}

				// For the selection "where" clause conditions
				Selection selection = queryTree.getSelection();
				if (selection != null) {
					// Stack for filter function
					Stack<Function> filterFunctionStack = new Stack<Function>();
					// Stack for boolean algebra predicate
					Stack<BooleanAlgebraPredicate> booleanPredicateStack = new Stack<BooleanAlgebraPredicate>();
					
					List<ICondition> conditions = selection.getRawConditions();
					for (int i = 0; i < conditions.size(); i++) {
						Object element = conditions.get(i);
						if (element instanceof ComparisonPredicate) {
							ComparisonPredicate pred = (ComparisonPredicate) element;
							Function filterFunction = getFunction(pred, lookupTable);
							if (hasBooleanOperator(booleanPredicateStack)) {
								BooleanOperator op = (BooleanOperator) booleanPredicateStack.pop();
								Function otherFilterFunction = filterFunctionStack.pop();
								filterFunction = createBooleanFunction(otherFilterFunction, filterFunction, op);
							}
							filterFunctionStack.push(filterFunction);
						} else if (element instanceof NullPredicate) {
							NullPredicate pred = (NullPredicate) element;
							Function filterFunction = getFunction(pred, lookupTable);
							if (hasBooleanOperator(booleanPredicateStack)) {
								BooleanOperator op = (BooleanOperator) booleanPredicateStack.pop();
								Function otherFilterFunction = filterFunctionStack.pop();
								filterFunction = createBooleanFunction(otherFilterFunction, filterFunction, op);
							}
							filterFunctionStack.push(filterFunction);
						} else if (element instanceof BooleanAlgebraPredicate) {
							BooleanAlgebraPredicate pred = (BooleanAlgebraPredicate) element;
							if (pred instanceof BooleanOperator) {
								BooleanOperator op = (BooleanOperator) pred;
								manageBooleanOperator(op, booleanPredicateStack);
							} else if (pred instanceof Parenthesis) {
								Parenthesis paren = (Parenthesis) pred;
								manageParenthesis(paren, booleanPredicateStack, filterFunctionStack);
							}
						}
					}
					
					// Check if there are still boolean operators left in the stack
					while (!booleanPredicateStack.isEmpty()) {
						BooleanOperator op = (BooleanOperator) booleanPredicateStack.pop();					
						Function filterFunction = createBooleanFunction(filterFunctionStack, op);
						filterFunctionStack.push(filterFunction);
					}
					
					// The filter function stack must have 1 element left
					if (filterFunctionStack.size() == 1) {
						Function filterFunction = filterFunctionStack.pop();
						Function atom = dfac.getFunction(filterFunction.getFunctionSymbol(), filterFunction.getTerms());
						atoms.add(atom);
					} else {						
						throwInvalidFilterExpressionException(filterFunctionStack);
					}
				}

				// Construct the head from the target query.
				List<Function> atomList = targetQuery.getBody();
				for (Function atom : atomList) {
					List<Term> terms = atom.getTerms();
					List<Term> newterms = new LinkedList<Term>();
					for (Term term : terms) {
						newterms.add(updateTerm(term, lookupTable));
					}
					Function newhead = dfac.getFunction(atom.getPredicate(), newterms);
					CQIE rule = dfac.getCQIE(newhead, atoms);
					datalog.appendRule(rule);
				}
			} catch (Exception e) {
				errorMessage.add("Error in mapping with id: " + axiom.getId() + " \n Description: "
						+ e.getMessage() + " \nMapping: [" + axiom.toString() + "]");
				
			}
		}
		if (errorMessage.size() > 0) {
			StringBuilder errors = new StringBuilder();
			for (String error: errorMessage) {
				errors.append(error + "\n");
			}
			final String msg = "There was an error analyzing the following mappings. Please correct the issue(s) to continue.\n" + errors.toString();
			RuntimeException r = new RuntimeException(msg);
			throw r;
		}
		return datalog;
	}
	
	private void throwInvalidFilterExpressionException(Stack<Function> filterFunctionStack) {
		StringBuilder filterExpression = new StringBuilder();
		while (!filterFunctionStack.isEmpty()) {
			filterExpression.append(filterFunctionStack.pop());
		}
		throw new RuntimeException("Illegal filter expression: " + filterExpression.toString());
	}

	private void manageBooleanOperator(BooleanOperator op, Stack<BooleanAlgebraPredicate> booleanPredicateStack) {
		booleanPredicateStack.push(op);
	}
	
	private void manageParenthesis(Parenthesis paren, Stack<BooleanAlgebraPredicate> booleanPredicateStack, Stack<Function> filterFunctionStack) {
		if (paren instanceof LeftParenthesis) {
			booleanPredicateStack.push(paren);
		} else if (paren instanceof RightParenthesis) {
			while (true) {
				BooleanAlgebraPredicate predicate = booleanPredicateStack.pop();		
				if (predicate instanceof LeftParenthesis) {
					break;
				}
				BooleanOperator op = (BooleanOperator) predicate;					
				Function filterFunction = createBooleanFunction(filterFunctionStack, op);
				filterFunctionStack.push(filterFunction);
			}
		}
	}
	
	private Function createBooleanFunction(Stack<Function> filterFunctionStack, BooleanOperator op) {
		Function rightFunction = filterFunctionStack.pop();
		Function leftFunction = filterFunctionStack.pop();
		return createBooleanFunction(leftFunction, rightFunction, op);
	}

	private Function createBooleanFunction(Function leftFunction, Function rightFunction, BooleanOperator op) {
		Function booleanFunction = null;
		if (op instanceof AndOperator) {
			booleanFunction = dfac.getFunctionAND(leftFunction, rightFunction);
		} else if (op instanceof OrOperator) {
			booleanFunction = dfac.getFunctionOR(leftFunction, rightFunction);
		}
		return booleanFunction;
	}

	private boolean hasBooleanOperator(Stack<BooleanAlgebraPredicate> boolStack) {
		if (!boolStack.isEmpty()) {
			BooleanAlgebraPredicate pred = boolStack.peek();
			return (pred instanceof BooleanOperator) ? true : false;
		}
		return false;
	}
	
	private Function getFunction(NullPredicate pred, LookupTable lookupTable) {
		IValueExpression column = pred.getValueExpression();

		String columnName = column.toString();
		String variableName = lookupTable.lookup(columnName);
		if (variableName == null) {
			throw new RuntimeException("Unable to find column name for variable: " + columnName);
		}
		Term var = dfac.getVariable(variableName);

		if (pred.useIsNullOperator()) {
			return dfac.getFunctionIsNull(var);
		} else {
			return dfac.getFunctionIsNotNull(var);
		}
	}

	private Function getFunction(ComparisonPredicate pred, LookupTable lookupTable) {
		IValueExpression left = pred.getValueExpressions()[0];
		IValueExpression right = pred.getValueExpressions()[1];

		String leftValueName = left.toString();
		String termLeftName = lookupTable.lookup(leftValueName);
		if (termLeftName == null) {
			throw new RuntimeException("Unable to find column name for variable: " + leftValueName);
		}
		Term t1 = dfac.getVariable(termLeftName);

		String termRightName = "";
		Term t2 = null;
		if (right instanceof ReferenceValueExpression) {
			String rightValueName = right.toString();
			termRightName = lookupTable.lookup(rightValueName);
			if (termRightName == null) {
				throw new RuntimeException("Unable to find column name for variable: " + rightValueName);
			}
			t2 = dfac.getVariable(termRightName);
		} else if (right instanceof Literal) {
			Literal literal = (Literal) right;
			termRightName = literal.get().toString();
			if (literal instanceof StringLiteral) {
				boolean isDateTime = containDateTimeString(termRightName);
				if (isDateTime) {
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.DATETIME);
				} else {
					t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.STRING);
				}
			} else if (literal instanceof IntegerLiteral) {
				t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.INTEGER);
			} else if (literal instanceof DecimalLiteral) {
				t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.DOUBLE);
			} else if (literal instanceof BooleanLiteral) {
				t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.BOOLEAN);
			} else {
				t2 = dfac.getConstantLiteral(termRightName, COL_TYPE.LITERAL);
			}
		}

		Operator op = pred.getOperator();

		Function funct = null;
		switch (op) {
		case EQ: funct = dfac.getFunctionEQ(t1, t2); break;
		case GT: funct = dfac.getFunctionGT(t1, t2); break;
		case LT: funct = dfac.getFunctionLT(t1, t2); break;
		case GE: funct = dfac.getFunctionGTE(t1, t2); break;
		case LE: funct = dfac.getFunctionLTE(t1, t2); break;
		case NE: funct = dfac.getFunctionNEQ(t1, t2); break;
		default:
			throw new RuntimeException("Unknown opertor: " + op.toString() + " " + op.getClass().toString());
		}
		return funct;
	}

	private boolean containDateTimeString(String value) {
		final String[] formatStrings = { "yyyy-MM-dd HH:mm:ss.SS", 
				"yyyy-MM-dd HH:mm:ss", 
				"yyyy-MM-dd", 
				"yyyy-MM-dd'T'HH:mm:ssZ",
				"yyyy-MM-dd'T'HH:mm:ss.sZ" };

		for (String formatString : formatStrings) {
			try {
				new SimpleDateFormat(formatString).parse(value);
				return true;
			} catch (ParseException e) {
				// NO-OP
			}
		}
		return false; // the string doesn't contain date time info if none of the formats is suitable.
	}

	/**
	 * Returns a new term with the updated references.
	 */
	private Term updateTerm(Term term, LookupTable lookupTable) {
		Term result = null;
		if (term instanceof Variable) {
			Variable var = (Variable) term;
			String varName = var.getName();
			String termName = lookupTable.lookup(varName);
			if (termName == null) {
				final String msg = String.format("Error in identifying column name \"%s\", please check the query source in the mappings.\nPossible reasons:\n1. The name is ambiguous, or\n2. The name is not defined in the database schema.", var);
				throw new RuntimeException(msg);
			}
			result = dfac.getVariable(termName);
		} else if (term instanceof Function) {
			Function func = (Function) term;
			List<Term> terms = func.getTerms();
			List<Term> newterms = new LinkedList<Term>();
			for (Term innerTerm : terms) {
				newterms.add(updateTerm(innerTerm, lookupTable));
			}
			result = dfac.getFunction(func.getFunctionSymbol(), newterms);
		} else if (term instanceof Constant) {
			result = term.clone();
		}
		return result;
	}

	private LookupTable createLookupTable(QueryTree queryTree) {
		LookupTable lookupTable = new LookupTable();

		// Collect all the possible column names from tables.
		ArrayList<Relation> tableList = queryTree.getTableSet();

		// Collect all known column aliases
		HashMap<String, String> aliasMap = queryTree.getAliasMap();
		
		int offset = 0; // the index offset

		for (Relation table : tableList) {
			String tableName = table.getTableName();
			String tableGivenName = table.getGivenName();
			DataDefinition def = dbMetaData.getDefinition(tableGivenName);
			if (def == null) {
				throw new RuntimeException("Definition not found for table '" + tableGivenName + "'.");
			}
			int size = def.countAttribute();

			for (int i = 1; i <= size; i++) {
				// assigned index number
				int index = i + offset;
				
				// simple attribute name
				String columnName = dbMetaData.getAttributeName(tableGivenName, i);
				lookupTable.add(columnName, index);
				if (aliasMap.containsKey(columnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(columnName), columnName);
				}
				
				// If the column name in the select string is in lower case
				if (aliasMap.containsKey(columnName.toLowerCase())) { // register the alias name, if any
					lookupTable.add(aliasMap.get(columnName.toLowerCase()), columnName);
				}
				

				// If the column name in the select string is in upper case
				if (aliasMap.containsKey(columnName.toUpperCase())) { // register the alias name, if any
					lookupTable.add(aliasMap.get(columnName.toUpperCase()), columnName);
				}
				
				
				// attribute name with table name prefix
				String tableColumnName = tableName + "." + columnName;
				lookupTable.add(tableColumnName, index);
				if (aliasMap.containsKey(tableColumnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(tableColumnName), tableColumnName);
				}
				
				
				// full qualified attribute name
				String qualifiedColumnName = dbMetaData.getFullQualifiedAttributeName(tableGivenName, i);
				lookupTable.add(qualifiedColumnName, index);
				if (aliasMap.containsKey(qualifiedColumnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(qualifiedColumnName), qualifiedColumnName);
				}
				
				// full qualified attribute name using table alias
				String tableAlias = table.getAlias();
				if (!tableAlias.isEmpty()) {
					String qualifiedColumnAlias = dbMetaData.getFullQualifiedAttributeName(tableName, tableAlias, i);
					lookupTable.add(qualifiedColumnAlias, index);
					if (aliasMap.containsKey(columnName)) {
						lookupTable.add(aliasMap.get(columnName), columnName);
					}
					// If the column name in the select string is in lower case
					if (aliasMap.containsKey(columnName.toLowerCase())) { // register the alias name, if any
						lookupTable.add(aliasMap.get(columnName.toLowerCase()), columnName);
					}

					// If the column name in the select string is in upper case
					if (aliasMap.containsKey(columnName.toUpperCase())) { // register the alias name, if any
						lookupTable.add(aliasMap.get(columnName.toUpperCase()), columnName);
					}
					
					if (aliasMap.containsKey(qualifiedColumnName)) {
						lookupTable.add(aliasMap.get(qualifiedColumnName), qualifiedColumnName);
					}
					// If the qualified column name in the select string is in lower case
					if (aliasMap.containsKey(qualifiedColumnName.toLowerCase())) { // register the alias name, if any
						lookupTable.add(aliasMap.get(qualifiedColumnName.toLowerCase()), qualifiedColumnName);
					}
					// If the qualified column name in the select string is in upper case
					if (aliasMap.containsKey(qualifiedColumnName.toUpperCase())) { // register the alias name, if any
						lookupTable.add(aliasMap.get(qualifiedColumnName.toUpperCase()), qualifiedColumnName);
					}
					if (aliasMap.containsKey(qualifiedColumnAlias)) {
						lookupTable.add(aliasMap.get(qualifiedColumnAlias), qualifiedColumnAlias);
					}
				}
			}
			offset += size;
		}
		return lookupTable;
	}
}
