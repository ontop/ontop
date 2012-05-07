package it.unibz.krdb.obda.owlrefplatform.core.sql;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DataTypePredicate;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
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

public class SQLGenerator implements SourceQueryGenerator {

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
	private static final String qualifiedColumn = "%s.\"%s\"";

	private final DBMetadata metadata;
	private final JDBCUtility jdbcutil;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(SQLGenerator.class);

	public SQLGenerator(DBMetadata metadata, JDBCUtility jdbcutil) {
		this.metadata = metadata;
		this.jdbcutil = jdbcutil;
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
		int ruleSize = query.getRules().size();
		if (ruleSize == 0) {
			throw new OBDAException("Cannot generate SQL for an empty query");
		}
		if (!isUCQ(query)) {
			throw new InvalidParameterException("Only UCQs are supported at the moment");
		}
		log.debug("Generating SQL. Initial query size: {}", query.getRules().size());
		List<CQIE> cqs = query.getRules();

		if (cqs.size() < 1)
			return "";

		/*
		 * BEFORE DOING ANytHING WE SHOULD NORMALIZE EQ ATOMS A(x), B(y),
		 * EQ(x,y), should be transformed into A(x), B(x)
		 */

		Object tempdist = query.getQueryModifiers().get("distinct");
		boolean distinct = false;
		if (tempdist != null)
			distinct = (Boolean) tempdist;

		/* Main look, constructing the SPJ query for each CQ */

		StringBuffer result = new StringBuffer();
		boolean isMoreThanOne = false;
		for (CQIE cq : cqs) {
			StringBuffer sb = new StringBuffer();
			int size = cq.getBody().size();
			String[] viewName = new String[size];
			String[] tableName = new String[size];
			DataDefinition[] dataDefinition = new DataDefinition[size];

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
				if (atom.getPredicate() instanceof BooleanOperationPredicate)
					continue;

				String name = atom.getPredicate().toString();
				DataDefinition def = metadata.getDefinition(name);
				if (def == null) {
					/*
					 * There is no definition for this atom, its not a database
					 * predicate, the query is empty.
					 */
					isempty = true;
					break;
				}

				viewName[i] = String.format(VIEW_NAME, i);
				tableName[i] = name;
				dataDefinition[i] = def;

				if (def instanceof TableDefinition) {
					fromTables.add(String.format("%s %s", tableName[i], viewName[i]));
				}
				if (def instanceof ViewDefinition) {
					fromTables.add(String.format("(%s) %s", ((ViewDefinition) def).getStatement(), viewName[i]));
				}
			}
			if (isempty) {
				continue;
			}

			/*
			 * First we generate all conditions for shared variables (join
			 * conditions, and duplciated variables in single atoms
			 */

			Map<Variable, Integer> varCount = new HashMap<Variable, Integer>();
			Map<Variable, List<Integer>> varAtomIndex = new HashMap<Variable, List<Integer>>();
			Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex = new HashMap<Variable, Map<Atom, List<Integer>>>();
			createVariableIndex(cq, varCount, varAtomIndex, varAtomTermIndex);

			for (Variable var : varCount.keySet()) {
				int count = varCount.get(var);
				if (count < 2)
					continue;
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
						String currentView = viewName[index];
						String column1 = metadata.getAttributeName(tableName[index], position1 + 1);
						String column2 = metadata.getAttributeName(tableName[index], position2 + 1);
						String qualifiedNameColumn1 = String.format(qualifiedColumn, currentView, column1);
						String qualifiedNameColumn2 = String.format(qualifiedColumn, currentView, column2);
						String currentcondition = String.format(EQ_OPERATOR, qualifiedNameColumn1, qualifiedNameColumn2);
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
						String view1 = viewName[indexatom1];
						String column1 = metadata.getAttributeName(tableName[indexatom1], indexatom1var + 1);
						String qualifiedNameColumn1 = String.format(qualifiedColumn, view1, column1);
						String view2 = viewName[indexatom2];
						String column2 = metadata.getAttributeName(tableName[indexatom2], indexatom2var2 + 1);
						String qualifiedNameColumn2 = String.format(qualifiedColumn, view2, column2);
						
						String currentcondition = String.format(EQ_OPERATOR, qualifiedNameColumn1, qualifiedNameColumn2);
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
					whereConditions.add(getSQLCondition(atom, body, tableName, viewName, varAtomIndex, varAtomTermIndex));

				} else {
					/*
					 * This is a normal DB atom, which can impose conditions
					 * (besides joins) by having constants in some terms.
					 */
					for (int termj = 0; termj < terms.size(); termj++) {
						Term term = terms.get(termj);
						if (term instanceof ValueConstant) {
							ValueConstant ct = (ValueConstant) term;
							String value = null;
							if (ct.getType() == COL_TYPE.LITERAL || ct.getType() == COL_TYPE.STRING) {
								value = getQuotedString(ct.getValue());
							} else if (ct.getType() == COL_TYPE.DATETIME) {
								value = getQuotedString(getSQLtimestampFromXSDDatetime(ct.getValue()));
							} else {
								value = ct.getValue();
							}
							String colname = metadata.getAttributeName(tableName[i1], termj + 1);
							String qualifiedName = String.format(qualifiedColumn, viewName[i1], colname);
							String condition = String.format(EQ_OPERATOR, qualifiedName, value);
							whereConditions.add(condition);
						} else if (term instanceof URIConstant) {
							URIConstant ct = (URIConstant) term;
							String value = getQuotedString(ct.getURI().toString());
							String colname = metadata.getAttributeName(tableName[i1], termj + 1);
							String qualifiedName = String.format(qualifiedColumn, viewName[i1], colname);
							String condition = String.format(EQ_OPERATOR, qualifiedName, value);
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
			fromBf.append("FROM ");
			boolean moreThanOne = false;
			for (String tdefinition : fromTables) {
				if (moreThanOne)
					fromBf.append(", ");
				fromBf.append(tdefinition);
				moreThanOne = true;
			}

			/* Creating the WHERE */
			StringBuffer whereBf = new StringBuffer();
			whereBf.append("WHERE ");
			moreThanOne = false;
			for (String tdefinition : whereConditions) {
				if (moreThanOne)
					whereBf.append(" AND ");
				whereBf.append(tdefinition);
				moreThanOne = true;
			}

			/*
			 * Creating the SELECT
			 */
			Atom head = cq.getHead();
			sb.append("SELECT ");
			if (distinct && cqs.size() == 1) {
				sb.append("DISTINCT ");
			}
			sb.append(getSelectClause(head, body, signature, tableName, viewName, varAtomIndex, varAtomTermIndex));

			StringBuffer sqlquery = new StringBuffer();

			sqlquery.append(sb);
			sqlquery.append(" ");
			sqlquery.append(fromBf);
			sqlquery.append(" ");
			if (whereConditions.size() > 0)
				sqlquery.append(whereBf);

			if (isMoreThanOne) {
				if (distinct) {
					result.append("\nUNION \n");
				} else {
					result.append("\nUNION ALL \n");
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
			Map<Variable, List<Integer>> varAtomIndex, Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex) throws OBDAException {
		List<Term> headterms = head.getTerms();
		StringBuilder sb = new StringBuilder();
		if (headterms.size() > 0) {
			Iterator<Term> hit = headterms.iterator();
			int hpos = 0;
			while (hit.hasNext()) {
				Term ht = hit.next();
				if (ht instanceof AnonymousVariable) {
					throw new RuntimeException("ComplexMappingSQLGenerator: Found an non-distinguished variable in the head: " + ht);
				}

				if (ht instanceof Variable) {
					String column = getSQLString(ht, body, tableName, viewName, varAtomIndex, varAtomTermIndex);
					sb.append(column);
				} else if (ht instanceof Function) {
					Vector<String> vex = new Vector<String>();
					Function ov = (Function) ht;
					Predicate functionSymbol = ov.getFunctionSymbol();
					String functionString = functionSymbol.toString();
					if (functionSymbol instanceof DataTypePredicate) {
						/*
						 * Case where we have a typing function in the head
						 * (this is the case for all literal columns
						 */
						if (functionSymbol == OBDAVocabulary.RDFS_LITERAL) {
							/*
							 * Case for rdf:literal s with a language, we need
							 * to select 2 terms from ".., rdf:literal(?x,"en"),
							 * 
							 * and signatrure "name" * we will generate a select
							 * with the projection of 2 columns
							 * 
							 * , 'en' as nameqlang, view.colforx as name,
							 */

							/*
							 * first we add the column for language, we have two
							 * cases, where the language is already in the
							 * funciton as a constant, e.g,. "en" or where the
							 * language is a variable that must be obtained from
							 * a column in the query
							 */
							String lang = "''";
							if (ov.getTerms().size() > 1) {
							Term langTerm = ov.getTerms().get(1);
							
							if (langTerm instanceof ValueConstant) {
								lang = getQuotedString(((ValueConstant) langTerm).getValue());
							} else {
								lang = getSQLString(langTerm, body, tableName, viewName, varAtomIndex, varAtomTermIndex);
							}
							}	
							sb.append(lang);
							sb.append(" AS ");
							sb.append(signature.get(hpos) + "LitLang, ");

							Term term = ov.getTerms().get(0);
							String termStr = null;
							if (term instanceof ValueConstant) {
								termStr = getQuotedString(((ValueConstant) term).getValue());
							} else {
								termStr = getSQLString(term, body, tableName, viewName, varAtomIndex, varAtomTermIndex);
							}
							sb.append(termStr);

						} else {
							/*
							 * Case for all simple datatypes, we only select one
							 * column from the table
							 */							
							Term term = ov.getTerms().get(0);
							if (term instanceof Variable) {
								Variable v = (Variable) term;
								String column = getSQLString(v, body, tableName, viewName, varAtomIndex, varAtomTermIndex);
								sb.append(column);
							} else if (term instanceof ValueConstant) {
								ValueConstant c = (ValueConstant) term;
								sb.append(getQuotedString(c.getValue()));
							}
						}
					} else {
						if (functionString.equals("http://obda.org/quest#uri")) {
							/***
							 * New template based URI building functions
							 */
							ValueConstant c = (ValueConstant) ov.getTerms().get(0);
							StringTokenizer tokenizer = new StringTokenizer(c.toString(), "{}");
							functionString = getQuotedString(tokenizer.nextToken());
							int termIndex = 1;
							do {
								Term currentTerm = ov.getTerms().get(termIndex);
								vex.add(getSQLString(currentTerm, body, tableName, viewName, varAtomIndex, varAtomTermIndex));
								if (tokenizer.hasMoreTokens()) {
									vex.add(getQuotedString(tokenizer.nextToken()));
								}
								termIndex += 1;
							} while (tokenizer.hasMoreElements() || termIndex < ov.getTerms().size());
							String concat = jdbcutil.getConcatination(functionString, vex);
							sb.append(concat);

						} else {
							/***
							 * OLD URI building function
							 */
							List<Term> terms = ov.getTerms();
							Iterator<Term> it = terms.iterator();
							while (it.hasNext()) {
								Term v = it.next();
								if (v instanceof Variable) {
									Variable var = (Variable) v;
									vex.add("'-'");
									vex.add(getSQLString(var, body, tableName, viewName, varAtomIndex, varAtomTermIndex));
								} else if (v instanceof ValueConstant) {
									ValueConstant ct = (ValueConstant) v;
									StringBuilder var = new StringBuilder();
									var.append(getQuotedString(ct.toString()));
									vex.add(var.toString());
								} else if (v instanceof URIConstant) {
									URIConstant uc = (URIConstant) v;
									StringBuilder var = new StringBuilder();
									var.append(getQuotedString(uc.toString()));
									vex.add(var.toString());
								} else {
									throw new RuntimeException("Invalid term in the head");
								}
							}
							String concat = jdbcutil.getConcatination(getQuotedString(functionString), vex);
							sb.append(concat);
						}
					}
				} else if (ht instanceof ValueConstant) {
					ValueConstant ct = (ValueConstant) ht;
					sb.append(getQuotedString(ct.getValue().toString()));
				} else if (ht instanceof URIConstant) {
					URIConstant uc = (URIConstant) ht;
					sb.append(getQuotedString(uc.getURI().toString()));
				}
				sb.append(" AS ");
				sb.append(signature.get(hpos));

				if (hit.hasNext()) {
					sb.append(", ");
				}
				hpos++;
			}
		} else {
			sb.append("true as x");
		}
		return sb.toString();
	}

	public String getSQLCondition(Atom atom, List<Atom> body, String[] tableName, String[] viewName,
			Map<Variable, List<Integer>> varAtomIndex, Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex) {
		if (isUnary(atom.getArity())) {
			// For unary boolean operators, e.g., NOT, IS NULL, IS NOT NULL.
			Term term = atom.getTerms().get(0);
			String expressionFormat = getBooleanOperatorString(atom.getPredicate());
			String column = getSQLString(term, body, tableName, viewName, varAtomIndex, varAtomTermIndex);
			return String.format("(" + expressionFormat + ")", column);
			
		} else if (isBinary(atom.getArity())) {
			// For binary boolean operators, e.g., EQ, GT, LT, etc.
			Term left = atom.getTerms().get(0);
			Term right = atom.getTerms().get(1);
	
			String expressionFormat = getBooleanOperatorString(atom.getPredicate());
			String leftOp = getSQLString(left, body, tableName, viewName, varAtomIndex, varAtomTermIndex);
			String rightOp = getSQLString(right, body, tableName, viewName, varAtomIndex, varAtomTermIndex);				
			return String.format("(" + expressionFormat + ")", leftOp, rightOp);
		
		} else {
			// Throw an exception for other types
			throw new RuntimeException("No support for n-ary boolean condition predicate: " + atom.getPredicate());
		}
	}

	private boolean isUnary(int arity) {
		return (arity == 1) ? true : false;
	}

	private boolean isBinary(int arity) {
		return (arity == 2) ? true : false;
	}

	private String getSQLtimestampFromXSDDatetime(String datetime) {
		StringBuffer bf = new StringBuffer();
		int indexofT = datetime.indexOf('T');
		bf.append(datetime.substring(0, indexofT));
		bf.append(" ");
		bf.append(datetime.subSequence(indexofT + 1, indexofT + 1 + 8));
		return bf.toString();
	}

	public String getSQLString(Term term, List<Atom> body, String[] tableName, String[] viewName,
			Map<Variable, List<Integer>> varAtomIndex, Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex) {
		StringBuffer result = new StringBuffer();
		if (term instanceof ValueConstant) {
			ValueConstant ct = (ValueConstant) term;
			if (ct.getType() == COL_TYPE.LITERAL || ct.getType() == COL_TYPE.STRING) {
				result.append(getQuotedString(ct.getValue()));
			} else if (ct.getType() == COL_TYPE.DATETIME) {
				result.append(getQuotedString(getSQLtimestampFromXSDDatetime(ct.getValue())));
			} else {
				result.append(ct.getValue());
			}
			
		} else if (term instanceof URIConstant) {
			URIConstant uc = (URIConstant) term;
			result.append(getQuotedString(uc.toString()));
			
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
			String columnName = metadata.getAttributeName(tableName[atomidx], termidx + 1);
			result.append(String.format(qualifiedColumn, viewname, columnName));
			
		} else if (term instanceof Function) {
			Function function = (Function) term;
			Predicate functionSymbol = function.getFunctionSymbol();
			int arity = function.getArity();
			if (arity == 1) {
				// if unary functions
				if (functionSymbol instanceof DataTypePredicate) {
					result.append(getSQLString(function.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex));
				} else if (functionSymbol instanceof BooleanOperationPredicate) {
					String expressionFormat = getBooleanOperatorString(functionSymbol);
					String op = getSQLString(function.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex);
					result.append(String.format(expressionFormat, op));
				} else {
					throw new RuntimeException("Unexpected function in the query: " + functionSymbol);
				}
			} else if (arity == 2) {
				// if binary functions
				if (isAndOrOperator(functionSymbol)) {
					result.append("(");
				} 
				if (functionSymbol instanceof BooleanOperationPredicate) {
					String expressionFormat = getBooleanOperatorString(functionSymbol);
					String leftOp = getSQLString(function.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex);
					String rightOp = getSQLString(function.getTerms().get(1), body, tableName, viewName, varAtomIndex, varAtomTermIndex);
					result.append(String.format(expressionFormat, leftOp, rightOp));
				} else {
					throw new RuntimeException("Unexpected function in the query: " + functionSymbol);
				}
				if (isAndOrOperator(functionSymbol)) {
					result.append(")");
				}
			}
		}
		return result.toString();
	}

	private boolean isAndOrOperator(Predicate functionSymbol) {
		return functionSymbol.equals(OBDAVocabulary.AND) || functionSymbol.equals(OBDAVocabulary.OR);
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

	private String getQuotedString(String str) {
		StringBuffer bf = new StringBuffer();
		bf.append("'");
		bf.append(str);
		bf.append("'");
		return bf.toString();
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
}
