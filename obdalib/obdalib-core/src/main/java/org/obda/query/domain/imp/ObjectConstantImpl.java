package org.obda.query.domain.imp;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.FunctionSymbol;
import org.obda.query.domain.ObjectConstant;
import org.obda.query.domain.ValueConstant;

public class ObjectConstantImpl implements ObjectConstant{

	private FunctionSymbol function = null;
	private List<ValueConstant> terms = null;
	
	protected ObjectConstantImpl(FunctionSymbol fs, List<ValueConstant> terms){
		this.function = fs;
		this.terms = terms;
	}
	
	public FunctionSymbol getFunctionSymbol() {
		return function;
	}

	public List<ValueConstant> getTerms() {
		return terms;
	}

	public ObjectConstant copy() {
		Vector<ValueConstant> v = new Vector<ValueConstant>();
		Iterator<ValueConstant> it = terms.iterator();
		while(it.hasNext()){
			v.add((ValueConstant) it.next().copy());
		}
		return new ObjectConstantImpl(this.function.copy(),v);
	}

	public String getName() {
		
		StringBuffer sb_t = new StringBuffer();
		for(int i=0;i<terms.size();i++){
			if(sb_t.length()>0){
				sb_t.append(",");
			}
			sb_t.append(terms.get(i).getName());
		}
		StringBuffer sb_name = new StringBuffer();
		sb_name.append(function.getName());
		sb_name.append("(");
		sb_name.append(sb_t);
		sb_name.append(")");
		return sb_name.toString();
	}

}
