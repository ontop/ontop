package inf.unibz.it.obda.constraints.domain.imp;

import inf.unibz.it.obda.constraints.domain.ForeignKeyConstraint;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.typing.CheckOperationTerm;

import java.util.Iterator;
import java.util.List;

public class RDBMSForeignKeyConstraint extends ForeignKeyConstraint{

	
	public static final String RDBMSFOREIGNKEYCONSTRAINT = "RDBMSForeignKeyConstraint";
	
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
	private List<QueryTerm> termsOfQueryOne = null;
	/**
	 * A list of terms associated to the second query involved in this disjointness dependency assertion
	 */
	private List<QueryTerm> termsOfQueryTwo = null;
	/**
	 * The mapping id from which the first query comes from
	 */
	private String mappingID1 = null;
	/**
	 * The mapping id from which the second query comes from
	 */
	private String mappingID2 = null;
	/**
	 * The data source to which the assertions is associated
	 */
	private String datasourceUri = null;
	
	
	public RDBMSForeignKeyConstraint(String id1, String id2,RDBMSSQLQuery q1, RDBMSSQLQuery q2,List<QueryTerm> l1, List<QueryTerm> l2){
		mappingID1 = id1;
		mappingID2 = id2;
		queryOne = q1;
		queryTwo = q2;
		termsOfQueryOne = l1;
		termsOfQueryTwo = l2;
	}
	
	public RDBMSForeignKeyConstraint(String id1, String id2, RDBMSSQLQuery q1, RDBMSSQLQuery q2,List<QueryTerm> l1){
		queryOne = q1;
		queryTwo = q2;
		termsOfQueryOne = l1;
		termsOfQueryTwo = null;
	}
	
	@Override
	public SourceQuery getSourceQueryOne( ) {
		return queryOne;
	}

	@Override
	public SourceQuery getSourceQueryTwo() {
		return queryTwo;
	}

	@Override
	public List<QueryTerm> getTermsOfQueryOne() {
		return termsOfQueryOne;
	}

	@Override
	public List<QueryTerm> getTermsOfQueryTwo() {
		if(termsOfQueryTwo == null){
			return termsOfQueryOne;
		}else{
			return termsOfQueryTwo;
		}
	}
	
	public String getIDForMappingOne(){
		return mappingID1;
	}
	
	public String getIDForMappingTwo(){
		return mappingID2;
	}

	public String toString(){
		
		String s = "";
		s = s + mappingID1 +" ";
		if(termsOfQueryOne.size() == 1){
			QueryTerm t = termsOfQueryOne.get(0);
			s = s + "("+ t.toString()+ ")";
		}else{
			String aux = "";
			Iterator<QueryTerm> it = termsOfQueryOne.iterator();
			while(it.hasNext()){
				QueryTerm t = it.next();
				if(aux.length() >0){
					aux = aux+ ",";
				}
				aux = aux + t.toString();
			}
			s =s+ "(" + aux + ")"; 
		}
		
		s = s +" REFERENCES " + mappingID2 +" ";
	 	
		if(termsOfQueryTwo != null){
			if(termsOfQueryTwo.size() == 1){
				QueryTerm t = termsOfQueryTwo.get(0);
				s = s + "(" + t.toString() +")";
			}else{
				String aux = "";
				Iterator<QueryTerm> it = termsOfQueryTwo.iterator();
				while(it.hasNext()){
					QueryTerm t = it.next();
					if(aux.length() >0){
						aux = aux+ ",";
					}
					aux = aux + t.toString();
				}
				if(aux.length()>0){
					s =s+ "(" + aux + ")";
				}
			}
		}		
		return s;
	}
	
	@Override
	public int hashCode(){
		
		int code = mappingID1.hashCode();
		code = code + queryOne.toString().hashCode();
		int c = 1;
		Iterator<QueryTerm> it = termsOfQueryOne.iterator();
		while(it.hasNext()){
			int aux2 = (int) Math.pow(it.next().getName().hashCode(), c);
			code = code + aux2;
			c++;
		}
		
		code = code + mappingID2.hashCode() + queryTwo.toString().hashCode();
		int d = 1;
		Iterator<QueryTerm> it1 = termsOfQueryTwo.iterator();
		while(it1.hasNext()){
			int aux2 = (int) Math.pow(it1.next().getName().hashCode(), d);
			code = code + aux2;
			d++;
		}
		return code;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof RDBMSForeignKeyConstraint){
			return o.toString().equals(this.toString());
		}else {
			return false;
		}
	}
	
}
