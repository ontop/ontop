package it.unibz.inf.ontop.si.repository.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.model.term.*;
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

import java.util.function.BiFunction;
import java.util.stream.Stream;

public class MappingProvider {

    public static final ImmutableList<String> MAPPING_VARIBLES = ImmutableList.of("X", "Y");

    private final static Logger LOGGER = LoggerFactory.getLogger(MappingProvider.class);
    private final TermFactory termFactory;
    private final TargetAtomFactory targetAtomFactory;
    private final SQLPPSourceQueryFactory sourceQueryFactory;

    public MappingProvider(LoadingConfiguration loadingConfiguration) {
        this.termFactory = loadingConfiguration.getTermFactory();
        this.targetAtomFactory = loadingConfiguration.getTargetAtomFactory();
        this.sourceQueryFactory = loadingConfiguration.getSourceQueryFactory();
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
        return views.getViewsStream()
                .filter(v -> !v.isEmptyForIntervals(range.getIntervals()))
                .map(v -> {
                    SQLPPSourceQuery sourceQuery = sourceQueryFactory.createSourceQuery(v.getSELECT(range));
                    TargetAtom targetAtom = transformer.apply(iri, v);
                    return new OntopNativeSQLPPTriplesMap(
                            IDGenerator.getNextUniqueID("MAPID-"), sourceQuery, ImmutableList.of(targetAtom));
                });
    }

    private ImmutableFunctionalTerm getTerm(RDFTermType type, Variable var) {
        if (type instanceof ObjectRDFType) {
            return termFactory.getRDFFunctionalTerm(var, termFactory.getRDFTermTypeConstant(type));
        }
        else if (type instanceof RDFDatatype) {
            RDFDatatype datatype = (RDFDatatype) type;
            if (datatype.getLanguageTag().isPresent()) {
                LanguageTag languageTag = datatype.getLanguageTag().get();
                return termFactory.getRDFLiteralFunctionalTerm(var, languageTag.getFullString());
            }
            else {
                return termFactory.getRDFLiteralFunctionalTerm(var, datatype);
            }
        }
        else
            throw new MinorOntopInternalBugException("Unexpected type" + type);
    }

    private TargetAtom constructClassTargetQuery(IRI iri, RepositoryTableSlice view) {
        Variable X = termFactory.getVariable(MAPPING_VARIBLES.get(0));
        ImmutableFunctionalTerm subjectTerm = getTerm(view.getId().get(0), X);
        ImmutableTerm predTerm = termFactory.getConstantIRI(RDF.TYPE);
        IRIConstant classTerm = termFactory.getConstantIRI(iri);
        return targetAtomFactory.getTripleTargetAtom(subjectTerm, predTerm, classTerm);
    }

    private TargetAtom constructPropertyTargetQuery(IRI iri, RepositoryTableSlice view) {
        Variable X = termFactory.getVariable(MAPPING_VARIBLES.get(0));
        ImmutableFunctionalTerm subjectTerm = getTerm(view.getId().get(0), X);
        IRIConstant iriTerm = termFactory.getConstantIRI(iri);
        Variable Y = termFactory.getVariable(MAPPING_VARIBLES.get(1));
        ImmutableFunctionalTerm objectTerm = getTerm(view.getId().get(1), Y);
        return targetAtomFactory.getTripleTargetAtom(subjectTerm, iriTerm, objectTerm);
    }
}
