package org.obda.reformulation.domain.imp;

import org.obda.reformulation.domain.PositiveInclusion;
import org.obda.reformulation.domain.RoleDescription;

public class DLLiterRoleInclusionImpl implements PositiveInclusion {

	private RoleDescription including = null;
	private RoleDescription included = null;
	
	
	public DLLiterRoleInclusionImpl(RoleDescription included, RoleDescription including){
	
		this.including = including;
		this.included = included;
	}
	
	public RoleDescription getIncluded() {
		return included;
	}

	public RoleDescription getIncluding() {
		return including;
	}

}
