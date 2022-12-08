package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.DataPropertyExpression;
import it.unibz.inf.ontop.spec.ontology.OClass;
import it.unibz.inf.ontop.spec.ontology.ObjectPropertyExpression;
import it.unibz.inf.ontop.utils.ImmutableCollectors;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Map.Entry;

public class SemanticIndex {

	private final ImmutableMap<OClass, SemanticIndexRange> classRanges;
	private final ImmutableMap<ObjectPropertyExpression, SemanticIndexRange> opRanges;
	private final ImmutableMap<DataPropertyExpression, SemanticIndexRange> dpRanges;
	

	public SemanticIndex(ClassifiedTBox reasoner)  {
		SemanticIndexBuilder builder = new SemanticIndexBuilder();
		classRanges = builder.createSemanticIndex(reasoner.classesDAG()).entrySet().stream()
				// the DAG is reduced to the named part, so the cast below is safe
				.collect(ImmutableCollectors.toMap(e -> (OClass)e.getKey(), Entry::getValue));
		opRanges = ImmutableMap.copyOf(builder.createSemanticIndex(reasoner.objectPropertiesDAG()));
		dpRanges = ImmutableMap.copyOf(builder.createSemanticIndex(reasoner.dataPropertiesDAG()));
	}

	public SemanticIndexRange getRange(OClass d) {
		return classRanges.get(d);
	}
	public SemanticIndexRange getRange(ObjectPropertyExpression d) {
		return opRanges.get(d);
	}
	public SemanticIndexRange getRange(DataPropertyExpression d) {
		return dpRanges.get(d);
	}


	public ImmutableSet<Entry<OClass, SemanticIndexRange>> getIndexedClasses() {
		return classRanges.entrySet();
	}
	public ImmutableSet<Entry<ObjectPropertyExpression, SemanticIndexRange>> getIndexedObjectProperties() {
		return opRanges.entrySet();
	}
	public ImmutableSet<Entry<DataPropertyExpression, SemanticIndexRange>> getIndexedDataProperties() {
		return dpRanges.entrySet();
	}
}
