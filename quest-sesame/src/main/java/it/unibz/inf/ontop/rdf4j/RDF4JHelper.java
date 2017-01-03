package it.unibz.inf.ontop.rdf4j;

import it.unibz.inf.ontop.model.BNode;
import it.unibz.inf.ontop.model.Constant;
import it.unibz.inf.ontop.model.DatatypeFactory;
import it.unibz.inf.ontop.model.ObjectConstant;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.URIConstant;
import it.unibz.inf.ontop.model.ValueConstant;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.ontology.AnnotationAssertion;
import it.unibz.inf.ontop.ontology.Assertion;
import it.unibz.inf.ontop.ontology.ClassAssertion;
import it.unibz.inf.ontop.ontology.DataPropertyAssertion;
import it.unibz.inf.ontop.ontology.ObjectPropertyAssertion;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class RDF4JHelper {

	private static final ValueFactory fact = SimpleValueFactory.getInstance();
	private static final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	
	public static Resource getResource(ObjectConstant obj) {
		if (obj instanceof BNode)
			return fact.createBNode(((BNode)obj).getName());
		else if (obj instanceof URIConstant)
			return fact.createIRI(((URIConstant)obj).getURI());
		else
            return null;
			//throw new IllegalArgumentException("Invalid constant as subject!" + obj);
	}
	
	public static Literal getLiteral(ValueConstant literal)
	{
		if ((literal.getType() == COL_TYPE.LITERAL) ||  (literal.getType() == COL_TYPE.LITERAL_LANG)) {
            return fact.createLiteral(literal.getValue(), literal.getLanguage());
		}
		else if (literal.getType() == COL_TYPE.OBJECT) {
            return fact.createLiteral(literal.getValue(), dtfac.getDatatypeURI(COL_TYPE.STRING));
		}	
		else {
			IRI datatype = dtfac.getDatatypeURI(literal.getType());
			if (datatype == null)
				throw new RuntimeException("Found unknown TYPE for constant: " + literal + " with COL_TYPE="+ literal.getType());

            return fact.createLiteral(literal.getValue(), datatype);
		}
	}

    public static Value getValue(Constant c) {

        if(c == null)
            return null;

        Value value = null;
        if (c instanceof ValueConstant) {
            value = RDF4JHelper.getLiteral((ValueConstant) c);
        } else if (c instanceof ObjectConstant){
            value = RDF4JHelper.getResource((ObjectConstant) c);
        }
        return value;
    }

	private static IRI createURI(String uri) {
		return fact.createIRI(uri);
	}

	public static Statement createStatement(Assertion assertion) {
		if (assertion instanceof ObjectPropertyAssertion) {
			return createStatement((ObjectPropertyAssertion) assertion);
		} else if (assertion instanceof DataPropertyAssertion) {
			return createStatement((DataPropertyAssertion) assertion);
		} else if (assertion instanceof ClassAssertion) {
			return createStatement((ClassAssertion) assertion);
		} else if (assertion instanceof AnnotationAssertion) {
			return createStatement((AnnotationAssertion) assertion);
	    }else {
			throw new RuntimeException("Unsupported assertion: " + assertion);
		}
	}

	private static Statement createStatement(ObjectPropertyAssertion assertion) {
		return fact.createStatement(getResource(assertion.getSubject()),
				createURI(assertion.getProperty().getPredicate().getName()),
				getResource(assertion.getObject()));
	}

	private static Statement createStatement(DataPropertyAssertion assertion) {
		return fact.createStatement(getResource(assertion.getSubject()),
				createURI(assertion.getProperty().getPredicate().getName()),
				getLiteral(assertion.getValue())
		);
	}

	private static Statement createStatement(AnnotationAssertion assertion) {
		Constant constant = assertion.getValue();

		if (constant instanceof ValueConstant) {
			return fact.createStatement(getResource(assertion.getSubject()),
					createURI(assertion.getProperty().getPredicate().getName()),
					getLiteral((ValueConstant) constant));
		} else if (constant instanceof ObjectConstant)  {
			return fact.createStatement(getResource(assertion.getSubject()),
					createURI(assertion.getProperty().getPredicate().getName()),
					getResource((ObjectConstant) constant));
		} else {
			throw new RuntimeException("Unsupported constant for an annotation property!"
					+ constant);
		}
	}

	private static Statement createStatement(ClassAssertion assertion) {
		return fact.createStatement(getResource(assertion.getIndividual()),
				createURI(OBDAVocabulary.RDF_TYPE),
				createURI(assertion.getConcept().getPredicate().getName()));
	}
}
