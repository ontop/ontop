package it.unibz.inf.ontop.injection.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import it.unibz.inf.ontop.dbschema.DatabaseInfoSupplier;
import it.unibz.inf.ontop.injection.CoreSingletons;
import it.unibz.inf.ontop.injection.IntermediateQueryFactory;
import it.unibz.inf.ontop.injection.OntopModelSettings;
import it.unibz.inf.ontop.injection.QueryTransformerFactory;
import it.unibz.inf.ontop.iq.node.normalization.ConstructionSubstitutionNormalizer;
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
    private final CoreUtilsFactory coreUtilsFactory;
    private final SingleTermTypeExtractor uniqueTermTypeExtractor;
    private final IntermediateQueryFactory iqFactory;
    private final OntopModelSettings settings;
    private final ConstructionSubstitutionNormalizer constructionSubstitutionNormalizer;
    private final QueryTransformerFactory queryTransformerFactory;
    private final NotYetTypedEqualityTransformer notYetTypedEqualityTransformer;
    private final NotYetTypedBinaryMathOperationTransformer notYetTypedBinaryMathOperationTransformer;
    private final PartiallyTypedSimpleCastTransformer partiallyTypedSimpleCastTransformer;
    private final DatabaseInfoSupplier databaseInfoSupplier;

    @Inject
    private CoreSingletonsImpl(TermFactory termFactory, TypeFactory typeFactory,
                               FunctionSymbolFactory functionSymbolFactory,
                               DBFunctionSymbolFactory dbFunctionsymbolFactory, AtomFactory atomFactory,
                               SubstitutionFactory substitutionFactory, CoreUtilsFactory coreUtilsFactory,
                               SingleTermTypeExtractor uniqueTermTypeExtractor,
                               IntermediateQueryFactory iqFactory,
                               OntopModelSettings settings,
                               ConstructionSubstitutionNormalizer constructionSubstitutionNormalizer,
                               QueryTransformerFactory queryTransformerFactory,
                               NotYetTypedEqualityTransformer notYetTypedEqualityTransformer,
                               NotYetTypedBinaryMathOperationTransformer notYetTypedBinaryMathOperationTransformer,
                               PartiallyTypedSimpleCastTransformer partiallyTypedSimpleCastTransformer,
                               DatabaseInfoSupplier databaseInfoSupplier) {
        this.termFactory = termFactory;
        this.typeFactory = typeFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.dbFunctionsymbolFactory = dbFunctionsymbolFactory;
        this.atomFactory = atomFactory;
        this.substitutionFactory = substitutionFactory;
        this.coreUtilsFactory = coreUtilsFactory;
        this.uniqueTermTypeExtractor = uniqueTermTypeExtractor;
        this.iqFactory = iqFactory;
        this.settings = settings;
        this.constructionSubstitutionNormalizer = constructionSubstitutionNormalizer;
        this.queryTransformerFactory = queryTransformerFactory;
        this.notYetTypedEqualityTransformer = notYetTypedEqualityTransformer;
        this.notYetTypedBinaryMathOperationTransformer = notYetTypedBinaryMathOperationTransformer;
        this.partiallyTypedSimpleCastTransformer = partiallyTypedSimpleCastTransformer;
        this.databaseInfoSupplier = databaseInfoSupplier;
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
    public IntermediateQueryFactory getIQFactory() {
        return iqFactory;
    }

    @Override
    public CoreUtilsFactory getCoreUtilsFactory() {
        return coreUtilsFactory;
    }

    @Override
    public QueryTransformerFactory getQueryTransformerFactory() {
        return queryTransformerFactory;
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
}
