package it.unibz.krdb.obda.codec;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.FunctionalTermImpl;

import java.net.URI;
import java.util.Iterator;
import java.util.List;


/**
 * A class that transforms a CQIE into a string
 * Note: class was implemented for debugging should not be used and still 
 * contains several errors
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class CQIEToTextCodec extends ObjectToTextCodec<CQIE> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4888736510436022782L;

	public CQIEToTextCodec(OBDAModel apic) {
		super(apic);
		// TODO Auto-generated constructor stub
	}

	@Override
	@Deprecated
	public CQIE decode(String input) {
		return null;
	}

	/**
	 * transforms the given input query into a string. 
	 */
	@Override
	public String encode(CQIE input) {
//		PrefixManager pm = apic.getPrefixManager();
//		StringBuffer sb = new StringBuffer();
//		Atom head =input.getHead();
//		StringBuffer headString = renderAtom(head, pm);
//		headString.append(" :- ");
//		
//		
//		List<Atom> body = input.getBody();
//		StringBuffer bodyString = new StringBuffer();
//		Iterator<Atom> bit = body.iterator();
//		while(bit.hasNext()){
//			Atom a = (Atom) bit.next();
//			if(bodyString.length() > 0){
//				bodyString.append(", ");
//			}
//			StringBuffer atomString = renderAtom(a, pm);
//			bodyString.append(atomString);
//		}
//		
//		sb.append(headString);
		return input.toString();
//		sb.append(bodyString);
//		return sb.toString();
	}
	
	private StringBuffer renderAtom(Atom a, PrefixManager pm) {
		StringBuffer atomString = new StringBuffer();
		URI atomuri = a.getPredicate().getName();
		
		atomString.append(pm.getShortForm(atomuri.toString()));
		atomString.append("(");
		
		List<Term> para = a.getTerms();
		Iterator<Term> pit = para.iterator();
		StringBuffer atomvar = new StringBuffer();
		while(pit.hasNext()){
			Term t = pit.next();
			if(atomvar.length()>0){
				atomvar.append(",");
			}
			if (t instanceof FunctionalTermImpl) {
				FunctionalTermImpl f = (FunctionalTermImpl) t;
				atomString.append(pm.getShortForm(f.getFunctionSymbol().toString()));
				Iterator<Term> innerterms = f.getTerms().iterator();
				while (innerterms.hasNext()) {
					atomvar.append(pm.getShortForm(innerterms.next().toString(), false));
					if (innerterms.hasNext())
						atomvar.append(",");
				}
			} else if (t instanceof Variable){
				atomvar.append("?");
				atomvar.append(((Variable) t).getName());
			} else if (t instanceof ValueConstant){
				atomvar.append("'");
				atomvar.append(((ValueConstant) t).getValue());
				atomvar.append("'");
			}else if (t instanceof URIConstant){
				atomvar.append("<");
				atomvar.append(((URIConstant) t).getURI().toString());
				atomvar.append(">");
			} else {
				throw new RuntimeException("invalid term found in atom.");
			}
		}
		atomString.append(atomvar);
		atomString.append(")");
		return atomString;
	}
}
