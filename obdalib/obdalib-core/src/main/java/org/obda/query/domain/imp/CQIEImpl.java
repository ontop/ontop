package org.obda.query.domain.imp;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;

public class CQIEImpl implements CQIE {

	private Atom		head		= null;
	private List<Atom>	body		= null;
	private boolean		isBoolean	= false;

	// TODO Remove isBoolean from the signature and from any method
	public CQIEImpl(Atom head, List<Atom> body, boolean isBoolean) {
		this.head = head;
		this.body = body;
		this.isBoolean = isBoolean;
	}

	public List<Atom> getBody() {
		return body;
	}

	public Atom getHead() {
		return head;
	}

	public void updateHead(Atom head) {
		this.head = head;
	}

	public void updateBody(List<Atom> body) {
		this.body = body;
	}

	@Override
	public int hashCode() {

		StringBuffer sb = new StringBuffer();
		Atom head = this.getHead();
		StringBuffer headString = new StringBuffer();
		headString.append(head.getPredicate().getName());
		List<Term> list = head.getTerms();
		Iterator<Term> it = list.iterator();
		StringBuffer var = new StringBuffer();
		while (it.hasNext()) {
			Term t = it.next();
			if (var.length() > 0) {
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
		while (bit.hasNext()) {
			Atom a = bit.next();
			if (bodyString.length() > 0) {
				bodyString.append(",");
			}
			StringBuffer atomString = new StringBuffer();
			URI atomuri = a.getPredicate().getName();
			atomString.append(atomuri.toString());
			atomString.append("(");
			List<Term> para = a.getTerms();
			Iterator<Term> pit = para.iterator();
			StringBuffer atomvar = new StringBuffer();
			while (pit.hasNext()) {
				Term t = pit.next();
				if (atomvar.length() > 0) {
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
		// TODO Auto-generated method stub
		return isBoolean;
	}

	@Override
	public String toString() {

		StringBuilder sbHead = new StringBuilder();
		sbHead.append(head.toString());
		sbHead.append(" "); // ending character.
		StringBuilder sbBody = new StringBuilder();
		Iterator<Atom> bit = body.iterator();
		while (bit.hasNext()) {
			Atom a = bit.next();
			if (sbBody.length() > 0) {
				sbBody.append(", ");
			}
			sbBody.append(a.toString());
		}
		return sbHead + ":- " + sbBody;
	}

	@Override
	public CQIEImpl clone() {
		Atom copyHead = head.copy();
		List<Atom> copyBody = new LinkedList<Atom>();
		for (Atom atom : body) {
			copyBody.add(atom.copy());
		}
		boolean copyIsBoolean = isBoolean;

		return new CQIEImpl(copyHead, copyBody, copyIsBoolean);
	}
}
