package org.semanticweb.ontop.sesame;



import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.semanticweb.ontop.model.BNode;
import org.semanticweb.ontop.model.DatatypeFactory;
import org.semanticweb.ontop.model.ObjectConstant;
import org.semanticweb.ontop.model.URIConstant;
import org.semanticweb.ontop.model.ValueConstant;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.Predicate.COL_TYPE;

public class SesameHelper {

	private final ValueFactory fact = new ValueFactoryImpl();
	private final DatatypeFactory dtfac = OBDADataFactoryImpl.getInstance().getDatatypeFactory();
	
	public Resource getResource(ObjectConstant obj) {
		if (obj instanceof BNode)
			return fact.createBNode(((BNode)obj).getName());
		else if (obj instanceof URIConstant)
			return fact.createURI(((URIConstant)obj).getURI());
		else 
			throw new RuntimeException("Invalid constant as subject!" + obj);		
	}
	
	public Literal getLiteral(ValueConstant literal)
	{
		if ((literal.getType() == COL_TYPE.LITERAL) ||  (literal.getType() == COL_TYPE.LITERAL_LANG)) {
			Literal value = fact.createLiteral(literal.getValue(), literal.getLanguage());
			return value;
		}
		else if (literal.getType() == COL_TYPE.OBJECT) {
			Literal value = fact.createLiteral(literal.getValue(), dtfac.getDataTypeURI(COL_TYPE.STRING));
			return value;
		}	
		else {
			URI datatype = dtfac.getDataTypeURI(literal.getType());
			if (datatype == null)
				throw new RuntimeException("Found unknown TYPE for constant: " + literal + " with COL_TYPE="+ literal.getType());
			
			Literal value = fact.createLiteral(literal.getValue(), datatype);
			return value;
		}
	}

	public URI createURI(String uri) {
		return fact.createURI(uri);
	}


}
