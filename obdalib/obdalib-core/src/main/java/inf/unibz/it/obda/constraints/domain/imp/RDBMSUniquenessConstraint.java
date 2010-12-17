package inf.unibz.it.obda.constraints.domain.imp;

import inf.unibz.it.obda.constraints.domain.UniquenessConstraint;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import java.util.Iterator;
import java.util.List;

import org.obda.query.domain.Query;
import org.obda.query.domain.Term;

public class RDBMSUniquenessConstraint extends UniquenessConstraint{

	public final static String RDBMSUNIQUENESSCONSTRAINT = "RDBMSUniquenessConstraint";

	private RDBMSSQLQuery query = null;
	private List<Term> terms = null;
	private String mappingid = null;

	public RDBMSUniquenessConstraint (String id ,RDBMSSQLQuery sq, List<Term> t){
		mappingid = id;
		query = sq;
		terms = t;
	}

	@Override
	public Query getSourceQuery() {
		return query;
	}

	@Override
	public List<Term> getTerms() {
		// TODO Auto-generated method stub
		return terms;
	}

	public String getMappingID(){
		return mappingid;
	}

	@Override
	public String toString(){

		String s = mappingid + " UNIQUE ";
		Iterator<Term> it = terms.iterator();
		String aux = "";
		while(it.hasNext()){
			if(aux.length() >0){
				aux = aux +",";
			}
			Term t = it.next();
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

	@Override
	public int hashCode(){

		int code = mappingid.hashCode() + query.toString().hashCode();

		int c =0;
		Iterator<Term> it = terms.iterator();
		while(it.hasNext()){
			int aux2 = (int) Math.pow(it.next().getName().hashCode(), c);
			code = code + aux2;
			c++;
		}

		return code;
	}
}
