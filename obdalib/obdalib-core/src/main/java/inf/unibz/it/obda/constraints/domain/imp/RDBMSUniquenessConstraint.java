package inf.unibz.it.obda.constraints.domain.imp;

import java.util.Iterator;
import java.util.List;

import inf.unibz.it.obda.constraints.domain.UniquenessConstraint;
import inf.unibz.it.obda.dependencies.domain.imp.RDBMSDisjointnessDependency;
import inf.unibz.it.obda.domain.SourceQuery;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.domain.QueryTerm;

public class RDBMSUniquenessConstraint extends UniquenessConstraint{

	public final static String RDBMSUNIQUENESSCONSTRAINT = "RDBMSUniquenessConstraint";
	
	private RDBMSSQLQuery query = null;
	private List<QueryTerm> terms = null;
	private String mappingid = null;
	
	public RDBMSUniquenessConstraint (String id ,RDBMSSQLQuery sq, List<QueryTerm> t){
		mappingid = id;
		query = sq;
		terms = t;
	}
	
	@Override
	public SourceQuery getSourceQuery() {
		return query;
	}

	@Override
	public List<QueryTerm> getTerms() {
		// TODO Auto-generated method stub
		return terms;
	}

	public String getMappingID(){
		return mappingid;
	}
	
	public String toString(){
		
		String s = mappingid + " UNIQUE ";
		Iterator<QueryTerm> it = terms.iterator();
		String aux = "";
		while(it.hasNext()){
			if(aux.length() >0){
				aux = aux +",";
			}
			QueryTerm t = it.next();
			aux = aux + t.toString();
		}
		s=s+"(" + aux +")";
		
		return s;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof RDBMSUniquenessConstraint){
			return o.toString().equals(this.toString());
		}else {
			return false;
		}
	}
	
	public int hashCode(){
		
		int code = mappingid.hashCode() + query.toString().hashCode();
		
		int c =0;
		Iterator<QueryTerm> it = terms.iterator();
		while(it.hasNext()){
			int aux2 = (int) Math.pow(it.next().getVariableName().hashCode(), c);
			code = code + aux2;
			c++;
		}
		
		return code;
	}
}
