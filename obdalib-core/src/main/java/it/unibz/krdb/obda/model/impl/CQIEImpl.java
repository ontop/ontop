package it.unibz.krdb.obda.model.impl;

import it.unibz.krdb.obda.model.Atom;
import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.NewLiteral;
import it.unibz.krdb.obda.model.OBDAQueryModifiers;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.utils.EventGeneratingArrayList;
import it.unibz.krdb.obda.utils.ListListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a rule implementation that keeps track of changes in the query by
 * externals. It is also optimized for .equals calls.
 */
public class CQIEImpl implements CQIE, ListListener {

	private static final long serialVersionUID = 5789854661851692098L;
	private Function head = null;
	private List<Function> body = null;

	private int hash = -1;
	private boolean rehash = true;

	private String string = null;

	private static final String SPACE = " ";
	private static final String COMMA = ",";
	private static final String INV_IMPLIES = ":-";

	private OBDAQueryModifiers modifiers = null;

	// TODO Remove isBoolean from the signature and from any method
	protected CQIEImpl(Function head, List<Function> body) {
		this(head, body.toArray(new Function[body.size()]));
	}

	// TODO Remove isBoolean from the signature and from any method
	protected CQIEImpl(Function head, Function[] body) {

		// The syntax for CQ may contain no body, thus, this condition will
		// check whether the construction of the link list is possible or not.
		if (body != null) {
			EventGeneratingArrayList<Function> eventbody = new EventGeneratingArrayList<Function>(
					body.length * 20);
			for (int i = 0; i < body.length; i++) {
				eventbody.add(body[i]);
			}
			this.body = eventbody;

			registerListeners(eventbody);
			// TODO possible memory leak!!! we should also de-register when
			// objects are removed
		}

		// The syntax for CQ may also contain no head, thus, this condition
		// will check whether we can look for the head terms or not.
		if (head != null) {
			this.head = head;
			EventGeneratingArrayList<NewLiteral> headterms = (EventGeneratingArrayList<NewLiteral>) head
					.getTerms();
			headterms.addListener(this);
		}
	}

	private void registerListeners(
			EventGeneratingArrayList<? extends NewLiteral> functions) {

		functions.addListener(this);

		for (Object o : functions) {
			if (!(o instanceof Function)) {
				continue;
			}
			Function f = (Function) o;
			EventGeneratingArrayList<NewLiteral> list = (EventGeneratingArrayList<NewLiteral>) f
					.getTerms();
			list.addListener(this);
			registerListeners(list);
		}
	}

	public List<Function> getBody() {
		return body;
	}

	public Function getHead() {
		return head;
	}

	public void updateHead(Function head) {
		this.head = head;

		EventGeneratingArrayList<NewLiteral> headterms = (EventGeneratingArrayList<NewLiteral>) head
				.getTerms();
		headterms.removeListener(this);
		headterms.addListener(this);
		listChanged();
	}

	public void updateBody(List<Function> body) {
		this.body.clear();
		this.body.addAll(body);
		listChanged();
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
	public String toString() {
		if (string == null) {
			StringBuilder sb = new StringBuilder();
			sb.append(head.toString());
			sb.append(SPACE + INV_IMPLIES + SPACE); // print " :- "

			Iterator<Function> bit = body.iterator();
			while (bit.hasNext()) {
				Function atom = bit.next();
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
		Function copyHead = (Atom) head.clone();
		List<Function> copyBody = new ArrayList<Function>(body.size() + 10);

		for (Function atom : body) {
			if (atom != null) {
				copyBody.add((Function) atom.clone());
			}
		}

		CQIEImpl newquery = new CQIEImpl(copyHead, copyBody);
		newquery.rehash = this.rehash;
		newquery.string = this.string;
		newquery.hash = this.hash;

		return newquery;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CQIEImpl)) {
			return false;
		}
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
		listChanged();
	}

	@Override
	public Set<Variable> getReferencedVariables() {
		Set<Variable> vars = new LinkedHashSet<Variable>();
		for (Function atom : body)
			for (NewLiteral t : atom.getTerms()) {
				for (Variable v : t.getReferencedVariables())
					vars.add(v);
			}
		return vars;
	}

	@Override
	public Map<Variable, Integer> getVariableCount() {
		Map<Variable, Integer> vars = new HashMap<Variable, Integer>();
		for (Function atom : body) {
			Map<Variable, Integer> atomCount = atom.getVariableCount();
			for (Variable var : atomCount.keySet()) {
				Integer count = vars.get(var);
				if (count != null) {
					vars.put(var, count + atomCount.get(var));
				} else {
					vars.put(var, new Integer(atomCount.get(var)));
				}
			}
		}

		Map<Variable, Integer> atomCount = head.getVariableCount();
		for (Variable var : atomCount.keySet()) {
			Integer count = vars.get(var);
			if (count != null) {
				vars.put(var, count + atomCount.get(var));
			} else {
				vars.put(var, new Integer(atomCount.get(var)));
			}
		}
		return vars;
	}
}
