package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.impl.Int2IRIStringFunctionSymbolImpl;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.vocabulary.RDF;
import it.unibz.inf.ontop.si.impl.LoadingConfiguration;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQuery;
import it.unibz.inf.ontop.spec.mapping.SQLPPSourceQueryFactory;
import it.unibz.inf.ontop.spec.mapping.TargetAtom;
import it.unibz.inf.ontop.spec.mapping.TargetAtomFactory;
import it.unibz.inf.ontop.spec.mapping.pp.SQLPPTriplesMap;
import it.unibz.inf.ontop.spec.mapping.pp.impl.OntopNativeSQLPPTriplesMap;
import it.unibz.inf.ontop.spec.ontology.ClassifiedTBox;
import it.unibz.inf.ontop.spec.ontology.Equivalences;
import it.unibz.inf.ontop.spec.ontology.OClass;
import it.unibz.inf.ontop.utils.IDGenerator;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MappingProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(MappingProvider.class);
    private final TermFactory termFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final FunctionSymbol int2IRIStringFunctionSymbol;
    private final RDFTermTypeConstant iriTypeConstant;
    private final SQLPPSourceQueryFactory sourceQueryFactory;

    public MappingProvider(IRIDictionaryImpl uriMap, LoadingConfiguration loadingConfiguration) {
        this.termFactory = loadingConfiguration.getTermFactory();
        this.targetAtomFactory = loadingConfiguration.getTargetAtomFactory();
        this.sourceQueryFactory = loadingConfiguration.getSourceQueryFactory();

        TypeFactory typeFactory = loadingConfiguration.getTypeFactory();
        DBTypeFactory dbTypeFactory = typeFactory.getDBTypeFactory();
        int2IRIStringFunctionSymbol = new Int2IRIStringFunctionSymbolImpl(
                dbTypeFactory.getDBTermType("INTEGER"), dbTypeFactory.getDBStringType(), uriMap);
        iriTypeConstant = termFactory.getRDFTermTypeConstant(typeFactory.getIRITermType());
    }

    public ImmutableList<SQLPPTriplesMap> getMappings(ClassifiedTBox tbox, SemanticIndex semanticIndex, RepositoryTableManager views) {

        ImmutableList<SQLPPTriplesMap> result = Stream.concat(Stream.concat(
                                tbox.objectPropertiesDAG().stream()
                                        .map(Equivalences::getRepresentative)
                                        .filter(ope -> !ope.isInverse())
                                        .filter(ope -> tbox.objectProperties().contains(ope.getIRI())) 	// no mappings for auxiliary roles, which are introduced by the ontology translation process
                                        .flatMap(ope -> getTripleMaps(views, semanticIndex.getRange(ope), ope.getIRI(), this::constructPropertyTargetQuery)),

                                tbox.dataPropertiesDAG().stream()
                                        .map(Equivalences::getRepresentative)
                                        .filter(dpe -> tbox.dataProperties().contains(dpe.getIRI())) 	// no mappings for auxiliary roles, which are introduced by the ontology translation process
                                        .flatMap(dpe -> getTripleMaps(views, semanticIndex.getRange(dpe), dpe.getIRI(), this::constructPropertyTargetQuery))),

                        tbox.classesDAG().stream()
                                .map(Equivalences::getRepresentative)
                                .filter(cle -> cle instanceof OClass) // only named classes are mapped
                                .map(cle -> (OClass)cle)
                                .flatMap(cls -> getTripleMaps(views, semanticIndex.getRange(cls), cls.getIRI(), this::constructClassTargetQuery)))

                .collect(ImmutableCollectors.toList());

        LOGGER.debug("Total: {} mappings", result.size());
        return result;
    }

    private Stream<SQLPPTriplesMap> getTripleMaps(RepositoryTableManager views, SemanticIndexRange range, IRI iri, BiFunction<IRI, RepositoryTableSlice, TargetAtom> transformer) {
        List<Interval> intervals = range.getIntervals();
        String filter = intervals.stream()
                .map(MappingProvider::getIntervalString)
                .collect(Collectors.joining(" OR "));

        return views.getViewsStream()
                .filter(v -> !v.isEmptyForIntervals(intervals))
                .map(v -> {
                    SQLPPSourceQuery sourceQuery = sourceQueryFactory.createSourceQuery(v.getSELECT(filter));
                    TargetAtom targetAtom = transformer.apply(iri, v);
                    return new OntopNativeSQLPPTriplesMap(
                            IDGenerator.getNextUniqueID("MAPID-"), sourceQuery, ImmutableList.of(targetAtom));
                });
    }

    private static String getIntervalString(Interval interval) {
        if (interval.getStart() == interval.getEnd())
            return String.format("%s = %d", RepositoryTableManager.IDX_COLUMN, interval.getStart());
        else
            return String.format("%s >= %d AND %s <= %d", RepositoryTableManager.IDX_COLUMN, interval.getStart(),
                    RepositoryTableManager.IDX_COLUMN, interval.getEnd());
    }

    private ImmutableFunctionalTerm getTerm(ObjectRDFType type, Variable var) {
        if (!type.isBlankNode()) {
            ImmutableFunctionalTerm lexicalValue = termFactory.getImmutableFunctionalTerm(
                    int2IRIStringFunctionSymbol, var);
            return termFactory.getRDFFunctionalTerm(lexicalValue, iriTypeConstant);
        }
        else {
            return termFactory.getRDFFunctionalTerm(var, termFactory.getRDFTermTypeConstant(type));
        }
    }

    private TargetAtom constructClassTargetQuery(IRI iri, RepositoryTableSlice view) {
        Variable X = termFactory.getVariable("X");

        ImmutableFunctionalTerm subjectTerm = getTerm(view.getId().getType1(), X);
        ImmutableTerm predTerm = termFactory.getConstantIRI(RDF.TYPE);
        IRIConstant classTerm = termFactory.getConstantIRI(iri);

        return targetAtomFactory.getTripleTargetAtom(subjectTerm, predTerm, classTerm);
    }

    private TargetAtom constructPropertyTargetQuery(IRI iri, RepositoryTableSlice view) {
        Variable X = termFactory.getVariable("X");
        Variable Y = termFactory.getVariable("Y");

        ImmutableFunctionalTerm subjectTerm = getTerm(view.getId().getType1(), X);
        IRIConstant iriTerm = termFactory.getConstantIRI(iri);

        RDFTermType type2 = view.getId().getType2();
        final ImmutableFunctionalTerm objectTerm;
        if (type2 instanceof ObjectRDFType) {
            objectTerm = getTerm((ObjectRDFType)type2, Y);
        }
        else {
            RDFDatatype datatype = (RDFDatatype) type2;
            if (datatype.getLanguageTag().isPresent()) {
                LanguageTag languageTag = datatype.getLanguageTag().get();
                objectTerm = termFactory.getRDFLiteralFunctionalTerm(Y, languageTag.getFullString());
            }
            else {
                objectTerm = termFactory.getRDFLiteralFunctionalTerm(Y, datatype);
            }
        }

        return targetAtomFactory.getTripleTargetAtom(subjectTerm, iriTerm, objectTerm);
    }
}
