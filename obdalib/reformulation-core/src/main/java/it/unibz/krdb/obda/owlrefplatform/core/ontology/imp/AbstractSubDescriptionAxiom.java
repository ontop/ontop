package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Description;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.OClass;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Property;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeClassRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.PropertySomeRestriction;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.SubDescriptionAxiom;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractSubDescriptionAxiom implements SubDescriptionAxiom {

	protected Description	including	= null; // righthand side

	protected Description	included	= null;

	String					string		= null;

	int						hash		= 0;

	public AbstractSubDescriptionAxiom(Description subDesc, Description superDesc) {
		if (subDesc == null || superDesc == null)
			throw new RuntimeException("Recieved null in concept inclusion");
		included = subDesc;
		including = superDesc;

		string = toString();
		hash = string.hashCode();

	}

	@Override
	public Set<Predicate> getReferencedEntities() {
		Set<Predicate> res = new HashSet<Predicate>();
		for (Predicate p : getPredicates(included))
			res.add(p);
		for (Predicate p : getPredicates(including))
			res.add(p);
		return res;
	}

	private Set<Predicate> getPredicates(Description desc) {
		Set<Predicate> preds = new HashSet<Predicate>();
		if (desc instanceof Property) {
			preds.add(((Property) desc).getPredicate());
		} else if (desc instanceof OClass) {
			preds.add(((OClass) desc).getPredicate());
		} else if (desc instanceof PropertySomeRestriction) {
			preds.add(((PropertySomeRestriction) desc).getPredicate());
		} else if (desc instanceof PropertySomeClassRestriction) {
			preds.add(((PropertySomeClassRestriction) desc).getPredicate());
			preds.add(((PropertySomeClassRestriction) desc).getFiller().getPredicate());
		} else {
			throw new UnsupportedOperationException("Cant understand: " + desc.toString());
		}
		return preds;
	}

	public int hashCode() {
		return hash;
	}

	public String toString() {
		if (string != null)
			return string;

		StringBuffer bf = new StringBuffer();
		bf.append(included.toString());
		bf.append(" ISA ");
		bf.append(including.toString());
		return bf.toString();
	}

}
