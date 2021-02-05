package it.unibz.inf.ontop.spec.ontology.impl;

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

	private final ImmutableSet<RDFFact> assertions;

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
        private final ImmutableMap<IRI, T> map;

        private final String NOT_FOUND;

        ImmutableOntologyVocabularyCategoryImpl(ImmutableMap<IRI, T> map, String NOT_FOUND) {
            this.map = map;
            this.NOT_FOUND = NOT_FOUND;
        }

		@Override
		public T get(IRI iri) {
			T oc = map.get(iri);
			if (oc == null)
				throw new RuntimeException(NOT_FOUND + iri);
			return oc;
		}

		@Override
		public boolean contains(IRI iri) {
			return map.containsKey(iri);
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
				 ImmutableSet<RDFFact> assertions) {
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
	    this.subDataRangeAxioms = subDataRangeAxioms;
	    this.reflexiveObjectPropertyAxioms = reflexiveObjectPropertyAxioms;
	    this.irreflexiveObjectPropertyAxioms = irreflexiveObjectPropertyAxioms;
        this.functionalObjectPropertyAxioms = functionalObjectPropertyAxioms;
	    this.functionalDataPropertyAxioms = functionalDataPropertyAxioms;
		this.assertions = assertions;

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
	public ImmutableSet<RDFFact> abox() {
	    return assertions;
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
