/*
 * Copyright (C) 2009-2013, Free University of Bozen Bolzano
 * This source code is available under the terms of the Affero General Public
 * License v3.
 * 
 * Please see LICENSE.txt for full license terms, including the availability of
 * proprietary exceptions.
 */
package it.unibz.krdb.obda.owlrefplatform.core.reformulation;

import it.unibz.krdb.obda.model.Function;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MinimalCQProducer {
	private final TreeWitnessReasonerLite reasoner;
	private List<Function> atoms = new LinkedList<Function>();
	private List<Function> noCheckAtoms = new LinkedList<Function>();
	
	public MinimalCQProducer(TreeWitnessReasonerLite reasoner) {
		this.reasoner = reasoner;
	}

	public MinimalCQProducer(MinimalCQProducer cqp) {
		this.reasoner = cqp.reasoner;
		this.atoms.addAll(cqp.atoms);
		this.noCheckAtoms.addAll(cqp.noCheckAtoms);
	}


	public boolean subsumes(Function atom) {
		for (Function a : atoms) 
			if (reasoner.isMoreSpecific(a, atom))
				return true;
		return false;
	}
	
	public void add(Function atom) {
		if (subsumes(atom))
			return;
		
		// removed all atoms that are subsumed
		Iterator<Function> i = atoms.iterator();
		while (i.hasNext()) {
			Function a = i.next();
			if (reasoner.isMoreSpecific(atom, a))
				i.remove();
		}
		atoms.add(atom);
	}
	
	public void addAll(Collection<Function> aa) {
		for (Function a : aa) 
			add(a);
	}

	public List<Function> getAtoms() {
		return atoms;
	}
	
	public void addNoCheck(Function atom) {
		noCheckAtoms.add(atom);
	}
	
	public void addAllNoCheck(Collection<Function> aa) {
		noCheckAtoms.addAll(aa);
	}

	public List<Function> getNoCheckAtoms() {
		return noCheckAtoms;
	}
}
