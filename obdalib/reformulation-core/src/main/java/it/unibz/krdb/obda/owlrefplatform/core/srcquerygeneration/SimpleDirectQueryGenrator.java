package it.unibz.krdb.obda.owlrefplatform.core.srcquerygeneration;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.PredicateAtom;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.impl.VariableImpl;
import it.unibz.krdb.obda.owlrefplatform.core.abox.URIIdentyfier;
import it.unibz.krdb.obda.owlrefplatform.core.abox.URIType;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.SimpleDirectViewManager;
import it.unibz.krdb.obda.owlrefplatform.core.viewmanager.ViewManager;
import it.unibz.krdb.obda.utils.QueryUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the sql generator for the direct mapping approach
 * 
 * @author Manfred Gerstgrasser
 * 
 */

// TODO This class needs restructuring

public class SimpleDirectQueryGenrator implements SourceQueryGenerator {

	private Map<URIIdentyfier, String>		mapper		= null;
	private HashMap<String, List<Object[]>>	termMap		= null;
	private HashMap<String, List<Object[]>>	constMap	= null;
	private Map<PredicateAtom, String>				aliasMapper	= null;
	private int								counter		= 1;
	private SimpleDirectViewManager			viewManager	= null;

	Logger									log			= LoggerFactory.getLogger(SimpleDirectQueryGenrator.class);

	public SimpleDirectQueryGenrator(Map<URIIdentyfier, String> mapper) {

		this.mapper = mapper;
		aliasMapper = new HashMap<PredicateAtom, String>();
		viewManager = new SimpleDirectViewManager();
	}

	/**
	 * Generates the final sql query for the given datalog program
	 */
	@Override
	public String generateSourceQuery(DatalogProgram query, List<String> signature) throws Exception {
		log.debug("Simple SQL generator. Generating SQL query for input query: \n\n{}\n\n", query);
		StringBuffer sb = new StringBuffer();
		List<CQIE> rules = query.getRules();
		Iterator<CQIE> it = rules.iterator();

		Object tempdist = query.getQueryModifiers().get("distinct");
		boolean distinct = false;
		if (tempdist != null)
			distinct = (Boolean) tempdist;

		while (it.hasNext()) {
			if (sb.length() > 0) {
				sb.append("\n");
				if (distinct) {
					sb.append("UNION");
				} else {
					sb.append("UNION ALL");
				}
				sb.append("\n");
			}
			if (isDPBoolean(query)) {
				sb.append("(");
				sb.append(generateSourceQuery(it.next(), distinct && rules.size() == 1));
				sb.append(")");
			} else {
				sb.append(generateSourceQuery(it.next(), distinct && rules.size() == 1));
			}
		}
		String output = sb.toString();
		log.debug("SQL query generated: \n\n{}\n\n", output);
		return output;
	}

	/**
	 * generates the sql query for a single CQIE
	 * 
	 * @param query
	 *            the SQIE
	 * @return the sql query for it
	 * @throws Exception
	 */
	private String generateSourceQuery(CQIE query, boolean distinct) throws Exception {

		createAuxIndex(query);
		StringBuffer sb = new StringBuffer();
		String fromclause = generateFromClause(query);
		sb.append("SELECT ");
		if (distinct) {
			sb.append("DISTINCT ");
		}
		sb.append(generateSelectClause(query));
		sb.append(" FROM ");
		sb.append(fromclause);
		String wc = generateWhereClause(query);
		if (wc.length() > 0) {
			sb.append(" WHERE ");
			sb.append(wc);
		}

		if (QueryUtils.isBoolean(query)) {
			sb.append(" LIMIT 1");
		}
		return sb.toString();

	}

	/**
	 * Generates the select clause for the given query
	 * 
	 * @param query
	 *            the CQIE
	 * @return the select clause
	 * @throws Exception
	 */
	private String generateSelectClause(CQIE query) throws Exception {
		StringBuffer sb = new StringBuffer();
		int variableCounter = 0;
		if (QueryUtils.isBoolean(query)) {
			sb.append("TRUE AS x ");
		} else {
			PredicateAtom head = query.getHead();
			List<Term> headterms = head.getTerms();
			Iterator<Term> it = headterms.iterator();
			while (it.hasNext()) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				Term t = it.next();
				if (t instanceof VariableImpl) {
					List<Object[]> list = termMap.get(((VariableImpl) t).getName());
					if (list != null && list.size() > 0) {
						Object[] obj = list.get(0);
						String table = aliasMapper.get(((PredicateAtom) obj[0]));
						String column = "term" + obj[1];
						StringBuffer aux = new StringBuffer();
						aux.append(table);
						aux.append(".");
						aux.append(column);
						aux.append(" as ");
						aux.append(((VariableImpl) t).getName());
						sb.append(aux.toString());
					}
				} else if (t instanceof URIConstant) {
					URIConstant uriterm = (URIConstant) t;
					sb.append("'");
					sb.append(uriterm.getURI().toString().trim());
					sb.append("' as auxvar");
					sb.append(variableCounter);
					variableCounter += 1;
					/*
					 * note that we use the counter to be able to give a name to
					 * the column that the URIConstant will occupy. Since we
					 * don't have the orginal name of the head of the query, we
					 * need to invent one.
					 */
				} else {
					throw new RuntimeException("SimpleDirectQueryGenrator: Unexpected term in the head of a query: " + t);
				}
			}
		}

		return sb.toString();
	}

	/**
	 * Generates the from clause for the given query
	 * 
	 * @param query
	 *            the CQIE
	 * @return the from clause
	 * @throws Exception
	 */
	private String generateFromClause(CQIE query) throws Exception {
		StringBuffer sb = new StringBuffer();

		List<Atom> body = query.getBody();
		Iterator<Atom> it = body.iterator();
		while (it.hasNext()) {
			PredicateAtom a = (PredicateAtom) it.next();
			if (sb.length() > 0) {
				sb.append(", ");
			}
			String table = getTablename(a);
			String tablealias = getAlias();
			aliasMapper.put(a, tablealias);
			sb.append(table + " as " + tablealias);
		}
		return sb.toString();
	}

	/**
	 * Generates the where clause for the given query
	 * 
	 * @param query
	 *            the CQIE
	 * @return the where clause
	 * @throws Exception
	 */
	private String generateWhereClause(CQIE query) throws Exception {
		StringBuffer sb = new StringBuffer();

		Set<String> keys = termMap.keySet();
		Iterator<String> it = keys.iterator();
		HashSet<String> equalities = new HashSet<String>();
		while (it.hasNext()) {
			List<Object[]> list = termMap.get(it.next());
			if (list != null && list.size() > 1) {
				Object[] obj1 = list.get(0);
				String t1 = aliasMapper.get(((PredicateAtom) obj1[0]));
				String col1 = "term" + obj1[1].toString();
				for (int i = 1; i < list.size(); i++) {
					Object[] obj2 = list.get(i);
					String t2 = aliasMapper.get(((PredicateAtom) obj2[0]));
					String col2 = "term" + obj2[1].toString();
					StringBuffer aux = new StringBuffer();
					aux.append(t1);
					aux.append(".");
					aux.append(col1);
					aux.append("=");
					aux.append(t2);
					aux.append(".");
					aux.append(col2);
					equalities.add(aux.toString());
				}
			}
		}

		Iterator<String> it2 = constMap.keySet().iterator();
		while (it2.hasNext()) {
			String con = it2.next();
			List<Object[]> list = constMap.get(con);
			for (int i = 0; i < list.size(); i++) {
				Object[] obj2 = list.get(i);
				String t2 = aliasMapper.get(((PredicateAtom) obj2[0]));
				String col2 = "term" + obj2[1].toString();
				StringBuffer aux = new StringBuffer();
				aux.append(t2);
				aux.append(".");
				aux.append(col2);
				aux.append("='");
				aux.append(con);
				aux.append("'");
				equalities.add(aux.toString());
			}
		}

		Iterator<String> equ_it = equalities.iterator();
		while (equ_it.hasNext()) {
			if (sb.length() > 0) {
				sb.append(" AND ");
			}
			sb.append(equ_it.next());
		}
		return sb.toString();
	}

	/**
	 * creates the term and constant occurrence index, which will simplify the
	 * translation for the given CQIE
	 * 
	 * @param the
	 *            CQIE
	 */
	private void createAuxIndex(CQIE q) {

		termMap = new HashMap<String, List<Object[]>>();
		constMap = new HashMap<String, List<Object[]>>();
		List<Atom> body = q.getBody();
		Iterator<Atom> it = body.iterator();
		while (it.hasNext()) {
			PredicateAtom atom = (PredicateAtom) it.next();
			List<Term> terms = atom.getTerms();
			int pos = 0;
			Iterator<Term> term_it = terms.iterator();
			while (term_it.hasNext()) {
				Term t = term_it.next();
				if (t instanceof VariableImpl) {
					Object[] obj = new Object[2];
					obj[0] = atom;
					obj[1] = pos;
					List<Object[]> list = termMap.get(((VariableImpl) t).getName());
					if (list == null) {
						list = new Vector<Object[]>();
					}
					list.add(obj);
					termMap.put(((VariableImpl) t).getName(), list);

				} else if (t instanceof ValueConstant) {
					Object[] obj = new Object[2];
					obj[0] = atom;
					obj[1] = pos;
					List<Object[]> list = constMap.get(((ValueConstant) t).getValue());
					if (list == null) {
						list = new Vector<Object[]>();
					}
					list.add(obj);
					constMap.put(((ValueConstant) t).getValue(), list);
				} else if (t instanceof URIConstant) {
					Object[] obj = new Object[2];
					obj[0] = atom;
					obj[1] = pos;
					List<Object[]> list = constMap.get(((URIConstant) t).getURI().toString());
					if (list == null) {
						list = new Vector<Object[]>();
					}
					list.add(obj);
					constMap.put(((URIConstant) t).getURI().toString(), list);
				} 
				pos++;
			}
		}
	}

	// @Override
	// public void update(PrefixManager man, DLLiterOntology onto, Set<URI>
	// uris) {
	// // this.ontology = onto;
	// // viewmanager = new SimpleDirectViewManager(man, onto, uris);
	//
	// }

	private String getAlias() {
		return "t_" + counter++;
	}

	@Override
	public ViewManager getViewManager() {
		return viewManager;
	}

	private boolean isDPBoolean(DatalogProgram dp) {

		List<CQIE> rules = dp.getRules();
		Iterator<CQIE> it = rules.iterator();
		boolean bool = true;
		while (it.hasNext() && bool) {
			CQIE query = it.next();
			PredicateAtom a = query.getHead();
			if (a.getTerms().size() != 0) {
				bool = false;
			}
		}
		return bool;
	}

	private String getTablename(PredicateAtom atom) {

		String tablename = null;
		if (atom.getArity() == 1) {
			URIIdentyfier id = new URIIdentyfier(atom.getPredicate().getName(), URIType.CONCEPT);
			tablename = mapper.get(id);
		} else {
			URIIdentyfier id = new URIIdentyfier(atom.getPredicate().getName(), URIType.OBJECTPROPERTY);
			tablename = mapper.get(id);
			if (tablename == null) {
				id = new URIIdentyfier(atom.getPredicate().getName(), URIType.DATAPROPERTY);
				tablename = mapper.get(id);
			}
		}
		return tablename;
	}
}
