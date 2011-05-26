package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.domain.QueryModifiers;
import inf.unibz.it.obda.model.impl.CQIEImpl;

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

	private static final String SPACE = " ";
	private static final String COMMA = ",";
	private static final String INV_IMPLIES = ":-";

	// TODO Remove isBoolean from the signature and from any method
	public CQIEImpl(Atom head, List<Atom> body, boolean isBoolean) {

		this.isBoolean = isBoolean;

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

	@Override
	public boolean isBoolean() {
		return isBoolean;
	}

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
	
	@Override
	public QueryModifiers getQueryModifiers() {
		return new QueryModifiers();
	}
}
