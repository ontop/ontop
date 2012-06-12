package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.parser.SQLQueryTranslator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.api.AndOperator;
import it.unibz.krdb.sql.api.BooleanLiteral;
import it.unibz.krdb.sql.api.ComparisonPredicate;
import it.unibz.krdb.sql.api.ComparisonPredicate.Operator;
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

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class MappingAnalyzer {

	private ArrayList<OBDAMappingAxiom> mappingList;
	private DBMetadata dbMetaData;

	private SQLQueryTranslator translator;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	/**
	 * Creates a mapping analyzer by taking into account the OBDA model.
	 * 
	 * @param model
	 */
	public MappingAnalyzer(ArrayList<OBDAMappingAxiom> mappingList, DBMetadata dbMetaData) {
		this.mappingList = mappingList;
		this.dbMetaData = dbMetaData;

		translator = new SQLQueryTranslator(dbMetaData);
	}

	public DatalogProgram constructDatalogProgram() {

		DatalogProgram datalog = dfac.getDatalogProgram();

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
				ArrayList<Atom> atoms = new ArrayList<Atom>();
				for (Relation table : tableList) {
					// Construct the URI from the table name
					String tableName = table.getName();
					URI predicateName = URI.create(tableName);

					// Construct the predicate using the table name
					int arity = dbMetaData.getDefinition(tableName).countAttribute();
					Predicate predicate = dfac.getPredicate(predicateName, arity);

					// Swap the column name with a new variable from the lookup
					// table
					List<Term> terms = new ArrayList<Term>();
					for (int i = 1; i <= arity; i++) {
						String columnName = dbMetaData.getFullQualifiedAttributeName(tableName, i);
						String termName = lookupTable.lookup(columnName);
						if (termName == null) {
							throw new RuntimeException("Column '" + columnName + "'was not found in the lookup table: ");
						}
						Term term = dfac.getVariable(termName);
						terms.add(term);
					}
					// Create an atom for a particular table
					Atom atom = dfac.getAtom(predicate, terms);
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
					
					
					
					Atom atom = dfac.getEQAtom(t1, t2);
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
									Term leftCondition = stack.pop();
									Term rightCondition = getFunction(pred, lookupTable);
									Function andOperator = dfac.getANDFunction(leftCondition, rightCondition);
									stack.push(andOperator);
								} else if (condition instanceof NullPredicate) {
									NullPredicate pred = (NullPredicate) condition;
									Term leftCondition = stack.pop();
									Term rightCondition = getFunction(pred, lookupTable);
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
					Atom atom = dfac.getAtom(f.getFunctionSymbol(), f.getTerms());
					atoms.add(atom);
				}

				// Construct the head from the target query.
				List<Atom> atomList = targetQuery.getBody();
				for (Atom atom : atomList) {
					List<Term> terms = atom.getTerms();
					List<Term> newterms = new LinkedList<Term>();
					for (Term term : terms) {
						newterms.add(updateTerm(term, lookupTable));
					}
					Atom newhead = dfac.getAtom(atom.getPredicate(), newterms);
					CQIE rule = dfac.getCQIE(newhead, atoms);
					datalog.appendRule(rule);
				}
			} catch (Exception e) {
				RuntimeException r = new RuntimeException("Error analyzing mapping [" + axiom.toString() + "] Error description: " + e.getMessage());
				r.setStackTrace(e.getStackTrace());
				throw r; 
			}
		}
		return datalog;
	}

	private Function getFunction(NullPredicate pred, LookupTable lookupTable) {
		IValueExpression column = pred.getValueExpression();

		String columnName = column.toString();
		String variableName = lookupTable.lookup(columnName);
		
		if (variableName == null)
			throw new RuntimeException("Unable to find column name for variable: " + columnName);
		
		Term var = dfac.getVariable(variableName);

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
		
		if (termLeftName == null)
			throw new RuntimeException("Unable to find column name for variable: " + leftValueName);
		
		Term t1 = dfac.getVariable(termLeftName);

		String termRightName = "";
		Term t2 = null;
		if (right instanceof ReferenceValueExpression) {
			String rightValueName = right.toString();
			termRightName = lookupTable.lookup(rightValueName);
			
			if (termRightName == null)
				throw new RuntimeException("Unable to find column name for variable: " + rightValueName);
			
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
		case EQ:
			funct = dfac.getEQFunction(t1, t2);
			break;
		case GT:
			funct = dfac.getGTFunction(t1, t2);
			break;
		case LT:
			funct = dfac.getLTFunction(t1, t2);
			break;
		case GE:
			funct = dfac.getGTEFunction(t1, t2);
			break;
		case LE:
			funct = dfac.getLTEFunction(t1, t2);
			break;
		case NE:
			funct = dfac.getNEQFunction(t1, t2);
			break;
		default:
			throw new RuntimeException("Unknown opertor: " + op.toString() + " " + op.getClass().toString());
		}
		return funct;
	}

	private boolean containDateTimeString(String value) {
		final String[] formatStrings = { "yyyy-MM-dd HH:mm:ss.SS", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ssZ",
				"yyyy-MM-dd'T'HH:mm:ss.sZ" };

		for (String formatString : formatStrings) {
			try {
				new SimpleDateFormat(formatString).parse(value);
				return true;
			} catch (ParseException e) {
			}
		}
		return false; // the string doesn't contain date time info if none of
						// the formats is suitable.
	}

	/***
	 * Returns a new term with the updated references
	 * 
	 * @param term
	 * @param lookupTable
	 * @return
	 */
	private Term updateTerm(Term term, LookupTable lookupTable) {
		Term result = null;
		if (term instanceof Variable) {
			Variable var = (Variable) term;
			String varName = var.getName();
			String termName = lookupTable.lookup(varName);
			if (termName == null) {
				throw new RuntimeException(
						String.format(
								"Column %s not found. Hint: don't use wildecards in your SQL query, e.g., star *, and verify word-casing for case-sensitive database.",
								var));
			}
			result = dfac.getVariable(termName);
		} else if (term instanceof Function) {
			Function func = (Function) term;
			List<Term> terms = func.getTerms();
			List<Term> newterms = new LinkedList<Term>();
			for (Term innerTerm : terms) {
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
		for (Relation table : tableList) {
			String tableName = table.getName();
			DataDefinition def = dbMetaData.getDefinition(tableName);
			if (def == null) {
				throw new RuntimeException("Definition not found for table '" + tableName + "'.");
			}
			int size = def.countAttribute();

			String[] columnList = new String[2];
			for (int i = 1; i <= size; i++) {
				columnList[0] = dbMetaData.getAttributeName(tableName, i); // get
																			// the
																			// simple
																			// attribute
																			// name
				columnList[1] = dbMetaData.getFullQualifiedAttributeName(tableName, i); // get
																						// the
																						// full
																						// qualified
																						// attribute
																						// name
				lookupTable.add(columnList);
			}
		}

		// Add the aliases
		ArrayList<String> aliasMap = queryTree.getAliasMap();
		for (String alias : aliasMap) {
			String[] reference = alias.split("=");
			String aliasName = reference[0];
			String columnName = reference[1];
			lookupTable.add(aliasName, columnName);
		}

		return lookupTable;
	}
}