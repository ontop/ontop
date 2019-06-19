package it.unibz.inf.ontop.spec.ontology.impl;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.model.vocabulary.OWL;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.model.vocabulary.RDFS;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.spec.ontology.*;
import org.apache.commons.rdf.api.IRI;


import java.util.Iterator;

public class OntologyImpl implements Ontology {

	// axioms 

    private final ImmutableSet<ObjectPropertyExpression> auxObjectProperties;

    private final ImmutableList<BinaryAxiom<ClassExpression>> classInclusions;
    private final ImmutableList<NaryAxiom<ClassExpression>> classDisjointness;
    private final ImmutableList<BinaryAxiom<ObjectPropertyExpression>> objectPropertyInclusions;
    private final ImmutableList<NaryAxiom<ObjectPropertyExpression>> objectPropertyDisjointness;
    private final ImmutableList<BinaryAxiom<DataPropertyExpression>> dataPropertyInclusions;
    private final ImmutableList<NaryAxiom<DataPropertyExpression>> dataPropertyDisjointness;

	private final ImmutableList<BinaryAxiom<DataRangeExpression>> subDataRangeAxioms;

	private final ImmutableSet<ObjectPropertyExpression> reflexiveObjectPropertyAxioms;
	private final ImmutableSet<ObjectPropertyExpression> irreflexiveObjectPropertyAxioms;

	private final ImmutableSet<ObjectPropertyExpression> functionalObjectPropertyAxioms;
	private final ImmutableSet<DataPropertyExpression> functionalDataPropertyAxioms;

	// assertions

	private final ImmutableList<ClassAssertion> classAssertions;
	private final ImmutableList<ObjectPropertyAssertion> objectPropertyAssertions;
	private final ImmutableList<DataPropertyAssertion> dataPropertyAssertions;
	private final ImmutableList<AnnotationAssertion> annotationAssertions;

	// exception messages

	private static final String DATATYPE_NOT_FOUND = "Datatype not found: ";

	public static final ImmutableMap<String, Datatype> OWL2QLDatatypes;
	
	private static final String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String rdfs = "http://www.w3.org/2000/01/rdf-schema#";	
	private static final String owl = "http://www.w3.org/2002/07/owl#";
	private static final String xsd = "http://www.w3.org/2001/XMLSchema#";
	
	static {
		
		OWL2QLDatatypes = ImmutableMap.<String, Datatype>builder()
				.put(rdf + "PlainLiteral", new DatatypeImpl(RDF.PLAINLITERAL)) // 	rdf:PlainLiteral
				.put(rdf + "XMLLiteral", new DatatypeImpl(RDF.XMLLITERAL)) //	rdf:XMLLiteral
				.put(rdfs + "Literal", new DatatypeImpl(RDFS.LITERAL)) //		rdfs:Literal
				.put(owl + "real", new DatatypeImpl(OWL.REAL)) // 			owl:real
				.put(owl + "rational", new DatatypeImpl(OWL.RATIONAL)) // 		owl:rational
				.put(xsd + "decimal", new DatatypeImpl(XSD.DECIMAL)) // 	xsd:decimal
				.put(xsd + "integer", new DatatypeImpl(XSD.INTEGER)) // 	xsd:integer
				.put(xsd + "nonNegativeInteger", new DatatypeImpl(XSD.NON_NEGATIVE_INTEGER)) // 	xsd:nonNegativeInteger
				.put(xsd + "string", new DatatypeImpl(XSD.STRING)) // 	xsd:string
				.put(xsd + "normalizedString", new DatatypeImpl(XSD.NORMALIZEDSTRING)) // 	xsd:normalizedString
				.put(xsd + "token", new DatatypeImpl(XSD.TOKEN)) // 	xsd:token
				.put(xsd + "Name", new DatatypeImpl(XSD.NAME)) // 	xsd:Name
				.put(xsd + "NCName", new DatatypeImpl(XSD.NCNAME)) //	xsd:NCName
				.put(xsd + "NMTOKEN", new DatatypeImpl(XSD.NMTOKEN)) // 	xsd:NMTOKEN
				.put(xsd + "hexBinary", new DatatypeImpl(XSD.HEXBINARY)) // 	xsd:hexBinary
				.put(xsd + "base64Binary", new DatatypeImpl(XSD.BASE64BINARY)) // 	xsd:base64Binary
				.put(xsd + "anyURI", new DatatypeImpl(XSD.ANYURI)) // 	xsd:anyURI
				.put(xsd + "dateTime", new DatatypeImpl(XSD.DATETIME)) // 	xsd:dateTime
				.put(xsd + "dateTimeStamp", new DatatypeImpl(XSD.DATETIMESTAMP)) // 	xsd:dateTimeStamp
				.put(xsd + "int", new DatatypeImpl(XSD.INT)) // 	TEMPORARY FOR Q9 / FISHMARK
				.put(xsd + "long", new DatatypeImpl(XSD.LONG)) // 	TEMPORARY FOR OntologyTypesTest
				.build();
	}


    static final class ImmutableOntologyVocabularyCategoryImpl<T> implements OntologyVocabularyCategory<T> {
        private final ImmutableMap<String, T> map;

        private final String NOT_FOUND;

        ImmutableOntologyVocabularyCategoryImpl(ImmutableMap<String, T> map, String NOT_FOUND) {
            this.map = map;
            this.NOT_FOUND = NOT_FOUND;
        }

		@Override
		public T get(IRI iri) {
			String uri = iri.getIRIString();
			T oc = map.get(uri);
			if (oc == null)
				throw new RuntimeException(NOT_FOUND + uri);
			return oc;
		}

		@Override
		public boolean contains(IRI iri) {
			return map.containsKey(iri.getIRIString());
		}

		@Override
        public Iterator<T> iterator() {
            return map.values().iterator();
        }
    }

    private final ImmutableOntologyVocabularyCategoryImpl<OClass> classes;
	private final ImmutableOntologyVocabularyCategoryImpl<ObjectPropertyExpression> objectProperties;
	private final ImmutableOntologyVocabularyCategoryImpl<DataPropertyExpression> dataProperties;
	private final ImmutableOntologyVocabularyCategoryImpl<AnnotationProperty> annotationProperties;

	OntologyImpl(ImmutableOntologyVocabularyCategoryImpl<OClass> classes,
                 ImmutableOntologyVocabularyCategoryImpl<ObjectPropertyExpression> objectProperties,
                 ImmutableSet<ObjectPropertyExpression> auxObjectProperties,
                 ImmutableOntologyVocabularyCategoryImpl<DataPropertyExpression> dataProperties,
                 ImmutableOntologyVocabularyCategoryImpl<AnnotationProperty> annotationProperties,
                 ImmutableList<BinaryAxiom<ClassExpression>> classInclusions,
                 ImmutableList<NaryAxiom<ClassExpression>> classDisjointness,
                 ImmutableList<BinaryAxiom<ObjectPropertyExpression>> objectPropertyInclusions,
                 ImmutableList<NaryAxiom<ObjectPropertyExpression>> objectPropertyDisjointness,
                 ImmutableList<BinaryAxiom<DataPropertyExpression>> dataPropertyInclusions,
                 ImmutableList<NaryAxiom<DataPropertyExpression>> dataPropertyDisjointness,
                 ImmutableList<BinaryAxiom<DataRangeExpression>> subDataRangeAxioms,
                 ImmutableSet<ObjectPropertyExpression> reflexiveObjectPropertyAxioms,
                 ImmutableSet<ObjectPropertyExpression> irreflexiveObjectPropertyAxioms,
                 ImmutableSet<ObjectPropertyExpression> functionalObjectPropertyAxioms,
                 ImmutableSet<DataPropertyExpression> functionalDataPropertyAxioms,
                 ImmutableList<ClassAssertion> classAssertions,
                 ImmutableList<ObjectPropertyAssertion> objectPropertyAssertions,
                 ImmutableList<DataPropertyAssertion> dataPropertyAssertions,
                 ImmutableList<AnnotationAssertion> annotationAssertions) {
	    this.classes = classes;
	    this.objectProperties = objectProperties;
	    this.auxObjectProperties = auxObjectProperties;
	    this.dataProperties = dataProperties;
	    this.annotationProperties = annotationProperties;
	    this.classInclusions = classInclusions;
	    this.classDisjointness = classDisjointness;
	    this.objectPropertyInclusions = objectPropertyInclusions;
	    this.objectPropertyDisjointness = objectPropertyDisjointness;
	    this.dataPropertyInclusions = dataPropertyInclusions;
	    this.dataPropertyDisjointness = dataPropertyDisjointness;
	    this.classAssertions = classAssertions;
	    this.objectPropertyAssertions = objectPropertyAssertions;
	    this.dataPropertyAssertions = dataPropertyAssertions;
	    this.annotationAssertions = annotationAssertions;
	    this.subDataRangeAxioms = subDataRangeAxioms;
	    this.reflexiveObjectPropertyAxioms = reflexiveObjectPropertyAxioms;
	    this.irreflexiveObjectPropertyAxioms = irreflexiveObjectPropertyAxioms;
        this.functionalObjectPropertyAxioms = functionalObjectPropertyAxioms;
	    this.functionalDataPropertyAxioms = functionalDataPropertyAxioms;

	    this.unclassifiedTBox = new UnclassifiedOntologyTBox();
	    this.tbox = ClassifiedTBoxImpl.classify(unclassifiedTBox);
    }

    @Override
    public OntologyVocabularyCategory<AnnotationProperty> annotationProperties() { return annotationProperties; }

 	@Override
	public Datatype getDatatype(String uri) {
        Datatype dt = OWL2QLDatatypes.get(uri);
        if (dt == null)
            throw new RuntimeException(DATATYPE_NOT_FOUND + uri);
        return dt;
	}

    private final UnclassifiedOntologyTBox unclassifiedTBox;

    private final ClassifiedTBox tbox;

    /*
        used only in OWLAPI Translation tests
     */

    public final class UnclassifiedOntologyTBox {

        public OntologyVocabularyCategory<OClass> classes() { return classes; }

        public OntologyVocabularyCategory<ObjectPropertyExpression> objectProperties() { return objectProperties; }

        public OntologyVocabularyCategory<DataPropertyExpression> dataProperties() { return dataProperties; }

        public OntologyVocabularyCategory<AnnotationProperty> annotationProperties() { return annotationProperties; }

        public ImmutableList<BinaryAxiom<ClassExpression>> getSubClassAxioms() { return classInclusions; }

        public ImmutableList<BinaryAxiom<DataRangeExpression>> getSubDataRangeAxioms() { return subDataRangeAxioms; }

        public ImmutableList<BinaryAxiom<ObjectPropertyExpression>> getSubObjectPropertyAxioms() { return objectPropertyInclusions; }

        public ImmutableList<BinaryAxiom<DataPropertyExpression>> getSubDataPropertyAxioms() { return dataPropertyInclusions; }

        public ImmutableSet<ObjectPropertyExpression> getFunctionalObjectProperties() { return functionalObjectPropertyAxioms; }

        public ImmutableSet<DataPropertyExpression> getFunctionalDataProperties() { return functionalDataPropertyAxioms; }

        public ImmutableList<NaryAxiom<ClassExpression>> getDisjointClassesAxioms() { return classDisjointness; }

        public ImmutableList<NaryAxiom<ObjectPropertyExpression>> getDisjointObjectPropertiesAxioms() { return objectPropertyDisjointness; }

        public ImmutableList<NaryAxiom<DataPropertyExpression>> getDisjointDataPropertiesAxioms() { return dataPropertyDisjointness; }

        public ImmutableSet<ObjectPropertyExpression> getReflexiveObjectPropertyAxioms() { return reflexiveObjectPropertyAxioms; }

        public ImmutableSet<ObjectPropertyExpression> getIrreflexiveObjectPropertyAxioms() { return irreflexiveObjectPropertyAxioms; }

        public ImmutableSet<ObjectPropertyExpression> getAuxiliaryObjectProperties() { return auxObjectProperties; }
    }

    @Override
    public ClassifiedTBox tbox() { return tbox; }

    // used only in tests
    public UnclassifiedOntologyTBox unclassifiedTBox() { return unclassifiedTBox; }

	@Override
	public OntologyABox abox() {
	    return new OntologyABox() {

            @Override
            public ImmutableList<ClassAssertion> getClassAssertions() { return classAssertions; }

            @Override
            public ImmutableList<ObjectPropertyAssertion> getObjectPropertyAssertions() { return objectPropertyAssertions; }

            @Override
            public ImmutableList<DataPropertyAssertion> getDataPropertyAssertions() { return dataPropertyAssertions; }

            @Override
            public ImmutableList<AnnotationAssertion> getAnnotationAssertions() { return annotationAssertions; }
        };
    }


    @Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[Ontology info.")
		 	.append(String.format(" Axioms: %d", classInclusions.size() +
		 			objectPropertyInclusions.size() + dataPropertyInclusions.size()))
			.append(String.format(" Classes: %d", classes.map.size()))
			.append(String.format(" Object Properties: %d", objectProperties.map.size()))
			.append(String.format(" Data Properties: %d", dataProperties.map.size()))
		    .append(String.format(" Annotation Properties: %d]", annotationProperties.map.size()));
		return str.toString();
	}
}
