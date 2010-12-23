package org.obda.query.domain.imp;

import inf.unibz.it.dl.domain.NamedPredicate;
import inf.unibz.it.ucq.domain.ConjunctiveQuery;
import inf.unibz.it.ucq.domain.ConstantTerm;
import inf.unibz.it.ucq.domain.FunctionTerm;
import inf.unibz.it.ucq.domain.QueryAtom;
import inf.unibz.it.ucq.domain.QueryTerm;
import inf.unibz.it.ucq.domain.TypedConstantTerm;
import inf.unibz.it.ucq.domain.VariableTerm;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;
import org.obda.query.domain.TermFactory;
import org.obda.query.domain.ValueConstant;
import org.obda.query.domain.PredicateFactory;
import org.obda.query.domain.imp.BasicPredicateFactoryImpl;

//** This class is no longer neccessary. It should be deleted

public class OBDAConjunctiveQueryWrapper implements CQIE {

	private ConjunctiveQuery cq = null;
	private List<Atom> body = null;
	private Atom head = null;
	private TermFactory factory = null;
	private PredicateFactory predFactory = null;
	private boolean isBoolean = false;

	public OBDAConjunctiveQueryWrapper(ConjunctiveQuery cq, boolean isBoolean){
		this.cq = cq;
		this.isBoolean = isBoolean;
		this.body = new Vector<Atom>();
		this.factory = TermFactory.getInstance();
		this.predFactory = BasicPredicateFactoryImpl.getInstance();
		transform();
	}

	public List<Atom> getBody() {
		return body;
	}

	public Atom getHead() {
		return head;
	}

	private void transform(){

		ArrayList<QueryTerm> h = cq.getHeadTerms();
		Iterator<QueryTerm> hit = h.iterator();
		Vector<Term> headterms = new Vector<Term>();
		if(!isBoolean){
			while(hit.hasNext()){
				QueryTerm qt = hit.next();
				Term t = transfrom(qt);
				headterms.add(t);
			}
		}
		Atom newHead = new AtomImpl(predFactory.getQueryOnlyPredicate("p", headterms.size()), headterms);
		head = newHead;

		ArrayList<QueryAtom> b = cq.getAtoms();
		Iterator<QueryAtom> it = b.iterator();
		Vector<Atom> newBody = new Vector<Atom>();
		while(it.hasNext()){
			QueryAtom qt = it.next();
			Vector<Term> terms = new Vector<Term>();
			Iterator<QueryTerm> hit2 = qt.getTerms().iterator();
			while(hit2.hasNext()){
				QueryTerm q = hit2.next();
				Term t = transfrom(q);
				terms.add(t);
			}

			NamedPredicate p = qt.getNamedPredicate();
			URI uri =p.getUri();
			Atom newAtom = new AtomImpl(predFactory.getPredicate(uri, terms.size()), terms);
			newBody.add(newAtom);
		}
		body = newBody;
	}

	private Term transfrom(QueryTerm qt){

		if(qt instanceof ConstantTerm){

			ConstantTerm ct = (ConstantTerm) qt;
			String name = ct.getVariableName();
			if(name.contains("(")){

				int i= name.lastIndexOf("(");
				int j= name.lastIndexOf(")");
				String n = name.substring(0,i);
				String aux = name.substring(i+1,j);
				String[] terms = aux.split(",");
				Vector<ValueConstant> vex = new Vector<ValueConstant>();
				if(terms.length >1){
					for(int k=0;k<terms.length;k++){
						vex.add((ValueConstant)factory.createValueConstant(terms[k]));
					}
					return factory.createObjectConstant(factory.getFunctionSymbol(n), vex);
				}else{
					return factory.createValueConstant(name);
				}

			}else{
				return factory.createValueConstant(name);
			}

		}else if(qt instanceof FunctionTerm){
			FunctionTerm ft = (FunctionTerm) qt;
			ArrayList<QueryTerm> terms = ft.getParameters();
			Vector<Term> vex = new Vector<Term>();
			for(int k=0;k<terms.size();k++){
				vex.add(factory.createVariable(terms.get(k).getVariableName()));
			}
			return factory.createObjectTerm(factory.getFunctionSymbol(ft.getVariableName()), vex);

		}else if(qt instanceof TypedConstantTerm){
			TypedConstantTerm tct = (TypedConstantTerm) qt;
			return factory.createValueConstant(tct.getVariableName(), tct.getDatatype());
		}else if(qt instanceof VariableTerm){
			VariableTerm vt = (VariableTerm) qt;
			return factory.createVariable(vt.getVariableName());
		}else{
			try {
				throw new Exception("Unknwon Term type!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}


@Override
public int hashCode(){

		StringBuffer sb = new StringBuffer();
		Atom head =this.getHead();
		StringBuffer headString = new StringBuffer();
		headString.append(head.getPredicate().getName());
		List<Term> list = head.getTerms();
		Iterator<Term> it = list.iterator();
		StringBuffer var = new StringBuffer();
		while(it.hasNext()){
			Term t = it.next();
			if(var.length()>0){
				var.append(",");
			}
			var.append(t.getName());
		}
		headString.append("(");
		headString.append(var.toString());
		headString.append(") -: ");


		List<Atom> body = this.getBody();
		StringBuffer bodyString = new StringBuffer();
		Iterator<Atom> bit = body.iterator();
		while(bit.hasNext()){
			Atom a = bit.next();
			if(bodyString.length()>0){
				bodyString.append(",");
			}
			StringBuffer atomString = new StringBuffer();
			URI atomuri = a.getPredicate().getName();
			atomString.append(atomuri.toString());
			atomString.append("(");
			List<Term> para = a.getTerms();
			Iterator<Term> pit = para.iterator();
			StringBuffer atomvar = new StringBuffer();
			while(pit.hasNext()){
				Term t = pit.next();
				if(atomvar.length()>0){
					atomvar.append(",");
				}
				atomvar.append(t.getName());
			}
			atomString.append(atomvar);
			atomString.append(")");
			bodyString.append(atomString);
		}

		sb.append(headString);
		sb.append(bodyString);
		String s = sb.toString();

		return s.hashCode();
	}

	@Override
	public boolean isBoolean() {
		return isBoolean;
	}

	@Override
	public void updateHead(Atom head) {
		this.head = head;
	}

	@Override
	public void updateBody(List<Atom> body) {
		this.body = body;
	}
	
	@Override
	public CQIEImpl clone() {
		//TODO implement it correctly
		Atom copyHead = head.copy();
		List<Atom> copyBody = new Vector<Atom>();
		for (Atom atom : body) {
			copyBody.add(atom.copy());
		}
		boolean copyIsBoolean = isBoolean;

		return new CQIEImpl(copyHead, copyBody, copyIsBoolean);
	}
}
