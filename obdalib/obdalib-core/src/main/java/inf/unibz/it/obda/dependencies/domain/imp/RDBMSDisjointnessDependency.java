package inf.unibz.it.obda.dependencies.domain.imp;

import inf.unibz.it.obda.dependencies.domain.DisjointnessDependencyAssertion;
import inf.unibz.it.obda.domain.Query;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.obda.query.domain.Variable;

/**
 * Class representing a disjointness dependency assertion for a
 * relational data base management system.
 *
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 * @author Josef Hardi <josef.hardi@unibz.it>
 *		   KRDB Research Center, Free University of Bolzano/Bozen, Italy
 */

public class RDBMSDisjointnessDependency extends DisjointnessDependencyAssertion {

	public static final String DISJOINEDNESSASSERTION = "RDBMSDisjoinednessAssertion";

	/**
	 * The first query involved in this disjointness dependency assertion
	 */
	private RDBMSSQLQuery queryOne = null;
	/**
	 * The second query involved in this disjointness dependency assertion
	 */
	private RDBMSSQLQuery queryTwo = null;
	/**
	 * A list of terms associated to the first query involved in this disjointness dependency assertion
	 */
	private List<Variable> variablesOfQueryOne = null;
	/**
	 * A list of terms associated to the second query involved in this disjointness dependency assertion
	 */
	private List<Variable> variablesOfQueryTwo = null;
	/**
	 * The mapping id from which the first query comes from
	 */
	private String mappingOneId = null;
	/**
	 * The mapping id from which the second query comes from
	 */
	private String mappingTwoId = null;
	/**
	 * The data source to which the assertions is associated
	 */
	private URI datasourceUri = null;

	/**
	 * Returns a new RDBMSDisjointnessDependency object
	 *
	 * @param uri 	the data source URI
	 * @param id1	id of first mapping
	 * @param id2	id of second mapping
	 * @param q1	the first query
	 * @param q2	the second query
	 * @param terms1	list of terms associated to the first query
	 * @param terms2	list of terms associated to the second query
	 */
	public RDBMSDisjointnessDependency(URI uri,String id1, String id2, RDBMSSQLQuery q1, RDBMSSQLQuery q2,
			List<Variable> terms1, List<Variable> terms2){

		datasourceUri = uri;
		queryOne = q1;
		queryTwo = q2;
		variablesOfQueryOne = terms1;
		variablesOfQueryTwo = terms2;
		mappingOneId = id1;
		mappingTwoId = id2;
	}

	/**
	 * Returns the first query involved in the assertion
	 */
	@Override
	public Query getSourceQueryOne() {
		return queryOne;
	}
	/**
	 * Returns the second query involved in the assertion
	 */
	@Override
	public Query getSourceQueryTwo() {
		return queryTwo;
	}
	/**
	 * Returns a list of terms associated to the first query involved in the assertion
	 */
	@Override
	public List<Variable> getVariablesOfQueryOne() {
		return variablesOfQueryOne;
	}
	/**
	 * Returns a list of terms associated to the second query involved in the assertion
	 */
	@Override
	public List<Variable> getVariablesOfQueryTwo() {
		return variablesOfQueryTwo;
	}

	@Override
	public int hashCode(){

		String s = queryOne.toString() + queryTwo.toString();
		Iterator<Variable> it1 = variablesOfQueryOne.iterator();
		Iterator<Variable> it2 = variablesOfQueryTwo.iterator();
		int code = s.hashCode();
		int c = 1;
		while(it1.hasNext() && it2.hasNext()){
			int aux1 = (int) Math.pow(it1.next().getName().hashCode(), c);
			code = code + aux1 ;
			c++;
		}
		while(it2.hasNext()){
			int aux2 = (int) Math.pow(it2.next().getName().hashCode(), c);
			code = code + aux2;
			c++;
		}
		return code;
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof RDBMSDisjointnessDependency){
			return o.toString().equals(this.toString());
		}else {
			return false;
		}
	}

	@Override
	public String toString(){

		String output = "disjoint(";
		String parameter1 = "Body."+mappingOneId + "[";
		Iterator<Variable> it1 = variablesOfQueryOne.iterator();
		String aux = "";
		while(it1.hasNext()){
			if(aux.length() >0){
				aux = aux + ",";
			}
			aux = aux + "$" + it1.next().getName();  // TODO Remove $ later
		}
		parameter1 = parameter1 +aux +"],";
		String parameter2 = "Body." + mappingTwoId +"[";
		Iterator<Variable> it2 = variablesOfQueryTwo.iterator();
		String aux2 ="";
		while(it2.hasNext()){
			if(aux2.length() >0){
				aux2 = aux2 + ",";
			}
			aux2 = aux2 + "$" + it2.next().getName();
		}
		parameter2 =  parameter2 +aux2+ "]";
		output = output + parameter1 + parameter2 +")";
		return output;
	}

	/**
	 * Returns the associated data source URI
	 * @return URI as String object
	 */
	public URI getDatasourceUri() {
		return datasourceUri;
	}

	public String getMappingOneId() {
		return mappingOneId;
	}

	public String getMappingTwoId() {
		return mappingTwoId;
	}
}
