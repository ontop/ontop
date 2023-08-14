package it.unibz.inf.ontop.model.term.functionsymbol.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import it.unibz.inf.ontop.iq.node.VariableNullability;
import it.unibz.inf.ontop.iq.request.DefinitionPushDownRequest;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.InequalityLabel;
import it.unibz.inf.ontop.model.type.ConcreteNumericRDFDatatype;
import it.unibz.inf.ontop.model.type.DBTermType;
import it.unibz.inf.ontop.model.type.RDFTermType;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.AGG;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.substitution.Substitution;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import it.unibz.inf.ontop.utils.VariableGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public class StdevSPARQLFunctionSymbolImpl extends AbstractStatisticalSPARQLAggregationFunctionSymbolImpl {

    private static final String DEFAULT_AGG_VAR_NAME = "stdev1";

    protected StdevSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isPop, boolean isDistinct) {
        this(rootRdfTermType, isPop, isDistinct, false);
    }

    protected StdevSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isDistinct) {
        this(rootRdfTermType, false, isDistinct, true);
    }

    private StdevSPARQLFunctionSymbolImpl(RDFTermType rootRdfTermType, boolean isPop, boolean isDistinct, boolean shortName) {
        super(DEFAULT_AGG_VAR_NAME, "SP_STDEV", shortName ? AGG.STDEV.getIRIString() : (isPop ? AGG.STDEV_POP.getIRIString() : AGG.STDEV_SAMP.getIRIString()), rootRdfTermType, isDistinct, isPop, shortName,
            ((sparqlFunction, termFactory, dbTerm, dbType) -> termFactory.getDBStdev(dbTerm, dbType, sparqlFunction.isPop(), sparqlFunction.isDistinct())));
    }


}
