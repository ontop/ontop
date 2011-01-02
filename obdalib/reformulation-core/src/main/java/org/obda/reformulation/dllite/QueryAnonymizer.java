package org.obda.reformulation.dllite;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.DatalogProgram;
import org.obda.query.domain.Term;
import org.obda.query.domain.Variable;
import org.obda.query.domain.imp.AtomImpl;
import org.obda.query.domain.imp.CQIEImpl;
import org.obda.query.domain.imp.DatalogProgramImpl;
import org.obda.query.domain.imp.UndistinguishedVariable;

public class QueryAnonymizer {

	
	public DatalogProgram anonymize(DatalogProgram prog){
		
		DatalogProgram newProg = new DatalogProgramImpl();
		List<CQIE> rules = prog.getRules();
		Iterator<CQIE> it = rules.iterator();
		while(it.hasNext()){
			CQIE q = it.next();
			newProg.appendRule(anonymize(q));
		}
		
		return newProg;
	}
	
	public CQIE anonymize(CQIE q){
		HashMap<String,List<Object[]>> auxmap = new HashMap<String,List<Object[]>>();
		List<Atom> body = q.getBody();
		Iterator<Atom> it = body.iterator();
		while(it.hasNext()){
			Atom atom = it.next();
			List<Term> terms = atom.getTerms();
			int pos =0;
			Iterator<Term> term_it = terms.iterator();
			while(term_it.hasNext()){
				Term t = term_it.next();
				if(t instanceof Variable){
					Object[] obj = new Object[2];
					obj[0] = atom;
					obj[1] = pos;
					List<Object[]> list = auxmap.get(t.getName());
					if(list == null){
						list = new Vector<Object[]>();
					}
					list.add(obj);
					auxmap.put(t.getName(), list);
				}
			}
		}
		
		Iterator<Atom> it2 = body.iterator();
		LinkedList<Atom> newBody = new LinkedList<Atom>();
		while(it2.hasNext()){
			Atom atom = it2.next();
			List<Term> terms = atom.getTerms();
			Iterator<Term> term_it = terms.iterator();
			Vector<Term> vex = new Vector<Term>();
			while(term_it.hasNext()){
				Term t = term_it.next();
				List<Object[]> list = auxmap.get(t.getName());
				if(list != null && list.size()<2 && !isVariableInHead(q, t)){
					vex.add(new UndistinguishedVariable());
				}else{
					vex.add(t);
				}
			}
			AtomImpl newatom = new AtomImpl(atom.getPredicate().copy(), vex);
			newBody.add(newatom);
		}
		CQIEImpl query = new CQIEImpl(q.getHead(), newBody, q.isBoolean());
		return query;
	}
	
	private boolean isVariableInHead(CQIE q, Term t){
		
		Atom head = q.getHead();
		List<Term> headterms = head.getTerms();
		Iterator< Term> it = headterms.iterator();
		boolean found = false;
		while(it.hasNext() && !found){
			Term headterm = it.next();
			if(headterm.getName().equals(t.getName())){
				found = true;
			}
		}
		return found;
	}
}
