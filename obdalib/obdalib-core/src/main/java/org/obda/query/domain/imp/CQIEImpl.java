package org.obda.query.domain.imp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.obda.query.domain.Atom;
import org.obda.query.domain.CQIE;
import org.obda.query.domain.Term;
import org.obda.query.tools.util.EventGeneratingLinkedList;
import org.obda.query.tools.util.ListListener;

public class CQIEImpl implements CQIE, ListListener {

	private Atom		head		= null;
	private List<Atom>	body		= null;
	private boolean		isBoolean	= false;

	private int			hash		= -1;

	private boolean		rehash		= true;

	private String		string		= null;

	// TODO Remove isBoolean from the signature and from any method
	public CQIEImpl(Atom head, List<Atom> body, boolean isBoolean) {
		this.head = head;
		
		EventGeneratingLinkedList<Atom> eventbody = new EventGeneratingLinkedList<Atom>();
		eventbody.addAll(body);
		
		this.body = eventbody;
		this.isBoolean = isBoolean;
		
		eventbody.addListener(this);
		EventGeneratingLinkedList<Term> headterms = (EventGeneratingLinkedList<Term>)head.getTerms();
		headterms.addListener(this);
	}

	public List<Atom> getBody() {
		return body;
	}

	public Atom getHead() {
		return head;
	}

	public void updateHead(Atom head) {
		
		EventGeneratingLinkedList<Term> headterms = (EventGeneratingLinkedList<Term>)head.getTerms();
		headterms.removeListener(this);
		
		this.head = head;
		
		rehash = true;
		string = null;
	}

	public void updateBody(List<Atom> body) {
		this.body.clear();
		this.body.addAll(body);
	}

	@Override
	public int hashCode() {
		if (rehash) {
			hash = toString().hashCode();
			rehash = false;
		}
		return hash;
	}

	@Override
	public boolean isBoolean() {
		return isBoolean;
	}

	@Override
	public String toString() {
		if (string == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(head.toString());
			sb.append(":-");

			Iterator<Atom> bit = body.iterator();
			while (bit.hasNext()) {
				Atom atom = bit.next();
				if (bit.hasNext()) {
					sb.append(", ");
				}
				sb.append(atom.toString());
			}
			string = sb.toString();
		}
		return string;
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

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CQIEImpl))
			return false;
		CQIEImpl q2 = (CQIEImpl) obj;
		return hashCode() == q2.hashCode();
	}

	@Override
	public void listChanged() {
		rehash = true;
		string = null;

	}
}
