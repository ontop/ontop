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
		return toString().hashCode();
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
