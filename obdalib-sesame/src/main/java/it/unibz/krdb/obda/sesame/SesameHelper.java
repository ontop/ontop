package it.unibz.krdb.obda.sesame;

import it.unibz.krdb.obda.model.BNode;
import it.unibz.krdb.obda.model.*;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import org.openrdf.model.*;
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


}
