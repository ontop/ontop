package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.io.PrefixManager;

public interface TargetQueryParser {
	
	public void setPrefixManager(PrefixManager manager);
	
	public CQIE parse(String input) throws TargetQueryParserException;
}
