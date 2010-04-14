package inf.unibz.it.ucq.typing;


import com.sun.msv.datatype.xsd.AnyURIType;
import com.sun.msv.datatype.xsd.Base64BinaryType;
import com.sun.msv.datatype.xsd.BooleanType;
import com.sun.msv.datatype.xsd.ByteType;
import com.sun.msv.datatype.xsd.DateTimeType;
import com.sun.msv.datatype.xsd.DateType;
import com.sun.msv.datatype.xsd.DoubleType;
import com.sun.msv.datatype.xsd.DurationType;
import com.sun.msv.datatype.xsd.FloatType;
import com.sun.msv.datatype.xsd.GDayType;
import com.sun.msv.datatype.xsd.GMonthDayType;
import com.sun.msv.datatype.xsd.GMonthType;
import com.sun.msv.datatype.xsd.GYearMonthType;
import com.sun.msv.datatype.xsd.GYearType;
import com.sun.msv.datatype.xsd.HexBinaryType;
import com.sun.msv.datatype.xsd.IntType;
import com.sun.msv.datatype.xsd.IntegerType;
import com.sun.msv.datatype.xsd.LanguageType;
import com.sun.msv.datatype.xsd.LongType;
import com.sun.msv.datatype.xsd.NameType;
import com.sun.msv.datatype.xsd.NcnameType;
import com.sun.msv.datatype.xsd.NegativeIntegerType;
import com.sun.msv.datatype.xsd.NonNegativeIntegerType;
import com.sun.msv.datatype.xsd.NonPositiveIntegerType;
import com.sun.msv.datatype.xsd.NormalizedStringType;
import com.sun.msv.datatype.xsd.NumberType;
import com.sun.msv.datatype.xsd.PositiveIntegerType;
import com.sun.msv.datatype.xsd.QnameType;
import com.sun.msv.datatype.xsd.ShortType;
import com.sun.msv.datatype.xsd.SimpleURType;
import com.sun.msv.datatype.xsd.StringType;
import com.sun.msv.datatype.xsd.TimeType;
import com.sun.msv.datatype.xsd.UnsignedByteType;
import com.sun.msv.datatype.xsd.UnsignedIntType;
import com.sun.msv.datatype.xsd.UnsignedLongType;
import com.sun.msv.datatype.xsd.UnsignedShortType;
import com.sun.msv.datatype.xsd.XSDatatype;


/**
 * A controller class for XSDatatypes defined in the xsldlib.jar  
 * 
 * @author Manfred Gerstgrasser
 * 		   KRDB Research Center, Free University of Bolzano/Bozen, Italy 
 *
 */

public class XSDTypingController {
	
	private final String XSDINTEGER = "xsd:integer";
	private final String XSDDECIMAL = "xsd:decimal";
	private final String XSDFLOAT = "xsd:float";
	private final String XSDDOUBLE = "xsd:double";
	private final String XSDSTRING = "xsd:string";
	private final String XSDBOOLEAN = "xsd:boolean";
	private final String XSDDATETIME = "xsd:dateTime";
	private final String XSDLONG = "xsd:long";
	private final String XSDSHORT = "xsd:short";
	private final String XSDBYTE = "xsd:byte";
	private final String XSDINT = "xsd:int";
	private final String XSDNONPOSITIVEINTEGER = "xsd:nonPositiveInteger";
	private final String XSDPOSITIVEINTEGER = "xsd:positiveInteger";
	private final String XSDNEGATIVEINTEGER = "xsd:negativeInteger";
	private final String XSDNONNEGATIVEINTEGER = "xsd:nonNegativeInteger";
	private final String XSDUNSIGNEDLONG = "xsd:unsignedLong";
	private final String XSDUNSIGNEDSHORT = "xsd:unsignedShort";
	private final String XSDUNSIGNEDBYTE = "xsd:unsignedByte";
	private final String XSDUNSIGNEDINT = "xsd:unsignedInt";
	
	
	/**
	 * The current instance of the XSDTypingcontroller
	 */
	private static XSDTypingController instance = null;
	
	/**
	 * Private constructor creating a new instance of the XSDTyingController
	 * Please use the static method getInstance for getting the current
	 * instance of the XSDTypingController
	 */
	private XSDTypingController (){}
	
	/**
	 * Method which returns the current instance of the XSDTypingController.
	 * If there is not yet a running instance it creates one.
	 * @return the current instance of the XSDTypingController
	 */
	public static XSDTypingController getInstance(){
		if(instance == null){
			instance = new XSDTypingController();
		}
		return instance;
	}
	
		
	/**
	 * Method which returns the corresponding XSDataType for the given string.
	 * 
	 * @param xsdString the input string
	 * @return the corresponding XSDataType
	 * @throws Exception if the string does not correspond to a known pattern
	 */
	public XSDatatype getType(String xsdString) throws UnknownXSDTypeException{
		
		int aux = xsdString.indexOf(":");
		String type = xsdString.substring(aux+1).toLowerCase(); 
		if(type.equals("string")){
			return StringType.theInstance;
		}else if(type.equals("double")){
			return DoubleType.theInstance;
		}else if(type.equals("float")){
			return FloatType.theInstance;
		}else if(type.equals("integer")){
			return IntegerType.theInstance;
		}else if(type.equals("int")){
			return IntegerType.theInstance;
		}else if(type.equals("long")){
			return LongType.theInstance;
		}else if(type.equals("number")){
			return NumberType.theInstance;
		}else if(type.equals("short")){
			return ShortType.theInstance;
		}else if(type.equals("time")){
			return TimeType.theInstance;
		}else if(type.equals("anyURI")){
			return AnyURIType.theInstance;
		}else if(type.equals("base64binay")){
			return Base64BinaryType.theInstance;
		}else if(type.equals("byte")){
			return ByteType.theInstance;
		}else if(type.equals("boolean")){
			return BooleanType.theInstance;
		}else if(type.equals("dateTime")){
			return DateTimeType.theInstance;
		}else if(type.equals("date")){
			return DateType.theInstance;
		}else if(type.equals("duration")){
			return DurationType.theInstance;
		}else if(type.equals("gDay")){
			return GDayType.theInstance;
		}else if(type.equals("gMonth")){
			return GMonthType.theInstance;
		}else if(type.equals("gMonthDay")){
			return GMonthDayType.theInstance;
		}else if(type.equals("gYearMonth")){
			return GYearMonthType.theInstance;
		}else if(type.equals("gYear")){
			return GYearType.theInstance;
		}else if(type.equals("hexBinary")){
			return HexBinaryType.theInstance;
		}else if(type.equals("language")){
			return LanguageType.theInstance;
		}else if(type.equals("name")){
			return NameType.theInstance;
		}else if(type.equals("ncname")){
			return NcnameType.theInstance;
		}else if(type.equals("negativeInteger")){
			return NegativeIntegerType.theInstance;
		}else if(type.equals("nonnegativeInteger")){
			return NonNegativeIntegerType.theInstance;
		}else if(type.equals("nonpositiveInteger")){
			return NonPositiveIntegerType.theInstance;
		}else if(type.equals("normalizedString")){
			return NormalizedStringType.theInstance;
		}else if(type.equals("positiveInteger")){
			return PositiveIntegerType.theInstance;
		}else if(type.equals("qname")){
			return QnameType.theInstance;
		}else if(type.equals("negativeInteger")){
			return NegativeIntegerType.theInstance;
		}else if(type.equals("simpleUr")){
			return SimpleURType.theInstance;
		}else if(type.equals("usignedByte")){
			return UnsignedByteType.theInstance;
		}else if(type.equals("usignedInteger")){
			return UnsignedIntType.theInstance;
		}else if(type.equals("usignedLong")){
			return UnsignedLongType.theInstance;
		}else if(type.equals("usignedShort")){
			return UnsignedShortType.theInstance;
		}else {
			throw new UnknownXSDTypeException("ERROR: unknow type specification. Recived string was: " + xsdString);
		}
	}
	
	public boolean isNumericType(XSDatatype type){
		
		if(type.isDerivedTypeOf(IntegerType.theInstance, false)){
			return true;
		}else if(type == DoubleType.theInstance){
			return true;
		}else if(type == FloatType.theInstance){
			return true;
		}else if(type == ByteType.theInstance){
			return true;
		}else if(type == NumberType.theInstance){
			return true;
		}else if(type.isDerivedTypeOf(IntType.theInstance, false)){
			return true;
		}else if(type.isDerivedTypeOf(LongType.theInstance, false)){
			return true;
		}else if(type.isDerivedTypeOf(ShortType.theInstance, false)){
			return true;
		}else{
			return false;
		}
	}

	
	public boolean isNumericType(String type) throws UnknownXSDTypeException{
		
		if(type.equals(XSDBOOLEAN)){
			return false;
		}else if(type.equals(XSDBYTE)){
			return true;
		}else if(type.equals(XSDDATETIME)){
			return false;
		}else if(type.equals(XSDDECIMAL)){
			return true;
		}else if(type.equals(XSDDOUBLE)){
			return true;
		}else if(type.equals(XSDFLOAT)){
			return true;
		}else if(type.equals(XSDINT)){
			return true;
		}else if(type.equals(XSDINTEGER)){
			return true;
		}else if(type.equals(XSDLONG)){
			return true;
		}else if(type.equals(XSDNEGATIVEINTEGER)){
			return true;
		}else if(type.equals(XSDNONNEGATIVEINTEGER)){
			return true;
		}else if(type.equals(XSDNONPOSITIVEINTEGER)){
			return true;
		}else if(type.equals(XSDPOSITIVEINTEGER)){
			return true;
		}else if(type.equals(XSDSHORT)){
			return true;
		}else if(type.equals(XSDSTRING)){
			return false;
		}else if(type.equals(XSDUNSIGNEDBYTE)){
			return true;
		}else if(type.equals(XSDUNSIGNEDINT)){
			return true;
		}else if(type.equals(XSDUNSIGNEDLONG)){
			return true;
		}else if(type.equals(XSDUNSIGNEDSHORT)){
			return true;
		}else{
			throw new UnknownXSDTypeException("Unknown type: "+ type);
		}
	}
}
