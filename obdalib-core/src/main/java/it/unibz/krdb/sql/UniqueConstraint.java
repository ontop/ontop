package it.unibz.krdb.sql;

import com.google.common.collect.ImmutableList;

public class UniqueConstraint {

	private ImmutableList<Attribute> attributes;
	
	public UniqueConstraint(ImmutableList<Attribute> attributes) {
		this.attributes = attributes;
		if (attributes.isEmpty())
			throw new IllegalArgumentException("Empty UNIQUE constraint");
	}
	
	public ImmutableList<Attribute> getAttributes() {
		return attributes;
	}
}
