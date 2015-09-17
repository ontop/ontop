package it.unibz.krdb.sql;

import com.google.common.collect.ImmutableList;

public class ForeignKeyConstraint {

	public static final class Component {
		private final Attribute attribute, reference;
		
		private Component(Attribute attribute, Attribute reference) {
			this.attribute = attribute;
			this.reference = reference;
		}
		
		public Attribute getAttribute() {
			return attribute;
		}
		
		public Attribute getReference() {
			return reference;
		}
	}
	
	public static final class Builder {
		private final ImmutableList.Builder<Component> builder = new ImmutableList.Builder<>();
		private final String name;
		
		public Builder(String name) {
			this.name = name;
		}
		
		public Builder add(Attribute attribute, Attribute reference) {
			builder.add(new Component(attribute, reference));
			return this;
		}
		
		public ForeignKeyConstraint build() {
			return new ForeignKeyConstraint(name, builder.build());
		}
	}
	
	public static ForeignKeyConstraint of(String name, Attribute attribute, Attribute reference) {
		return new Builder(name).add(attribute, reference).build();
	}
	
	private final ImmutableList<Component> components;
	private final String name;
	
	private ForeignKeyConstraint(String name, ImmutableList<Component> components) {
		if (components.isEmpty())
			throw new IllegalArgumentException("Empty list of components");
		
		this.name = name;
		this.components = components;
	}
	
	public String getName() {
		return name;
	}
	
	public ImmutableList<Component> getComponents() {
		return components;
	}
}
