package it.unibz.krdb.obda.owlapi3;

import java.util.HashMap;
import java.util.Map;

import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.owlapi3.OWLAPI3TranslatorBase.TranslationException;

import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class OWLTypeMapper {

	
	private static final Map<OWL2Datatype, COL_TYPE> OWLtoCOLTYPE = new HashMap<OWL2Datatype, COL_TYPE>();
	private static final Map<COL_TYPE, OWL2Datatype> COLTYPEtoOWL = new HashMap<COL_TYPE, OWL2Datatype>();
	
	static {
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_STRING, COL_TYPE.STRING);
		OWLtoCOLTYPE.put(OWL2Datatype.RDF_PLAIN_LITERAL, COL_TYPE.LITERAL);
		OWLtoCOLTYPE.put(OWL2Datatype.RDFS_LITERAL, COL_TYPE.LITERAL);		
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_DECIMAL, COL_TYPE.DECIMAL);
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_INTEGER, COL_TYPE.INTEGER);
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_NON_NEGATIVE_INTEGER, COL_TYPE.NON_NEGATIVE_INTEGER);
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_DATE_TIME, COL_TYPE.DATETIME);
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_DATE_TIME_STAMP, COL_TYPE.DATETIME);
		
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_INT, COL_TYPE.INT); // not QL
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_POSITIVE_INTEGER, COL_TYPE.POSITIVE_INTEGER); // not QL
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_NEGATIVE_INTEGER, COL_TYPE.NEGATIVE_INTEGER); // not QL
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_NON_POSITIVE_INTEGER, COL_TYPE.NON_POSITIVE_INTEGER); // not QL
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_UNSIGNED_INT, COL_TYPE.UNSIGNED_INT); // not QL
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_DOUBLE, COL_TYPE.DOUBLE); // not QL
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_FLOAT, COL_TYPE.FLOAT); // not QL
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_LONG, COL_TYPE.LONG); // not QL 
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_BOOLEAN, COL_TYPE.BOOLEAN); // not QL

	
		COLTYPEtoOWL.put(COL_TYPE.BOOLEAN, OWL2Datatype.XSD_BOOLEAN);
		COLTYPEtoOWL.put(COL_TYPE.DATETIME, OWL2Datatype.XSD_DATE_TIME);
		COLTYPEtoOWL.put(COL_TYPE.DATE, OWL2Datatype.RDF_PLAIN_LITERAL);  // not XSD_DATE;
		COLTYPEtoOWL.put(COL_TYPE.TIME, OWL2Datatype.RDF_PLAIN_LITERAL);
		COLTYPEtoOWL.put(COL_TYPE.YEAR, OWL2Datatype.RDF_PLAIN_LITERAL);
		COLTYPEtoOWL.put(COL_TYPE.DECIMAL, OWL2Datatype.XSD_DECIMAL);
		COLTYPEtoOWL.put(COL_TYPE.DOUBLE, OWL2Datatype.XSD_DOUBLE);
		COLTYPEtoOWL.put(COL_TYPE.INTEGER, OWL2Datatype.XSD_INTEGER);
		COLTYPEtoOWL.put(COL_TYPE.NEGATIVE_INTEGER, OWL2Datatype.XSD_NEGATIVE_INTEGER);
		COLTYPEtoOWL.put(COL_TYPE.NON_NEGATIVE_INTEGER, OWL2Datatype.XSD_NON_NEGATIVE_INTEGER);
		COLTYPEtoOWL.put(COL_TYPE.POSITIVE_INTEGER, OWL2Datatype.XSD_POSITIVE_INTEGER);
		COLTYPEtoOWL.put(COL_TYPE.NON_POSITIVE_INTEGER, OWL2Datatype.XSD_NON_POSITIVE_INTEGER);
		COLTYPEtoOWL.put(COL_TYPE.INT, OWL2Datatype.XSD_INT);
		COLTYPEtoOWL.put(COL_TYPE.UNSIGNED_INT, OWL2Datatype.XSD_UNSIGNED_INT);
		COLTYPEtoOWL.put(COL_TYPE.FLOAT, OWL2Datatype.XSD_FLOAT);
		COLTYPEtoOWL.put(COL_TYPE.LONG, OWL2Datatype.XSD_LONG);
		COLTYPEtoOWL.put(COL_TYPE.LITERAL, OWL2Datatype.RDF_PLAIN_LITERAL);
		COLTYPEtoOWL.put(COL_TYPE.STRING, OWL2Datatype.XSD_STRING);
	}
	
	public static Predicate.COL_TYPE getType(OWLDatatype datatype) throws TranslationException {
		if (datatype == null) 
			return COL_TYPE.LITERAL;
		
		COL_TYPE type = OWLtoCOLTYPE.get(datatype.getBuiltInDatatype());
		if (type == null)
			throw new TranslationException("Unsupported data range: " + datatype);
		return type;
	}
	
	public static OWL2Datatype getOWLType(COL_TYPE type) {
		return COLTYPEtoOWL.get(type);
	}	
	
}
