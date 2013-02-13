package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.io.PrefixManager;
import it.unibz.krdb.obda.model.CQIE;

public interface TargetQueryParser {
	void setPrefixManager(PrefixManager manager);
	
	CQIE parse(String input) throws TargetQueryParserException;
}
