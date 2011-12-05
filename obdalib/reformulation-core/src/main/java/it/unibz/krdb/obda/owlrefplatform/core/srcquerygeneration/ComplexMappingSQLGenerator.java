package it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Constant;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;
import it.unibz.krdb.obda.model.impl.AnonymousVariable;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.queryevaluation.JDBCUtility;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.AuxSQLMapping;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.MappingViewManager;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.ViewManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.LoggerFactory;

/**
 * Generates the SQL query for a given Datalog program using the OBDA mappings
 * 
 * @author Manfred Gerstgrasser
 * 
 */
// TODO This class needs to be restructured

public class ComplexMappingSQLGenerator implements SourceQueryGenerator {

	private MappingViewManager			viewManager				= null;
	private Map<String, List<Object[]>>	termoccurenceIndex		= null;
	private Map<String, List<Object[]>>	constantoccurenceIndex	= null;
	private List<String>				sqlqueries				= null;
	private JDBCUtility					util					= null;
	private Map<Integer, String>		localAliasMap			= null;

	private static final org.slf4j.Logger					log						= LoggerFactory.getLogger(ComplexMappingSQLGenerator.class);

	public ComplexMappingSQLGenerator(MappingViewManager man, JDBCUtility util) {
		viewManager = man;

		this.util = util;
	}

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

	/**
	 * Creates term and constant occurrence indices for the given CQIE
	 * 
	 * @param q
	 *            the given CQIE
	 */
	private void prepareIndex(CQIE q) {
		termoccurenceIndex = new HashMap<String, List<Object[]>>();
		constantoccurenceIndex = new HashMap<String, List<Object[]>>();
		List<Atom> body = q.getBody();
		Iterator<Atom> it = body.iterator();
		while (it.hasNext()) {
			Atom a = (Atom) it.next();
			List<Term> terms = a.getTerms();
			Iterator<Term> tit = terms.iterator();
			int i = 0;
			while (tit.hasNext()) {
				Term t = tit.next();
				if (t instanceof AnonymousVariable) {
					i++;
				} else if (t instanceof VariableImpl) {
					Object[] o = new Object[2];
					o[0] = a;
					o[1] = i;
					i++;
					List<Object[]> aux = termoccurenceIndex.get(((VariableImpl) t).getName());
					if (aux == null) {
						aux = new LinkedList<Object[]>();
					}
					aux.add(o);
					termoccurenceIndex.put(((VariableImpl) t).getName(), aux);
				} else if (t instanceof FunctionalTermImpl) {
					FunctionalTermImpl ov = (FunctionalTermImpl) t;
					List<Term> vars = ov.getTerms();
					Iterator<Term> vit = vars.iterator();
					while (vit.hasNext()) {
						Term v = vit.next();
						Object[] o = new Object[2];
						o[0] = a;
						o[1] = i;
						i++;
						List<Object[]> aux = termoccurenceIndex.get(((Variable) v).getName());
						if (aux == null) {
							aux = new Vector<Object[]>();
						}
						aux.add(o);
						termoccurenceIndex.put(((Variable) v).getName(), aux);
					}
				} else if (t instanceof ValueConstant) {
					Object[] o = new Object[2];
					o[0] = a;
					o[1] = i;
					i++;
					List<Object[]> aux = constantoccurenceIndex.get(((ValueConstant) t).getValue());
					if (aux == null) {
						aux = new Vector<Object[]>();
					}
					aux.add(o);
					constantoccurenceIndex.put(((ValueConstant) t).getValue(), aux);
				} else if (t instanceof URIConstant) {
					Object[] o = new Object[2];
					o[0] = a;
					o[1] = i;
					i++;
					List<Object[]> aux = constantoccurenceIndex.get(((URIConstant) t).getURI().toString());
					if (aux == null) {
						aux = new Vector<Object[]>();
					}
					aux.add(o);
					constantoccurenceIndex.put(((URIConstant) t).getURI().toString(), aux);
				}
			}
		}
	}

	@Override
	public String generateSourceQuery(DatalogProgram query, List<String> signature) throws Exception {
		log.debug("Generating source query");
		List<CQIE> queries = query.getRules();

		if (queries.size() < 1)
			return "";
		Iterator<CQIE> it = queries.iterator();

		sqlqueries = new Vector<String>();

		Object tempdist = query.getQueryModifiers().get("distinct");
		boolean distinct = false;
		if (tempdist != null)
			distinct = (Boolean) tempdist;

		StringBuilder finalquery = new StringBuilder();
		while (it.hasNext()) {
			CQIE q = it.next();
			prepareIndex(q);
			String from = getFromClause(q);
			String select = getSelectClause(q, signature);
			String where = getWhereClause(q);
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			if (distinct && query.getRules().size() == 1) {
				sb.append("DISTINCT ");
			}
			sb.append(select);
			sb.append(" FROM ");
			sb.append(from);
			if (where.length() > 0) {
				sb.append(" WHERE ");
				sb.append(where);
			}
			if (isDPBoolean(query)) {
				sb.append(" ");
				sb.append(util.getLimitFunction(1));
			}
			sqlqueries.add(sb.toString());
			if (finalquery.length() > 0) {
				if (distinct) {
					finalquery.append("\nUNION\n");
				} else {
					finalquery.append("\nUNION ALL\n");
				}
			}
			finalquery.append("(");
			finalquery.append(sb.toString());
			finalquery.append(")");
		}
		String result = finalquery.toString();
		log.debug("Resulting SQL query: \n{}:", result);
		return result;
	}

	// @Override
	// public void update(PrefixManager man, DLLiterOntology onto, Set<URI>
	// uris) {
	// // TODO Auto-generated method stub
	//
	// }

	/**
	 * produces the from clause of the sql query for the given CQIE
	 * 
	 * @param q
	 *            the query
	 * @return the sql from clause
	 */
	private String getFromClause(CQIE q) {
		HashSet<String> used = new HashSet<String>();
		localAliasMap = new HashMap<Integer, String>();
		int usedcount = 0;
		List<Atom> atoms = q.getBody();
		Iterator<Atom> ait = atoms.iterator();
		StringBuilder sb = new StringBuilder();
		while (ait.hasNext()) {
			Atom a = (Atom) ait.next();
			String sql = viewManager.getSQLForAuxPredicate(a.getPredicate().getName());
			String alias = viewManager.getAlias(sql);
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append("(");
			sb.append(sql);
			sb.append(") ");
			if (used.contains(alias)) {
				alias = alias + "_" + usedcount++;
			}
			sb.append(alias);
			used.add(alias);
			localAliasMap.put(a.hashCode(), alias);
		}
		return sb.toString();
	}

	/**
	 * produces the where clause of the sql query for the given CQIE
	 * 
	 * @param q
	 *            the query
	 * @return the sql where clause
	 */
	private String getWhereClause(CQIE q) throws Exception {
		List<Atom> atoms = q.getBody();
		Iterator<Atom> atomIterator = atoms.iterator();
		HashSet<String> equalities = new HashSet<String>();
		HashSet<String> processedTerms = new HashSet<String>();
		while (atomIterator.hasNext()) {
			Atom a = (Atom) atomIterator.next();
			List<Term> terms = a.getTerms();
			Iterator<Term> term_it = terms.iterator();
			int p = 0;
			while (term_it.hasNext()) {
				Term term = term_it.next();
				if (term instanceof VariableImpl) {
					if (!processedTerms.contains(((VariableImpl) term).getName())) {
						List<Object[]> list = termoccurenceIndex.get(((VariableImpl) term).getName());
						if (list == null) {
							throw new Exception("Unknown term in body");
						}
						if (list.size() > 1) {
							Object[] first_O = list.get(0);
							Atom a_0 = (Atom) first_O[0];
							Integer pos_0 = (Integer) first_O[1];
							String alias_0 = localAliasMap.get(a_0.hashCode());
							Term term_0 = a_0.getTerms().get(pos_0);
							AuxSQLMapping map = viewManager.getAuxSQLMapping(a_0.getPredicate().getName());
							for (int i = 1; i < list.size(); i++) {
								Object[] first_N = list.get(i);
								Atom a_N = (Atom) first_N[0];
								Integer pos_N = (Integer) first_N[1];
								Term term_N = a_N.getTerms().get(pos_N);
								String alias_N = localAliasMap.get(a_N.hashCode());
								AuxSQLMapping map_N = viewManager.getAuxSQLMapping(a_N.getPredicate().getName());
								if (term_N instanceof VariableImpl && term_0 instanceof VariableImpl) {
									String sqlVar_N = map_N.getSQLVariableAt(pos_N);
									String sqlVar_0 = map.getSQLVariableAt(pos_0);
									StringBuilder equ = new StringBuilder();
									equ.append(alias_0);
									equ.append(".");
									equ.append(sqlVar_0);
									equ.append("=");
									equ.append(alias_N);
									equ.append(".");
									equ.append(sqlVar_N);
									equalities.add(equ.toString());
								} else if (term_N instanceof FunctionalTermImpl && term_0 instanceof FunctionalTermImpl) {
									FunctionalTermImpl ov1 = (FunctionalTermImpl) term_0;
									FunctionalTermImpl ov2 = (FunctionalTermImpl) term_N;
									if (ov1.getTerms().size() == ov2.getTerms().size()) {
										for (int j = 0; j < ov1.getTerms().size(); j++) {
											// Term t0 = ov1.getTerms().get(j);
											// Term tn = ov2.getTerms().get(j);
											String sqlVar_0 = map.getSQLVariableAt(pos_0);
											String sqlVar_N = map_N.getSQLVariableAt(pos_N);
											StringBuilder equ = new StringBuilder();
											equ.append(alias_0);
											equ.append(".");
											equ.append(sqlVar_0);
											equ.append("=");
											equ.append(alias_N);
											equ.append(".");
											equ.append(sqlVar_N);
											equalities.add(equ.toString());
										}
									} else {
										return "0=1";
									}
								} else {
									return "0=1"; // since this query can never
									// be satisfied;
								}
							}
						}
					}
				} else if (term instanceof ValueConstant) {
					Constant ct = (Constant) term;
					String alias = localAliasMap.get(a.hashCode());
					AuxSQLMapping map = viewManager.getAuxSQLMapping(a.getPredicate().getName());
					String sqlVar = map.getSQLVariableAt(p);
					StringBuilder equ = new StringBuilder();
					equ.append(alias);
					equ.append(".");
					equ.append(sqlVar);
					equ.append("='");
					equ.append(((ValueConstant) ct).getValue());
					equ.append("'");
					equalities.add(equ.toString());
				} else if (term instanceof URIConstant) {
					URIConstant ct = (URIConstant) term;
					String alias = localAliasMap.get(a.hashCode());
					AuxSQLMapping map = viewManager.getAuxSQLMapping(a.getPredicate().getName());
					String sqlVar = map.getSQLVariableAt(p);
					StringBuilder equ = new StringBuilder();
					equ.append(alias);
					equ.append(".");
					equ.append(sqlVar);
					equ.append("='");
					equ.append(ct.getURI());
					equ.append("'");
					equalities.add(equ.toString());
				} else if (term instanceof FunctionalTermImpl) {
					FunctionalTermImpl ov = (FunctionalTermImpl) term;
					List<Term> vars = ov.getTerms();
					Iterator<Term> vit = vars.iterator();
					while (vit.hasNext()) {
						Term v = vit.next();
						if (!processedTerms.contains(((Variable) v).getName())) {
							List<Object[]> list = termoccurenceIndex.get(((Variable) v).getName());
							if (list == null) {
								throw new Exception("Unknown term in body");
							}
							if (list.size() > 1) {
								Object[] first_O = list.get(0);
								Atom a_0 = (Atom) first_O[0];
								Integer pos_0 = (Integer) first_O[1];
								String alias_0 = localAliasMap.get(a_0.hashCode());
								Term term_0 = a_0.getTerms().get(pos_0);
								AuxSQLMapping map = viewManager.getAuxSQLMapping(a_0.getPredicate().getName());
								for (int i = 1; i < list.size(); i++) {
									Object[] first_N = list.get(i);
									Atom a_N = (Atom) first_N[0];
									Integer pos_N = (Integer) first_N[1];
									Term term_N = a_N.getTerms().get(pos_N);
									String alias_N = localAliasMap.get(a_N.hashCode());
									AuxSQLMapping map_N = viewManager.getAuxSQLMapping(a_N.getPredicate().getName());
									if (term_N instanceof VariableImpl && term_0 instanceof VariableImpl) {
										String sqlVar_N = map_N.getSQLVariableAt(pos_N);
										String sqlVar_0 = map.getSQLVariableAt(pos_0);
										StringBuilder equ = new StringBuilder();
										equ.append(alias_0);
										equ.append(".");
										equ.append(sqlVar_0);
										equ.append("=");
										equ.append(alias_N);
										equ.append(".");
										equ.append(sqlVar_N);
										equalities.add(equ.toString());
									} else if (term_N instanceof FunctionalTermImpl && term_0 instanceof FunctionalTermImpl) {
										FunctionalTermImpl ov1 = (FunctionalTermImpl) term_0;
										FunctionalTermImpl ov2 = (FunctionalTermImpl) term_N;
										if (ov1.getTerms().size() == ov2.getTerms().size()) {
											for (int j = 0; j < ov1.getTerms().size(); j++) {
												// Term t0 =
												// ov1.getTerms().get(j);
												// Term tn =
												// ov2.getTerms().get(j);
												String sqlVar_0 = map.getSQLVariableAt(pos_0);
												String sqlVar_N = map_N.getSQLVariableAt(pos_N);
												StringBuilder equ = new StringBuilder();
												equ.append(alias_0);
												equ.append(".");
												equ.append(sqlVar_0);
												equ.append("=");
												equ.append(alias_N);
												equ.append(".");
												equ.append(sqlVar_N);
												equalities.add(equ.toString());
											}
										} else {
											return "0=1";
										}
									} else {
										return "0=1"; // since this query can
										// never be satisfied;
									}
								}
							}
						}
					}
				} else if (term instanceof AnonymousVariable) {
					/*
					 * Ignore these during where generation
					 */

				} else {
					throw new IllegalArgumentException("Error during SQL geration: Unsupported type of term " + term.getClass());
				}
				p++;
			}
		}

		Iterator<String> it = equalities.iterator();
		StringBuilder finalWhere = new StringBuilder();
		while (it.hasNext()) {
			String equ = it.next();
			if (finalWhere.length() > 0) {
				finalWhere.append(" AND ");
			}
			finalWhere.append(equ);
		}
		return finalWhere.toString();
	}

	/**
	 * produces the select clause of the sql query for the given CQIE
	 * 
	 * @param q
	 *            the query
	 * @return the sql select clause
	 */
	private String getSelectClause(CQIE q, List<String> signature) throws Exception {
		Atom head = q.getHead();
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

				if (ht instanceof VariableImpl) {
					List<Object[]> list = termoccurenceIndex.get(((VariableImpl) ht).getName());
					if (list == null) {
						throw new Exception("Unknown term in head");
					}
					Object[] o = list.get(0);
					Atom a = (Atom) o[0];
					Integer pos = (Integer) o[1];
					// String sql =
					// viewManager.getSQLForAuxPredicate(a.getPredicate().getName());
					String alias = localAliasMap.get(a.hashCode());
					AuxSQLMapping map = viewManager.getAuxSQLMapping(a.getPredicate().getName());
					String sqlvar = map.getSQLVariableAt(pos);
					sb.append(alias);
					sb.append(".");
					sb.append(sqlvar);
					sb.append(" as ");
					sb.append(viewManager.getOrgHeadVariableName(hpos, signature));
				} else if (ht instanceof FunctionalTermImpl) {
					// TODO ComplexMappingSQLGenerator: This code is probably
					// wrong, we need unit test to check it
					FunctionalTermImpl ov = (FunctionalTermImpl) ht;
					String name = ov.getFunctionSymbol().toString();
					List<Term> terms = ov.getTerms();
					Iterator<Term> it = terms.iterator();
					Vector<String> vex = new Vector<String>();
					while (it.hasNext()) {
						Term v = it.next();
						if (v instanceof VariableImpl) {
							List<Object[]> list = termoccurenceIndex.get(((VariableImpl) v).getName());
							if (list == null) {
								throw new Exception("Unknown term in head:" + v + " The query was: " + q);
							}
							Object[] o = list.get(0);
							Atom a = (Atom) o[0];
							Integer pos = (Integer) o[1];
							// String sql =
							// viewManager.getSQLForAuxPredicate(a.getPredicate().getName());
							String alias = localAliasMap.get(a.hashCode());
							AuxSQLMapping map = viewManager.getAuxSQLMapping(a.getPredicate().getName());
							String sqlvar = map.getSQLVariableAt(pos);
							StringBuilder var = new StringBuilder();
							// if (it.hasNext()) {
							// sb.append(", ");
							// }
							var.append(alias);
							var.append(".");
							var.append(sqlvar);
							vex.add(var.toString());
						} else if (v instanceof ValueConstant) {
							StringBuilder var = new StringBuilder();
							var.append("'" + ((ValueConstant) v).getValue() + "'");
							vex.add(var.toString());
						} else if (v instanceof URIConstant) {
							StringBuilder var = new StringBuilder();
							var.append("'" + ((URIConstant) v).getURI().toString() + "'");
							vex.add(var.toString());
						} else {
							throw new RuntimeException("Invalid term in the head");
						}
					}
					String concat = util.getConcatination(name, vex);
					sb.append(concat);
					sb.append(" as ");
					sb.append(viewManager.getOrgHeadVariableName(hpos, signature));

				} else if (ht instanceof ValueConstant) {
					sb.append("'");
					sb.append(((ValueConstant) ht).getValue());
					sb.append("'");
					sb.append(" as ");
					sb.append(viewManager.getOrgHeadVariableName(hpos, signature));
				} else if (ht instanceof URIConstant) {
					sb.append("'");
					sb.append(((URIConstant) ht).getURI().toString());
					sb.append("'");
					sb.append(" as ");
					sb.append(viewManager.getOrgHeadVariableName(hpos, signature));
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

	/**
	 * returns the current view manager
	 */
	@Override
	public ViewManager getViewManager() {
		return viewManager;
	}

}
