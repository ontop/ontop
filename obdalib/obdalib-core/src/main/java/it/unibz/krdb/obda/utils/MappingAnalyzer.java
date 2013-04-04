package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.BooleanLiteral;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.DecimalLiteral;
import it.unibz.krdb.sql.api.ICondition;
import it.unibz.krdb.sql.api.IValueExpression;
import it.unibz.krdb.sql.api.IntegerLiteral;
import it.unibz.krdb.sql.api.Literal;
import it.unibz.krdb.sql.api.LogicalOperator;
import it.unibz.krdb.sql.api.NullPredicate;
import it.unibz.krdb.sql.api.OrOperator;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.StringLiteral;
import it.unibz.krdb.sql.api.ComparisonPredicate.Operator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import com.hp.hpl.jena.iri.IRI;

public class MappingAnalyzer {

	private ArrayList<OBDAMappingAxiom> mappingList;
	private DBMetadata dbMetaData;

	private SQLQueryTranslator translator;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	/**
	 * Creates a mapping analyzer by taking into account the OBDA model.
	 */
	public MappingAnalyzer(ArrayList<OBDAMappingAxiom> mappingList, DBMetadata dbMetaData) {
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
					String tableName = table.getName();
					IRI predicateName = OBDADataFactoryImpl.getIRI(tableName);

					// Construct the predicate using the table name
					int arity = dbMetaData.getDefinition(tableName).countAttribute();
					Predicate predicate = dfac.getPredicate(predicateName, arity);

					// Swap the column name with a new variable from the lookup table
					List<NewLiteral> terms = new ArrayList<NewLiteral>();
					for (int i = 1; i <= arity; i++) {
						String columnName = dbMetaData.getFullQualifiedAttributeName(tableName, table.getAlias(), i);
						String termName = lookupTable.lookup(columnName);
						if (termName == null) {
							throw new RuntimeException("Column '" + columnName + "'was not found in the lookup table: ");
						}
						NewLiteral term = dfac.getVariable(termName);
						terms.add(term);
					}
					// Create an atom for a particular table
					Function atom = dfac.getAtom(predicate, terms);
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

					NewLiteral t1 = dfac.getVariable(lookup1);
					NewLiteral t2 = dfac.getVariable(lookup2);

					Function atom = dfac.getEQAtom(t1, t2);
					atoms.add(atom);
				}

				// For the selection "where" clause conditions
				Selection selection = queryTree.getSelection();
				if (selection != null) {
					// Filling up the OR stack
					Stack<Function> stack = new Stack<Function>();
					List<ICondition> conditions = selection.getRawConditions();
					for (int i = 0; i < conditions.size(); i++) {
						Object element = conditions.get(i);
						if (element instanceof ComparisonPredicate) {
							ComparisonPredicate pred = (ComparisonPredicate) element;
							Function compOperator = getFunction(pred, lookupTable);
							stack.push(compOperator);
						} else if (element instanceof NullPredicate) {
							NullPredicate pred = (NullPredicate) element;
							Function nullOperator = getFunction(pred, lookupTable);
							stack.push(nullOperator);
						} else if (element instanceof LogicalOperator) {
							// Check either it's AND or OR operator
							if (element instanceof AndOperator) {
								/*
								 * The AND operator has the expression:
								 * <condition> AND <condition> There are two
								 * types of conditions: (1) using the comparison
								 * predicate (such as EQ, LT, GT, etc.) and (2)
								 * using the null predicate (i.e., IS NULL or IS
								 * NOT NULL). Each has a different way to
								 * handle.
								 */
								ICondition condition = conditions.get(i + 1);
								i++; // the right-hand condition
								if (condition instanceof ComparisonPredicate) {
									ComparisonPredicate pred = (ComparisonPredicate) condition;
									NewLiteral leftCondition = stack.pop();
									NewLiteral rightCondition = getFunction(pred, lookupTable);
									Function andOperator = dfac.getANDFunction(leftCondition, rightCondition);
									stack.push(andOperator);
								} else if (condition instanceof NullPredicate) {
									NullPredicate pred = (NullPredicate) condition;
									NewLiteral leftCondition = stack.pop();
									NewLiteral rightCondition = getFunction(pred, lookupTable);
									Function andOperator = dfac.getANDFunction(leftCondition, rightCondition);
									stack.push(andOperator);
								}
							} else if (element instanceof OrOperator) {
								// NO-OP
							}
						} else {
							/* Unsupported query */
							return null;
						}
					}

					// Collapsing into a single atom.
					while (stack.size() > 1) {
						Function orAtom = dfac.getORFunction(stack.pop(), stack.pop());
						stack.push(orAtom);
					}
					Function f = stack.pop();
					Function atom = dfac.getAtom(f.getFunctionSymbol(), f.getTerms());
					atoms.add(atom);
				}

				// Construct the head from the target query.
				List<Function> atomList = targetQuery.getBody();
				for (Function atom : atomList) {
					List<NewLiteral> terms = atom.getTerms();
					List<NewLiteral> newterms = new LinkedList<NewLiteral>();
					for (NewLiteral term : terms) {
						newterms.add(updateTerm(term, lookupTable));
					}
					Function newhead = dfac.getAtom(atom.getPredicate(), newterms);
					CQIE rule = dfac.getCQIE(newhead, atoms);
					datalog.appendRule(rule);
				}
			} catch (Exception e) {
				errorMessage.add("Error in mapping with id: " + axiom.getId() + " \nDescription: "
						+ e.getMessage() + " \nMapping: [" + axiom.toString() + "]");
				
			}
		}
		if (errorMessage.size() > 0) {
			StringBuffer errors = new StringBuffer();
			for (String error: errorMessage) {
				errors.append(error + "\n");
			}
			final String msg = "There was an error analyzing the following mappings. Please correct the issue(s) to continue.\n" + errors.toString();
			RuntimeException r = new RuntimeException(msg);
			throw r;
		}
		return datalog;
	}

	private Function getFunction(NullPredicate pred, LookupTable lookupTable) {
		IValueExpression column = pred.getValueExpression();

		String columnName = column.toString();
		String variableName = lookupTable.lookup(columnName);
		if (variableName == null) {
			throw new RuntimeException("Unable to find column name for variable: " + columnName);
		}
		NewLiteral var = dfac.getVariable(variableName);

		if (pred.useIsNullOperator()) {
			return dfac.getIsNullFunction(var);
		} else {
			return dfac.getIsNotNullFunction(var);
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
		NewLiteral t1 = dfac.getVariable(termLeftName);

		String termRightName = "";
		NewLiteral t2 = null;
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
					t2 = dfac.getValueConstant(termRightName, COL_TYPE.DATETIME);
				} else {
					t2 = dfac.getValueConstant(termRightName, COL_TYPE.STRING);
				}
			} else if (literal instanceof IntegerLiteral) {
				t2 = dfac.getValueConstant(termRightName, COL_TYPE.INTEGER);
			} else if (literal instanceof DecimalLiteral) {
				t2 = dfac.getValueConstant(termRightName, COL_TYPE.DOUBLE);
			} else if (literal instanceof BooleanLiteral) {
				t2 = dfac.getValueConstant(termRightName, COL_TYPE.BOOLEAN);
			} else {
				t2 = dfac.getValueConstant(termRightName, COL_TYPE.LITERAL);
			}
		}

		Operator op = pred.getOperator();

		Function funct = null;
		switch (op) {
		case EQ: funct = dfac.getEQFunction(t1, t2); break;
		case GT: funct = dfac.getGTFunction(t1, t2); break;
		case LT: funct = dfac.getLTFunction(t1, t2); break;
		case GE: funct = dfac.getGTEFunction(t1, t2); break;
		case LE: funct = dfac.getLTEFunction(t1, t2); break;
		case NE: funct = dfac.getNEQFunction(t1, t2); break;
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
	private NewLiteral updateTerm(NewLiteral term, LookupTable lookupTable) {
		NewLiteral result = null;
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
			List<NewLiteral> terms = func.getTerms();
			List<NewLiteral> newterms = new LinkedList<NewLiteral>();
			for (NewLiteral innerTerm : terms) {
				newterms.add(updateTerm(innerTerm, lookupTable));
			}
			result = dfac.getFunctionalTerm(func.getFunctionSymbol(), newterms);
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
			String tableName = table.getName();
			DataDefinition def = dbMetaData.getDefinition(tableName);
			if (def == null) {
				throw new RuntimeException("Definition not found for table '" + tableName + "'.");
			}
			int size = def.countAttribute();

			for (int i = 1; i <= size; i++) {
				// assigned index number
				int index = i + offset;
				
				// simple attribute name
				String columnName = dbMetaData.getAttributeName(tableName, i);
				lookupTable.add(columnName, index);
				if (aliasMap.containsKey(columnName)) { // register the alias name, if any
					lookupTable.add(aliasMap.get(columnName), columnName);
				}
				
				// full qualified attribute name
				String qualifiedColumnName = dbMetaData.getFullQualifiedAttributeName(tableName, i);
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
					if (aliasMap.containsKey(qualifiedColumnName)) {
						lookupTable.add(aliasMap.get(qualifiedColumnName), qualifiedColumnName);
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