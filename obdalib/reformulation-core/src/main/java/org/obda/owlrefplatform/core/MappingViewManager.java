package org.obda.owlrefplatform.core;

import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Predicate;
import org.obda.query.domain.PredicateFactory;
import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.FunctionalTermImpl;
import org.obda.query.domain.imp.TermFactoryImpl;
import org.obda.query.domain.imp.VariableImpl;

/**
 * The mapping view manager is the module which allows us to translate CQIEs
 * into sql queries using the provided mappings.
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class MappingViewManager implements ViewManager {

	private static final String						auxpreduri					= "http://obda.org/reformulation/auxPredicate#";

	private List<OBDAMappingAxiom>					mappings					= null;
	private Map<String, Vector<OBDAMappingAxiom>>	mappingswithsambodyIndex	= null;
	private Map<String, Predicate>					mappingToNarysetMap			= null;
	private Map<String, Integer>					globalAliases				= null;
	private Map<URI, AuxSQLMapping>					predicateAuxMappingMap		= null;
	private PredicateFactory						predFactory					= null;
	private Map<URI, String>						predicateToSQLMap			= null;
	private int										globalAlias					= 1;
	private Atom									head						= null;

	private TermFactory								termFactory					= new TermFactoryImpl();

	public MappingViewManager(List<OBDAMappingAxiom> mappings) {
		this.mappings = mappings;
		predFactory = BasicPredicateFactoryImpl.getInstance();
		mappingswithsambodyIndex = new HashMap<String, Vector<OBDAMappingAxiom>>();
		mappingToNarysetMap = new HashMap<String, Predicate>();
		globalAliases = new HashMap<String, Integer>();
		predicateAuxMappingMap = new HashMap<URI, AuxSQLMapping>();
		predicateToSQLMap = new HashMap<URI, String>();
		try {
			prepareIndexes();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getTranslatedName(Atom atom) throws Exception {
		throw new Exception("Method is not implemented for the MappingViewManager");
	}

	/**
	 * the method constructs a number of useful indexes and maps which Simplify
	 * the translation of the CQIE into sql, i.e. it constructs a map for all
	 * obda mappings with their corresponding auxilary predicate, it creates the
	 * Auxiliary Mappings, it produces the global aliases for the sql queries,
	 * etc.
	 * 
	 * @throws Exception
	 */
	private void prepareIndexes() throws Exception {

		Iterator<OBDAMappingAxiom> it = mappings.iterator();
		HashSet<String> usedSQL = new HashSet<String>();
		while (it.hasNext()) {
			OBDAMappingAxiom ax = it.next();
			RDBMSSQLQuery sourceQuery = (RDBMSSQLQuery) ax.getSourceQuery();
			String sql = sourceQuery.toString();
			Vector<OBDAMappingAxiom> sameSQL = mappingswithsambodyIndex.get(sql);
			if (sameSQL == null) {
				sameSQL = new Vector<OBDAMappingAxiom>();
			}
			sameSQL.add(ax);
			mappingswithsambodyIndex.put(sql, sameSQL);
			Integer i = globalAliases.get(sql);
			if (i == null) {
				i = new Integer(globalAlias);
				globalAliases.put(sql, i);
				globalAlias++;
			}
			if (!usedSQL.contains(sql.trim())) {
				String name = auxpreduri + "Aux" + i;
				CQIE cq = (CQIEImpl) ax.getTargetQuery();
				List<Atom> atoms = cq.getBody();
				Iterator<Atom> ait = atoms.iterator();
				List<String> sqlVars = new Vector<String>();
				while (ait.hasNext()) {
					Atom a = ait.next();
					List<Term> terms = a.getTerms();
					Iterator<Term> tit = terms.iterator();
					while (tit.hasNext()) {
						Term t = tit.next();
						if (t instanceof FunctionalTermImpl) {
							FunctionalTermImpl ft = (FunctionalTermImpl) t;
							List<Term> para = ft.getTerms();
							Iterator<Term> pit = para.iterator();
							while (pit.hasNext()) {
								Term qt = pit.next();
								if (qt instanceof VariableImpl) {
									if (!sqlVars.contains(qt.getName())) {
										sqlVars.add(qt.getName());
									}
								} else {
									throw new Exception("Function terms can only have variables as parameter!");
								}
							}
						} else if (t instanceof VariableImpl) {
							if (!sqlVars.contains(t.getName())) {
								sqlVars.add(t.getName());
							}
						} else {
							throw new Exception("Mappings cannot contain constants!");
						}
					}

				}
				String[] vars = new String[sqlVars.size()];
				vars = sqlVars.toArray(vars);
				AuxSQLMapping auxmap = new AuxSQLMapping(vars);
				URI preduri = URI.create(name);
				Predicate p = predFactory.getPredicate(preduri, vars.length);
				mappingToNarysetMap.put(sql, p);
				predicateAuxMappingMap.put(preduri, auxmap);
				predicateToSQLMap.put(preduri, sql);
				usedSQL.add(sql.trim());
			}
		}
	}

	/**
	 * Return the Predicate associated the given SQL query.
	 * 
	 * @param ax
	 *            the obda mapping
	 * @return the associated predicate
	 */
	public Predicate getViewName(String sqlquery) {
		return mappingToNarysetMap.get(sqlquery);
	}

	

	/**
	 * Returns the auxiliary mapping associated to the given predicate
	 * 
	 * @param preduri
	 *            the predicate identifier
	 * @return the associated aux mapping
	 */
	public AuxSQLMapping getAuxSQLMapping(URI preduri) {
		return predicateAuxMappingMap.get(preduri);
	}

	/**
	 * Returns the sql query associated the given predicate as String
	 * 
	 * @param uri
	 *            the predicate identifier
	 * @return the associated sql
	 */
	public String getSQLForAuxPredicate(URI uri) {
		return predicateToSQLMap.get(uri);
	}

	/**
	 * Returns the alias associated to the given sql query
	 * 
	 * @param sql
	 *            sql query
	 * @return the associated alias
	 */
	public String getAlias(String sql) {
		return "alias_" + globalAliases.get(sql);
	}

	/**
	 * copies the the given atom in order to keep references to the original
	 * variable names used in the data log program. Note: we need this reference
	 * in order to provide the same column names the user specified in the
	 * initial sparql query. E.g. if the user does "Select $a $b $c where ...."
	 * we will name the columns in the answer also a, b, c.
	 */
	public void storeOrgQueryHead(Atom head) {
		this.head = head.copy();
	}

	/**
	 * Returns the original head variable for the given position
	 * 
	 * @param pos
	 *            the position
	 * @return the original variable name at the given position
	 * @throws Exception
	 */
	public String getOrgHeadVariableName(int pos) throws Exception {
		if (pos < 0 || pos > head.getArity()) {
			throw new Exception("Invalid position for HeadVariable");
		} else {
			return head.getTerms().get(pos).getName();
		}
	}
}
