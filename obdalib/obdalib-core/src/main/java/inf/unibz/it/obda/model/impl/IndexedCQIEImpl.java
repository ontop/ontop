package inf.unibz.it.obda.model.impl;

import inf.unibz.it.obda.domain.QueryModifiers;
import inf.unibz.it.obda.model.Atom;
import inf.unibz.it.obda.model.CQIE;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


public class IndexedCQIEImpl implements CQIE {

	/**
	 * This class is not yet used. It is thought for later to be used for speeding up the
	 * computation.
	 */
	private Atom head = null;
	private List<Atom> body = null;
//	private Map<Predicate, Vector<Atom>> predicateToAtomMap = null;
//	private Map<String, Vector<Atom>> termToAtomMap = null;
	private final boolean isBoolean = false;

	public IndexedCQIEImpl (Atom head,List<Atom> body){
		this.head = head;
		this.body = body;
//		predicateToAtomMap = new HashMap<Predicate, Vector<Atom>>();
//		termToAtomMap = new HashMap<String, Vector<Atom>>();

		Iterator<Atom> it = body.iterator();
		while(it.hasNext()){
			//TODO create indexes
		}
	}

	public List<Atom> getBody() {
		return body;
	}

	public Atom getHead() {
		return head;
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
		List<Atom> copyBody = new LinkedList<Atom>();
		for (Atom atom : body) {
			copyBody.add(atom.copy());
		}
		boolean copyIsBoolean = isBoolean;

		return new CQIEImpl(copyHead, copyBody, copyIsBoolean);
	}
	
	@Override
	public QueryModifiers getQueryModifiers() {
		return new QueryModifiers();
	}
}
