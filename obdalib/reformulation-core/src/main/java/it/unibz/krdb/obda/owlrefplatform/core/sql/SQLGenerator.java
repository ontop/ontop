package it.unibz.krdb.obda.owlrefplatform.core.sql;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.OBDAQueryModifiers.OrderCondition;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.SQLDialectAdapter;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SQLQueryGenerator;
import it.unibz.krdb.sql.DBMetadata;
import it.unibz.krdb.sql.DataDefinition;
import it.unibz.krdb.sql.TableDefinition;
import it.unibz.krdb.sql.ViewDefinition;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.LoggerFactory;

public class SQLGenerator implements SQLQueryGenerator {

	private static final long serialVersionUID = 7477161929752147045L;

	/**
	 * Operator symbols
	 */
	private static final String EQ_OPERATOR = "%s = %s";
	private static final String NEQ_OPERATOR = "%s <> %s";
	private static final String GT_OPERATOR = "%s > %s";
	private static final String GTE_OPERATOR = "%s >= %s";
	private static final String LT_OPERATOR = "%s < %s";
	private static final String LTE_OPERATOR = "%s <= %s";
	private static final String AND_OPERATOR = "%s AND %s";
	private static final String OR_OPERATOR = "%s OR %s";
	private static final String NOT_OPERATOR = "NOT %s";
	private static final String IS_NULL_OPERATOR = "%s IS NULL";
	private static final String IS_NOT_NULL_OPERATOR = "%s IS NOT NULL";

	/**
	 * Formatting template
	 */
	private static final String VIEW_NAME = "QVIEW%s";

	private final DBMetadata metadata;
	private final JDBCUtility jdbcutil;
	private final SQLDialectAdapter sqladapter;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(SQLGenerator.class);

	public SQLGenerator(DBMetadata metadata, JDBCUtility jdbcutil, SQLDialectAdapter sqladapter) {
		this.metadata = metadata;
		this.jdbcutil = jdbcutil;
		this.sqladapter = sqladapter;
	}

	/***
	 * Creates an index for the variables that appear in the body of query. The
	 * indexes are
	 * <p/>
	 * Variable -> Count (number of appereances in DB atoms)<br/>
	 * Variable -> List(Integer) array of size body.size(), true if the variable
	 * appears in that atom<br/>
	 * Variable -> Atom -> List(integer) -> boolean[] (variable, and atom index
	 * to the locations in that atom where the variable appears.
	 * 
	 * @param query
	 */
	private void createVariableIndex(CQIE query, Map<Variable, Integer> varCount, Map<Variable, List<Integer>> varAtomIndex,
			Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex) {
		List<Atom> body = query.getBody();

		int atomindex = -1;
		for (Atom atom : body) {
			atomindex += 1;
			if (atom.getPredicate() instanceof BooleanOperationPredicate) {
				continue;
			}
			int termindex = -1;
			for (Term term : atom.getTerms()) {
				termindex += 1;
				if (term instanceof Variable) {
					Variable var = (Variable) term;

					/* Updating the count */

					Integer count = varCount.get(var);
					if (count == null) {
						count = 1;
					} else {
						count += 1;
					}
					varCount.put(var, count);

					/* Updating the atom position */

					List<Integer> atomIndex = varAtomIndex.get(var);
					if (atomIndex == null) {
						atomIndex = new LinkedList<Integer>();
						varAtomIndex.put(var, atomIndex);
					}
					atomIndex.add(atomindex);

					/* Updating the term in atom position */

					Map<Atom, List<Integer>> atomTermIndex = varAtomTermIndex.get(var);
					if (atomTermIndex == null) {
						atomTermIndex = new HashMap<Atom, List<Integer>>();
						varAtomTermIndex.put(var, atomTermIndex);
					}
					List<Integer> termIndex = atomTermIndex.get(atom);
					if (termIndex == null) {
						termIndex = new LinkedList<Integer>();
						atomTermIndex.put(atom, termIndex);
					}
					termIndex.add(termindex);
				}
			}
		}
	}

	@Override
	public String generateSourceQuery(DatalogProgram query, List<String> signature) throws OBDAException {
		String indent = "   ";
		if (query.getQueryModifiers().hasModifiers()) {
			final String outerViewName = "SUB_QVIEW";
			String subquery = generateQuery(query, signature, indent);

			String modifier = "";
			List<OrderCondition> conditions = query.getQueryModifiers().getSortConditions();
			if (!conditions.isEmpty()) {
				modifier += sqladapter.sqlOrderBy(conditions, outerViewName) + "\n";
			}

			long limit = query.getQueryModifiers().getLimit();
			long offset = query.getQueryModifiers().getOffset();
			if (limit != -1 || offset != -1) {
				modifier += sqladapter.sqlSlice(limit, offset) + "\n";
			}

			String sql = "SELECT *\n";
			sql += "FROM (\n";
			sql += subquery + "\n";
			sql += ") " + outerViewName + "\n";
			sql += modifier;

			return sql;

		} else {
			return generateQuery(query, signature, "");
		}
	}

	private String generateQuery(DatalogProgram query, List<String> signature, String indent) throws OBDAException {
		int ruleSize = query.getRules().size();

		if (ruleSize == 0) {
			throw new OBDAException("Cannot generate SQL for an empty query");
		}
		if (!isUCQ(query)) {
			throw new InvalidParameterException("Only UCQs are supported at the moment");
		}
		log.debug("Generating SQL. Initial query size: {}", query.getRules().size());
		List<CQIE> cqs = query.getRules();

		if (cqs.size() < 1) {
			return "";
		}
		/*
		 * BEFORE DOING ANytHING WE SHOULD NORMALIZE EQ ATOMS A(x), B(y),
		 * EQ(x,y), should be transformed into A(x), B(x)
		 */
		boolean distinct = query.getQueryModifiers().isDistinct();

		/* Main loop, constructing the SPJ query for each CQ */
		StringBuffer result = new StringBuffer();
		boolean isMoreThanOne = false;
		for (CQIE cq : cqs) {
			StringBuffer sb = new StringBuffer();
			int size = cq.getBody().size();
			String[] viewNames = new String[size];
			String[] tableNames = new String[size];
			DataDefinition[] dataDefinitions = new DataDefinition[size];

			LinkedList<String> fromTables = new LinkedList<String>();
			LinkedList<String> whereConditions = new LinkedList<String>();

			/*
			 * Generating FROM clause and stablishing view names for all the
			 * atoms in the query
			 */
			List<Atom> body = new ArrayList<Atom>(cq.getBody());

			/* Contains the list of all the table/views in the FROM clause */
			boolean isempty = false;
			for (int i = 0; i < body.size(); i++) {

				Atom atom = body.get(i);
				if (atom.getPredicate() instanceof BooleanOperationPredicate) {
					continue;
				}
				Predicate tablePredicate = atom.getPredicate();
				String tableName = tablePredicate.toString();
				DataDefinition def = metadata.getDefinition(tableName);
				if (def == null) {
					/*
					 * There is no definition for this atom, its not a database
					 * predicate, the query is empty.
					 */
					isempty = true;
					break;
				}
				viewNames[i] = String.format(VIEW_NAME, i);
				tableNames[i] = tableName;
				dataDefinitions[i] = def;

				if (def instanceof TableDefinition) {
					fromTables.add(sqladapter.sqlTableName(tableNames[i], viewNames[i]));
				}
				if (def instanceof ViewDefinition) {
					fromTables.add(String.format("(%s) %s", ((ViewDefinition) def).getStatement(), viewNames[i]));
				}
			}
			if (isempty) {
				continue;
			}

			/*
			 * First we generate all conditions for shared variables (join
			 * conditions, and duplicated variables in single atoms
			 */

			Map<Variable, Integer> varCount = new HashMap<Variable, Integer>();
			Map<Variable, List<Integer>> varAtomIndex = new HashMap<Variable, List<Integer>>();
			Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex = new HashMap<Variable, Map<Atom, List<Integer>>>();
			createVariableIndex(cq, varCount, varAtomIndex, varAtomTermIndex);

			for (Variable var : varCount.keySet()) {
				int count = varCount.get(var);
				if (count < 2) {
					continue;
				}
				List<Integer> atomIndexes = varAtomIndex.get(var);

				/* first shared within the same atom, e.g., atom(x,y,x,x) */
				for (int index : atomIndexes) {
					Atom atom = body.get(index);
					List<Integer> positionsInAtom = varAtomTermIndex.get(var).get(atom);
					if (positionsInAtom.size() < 2) {
						continue;
					}
					Iterator<Integer> positionIterator = positionsInAtom.iterator();
					Integer position1 = positionIterator.next();
					while (positionIterator.hasNext()) {
						Integer position2 = positionIterator.next();
						String currentView = viewNames[index];
						String attributeName1 = metadata.getAttributeName(tableNames[index], position1 + 1);
						String column1 = getColumnName(attributeName1);
						String qualifiedNameColumn1 = sqladapter.sqlQualifiedColumn(currentView, column1);
						String attributeName2 = metadata.getAttributeName(tableNames[index], position2 + 1);
						String column2 = getColumnName(attributeName2);
						String qualifiedNameColumn2 = sqladapter.sqlQualifiedColumn(currentView, column2);
						String currentcondition = String.format("(" + EQ_OPERATOR + ")", qualifiedNameColumn1, qualifiedNameColumn2);
						whereConditions.add(currentcondition);
					}
				}

				/* doing shared across atoms e.g., atom1(x,y,z), atom2(m,x,k) */
				Iterator<Integer> atomIndexIterator = varAtomIndex.get(var).iterator();
				int indexatom1 = atomIndexIterator.next();
				Atom atom = body.get(indexatom1);
				int indexatom1var = varAtomTermIndex.get(var).get(atom).get(0);
				while (atomIndexIterator.hasNext()) {
					int indexatom2 = atomIndexIterator.next();
					Atom atom2 = body.get(indexatom2);
					for (int indexatom2var2 : varAtomTermIndex.get(var).get(atom2)) {
						String view1 = viewNames[indexatom1];
						String attributeName1 = metadata.getAttributeName(tableNames[indexatom1], indexatom1var + 1);
						String column1 = getColumnName(attributeName1);
						String qualifiedNameColumn1 = sqladapter.sqlQualifiedColumn(view1, column1);
						String view2 = viewNames[indexatom2];
						String attributeName2 = metadata.getAttributeName(tableNames[indexatom2], indexatom2var2 + 1);
						String column2 = getColumnName(attributeName2);
						String qualifiedNameColumn2 = sqladapter.sqlQualifiedColumn(view2, column2);
						String currentcondition = String.format("(" + EQ_OPERATOR + ")", qualifiedNameColumn1, qualifiedNameColumn2);
						whereConditions.add(currentcondition);
					}
				}
			}

			/*
			 * Generating the rest of the where clause, that is, constants in
			 * the atoms, and boolean condition atoms
			 */
			for (int i1 = 0; i1 < body.size(); i1++) {

				Atom atom = body.get(i1);
				Predicate predicate = atom.getPredicate();
				List<Term> terms = atom.getTerms();

				if (predicate instanceof BooleanOperationPredicate) {
					/*
					 * This is a comparison atom, not associated with a DB atom,
					 * but imposing conditions on columns of those.
					 */
					whereConditions.add(getSQLCondition(atom, body, tableNames, viewNames, varAtomIndex, varAtomTermIndex));

				} else {
					/*
					 * This is a normal DB atom, which can impose conditions
					 * (besides joins) by having constants in some terms.
					 */
					for (int termj = 0; termj < terms.size(); termj++) {
						Term term = terms.get(termj);
						if (term instanceof ValueConstant) {
							ValueConstant ct = (ValueConstant) term;
							String value = jdbcutil.getSQLLexicalForm(ct);
							String attributeName = metadata.getAttributeName(tableNames[i1], termj + 1);
							String colname = getColumnName(attributeName);
							String qualifiedName = sqladapter.sqlQualifiedColumn(viewNames[i1], colname);
							String condition = String.format("(" + EQ_OPERATOR + ")", qualifiedName, value);
							whereConditions.add(condition);
						} else if (term instanceof URIConstant) {
							URIConstant ct = (URIConstant) term;
							String value = jdbcutil.getSQLLexicalForm(ct.getURI().toString());
							String attributeName = metadata.getAttributeName(tableNames[i1], termj + 1);
							String colname = getColumnName(attributeName);
							String qualifiedName = sqladapter.sqlQualifiedColumn(viewNames[i1], colname);
							String condition = String.format("(" + EQ_OPERATOR + ")", qualifiedName, value);
							whereConditions.add(condition);
						} else if (term instanceof Variable) {
							// NO-OP
						} else if (term instanceof Function) {
							// NO-OP
						} else {
							throw new RuntimeException("Found a non-supported term in the body while generating SQL");
						}
					}
				}
			}

			/* Creating the FROM */
			StringBuffer fromBf = new StringBuffer();
			fromBf.append(indent + "FROM ");
			boolean moreThanOne = false;
			for (String tdefinition : fromTables) {
				if (moreThanOne) {
					fromBf.append(", ");
				}
				fromBf.append("\n" + indent + "    ");
				fromBf.append(tdefinition);
				moreThanOne = true;
			}

			/* Creating the WHERE */
			StringBuffer whereBf = new StringBuffer();
			whereBf.append(indent + "WHERE ");
			moreThanOne = false;
			for (String tdefinition : whereConditions) {
				whereBf.append("\n" + indent + "    ");
				if (moreThanOne) {
					whereBf.append(" AND ");
				}
				whereBf.append(tdefinition);
				moreThanOne = true;
			}

			/* Creating the SELECT */
			Atom head = cq.getHead();
			sb.append(indent + "SELECT ");
			if (distinct && cqs.size() == 1) {
				sb.append("DISTINCT ");
			}
			sb.append(getSelectClause(head, body, signature, tableNames, viewNames, varAtomIndex, varAtomTermIndex, indent));

			StringBuffer sqlquery = new StringBuffer();
			sqlquery.append(sb);
			sqlquery.append("\n");
			sqlquery.append(fromBf);
			if (whereConditions.size() > 0) {
				sqlquery.append("\n");
				sqlquery.append(whereBf);
			}
			if (isMoreThanOne) {
				if (distinct) {
					result.append("\n\n");
					result.append(indent + "UNION");
					result.append("\n\n");
				} else {
					result.append("\n\n");
					result.append(indent + "UNION ALL");
					result.append("\n\n");
				}
			}
			result.append(sqlquery);
			isMoreThanOne = true;
		}
		return result.toString();
	}

	/**
	 * produces the select clause of the sql query for the given CQIE
	 * 
	 * @param q
	 *            the query
	 * @return the sql select clause
	 */
	private String getSelectClause(Atom head, List<Atom> body, List<String> signature, String[] tableName, String[] viewName,
			Map<Variable, List<Integer>> varAtomIndex, Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex, String indent)
			throws OBDAException {
		List<Term> headterms = head.getTerms();

		String typeStr = "%s AS \"%sQuestType\", ";

		StringBuilder sb = new StringBuilder();
		if (headterms.size() == 0) {
			sb.append("true as x");
			return sb.toString();
		}

		Iterator<Term> hit = headterms.iterator();
		int hpos = 0;
		while (hit.hasNext()) {
			sb.append("\n" + indent + "   ");
			Term ht = hit.next();
			if (!((ht instanceof Function) || (ht instanceof Constant))) {
				throw new IllegalArgumentException(
						"Unexpected error. Contact the authors. Message: Imposible to generate SELECT clause. Found non-functional term \""
								+ ht.toString() + "\"");
			}

			if (ht instanceof URIConstant) {
				sb.append(String.format(typeStr, 1, signature.get(hpos)));
				URIConstant uc = (URIConstant) ht;
				sb.append(jdbcutil.getSQLLexicalForm(uc.getURI().toString()));
			} else if (ht instanceof Function) {
				Vector<String> vex = new Vector<String>();
				Function ov = (Function) ht;
				Predicate function = ov.getFunctionSymbol();
				String functionString = function.toString();

				/*
				 * Adding the ColType column to the projection (used in the
				 * result set to know the type of constant)
				 */
				if (functionString.equals(OBDAVocabulary.XSD_BOOLEAN.getName().toString())) {
					sb.append(String.format(typeStr, 9, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.XSD_DATETIME.getName().toString())) {
					sb.append(String.format(typeStr, 8, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.XSD_DECIMAL.getName().toString())) {
					sb.append(String.format(typeStr, 5, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.XSD_DOUBLE.getName().toString())) {
					sb.append(String.format(typeStr, 6, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.XSD_INTEGER.getName().toString())) {
					sb.append(String.format(typeStr, 4, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.XSD_STRING.getName().toString())) {
					sb.append(String.format(typeStr, 7, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.RDFS_LITERAL.getName().toString())) {
					sb.append(String.format(typeStr, 3, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.QUEST_URI)) {
					sb.append(String.format(typeStr, 1, signature.get(hpos)));
				} else if (functionString.equals(OBDAVocabulary.QUEST_BNODE)) {
					sb.append(String.format(typeStr, 2, signature.get(hpos)));
				}

				/*
				 * Adding the column(s) with the actual value(s)
				 */
				if (function instanceof DataTypePredicate) {
					/*
					 * Case where we have a typing function in the head (this is
					 * the case for all literal columns
					 */
					String langStr = "%s AS \"%sLang\", ";
					if (function == OBDAVocabulary.RDFS_LITERAL) {

						/*
						 * Case for rdf:literal s with a language, we need to
						 * select 2 terms from ".., rdf:literal(?x,"en"),
						 * 
						 * and signature "name" * we will generate a select with
						 * the projection of 2 columns
						 * 
						 * , 'en' as nameqlang, view.colforx as name,
						 */

						/*
						 * first we add the column for language, we have two
						 * cases, where the language is already in the function
						 * as a constant, e.g,. "en" or where the language is a
						 * variable that must be obtained from a column in the
						 * query
						 */
						String lang = "''";
						if (ov.getTerms().size() > 1) {
							Term langTerm = ov.getTerms().get(1);
							if (langTerm instanceof ValueConstant) {
								lang = jdbcutil.getSQLLexicalForm((ValueConstant) langTerm);
							} else {
								lang = getSQLString(langTerm, body, tableName, viewName, varAtomIndex, varAtomTermIndex, false);
							}
						}
						sb.append(String.format(langStr, lang, signature.get(hpos)));

						Term term = ov.getTerms().get(0);
						String termStr = null;
						if (term instanceof ValueConstant) {
							termStr = jdbcutil.getSQLLexicalForm((ValueConstant) term);
						} else {
							termStr = getSQLString(term, body, tableName, viewName, varAtomIndex, varAtomTermIndex, false);
						}
						sb.append(termStr);

					} else {
						// The default value for language column: NULL
						sb.append(String.format(langStr, "''", signature.get(hpos)));

						// The column name
						Term term = ov.getTerms().get(0);
						if (term instanceof Variable) {
							Variable v = (Variable) term;
							String column = getSQLString(v, body, tableName, viewName, varAtomIndex, varAtomTermIndex, false);
							sb.append(column);
						} else if (term instanceof ValueConstant) {
							ValueConstant c = (ValueConstant) term;
							sb.append(jdbcutil.getSQLLexicalForm(c));
						}
					}
				} else if (functionString.equals("http://obda.org/quest#uri")) {
					/***
					 * New template based URI building functions
					 */
					Term t = ov.getTerms().get(0);
					if (t instanceof ValueConstant) {
						ValueConstant c = (ValueConstant) t;
						StringTokenizer tokenizer = new StringTokenizer(c.toString(), "{}");
						functionString = jdbcutil.getSQLLexicalForm(tokenizer.nextToken());
						int termIndex = 1;
						do {
							Term currentTerm = ov.getTerms().get(termIndex);
							vex.add(getSQLString(currentTerm, body, tableName, viewName, varAtomIndex, varAtomTermIndex, false));
							if (tokenizer.hasMoreTokens()) {
								vex.add(jdbcutil.getSQLLexicalForm(tokenizer.nextToken()));
							}
							termIndex += 1;
						} while (tokenizer.hasMoreElements() || termIndex < ov.getTerms().size());
						String[] params = new String[vex.size() + 1];
						int i = 0;
						params[i] = functionString;
						i += 1;
						for (String param : vex) {
							params[i] = param;
							i += 1;
						}
						String concat = sqladapter.strconcat(params);
						sb.append(concat);
					} else if (t instanceof Variable) {
						sb.append(getSQLString(((Variable) t), body, tableName, viewName, varAtomIndex, varAtomTermIndex, false));
					} else if (t instanceof URIConstant) {
						URIConstant uc = (URIConstant) t;
						sb.append(jdbcutil.getSQLLexicalForm(uc.getURI().toString()));
					} else {
						throw new IllegalArgumentException("Error, cannot generate SELECT clause for a term. Contact the authors. Term: "
								+ ov.toString());
					}

				} else {
					throw new IllegalArgumentException(
							"Error generating SQL query. Contact the developers. Found an invalid function during translation: "
									+ ov.toString());
				}
			}
			sb.append(" AS ");
			sb.append(sqladapter.sqlQuote(signature.get(hpos)));

			if (hit.hasNext()) {
				sb.append(", ");
			}
			hpos++;
		}
		return sb.toString();
	}

	public String getSQLCondition(Atom atom, List<Atom> body, String[] tableName, String[] viewName,
			Map<Variable, List<Integer>> varAtomIndex, Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex) {
		final Predicate functionSymbol = atom.getPredicate();
		if (isUnary(atom)) {
			// For unary boolean operators, e.g., NOT, IS NULL, IS NOT NULL.
			Term term = atom.getTerms().get(0);
			String expressionFormat = getBooleanOperatorString(functionSymbol);
			String column = getSQLString(term, body, tableName, viewName, varAtomIndex, varAtomTermIndex, false);
			return String.format(expressionFormat, column);

		} else if (isBinary(atom)) {
			// For binary boolean operators, e.g., AND, OR, EQ, GT, LT, etc.
			Term left = atom.getTerms().get(0);
			Term right = atom.getTerms().get(1);
			String expressionFormat = getBooleanOperatorString(functionSymbol);
			String leftOp = getSQLString(left, body, tableName, viewName, varAtomIndex, varAtomTermIndex, true);
			String rightOp = getSQLString(right, body, tableName, viewName, varAtomIndex, varAtomTermIndex, true);
			return String.format("(" + expressionFormat + ")", leftOp, rightOp);

		} else {
			// Throw an exception for other types
			throw new RuntimeException("No support for n-ary boolean condition predicate: " + atom.getPredicate());
		}
	}

	/**
	 * Determines if it is a unary atom.
	 */
	private boolean isUnary(Atom atom) {
		return (atom.getArity() == 1) ? true : false;
	}

	/**
	 * Determines if it is a binary atom.
	 */
	private boolean isBinary(Atom atom) {
		return (atom.getArity() == 2) ? true : false;
	}

	/**
	 * Determines if it is a unary function.
	 */
	private boolean isUnary(Function fun) {
		return (fun.getArity() == 1) ? true : false;
	}

	/**
	 * Determines if it is a binary function.
	 */
	private boolean isBinary(Function fun) {
		return (fun.getArity() == 2) ? true : false;
	}

	public String getSQLString(Term term, List<Atom> body, String[] tableName, String[] viewName,
			Map<Variable, List<Integer>> varAtomIndex, Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex, boolean useBrackets) {
		StringBuffer result = new StringBuffer();
		if (term instanceof ValueConstant) {
			ValueConstant ct = (ValueConstant) term;
			result.append(jdbcutil.getSQLLexicalForm(ct));

		} else if (term instanceof URIConstant) {
			URIConstant uc = (URIConstant) term;
			result.append(jdbcutil.getSQLLexicalForm(uc.toString()));

		} else if (term instanceof Variable) {
			Variable var = (Variable) term;
			List<Integer> posList = varAtomIndex.get(var); // Locating the first occurrence of the variable in a DB atom (using the indexes)
			if (posList == null) {
				throw new RuntimeException("Unbound variable found in WHERE clause: " + term);
			}
			int atomidx = posList.get(0);
			Atom atom = body.get(atomidx);
			int termidx = varAtomTermIndex.get(var).get(atom).get(0);
			String viewname = viewName[atomidx];
			String attributeName = metadata.getAttributeName(tableName[atomidx], termidx + 1);
			String columnName = getColumnName(attributeName);
			result.append(sqladapter.sqlQualifiedColumn(viewname, columnName));

		} else if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			if (functionSymbol instanceof DataTypePredicate) {
				result.append(getSQLString(function.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex, false));
			} else if (functionSymbol instanceof BooleanOperationPredicate) {
				if (isUnary(function)) {
					// for unary functions, e.g., NOT, IS NULL, IS NOT NULL
					String expressionFormat = getBooleanOperatorString(functionSymbol);
					String op = getSQLString(function.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex, true);
					result.append(String.format(expressionFormat, op));
				} else if (isBinary(function)) {
					// for binary functions, e.g., AND, OR, EQ, NEQ, GT, etc.
					String expressionFormat = getBooleanOperatorString(functionSymbol);
					String leftOp = getSQLString(function.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex,
							true);
					String rightOp = getSQLString(function.getTerms().get(1), body, tableName, viewName, varAtomIndex, varAtomTermIndex,
							true);
					result.append(String.format(expressionFormat, leftOp, rightOp));
					if (useBrackets) {
						result.insert(0, "(");
						result.append(")");
					}
				}
			} else {
				throw new RuntimeException("Unexpected function in the query: " + functionSymbol);
			}
		}
		return result.toString();
	}

	private boolean isUCQ(DatalogProgram query) {
		boolean isUCQ = true;
		int arity = query.getRules().get(0).getHead().getArity();
		for (CQIE cq : query.getRules()) {
			if (!cq.getHead().getPredicate().getName().equals(OBDALibConstants.QUERY_HEAD_URI)) {
				isUCQ = false;
			}
			if (cq.getHead().getArity() != arity)
				isUCQ = false;
			if (isUCQ == false)
				break;
		}
		return isUCQ;
	}

	private String getBooleanOperatorString(Predicate functionSymbol) {
		String operator = null;
		if (functionSymbol.equals(OBDAVocabulary.EQ)) {
			operator = EQ_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.NEQ)) {
			operator = NEQ_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.GT)) {
			operator = GT_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.GTE)) {
			operator = GTE_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.LT)) {
			operator = LT_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.LTE)) {
			operator = LTE_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.AND)) {
			operator = AND_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.OR)) {
			operator = OR_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.NOT)) {
			operator = NOT_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.IS_NULL)) {
			operator = IS_NULL_OPERATOR;
		} else if (functionSymbol.equals(OBDAVocabulary.IS_NOT_NULL)) {
			operator = IS_NOT_NULL_OPERATOR;
		} else {
			throw new RuntimeException("Unknown boolean operator: " + functionSymbol);
		}
		return operator;
	}

	private String getColumnName(String attributeName) {
		String columnName = attributeName;
		if (attributeName.contains(".")) {
			 // Get the column name only. E.g., `Person.name` --> `name`
			columnName = attributeName.substring(attributeName.lastIndexOf(".")+1, attributeName.length());
		}
		return columnName;
	}
}
