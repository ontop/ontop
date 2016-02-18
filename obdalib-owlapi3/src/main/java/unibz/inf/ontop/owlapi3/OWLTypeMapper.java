package unibz.inf.ontop.owlapi3;

import java.util.HashMap;
import java.util.Map;

import unibz.inf.ontop.model.Predicate;
import unibz.inf.ontop.owlapi3.OWLAPI3TranslatorBase.TranslationException;

import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

public class OWLTypeMapper {

	
	private static final Map<OWL2Datatype, Predicate.COL_TYPE> OWLtoCOLTYPE = new HashMap<>();
	private static final Map<Predicate.COL_TYPE, OWL2Datatype> COLTYPEtoOWL = new HashMap<>();
	
	static {
		registerType(OWL2Datatype.RDF_PLAIN_LITERAL, Predicate.COL_TYPE.LITERAL);
		registerType(OWL2Datatype.XSD_DECIMAL, Predicate.COL_TYPE.DECIMAL);   // 1
		registerType(OWL2Datatype.XSD_STRING, Predicate.COL_TYPE.STRING);     // 2
		registerType(OWL2Datatype.XSD_INTEGER, Predicate.COL_TYPE.INTEGER);   // 3
		registerType(OWL2Datatype.XSD_NON_NEGATIVE_INTEGER, Predicate.COL_TYPE.NON_NEGATIVE_INTEGER);  // 4
		registerType(OWL2Datatype.XSD_DATE_TIME, Predicate.COL_TYPE.DATETIME);  // 5
		registerType(OWL2Datatype.XSD_DATE_TIME_STAMP, Predicate.COL_TYPE.DATETIME_STAMP);  // 15
		
		// not OWL 2 QL types
		registerType(OWL2Datatype.XSD_INT, Predicate.COL_TYPE.INT); // 6
		registerType(OWL2Datatype.XSD_POSITIVE_INTEGER, Predicate.COL_TYPE.POSITIVE_INTEGER); // 7
		registerType(OWL2Datatype.XSD_NEGATIVE_INTEGER, Predicate.COL_TYPE.NEGATIVE_INTEGER); // 8
		registerType(OWL2Datatype.XSD_NON_POSITIVE_INTEGER, Predicate.COL_TYPE.NON_POSITIVE_INTEGER); // 9
		registerType(OWL2Datatype.XSD_UNSIGNED_INT, Predicate.COL_TYPE.UNSIGNED_INT); // 10
		registerType(OWL2Datatype.XSD_DOUBLE, Predicate.COL_TYPE.DOUBLE); // 11
		// registerType(OWL2Datatype.XSD_FLOAT, COL_TYPE.DOUBLE); // 12 // TEMPORARY!!
		registerType(OWL2Datatype.XSD_LONG, Predicate.COL_TYPE.LONG); // 13
		registerType(OWL2Datatype.XSD_BOOLEAN, Predicate.COL_TYPE.BOOLEAN); // 14
	
		
		// irregularities
		OWLtoCOLTYPE.put(OWL2Datatype.XSD_FLOAT, Predicate.COL_TYPE.DOUBLE); // 12 // TEMPORARY!!
		OWLtoCOLTYPE.put(OWL2Datatype.RDFS_LITERAL, Predicate.COL_TYPE.LITERAL);

		COLTYPEtoOWL.put(Predicate.COL_TYPE.DATE, OWL2Datatype.RDF_PLAIN_LITERAL);  // not XSD_DATE;
		COLTYPEtoOWL.put(Predicate.COL_TYPE.TIME, OWL2Datatype.RDF_PLAIN_LITERAL);
		COLTYPEtoOWL.put(Predicate.COL_TYPE.YEAR, OWL2Datatype.RDF_PLAIN_LITERAL);
	}
	
	private static void registerType(OWL2Datatype owlType, Predicate.COL_TYPE type) {
		OWLtoCOLTYPE.put(owlType, type); 
		COLTYPEtoOWL.put(type, owlType);  	
	}
	
	// OWLAPI3TranslatorDLLiteA only
	public static Predicate.COL_TYPE getType(OWLDatatype datatype) throws TranslationException {
		if (datatype == null) 
			return Predicate.COL_TYPE.LITERAL;
		
		Predicate.COL_TYPE type = OWLtoCOLTYPE.get(datatype.getBuiltInDatatype());
		if (type == null)
			throw new TranslationException("Unsupported data range: " + datatype);
		return type;
	}
	
	// OWLAPI3IndividualTranslator only
	public static OWL2Datatype getOWLType(Predicate.COL_TYPE type) {
		return COLTYPEtoOWL.get(type);
	}	
	
}
