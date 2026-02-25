package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.constraints.HomomorphismFactory;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.iq.impl.IQTreeTools;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
import it.unibz.inf.ontop.iq.tools.UnionBasedQueryMerger;
import it.unibz.inf.ontop.iq.transform.QueryRenamer;
import it.unibz.inf.ontop.iq.type.NotYetTypedBinaryMathOperationTransformer;
import it.unibz.inf.ontop.iq.type.NotYetTypedEqualityTransformer;
import it.unibz.inf.ontop.iq.type.PartiallyTypedSimpleCastTransformer;
import it.unibz.inf.ontop.iq.type.SingleTermTypeExtractor;
import it.unibz.inf.ontop.model.atom.AtomFactory;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.db.DBFunctionSymbolFactory;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.substitution.SubstitutionFactory;
import it.unibz.inf.ontop.utils.CoreUtilsFactory;

@Singleton
public class CoreSingletonsImpl implements CoreSingletons {

    private final TermFactory termFactory;
    private final TypeFactory typeFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final DBFunctionSymbolFactory dbFunctionsymbolFactory;
    private final AtomFactory atomFactory;
    private final SubstitutionFactory substitutionFactory;
    private final HomomorphismFactory homomorphismFactory;
    private final CoreUtilsFactory coreUtilsFactory;
    private final SingleTermTypeExtractor uniqueTermTypeExtractor;
    private final IntermediateQueryFactory iqFactory;
    private final IQTreeTools iqTreeTools;
    private final OntopModelSettings settings;
    private final ConstructionSubstitutionNormalizer constructionSubstitutionNormalizer;
    private final QueryRenamer queryRenamer;
    private final NotYetTypedEqualityTransformer notYetTypedEqualityTransformer;
    private final NotYetTypedBinaryMathOperationTransformer notYetTypedBinaryMathOperationTransformer;
    private final PartiallyTypedSimpleCastTransformer partiallyTypedSimpleCastTransformer;
    private final DatabaseInfoSupplier databaseInfoSupplier;
    private final UnionBasedQueryMerger unionBasedQueryMerger;

    @Inject
    private CoreSingletonsImpl(TermFactory termFactory, TypeFactory typeFactory,
                               FunctionSymbolFactory functionSymbolFactory,
                               DBFunctionSymbolFactory dbFunctionsymbolFactory, AtomFactory atomFactory,
                               SubstitutionFactory substitutionFactory, HomomorphismFactory homomorphismFactory,
                               CoreUtilsFactory coreUtilsFactory,
                               SingleTermTypeExtractor uniqueTermTypeExtractor,
                               IntermediateQueryFactory iqFactory,
                               IQTreeTools iqTreeTools,
                               OntopModelSettings settings,
                               ConstructionSubstitutionNormalizer constructionSubstitutionNormalizer,
                               QueryRenamer queryRenamer,
                               NotYetTypedEqualityTransformer notYetTypedEqualityTransformer,
                               NotYetTypedBinaryMathOperationTransformer notYetTypedBinaryMathOperationTransformer,
                               PartiallyTypedSimpleCastTransformer partiallyTypedSimpleCastTransformer,
                               DatabaseInfoSupplier databaseInfoSupplier, UnionBasedQueryMerger unionBasedQueryMerger) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.dbFunctionsymbolFactory = dbFunctionsymbolFactory;
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
        this.homomorphismFactory = homomorphismFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
        this.iqFactory = iqFactory;
        this.iqTreeTools = iqTreeTools;
        this.settings = settings;
        this.constructionSubstitutionNormalizer = constructionSubstitutionNormalizer;
        this.queryRenamer = queryRenamer;
        this.notYetTypedEqualityTransformer = notYetTypedEqualityTransformer;
        this.notYetTypedBinaryMathOperationTransformer = notYetTypedBinaryMathOperationTransformer;
        this.partiallyTypedSimpleCastTransformer = partiallyTypedSimpleCastTransformer;
        this.databaseInfoSupplier = databaseInfoSupplier;
        this.unionBasedQueryMerger = unionBasedQueryMerger;
    }

    @Override
    public TermFactory getTermFactory() {
        return termFactory;
    }

    @Override
    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    @Override
    public FunctionSymbolFactory getFunctionSymbolFactory() {
        return functionSymbolFactory;
    }

    @Override
    public DBFunctionSymbolFactory getDBFunctionsymbolFactory() {
        return dbFunctionsymbolFactory;
    }

    @Override
    public AtomFactory getAtomFactory() {
        return atomFactory;
    }

    @Override
    public SubstitutionFactory getSubstitutionFactory() {
        return substitutionFactory;
    }

    @Override
    public HomomorphismFactory getHomomorphismFactory() {
        return homomorphismFactory;
    }

    @Override
    public IntermediateQueryFactory getIQFactory() {
        return iqFactory;
    }

    @Override
    public IQTreeTools getIQTreeTools() {
        return iqTreeTools;
    }

    @Override
    public CoreUtilsFactory getCoreUtilsFactory() {
        return coreUtilsFactory;
    }

    @Override
    public QueryRenamer getQueryRenamer() {
        return queryRenamer;
    }

    @Override
    public SingleTermTypeExtractor getUniqueTermTypeExtractor() {
        return uniqueTermTypeExtractor;
    }

    @Override
    public OntopModelSettings getSettings() {
        return settings;
    }

    @Override
    public ConstructionSubstitutionNormalizer getConstructionSubstitutionNormalizer() {
        return constructionSubstitutionNormalizer;
    }

    @Override
    public NotYetTypedEqualityTransformer getNotYetTypedEqualityTransformer() {
        return notYetTypedEqualityTransformer;
    }

    @Override
    public NotYetTypedBinaryMathOperationTransformer getNotYetTypedBinaryMathOperationTransformer() {
        return notYetTypedBinaryMathOperationTransformer;
    }

    @Override
    public PartiallyTypedSimpleCastTransformer getPartiallyTypeSimpleCastTransformer() {
        return partiallyTypedSimpleCastTransformer;
    }

    @Override
    public DatabaseInfoSupplier getDatabaseInfoSupplier() {
        return databaseInfoSupplier;
    }

    @Override
    public UnionBasedQueryMerger getUnionBasedQueryMerger() {
        return unionBasedQueryMerger;
    }
}
