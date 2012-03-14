package it.unibz.krdb.obda.utils;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.ValueConstant;
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
import it.unibz.krdb.sql.api.OrOperator;
import it.unibz.krdb.sql.api.QueryTree;
import it.unibz.krdb.sql.api.ReferenceValueExpression;
import it.unibz.krdb.sql.api.Relation;
import it.unibz.krdb.sql.api.Selection;
import it.unibz.krdb.sql.api.StringLiteral;
import it.unibz.krdb.sql.api.ComparisonPredicate.Operator;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;

public class MappingAnalyzer {

	private ArrayList<OBDAMappingAxiom> mappingList;
	private DBMetadata dbMetaData;

	private SQLQueryTranslator translator;

	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	private static Logger log = org.slf4j.LoggerFactory.getLogger(MappingAnalyzer.class);

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
			// Obtain the target and source query from each mapping axiom in the model.
			CQIE targetQuery = (CQIE) axiom.getTargetQuery();
			OBDASQLQuery sourceQuery = (OBDASQLQuery) axiom.getSourceQuery();

			// Construct the SQL query tree from the source query
			QueryTree queryTree = translator.contructQueryTree(sourceQuery.toString());
			
			// Create a lookup table for variable swapping
			LookupTable lookupTable = createLookupTable(queryTree, dbMetaData);

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

				// Swap the column name with a new variable from the lookup table
				List<Term> terms = new ArrayList<Term>();
				for (int i = 1; i <= arity; i++) {
					String columnName = dbMetaData.getFullQualifiedAttributeName(tableName, i);
					String termName = lookupTable.lookup(columnName);
					if (termName == null) {
						throw new RuntimeException("Column was not found in the lookup table: " + columnName);
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
				Term t1 = dfac.getVariable(lookupTable.lookup(value[0]));
				Term t2 = dfac.getVariable(lookupTable.lookup(value[1]));
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
						Function operator = getFunction(pred, lookupTable);
						stack.push(operator);
					} else if (element instanceof AndOperator) {
						Term leftCondition = stack.pop();
						ComparisonPredicate rightPred = (ComparisonPredicate) conditions.get(i + 1);
						Term rightCondition = getFunction(rightPred, lookupTable);
						Function andFunct = dfac.getANDFunction(leftCondition, rightCondition);
						stack.push(andFunct);
						i += 1;
					} else if (element instanceof OrOperator) {
						// NO-OP
					} else {
						/* Unsupported query */
						return null;
					}
				}

				// Collapsing into a single atom.
				while (stack.size() != 1) {
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
		}
		return datalog;
	}

	private Function getFunction(ComparisonPredicate pred, LookupTable lookupTable) {
		IValueExpression left = pred.getValueExpressions()[0];
		IValueExpression right = pred.getValueExpressions()[1];

		String termLeftName = lookupTable.lookup(left.toString());
		Term t1 = dfac.getVariable(termLeftName);

		String termRightName = "";
		Term t2 = null;
		if (right instanceof ReferenceValueExpression) {
			termRightName = lookupTable.lookup(right.toString());
			t2 = dfac.getVariable(termRightName);
		} else if (right instanceof Literal) {
			Literal literal = (Literal) right;
			termRightName = literal.get().toString();			
			if (literal instanceof StringLiteral) {
				boolean isString = containDateTimeString(termRightName);
				if (isString) {
					t2 = dfac.getValueConstant(termRightName, COL_TYPE.STRING);
				} else {
					t2 = dfac.getValueConstant(termRightName, COL_TYPE.DATETIME);
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
		final String[] formatStrings = {
				"yyyy-MM-dd HH:mm:ss.SS",
				"yyyy-MM-dd HH:mm:ss",
				"yyyy-MM-dd",
				"yyyy-MM-ddTHH:mm:ssZ",
				"yyyy-MM-ddTHH:mm:ss.sZ"
		};
		
		for (String formatString : formatStrings) {
	        try {
	        	new SimpleDateFormat(formatString).parse(value);
	        	return true;
	        } catch (ParseException e) { }
	    }
    	return false; // the string doesn't contain date time info if none of the formats is suitable.
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
			String termName = lookupTable.lookup(var.getName());
			if (termName == null)
				throw new RuntimeException(String.format("Variable %s not found. Make sure not to use wildecard in your SQL query, e.g., star *", var));

			result = this.dfac.getVariable(termName);
			// System.out.println(termName);
			// var.setName(termName);
		} else if (term instanceof Function) {
			Function func = (Function) term;
			List<Term> terms = func.getTerms();
			List<Term> newterms = new LinkedList<Term>();
			for (Term innerTerm : terms) {
				newterms.add(updateTerm(innerTerm, lookupTable));
				// updateTerm(innerTerm, lookupTable);
			}
			result = dfac.getFunctionalTerm(func.getFunctionSymbol(), newterms);
		} else if (term instanceof ValueConstant) {
			result = term.clone();
		}
		return result;
	}

	private LookupTable createLookupTable(QueryTree queryTree, DBMetadata dbmetadata) {
		LookupTable lookupTable = new LookupTable(dbmetadata);

		// Collect all the possible column names from tables.
		ArrayList<Relation> tableList = queryTree.getTableSet();
		for (Relation table : tableList) {
			String tableName = table.getName();

			DataDefinition def = dbMetaData.getDefinition(tableName);
			if (def == null)
				throw new RuntimeException("Definition not found for table: " + tableName);
			int size = def.countAttribute();

			String[] columnList = new String[2];
			for (int i = 1; i <= size; i++) {
				columnList[0] = dbMetaData.getAttributeName(tableName, i);
				columnList[1] = dbMetaData.getFullQualifiedAttributeName(tableName, i);
				lookupTable.add(columnList);
			}
		}

		// Add the aliases
		ArrayList<String> aliasMap = queryTree.getAliasMap();
		for (String alias : aliasMap) {
			String[] reference = alias.split("=");
			lookupTable.add(reference[0], reference[1]);
		}

		return lookupTable;
	}
}