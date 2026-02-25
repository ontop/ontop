package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.injection.OntopSQLCoreSettings;
import it.unibz.inf.ontop.model.term.TermFactory;

@Singleton
public class MariaDBSelectFromWhereSerializer extends MySQLSelectFromWhereSerializer {

    @Inject
    private MariaDBSelectFromWhereSerializer(TermFactory termFactory, OntopSQLCoreSettings settings) {
        super(termFactory, settings);
    }

    @Override
    protected String getFlattenFunctionFormat() {
        /*
         *   MariaDB does not require the same workaround as MySQL, so we can just call JSON_TABLE on the
         *   array directly.
         */
        return "JSON_TABLE(%s, '$[*]' columns(%s JSON path '$' %s))";
    }
}
