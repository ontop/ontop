package it.unibz.inf.ontop.sesame;

import it.unibz.inf.ontop.model.BNode;
import it.unibz.inf.ontop.model.*;
import it.unibz.inf.ontop.model.Predicate.COL_TYPE;
import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.OBDAVocabulary;
import it.unibz.inf.ontop.ontology.*;
import org.openrdf.model.*;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.ValueFactoryImpl;

public class SesameHelper {

	private static final ValueFactory fact = new ValueFactoryImpl();
	private static final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	
	public static Resource getResource(ObjectConstant obj) {
		if (obj instanceof BNode)
			return fact.createBNode(((BNode)obj).getName());
		else if (obj instanceof URIConstant)
			return fact.createURI(((URIConstant)obj).getURI());
		else
            return null;
			//throw new IllegalArgumentException("Invalid constant as subject!" + obj);
	}
	
	public static Literal getLiteral(ValueConstant literal)
	{
		if ((literal.getType() == COL_TYPE.LITERAL) ||  (literal.getType() == COL_TYPE.LITERAL_LANG)) {
			Literal value = fact.createLiteral(literal.getValue(), literal.getLanguage());
			return value;
		}
		else if (literal.getType() == COL_TYPE.OBJECT) {
			Literal value = fact.createLiteral(literal.getValue(), dtfac.getDatatypeURI(COL_TYPE.STRING));
			return value;
		}	
		else {
			URI datatype = dtfac.getDatatypeURI(literal.getType());
			if (datatype == null)
				throw new RuntimeException("Found unknown TYPE for constant: " + literal + " with COL_TYPE="+ literal.getType());
			
			Literal value = fact.createLiteral(literal.getValue(), datatype);
			return value;
		}
	}

    public static Value getValue(Constant c) {

        if(c == null)
            return null;

        Value value = null;
        if (c instanceof ValueConstant) {
            value = SesameHelper.getLiteral((ValueConstant) c);
        } else if (c instanceof ObjectConstant){
            value = SesameHelper.getResource((ObjectConstant) c);
        }
        return value;
    }

	public static URI createURI(String uri) {
		return fact.createURI(uri);
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
		return new StatementImpl(getResource(assertion.getSubject()),
				createURI(assertion.getProperty().getPredicate().getName().toString()),
				getResource(assertion.getObject()));
	}

	private static Statement createStatement(DataPropertyAssertion assertion) {
		if (!(assertion.getValue() instanceof ValueConstant)) {
			throw new RuntimeException("Invalid constant as object!" + assertion.getValue());
		}

		return new StatementImpl(getResource(assertion.getSubject()),
				createURI(assertion.getProperty().getPredicate().getName().toString()),
				getLiteral(assertion.getValue())
		);
	}

	private static Statement createStatement(AnnotationAssertion assertion) {
		Constant constant = assertion.getValue();

		if (constant instanceof ValueConstant) {
			return new StatementImpl(getResource(assertion.getSubject()),
					createURI(assertion.getProperty().getPredicate().getName().toString()),
					getLiteral((ValueConstant) constant));
		} else if (constant instanceof ObjectConstant)  {
			return new StatementImpl(getResource(assertion.getSubject()),
					createURI(assertion.getProperty().getPredicate().getName().toString()),
					getResource((ObjectConstant) constant));
		} else {
			throw new RuntimeException("Unsupported constant for an annotation property!"
					+ constant);
		}
	}

	private static Statement createStatement(ClassAssertion assertion) {
		return new StatementImpl(getResource(assertion.getIndividual()),
				createURI(OBDAVocabulary.RDF_TYPE),
				createURI(assertion.getConcept().getPredicate().getName().toString()));
	}
}
