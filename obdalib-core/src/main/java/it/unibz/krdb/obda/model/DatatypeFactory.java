package it.unibz.krdb.obda.model;

import it.unibz.krdb.obda.model.Predicate.COL_TYPE;
import it.unibz.krdb.obda.model.impl.QuestTypeMapper;
import it.unibz.krdb.obda.utils.JdbcTypeMapper;

public interface DatatypeFactory {

	
	public COL_TYPE getDataType(String uri);
	
	public String getDataTypeURI(COL_TYPE type);

	
	public Predicate getTypePredicate(COL_TYPE type);
	
		
	public boolean isBoolean(Predicate p);
	
	public boolean isInteger(Predicate p);
	
	public boolean isFloat(Predicate p);
	
	public boolean isLiteral(Predicate p);
	
	public boolean isString(Predicate p);

	
	public Predicate getDataTypePredicateLiteral();

	public Predicate getDataTypePredicateLiteralLang();

	public QuestTypeMapper getQuestTypeMapper();

	public JdbcTypeMapper getJdbcTypeMapper();

}
