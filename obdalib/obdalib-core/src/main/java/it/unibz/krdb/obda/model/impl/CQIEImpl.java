package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.utils.EventGeneratingLinkedList;
import it.unibz.krdb.obda.utils.ListListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/***
 * This is a rule implementation that keeps track of changes in the query by
 * externals. It is also optimized for .equals calls.
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class CQIEImpl implements CQIE, ListListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5789854661851692098L;
	private Atom head = null;
	private List<Atom> body = null;
	// private boolean isBoolean = false;

	private int hash = -1;

	private boolean rehash = true;

	private String string = null;

	private static final String SPACE = " ";
	private static final String COMMA = ",";
	private static final String INV_IMPLIES = ":-";

	private OBDAQueryModifiers modifiers = null;

	// TODO Remove isBoolean from the signature and from any method
	protected CQIEImpl(Atom head, List<Atom> body) {

		// this.isBoolean = isBoolean;

		// The syntax for CQ may contain no body, thus, this condition will
		// check whether the construction of the link list is possible or not.
		if (body != null) {
			EventGeneratingLinkedList<Atom> eventbody = new EventGeneratingLinkedList<Atom>();
			eventbody.addAll(body);

			this.body = eventbody;

			eventbody.addListener(this);
		}

		// The syntax for CQ may also contain no head, thus, this condition
		// will check whether we can look for the head terms or not.
		if (head != null) {
			this.head = head;

			EventGeneratingLinkedList<Term> headterms = (EventGeneratingLinkedList<Term>) head.getTerms();
			headterms.addListener(this);
		}
	}

	public List<Atom> getBody() {
		return body;
	}

	public Atom getHead() {
		return head;
	}

	public void updateHead(Atom head) {

		EventGeneratingLinkedList<Term> headterms = (EventGeneratingLinkedList<Term>) head.getTerms();
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

	// @Override
	// public boolean isBoolean() {
	// return isBoolean;
	// }

	@Override
	public String toString() {
		if (string == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(head.toString());
			sb.append(SPACE + INV_IMPLIES + SPACE); // print " :- "

			Iterator<Atom> bit = body.iterator();
			while (bit.hasNext()) {
				Atom atom = bit.next();
				sb.append(atom.toString());

				if (bit.hasNext()) { // if there is a next atom.
					sb.append(COMMA + SPACE); // print ", "
				}
			}
			string = sb.toString();
		}
		return string;
	}

	@Override
	public CQIEImpl clone() {
		Atom copyHead = (Atom) head.clone();
		List<Atom> copyBody = new ArrayList<Atom>(body.size() + 10);

		for (Atom atom : body) {
			if (atom != null) {
				copyBody.add(atom.clone());
			}
		}

		return new CQIEImpl(copyHead, copyBody);
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

	@Override
	public OBDAQueryModifiers getQueryModifiers() {
		return modifiers;
	}

	@Override
	public void setQueryModifiers(OBDAQueryModifiers modifiers) {
		this.modifiers = modifiers;
	}

	@Override
	public Set<Variable> getReferencedVariables() {

		Set<Variable> vars = new LinkedHashSet<Variable>();
		for (Atom atom : body)
			for (Term t : atom.getTerms()) {
				for (Variable v : t.getReferencedVariables())
					vars.add(v);
			}
		return vars;
	}
}
