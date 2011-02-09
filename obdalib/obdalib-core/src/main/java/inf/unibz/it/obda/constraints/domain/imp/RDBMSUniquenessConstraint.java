package inf.unibz.it.obda.constraints.domain.imp;

import inf.unibz.it.obda.constraints.domain.UniquenessConstraint;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;

import java.util.Iterator;
import java.util.List;

import org.obda.query.domain.Query;
import org.obda.query.domain.Variable;

public class RDBMSUniquenessConstraint extends UniquenessConstraint{

	public final static String RDBMSUNIQUENESSCONSTRAINT = "RDBMSUniquenessConstraint";

	private RDBMSSQLQuery query = null;
	private List<Variable> variables = null;
	private String mappingid = null;

	public RDBMSUniquenessConstraint (String id ,RDBMSSQLQuery sq, List<Variable> vars){
		mappingid = id;
		query = sq;
		variables = vars;
	}

	@Override
	public Query getSourceQuery() {
		return query;
	}

	@Override
	public List<Variable> getVariables() {
		return variables;
	}

	public String getMappingID(){
		return mappingid;
	}

	@Override
	public String toString(){

		String s = mappingid + " UNIQUE ";
		Iterator<Variable> it = variables.iterator();
		String aux = "";
		while(it.hasNext()){
			if(aux.length() >0){
				aux = aux +",";
			}
			Variable var = it.next();
			aux = aux + "$" + var.getName();  // TODO Remove $ later
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
		Iterator<Variable> it = variables.iterator();
		while(it.hasNext()){
			int aux2 = (int) Math.pow(it.next().getName().hashCode(), c);
			code = code + aux2;
			c++;
		}

		return code;
	}
}
