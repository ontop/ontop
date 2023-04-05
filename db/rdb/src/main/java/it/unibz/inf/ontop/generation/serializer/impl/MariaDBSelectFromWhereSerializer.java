package it.unibz.inf.ontop.generation.serializer.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DBParameters;
import it.unibz.inf.ontop.dbschema.QualifiedAttributeID;
import it.unibz.inf.ontop.generation.algebra.SQLFlattenExpression;
import it.unibz.inf.ontop.generation.algebra.SQLOrderComparator;
import it.unibz.inf.ontop.generation.algebra.SelectFromWhereWithModifiers;
import it.unibz.inf.ontop.generation.serializer.SelectFromWhereSerializer;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.StringUtils;

import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class MariaDBSelectFromWhereSerializer extends MySQLSelectFromWhereSerializer {

    @Inject
    private MariaDBSelectFromWhereSerializer(TermFactory termFactory) {
        super(termFactory);
    }

    @Override
    protected String getFlattenFunctionFormat() {
        /*
         *   MariaDB does not require the same workaround as MySQL, so we can just call JSON_TABLE on the
         *   array directly.
         */
        return "%s CROSS JOIN JSON_TABLE(%s, '$[*]' columns(%s JSON path '$'";
    }
}
