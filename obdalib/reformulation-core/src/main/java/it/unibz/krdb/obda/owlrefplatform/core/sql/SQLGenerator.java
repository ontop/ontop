package it.unibz.krdb.obda.owlrefplatform.core.sql;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.BooleanOperationPredicate;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDALibConstants;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration.SourceQueryGenerator;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.AuxSQLMapping;
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
import java.util.Vector;

import org.slf4j.LoggerFactory;

public class SQLGenerator implements SourceQueryGenerator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7477161929752147045L;

	private static final String VIEW_NAME = "QVIEW%s";

	final private DBMetadata metadata;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(SQLGenerator.class);

	private final JDBCUtility jdbcutil;

	private static final String qualifiedColumn = "%s.%s";

	private static final String conditionEQCol = "%s.%s = %s.%s";
	private static final String conditionEQConstant = "%s.%s = %s";
	private static final String conditionGTConstant = "%s.%s > %s";
	private static final String conditionGTEConstant = "%s.%s >= %s";
	private static final String conditionLTConstant = "%s.%s < %s";
	private static final String conditionLTEQConstant = "%s.%s <= %s";

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
			if (atom.getPredicate() instanceof BooleanOperationPredicate)
				continue;

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
		    throw new OBDAException("No axiom has been generated from the system! Please recheck your input query.");
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

		LinkedList<String> sqls = new LinkedList<String>();
		for (CQIE cq : cqs) {
			StringBuffer sb = new StringBuffer();
			int size = cq.getBody().size();
			String[] viewName = new String[size];
			String[] tableName = new String[size];
			DataDefinition[] dataDefinition = new DataDefinition[size];

			LinkedList<String> fromTables = new LinkedList<String>();
			LinkedList<String> whereConditions = new LinkedList<String>();
			String selectClause = null;

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
			if (isempty)
				continue;

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
						String currentcondition = String.format(conditionEQCol, currentView, column1, currentView, column2);
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
						String view2 = viewName[indexatom2];
						String column2 = metadata.getAttributeName(tableName[indexatom2], indexatom2var2 + 1);
						String currentcondition = String.format(conditionEQCol, view1, column1, view2, column2);
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
				List<Term> terms = atom.getTerms();

				if (atom.getPredicate() instanceof BooleanOperationPredicate) {
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
							String value = ct.getValue();
							if (ct.getType() == COL_TYPE.LITERAL || ct.getType() == COL_TYPE.STRING || ct.getType() == COL_TYPE.DATETIME) {
								value = getQuotedString(value);
							}
							String colname = metadata.getAttributeName(tableName[i1], termj + 1);
							String condition = String.format(conditionEQConstant, viewName[i1], colname, value);
							whereConditions.add(condition);
						} else if (term instanceof URIConstant) {
							URIConstant ct = (URIConstant) term;
							String value = ct.getURI().toString();
							String colname = metadata.getAttributeName(tableName[i1], termj + 1);
							String condition = String.format(conditionEQConstant, viewName[i1], colname, getQuotedString(value));
							whereConditions.add(condition);
						} else if (term instanceof Variable) {
							/*
							 * Do nothing, variable conditions have been set
							 * already
							 */
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
					sb.append(" as ");
					sb.append(signature.get(hpos));
				} else if (ht instanceof Function) {
                    Vector<String> vex = new Vector<String>();
					Function ov = (Function) ht;
					String name = ov.getFunctionSymbol().toString();					
					if (name.equals(OBDAVocabulary.RDFS_LITERAL_URI) 
                            || name.equals(OBDAVocabulary.XSD_STRING_URI)
                            || name.equals(OBDAVocabulary.XSD_INTEGER_URI)
                            || name.equals(OBDAVocabulary.XSD_DOUBLE_URI)
                            || name.equals(OBDAVocabulary.XSD_DATETIME_URI)
                            || name.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
                        Variable v = (Variable) ov.getTerms().get(0);
                        String column = getSQLString(v, body, tableName, viewName, varAtomIndex, varAtomTermIndex);
                        sb.append(column);
                        sb.append(" as ");
                        sb.append(signature.get(hpos));
                    } else {
    					List<Term> terms = ov.getTerms();
    					Iterator<Term> it = terms.iterator();
    					while (it.hasNext()) {
    						Term v = it.next();
    						if (v instanceof Variable) {
    							vex.add(getSQLString(v, body, tableName, viewName, varAtomIndex, varAtomTermIndex));
    						} else if (v instanceof ValueConstant) {
    							ValueConstant ct = (ValueConstant) v;
    							StringBuilder var = new StringBuilder();
    							if (ct.getType() == COL_TYPE.LITERAL || ct.getType() == COL_TYPE.STRING || ct.getType() == COL_TYPE.DATETIME) {
    								var.append("'");
    								var.append(ct.getValue());
    								var.append("'");
    							} else {
    								var.append(ct.getValue());
    							}
    							vex.add(var.toString());
    						} else if (v instanceof URIConstant) {
    							StringBuilder var = new StringBuilder();
    							var.append("'" + ((URIConstant) v).getURI().toString() + "'");
    							vex.add(var.toString());
    						} else {
    							throw new RuntimeException("Invalid term in the head");
    						}
    					}
    					String concat = jdbcutil.getConcatination(name, vex);
    					sb.append(concat);
    					sb.append(" as ");
    					sb.append(signature.get(hpos));
                    }
				} else if (ht instanceof ValueConstant) {
					ValueConstant ct = (ValueConstant) ht;
					if (ct.getType() == COL_TYPE.LITERAL || ct.getType() == COL_TYPE.STRING || ct.getType() == COL_TYPE.DATETIME) {
						sb.append("'");
						sb.append(ct.getValue());
						sb.append("'");
					} else {
						sb.append(ct.getValue());
					}
					sb.append(" as ");
					sb.append(signature.get(hpos));
				} else if (ht instanceof URIConstant) {
					sb.append("'");
					sb.append(((URIConstant) ht).getURI().toString());
					sb.append("'");
					sb.append(" as ");
					sb.append(signature.get(hpos));
				}

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
		StringBuffer result = new StringBuffer();

		Predicate predicate = atom.getPredicate();
		result.append("(");
		result.append(getSQLString(atom.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex));
		if (predicate == OBDAVocabulary.EQ) {
			result.append(" = ");
		} else if (predicate == OBDAVocabulary.LT) {
			result.append(" < ");
		} else if (predicate == OBDAVocabulary.LTE) {
			result.append(" <= ");
		} else if (predicate == OBDAVocabulary.GT) {
			result.append(" > ");
		} else if (predicate == OBDAVocabulary.GTE) {
			result.append(" >= ");
		} else if (predicate == OBDAVocabulary.AND) {
			result.append(" AND ");
		} else if (predicate == OBDAVocabulary.OR) {
			result.append(" OR ");
		} else if (predicate == OBDAVocabulary.NEQ) {
			result.append(" <> ");
		} else {
			throw new RuntimeException("Unexpected function in the query: " + predicate);
		}
		result.append(getSQLString(atom.getTerms().get(1), body, tableName, viewName, varAtomIndex, varAtomTermIndex));
		result.append(")");
		return result.toString();
	}

	public String getSQLString(Term term, List<Atom> body, String[] tableName, String[] viewName,
			Map<Variable, List<Integer>> varAtomIndex, Map<Variable, Map<Atom, List<Integer>>> varAtomTermIndex) {
		StringBuffer result = new StringBuffer();
		if (term instanceof ValueConstant) {
			ValueConstant ct = (ValueConstant) term;
			if (ct.getType() == COL_TYPE.LITERAL || ct.getType() == COL_TYPE.STRING || ct.getType() == COL_TYPE.DATETIME) {
				result.append("'");
				result.append(ct.getValue());
				result.append("'");
			} else {
				result.append(ct.getValue());
			}
		} else if (term instanceof URIConstant) {
			URIConstant ct = (URIConstant) term;
			result.append("'");
			result.append(ct.getURI());
			result.append("'");
		} else if (term instanceof Variable) {
			Variable var = (Variable) term;
			/*
			 * locating the first occurrence of the variable in a DB atom (using
			 * the indexes)
			 */
			List<Integer> posList = varAtomIndex.get(var);
			if (posList == null)
				throw new RuntimeException("Unbound variable found in WHERE clause: " + term);
			int atomidx = posList.get(0);
			Atom atom = body.get(atomidx);
			int termidx = varAtomTermIndex.get(var).get(atom).get(0);

			String viewname = viewName[atomidx];
			String columnName = metadata.getAttributeName(tableName[atomidx], termidx + 1);
			result.append(viewname);
			result.append(".");
			result.append(columnName);
		} else if (term instanceof Function) {
			Function function = (Function) term;
			Predicate predicate = function.getFunctionSymbol();

			result.append(getSQLString(function.getTerms().get(0), body, tableName, viewName, varAtomIndex, varAtomTermIndex));
			if (predicate == OBDAVocabulary.EQ) {
				result.append(" = ");
			} else if (predicate == OBDAVocabulary.LT) {
				result.append(" < ");
			} else if (predicate == OBDAVocabulary.LTE) {
				result.append(" <= ");
			} else if (predicate == OBDAVocabulary.GT) {
				result.append(" > ");
			} else if (predicate == OBDAVocabulary.GTE) {
				result.append(" >= ");
			} else if (predicate == OBDAVocabulary.AND) {
				result.append(" AND ");
			} else if (predicate == OBDAVocabulary.OR) {
				result.append(" OR ");
			} else if (predicate == OBDAVocabulary.NEQ) {
				result.append(" <> ");
			} else {

			}
			result.append(getSQLString(function.getTerms().get(1), body, tableName, viewName, varAtomIndex, varAtomTermIndex));
		}

		return result.toString();
	}

	// /**
	// * Returns the location of the first ocurrance of Variable var in the
	// query
	// * in a database atom (e.g., not in a comparison atom). The location is
	// * given as a pair, where res[0] = the atom index that contains the
	// * variable, and res[1] the term index.
	// *
	// * @param query
	// * @param var
	// * @return
	// */
	// private int[] firstDBAtomIndexOf(CQIE query, Variable var) {
	// List<Atom> body = query.getBody();
	// int[] result = new int[2];
	// result[0] = -1;
	// result[1] = -1;
	//
	// for (int i1 = 0; i1 < body.size(); i1++) {
	// Atom atom = body.get(i1);
	// if (atom.getPredicate() instanceof BooleanOperationPredicate)
	// continue;
	// for (int i2 = 0; i2 < atom.getTerms().size(); i2++) {
	// Term term1 = atom.getTerm(i2);
	// Variable var1 = (Variable) term1;
	// if (var.equals(var1)) {
	// result[0] = i1;
	// result[1] = 12;
	// }
	// break;
	// }
	// if (result[0] != -1)
	// break;
	// }
	// return result;
	// }

	/**
	 * Checks whether a datalog program is a boolean query or not
	 * 
	 * @param dp
	 *            the data log program
	 * @return true if it is boolean false otherwise
	 */
	private boolean isDPBoolean(DatalogProgram dp) {

		List<CQIE> rules = dp.getRules();
		Iterator<CQIE> it = rules.iterator();
		boolean bool = true;
		while (it.hasNext() && bool) {
			CQIE query = it.next();
			Atom a = query.getHead();
			if (a.getTerms().size() != 0) {
				bool = false;
			}
		}
		return bool;
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
}
