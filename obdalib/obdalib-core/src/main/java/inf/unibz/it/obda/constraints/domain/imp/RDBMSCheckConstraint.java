package inf.unibz.it.obda.constraints.domain.imp;

import inf.unibz.it.obda.constraints.domain.CheckConstraint;
import inf.unibz.it.obda.rdbmsgav.domain.RDBMSSQLQuery;
import inf.unibz.it.ucq.typing.CheckOperationTerm;
import inf.unibz.it.ucq.typing.XSDTypingController;

import java.util.Iterator;
import java.util.List;

import org.obda.query.domain.Query;
import org.obda.query.domain.Term;
import org.obda.query.domain.imp.ValueConstantImpl;

import com.sun.msv.datatype.xsd.XSDatatype;

public class RDBMSCheckConstraint extends CheckConstraint{

	public static final String RDBMSCHECKSONSTRAINT = "RDBMSCheckConstraint";

	private RDBMSSQLQuery query = null;
	private List<CheckOperationTerm> checks = null;
	private String mappingid = null;

	public RDBMSCheckConstraint(String id, RDBMSSQLQuery q, List<CheckOperationTerm> c){
		mappingid = id;
		query = q;
		checks = c;
	}

	@Override
	public List<CheckOperationTerm> getChecks() {
		return checks;
	}

	@Override
	public Query getSourceQueryOne() {
		return query;
	}

	public String getMappingID(){
		return mappingid;
	}

	@Override
	public String toString(){

		String s = "";
		s = mappingid + " CHECK ";
		Iterator<CheckOperationTerm> it = checks.iterator();
		String che ="";
		while(it.hasNext()){
			if(che.length() >0){
				che = che +",";
			}
			String aux = "(";
			CheckOperationTerm t = it.next();
			Term qt1 = t.getTerm1();
			Term qt2 = t.getTerm2();
			aux = aux + qt1.toString() + " "+t.getOperator() +" ";
			String v2 = "";
			if(qt2 instanceof ValueConstantImpl){
				ValueConstantImpl constant = (ValueConstantImpl) qt2;
				XSDatatype type = constant.getType();
				if(XSDTypingController.getInstance().isNumericType(type)){
					v2 = constant.getName();
				}else{
					v2="'" + constant.getName() +"'";
				}
			}else{
				v2 = qt2.toString();
			}
			aux = aux + v2 + ")";
			che = che + aux;
		}
		s = s + che;
		return s;
	}

	@Override
	public int hashCode(){

		int code = mappingid.hashCode();
		code = code + query.toString().hashCode();
		int c = 1;
		Iterator<CheckOperationTerm> it = checks.iterator();
		while(it.hasNext()){
			int aux2 = (int) Math.pow(it.next().toString().hashCode(), c);
			code = code + aux2;
			c++;
		}

		return code;
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof RDBMSCheckConstraint){
			return o.toString().equals(this.toString());
		}else {
			return false;
		}
	}

}
