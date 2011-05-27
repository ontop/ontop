package it.unibz.krdb.obda.owlrefplatform.core.ontology.imp;

import it.unibz.krdb.obda.owlrefplatform.core.ontology.PositiveInclusion;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.RoleDescription;

public class DLLiterRoleInclusionImpl implements PositiveInclusion {

	private RoleDescription	including	= null;
	private RoleDescription	included	= null;

	String					string		= null;

	public DLLiterRoleInclusionImpl(RoleDescription included, RoleDescription including) {

		this.including = including;
		this.included = included;

		this.string = toString();

	}

	public RoleDescription getIncluded() {
		return included;
	}

	public RoleDescription getIncluding() {
		return including;
	}

	public int hashCode() {
		return toString().hashCode();
	}

	public boolean equals(Object obj) {

		if (!(obj instanceof DLLiterRoleInclusionImpl))
			return false;
		DLLiterRoleInclusionImpl inc2 = (DLLiterRoleInclusionImpl) obj;
		if (!including.equals(inc2.including))
			return false;
		return (included.equals(inc2.included));
	}

	public String toString() {
		if (string != null)
			return string;
		StringBuffer bf = new StringBuffer();
		bf.append(included.toString());
		bf.append(" ISAR ");
		bf.append(including.toString());
		return bf.toString();
	}

}
