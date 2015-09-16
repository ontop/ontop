package it.unibz.krdb.sql;

import com.google.common.collect.ImmutableList;

public class UniqueConstraint {

	private ImmutableList<Attribute> attributes;
	
	public UniqueConstraint(ImmutableList<Attribute> attributes) {
		this.attributes = attributes;
	}
	
	public ImmutableList<Attribute> getAttributes() {
		return attributes;
	}
}
