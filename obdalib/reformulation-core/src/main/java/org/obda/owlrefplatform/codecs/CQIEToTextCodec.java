package org.obda.owlrefplatform.codecs;

import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.utils.codec.ObjectToTextCodec;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;

/**
 * A class that transforms a CQIE into a string
 * Note: class was implemented for debugging should not be used and still 
 * contains several errors
 * 
 * @author Manfred Gerstgrasser
 *
 */

public class CQIEToTextCodec extends ObjectToTextCodec<CQIE> {

	public CQIEToTextCodec(APIController apic) {
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
		
		StringBuffer sb = new StringBuffer();
		Atom head =input.getHead();
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
		
		
		List<Atom> body = input.getBody();
		StringBuffer bodyString = new StringBuffer();
		Iterator<Atom> bit = body.iterator();
		while(bit.hasNext()){
			Atom a = bit.next();
			if(bodyString.length()>0){
				bodyString.append(",");
			}
			StringBuffer atomString = new StringBuffer();
			URI atomuri = a.getPredicate().getName();
			String prefix = apic.getCoupler().getPrefixForUri(atomuri);
			atomString.append(prefix);
			atomString.append(":");
			atomString.append(atomuri.getFragment());
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
		return sb.toString();
	}

}
