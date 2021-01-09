package it.unibz.inf.ontop.injection;

import it.unibz.inf.ontop.spec.mapping.PrefixManager;
import it.unibz.inf.ontop.spec.mapping.parser.TargetQueryParser;

public interface TargetQueryParserFactory {

    TargetQueryParser createParser(PrefixManager prefixManager);
}
