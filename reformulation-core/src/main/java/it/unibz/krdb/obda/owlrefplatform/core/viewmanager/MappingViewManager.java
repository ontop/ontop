package it.unibz.krdb.obda.owlrefplatform.core.viewmanager;

/*
 * #%L
 * ontop-reformulation-core
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;
import it.unibz.krdb.obda.model.OBDASQLQuery;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.CQIEImpl;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * The mapping view manager is the module which allows us to translate CQIEs
 * into sql queries using the provided mappings.
 * 
 * @author Manfred Gerstgrasser
 * 
 */

public class MappingViewManager implements ViewManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5292913933385676528L;

	private static final String						auxpreduri					= "http://obda.org/reformulation/auxPredicate#";

	private List<OBDAMappingAxiom>					mappings					= new LinkedList<OBDAMappingAxiom>();
	private Map<String, Vector<OBDAMappingAxiom>>	mappingswithsambodyIndex	= null;
	private Map<String, Predicate>					mappingToNarysetMap			= null;
	private Map<String, Integer>					globalAliases				= null;
	private Map<String, AuxSQLMapping>					predicateAuxMappingMap		= null;
	private static final OBDADataFactory							predFactory					= OBDADataFactoryImpl.getInstance();
	private Map<String, String>						predicateToSQLMap			= null;
	private int										globalAlias					= 1;
	private Function							head						= null;

	public MappingViewManager(Collection<OBDAMappingAxiom> mappings) {
		this.mappings.addAll(mappings);
		mappingswithsambodyIndex = new HashMap<String, Vector<OBDAMappingAxiom>>();
		mappingToNarysetMap = new HashMap<String, Predicate>();
		globalAliases = new HashMap<String, Integer>();
		predicateAuxMappingMap = new HashMap<String, AuxSQLMapping>();
		predicateToSQLMap = new HashMap<String, String>();
		try {
			prepareIndexes();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getTranslatedName(Function atom) throws Exception {
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
			OBDASQLQuery sourceQuery = (OBDASQLQuery) ax.getSourceQuery();
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
				List<Function> atoms = cq.getBody();
				Iterator<Function> ait = atoms.iterator();
				List<String> sqlVars = new Vector<String>();
				while (ait.hasNext()) {
					Function a = (Function) ait.next();
					List<Term> terms = a.getTerms();
					Iterator<Term> tit = terms.iterator();
					while (tit.hasNext()) {
						Term t = tit.next();
						if (t instanceof Function) {
							Function ft = (Function) t;
							List<Term> para = ft.getTerms();
							Iterator<Term> pit = para.iterator();
							while (pit.hasNext()) {
								Term qt = pit.next();
								if (qt instanceof Variable) {
									if (!sqlVars.contains(((Variable) qt).getName())) {
										sqlVars.add(((Variable) qt).getName());
									}
								} else {
									throw new Exception("Function terms can only have variables as parameter.");
								}
							}
						} else if (t instanceof Variable) {
							if (!sqlVars.contains(((Variable) t).getName())) {
								sqlVars.add(((Variable) t).getName());
							}
						} else {
							throw new Exception("Mappings cannot contain constants.");
						}
					}

				}
				String[] vars = new String[sqlVars.size()];
				vars = sqlVars.toArray(vars);
				AuxSQLMapping auxmap = new AuxSQLMapping(vars);
				Predicate p = predFactory.getPredicate(name, vars.length);
				mappingToNarysetMap.put(sql, p);
				predicateAuxMappingMap.put(name, auxmap);
				predicateToSQLMap.put(name, sql);
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
	public void storeOrgQueryHead(Function head) {
		this.head = (Function) head.clone();
	}

	/**
	 * Returns the original head variable for the given position
	 * 
	 * @param pos
	 *            the position
	 * @return the original variable name at the given position
	 * @throws Exception
	 */
	public String getOrgHeadVariableName(int pos, List<String> signature) throws OBDAException {
		if (pos < 0 || pos > signature.size()) {
			throw new OBDAException("Invalid position for HeadVariable");
		} else {
			return signature.get(pos);
			// return head.getTerms().get(pos).getName();
		}
	}
}
