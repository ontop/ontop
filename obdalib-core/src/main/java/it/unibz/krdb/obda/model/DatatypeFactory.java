package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;

public interface DatatypeFactory {

	
	public COL_TYPE getDataType(String uri);
	
	public String getDataTypeURI(COL_TYPE type);

	
	public Predicate getTypePredicate(Predicate.COL_TYPE type);
	
		
	public boolean isBoolean(Predicate p);
	
	public boolean isInteger(Predicate p);
	
	public boolean isFloat(Predicate p);
	
	public boolean isLiteral(Predicate p);
	
	public boolean isString(Predicate p);

	
	public Predicate getDataTypePredicateLiteral();

	public Predicate getDataTypePredicateLiteralLang();

	public Predicate getDataTypePredicateString();

	public Predicate getDataTypePredicateInteger();

    public Predicate getDataTypePredicateNonNegativeInteger();

    public Predicate getDataTypePredicateInt();

    public Predicate getDataTypePredicatePositiveInteger();

    public Predicate getDataTypePredicateNegativeInteger();

    public Predicate getDataTypePredicateNonPositiveInteger();

    public Predicate getDataTypePredicateUnsignedInt();

    public Predicate getDataTypePredicateLong();

	public Predicate getDataTypePredicateDecimal();

	public Predicate getDataTypePredicateDouble();

    public Predicate getDataTypePredicateFloat();

    public Predicate getDataTypePredicateDateTime();

	public Predicate getDataTypePredicateBoolean();

	public Predicate getDataTypePredicateDate();
	
	public Predicate getDataTypePredicateTime();

	public Predicate getDataTypePredicateYear();

}
