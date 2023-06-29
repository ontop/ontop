package it.unibz.inf.ontop.iq.optimizer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.iq.IQ;
import it.unibz.inf.ontop.iq.IQTree;
import it.unibz.inf.ontop.iq.UnaryIQTree;
import it.unibz.inf.ontop.iq.node.*;
import it.unibz.inf.ontop.model.atom.DistinctVariableOnlyDataAtom;
import it.unibz.inf.ontop.model.term.ImmutableTerm;
import it.unibz.inf.ontop.model.term.TermFactory;
import it.unibz.inf.ontop.model.term.Variable;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.db.impl.AbstractTypedDBFunctionSymbol;
import it.unibz.inf.ontop.model.type.*;
import it.unibz.inf.ontop.model.type.impl.DBTermTypeImpl;
import it.unibz.inf.ontop.substitution.Substitution;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

import static it.unibz.inf.ontop.OptimizationTestingTools.*;
import static org.junit.Assert.assertEquals;

public class PreventDistinctTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreventDistinctTest.class);
    public final static RelationDefinition T1_AR2;

    private final static DBTermType ALLOW_DISTINCT_TYPE;
    private final static DBTermType PREVENT_DISTINCT_TYPE;

    private final static FunctionSymbol KEEP_PREVENT_FUNCTION;
    private final static FunctionSymbol SANITIZE_FUNCTION;
    private final static FunctionSymbol UNARY_FUNCTION;
    private final static FunctionSymbol BINARY_FUNCTION;
    private final static FunctionSymbol UNSAFE_BINARY_FUNCTION;

    private final static Variable PUSH_PREVENT_DISTINCT;
    private final static Variable PUSH_PREVENT_DISTINCT2;

    static {
        OfflineMetadataProviderBuilder3 builder = createMetadataProviderBuilder();
        T1_AR2 = builder.createRelationWithFD(1, 2, true);
        var ancestry = TYPE_FACTORY.getDBTypeFactory().getDBStringType().getAncestry();
        ALLOW_DISTINCT_TYPE = makeType("ALLOW_DISTINCT", ancestry, true);
        PREVENT_DISTINCT_TYPE = makeType("PREVENT_DISTINCT", ancestry, false);

        SANITIZE_FUNCTION = makeFunctionSymbol("SANITIZE", ImmutableList.of(PREVENT_DISTINCT_TYPE), ALLOW_DISTINCT_TYPE);
        KEEP_PREVENT_FUNCTION = makeFunctionSymbol("KEEP_PREVENT", ImmutableList.of(PREVENT_DISTINCT_TYPE), PREVENT_DISTINCT_TYPE);
        UNARY_FUNCTION = makeFunctionSymbol("UNARY", ImmutableList.of(ALLOW_DISTINCT_TYPE), ALLOW_DISTINCT_TYPE);
        BINARY_FUNCTION = makeFunctionSymbol("BINARY", ImmutableList.of(ALLOW_DISTINCT_TYPE, ALLOW_DISTINCT_TYPE), ALLOW_DISTINCT_TYPE);
        UNSAFE_BINARY_FUNCTION = makeFunctionSymbol("UNSAFE_BINARY", ImmutableList.of(PREVENT_DISTINCT_TYPE, PREVENT_DISTINCT_TYPE), ALLOW_DISTINCT_TYPE);

        PUSH_PREVENT_DISTINCT = TERM_FACTORY.getVariable("f0");
        PUSH_PREVENT_DISTINCT2 = TERM_FACTORY.getVariable("f1");
    }

    @Test
    public void testPreventDistinctDirect() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, A);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(T1_AR2, ImmutableMap.of(0, A, 1, B));
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        Substitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X,
                TERM_FACTORY.getImmutableFunctionalTerm(SANITIZE_FUNCTION, B));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), substitution);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        dataNode
                ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                tree);

        IQTree expectedResult = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), SUBSTITUTION_FACTORY.getSubstitution()),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getImmutableFunctionalTerm(
                                        SANITIZE_FUNCTION,
                                        B
                                ))),
                                dataNode
                        )
                )
        );

        optimizeAndCompare(initialQuery, IQ_FACTORY.createIQ(projectionAtom, expectedResult));
    }

    @Test
    public void testPreventDistinctMoreVariables() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR3_PREDICATE, X, A, Y);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(T1_AR2, ImmutableMap.of(0, A, 1, B));
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        Substitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X,
                TERM_FACTORY.getImmutableFunctionalTerm(SANITIZE_FUNCTION, B),
                Y, A);
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X, Y), substitution);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        dataNode
                ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                tree);

        IQTree expectedResult = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X, Y), SUBSTITUTION_FACTORY.getSubstitution(Y, A)),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getImmutableFunctionalTerm(
                                        SANITIZE_FUNCTION,
                                        B
                                ))),
                                dataNode
                        )
                )
        );

        optimizeAndCompare(initialQuery, IQ_FACTORY.createIQ(projectionAtom, expectedResult));
    }

    @Test
    public void testPreventDistinctIndirect() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, A);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(T1_AR2, ImmutableMap.of(0, A, 1, B));
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        Substitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getImmutableFunctionalTerm(UNARY_FUNCTION,
                            TERM_FACTORY.getImmutableFunctionalTerm(SANITIZE_FUNCTION, B)));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), substitution);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        dataNode
                ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                tree);

        IQTree expectedResult = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), SUBSTITUTION_FACTORY.getSubstitution(X,
                        TERM_FACTORY.getImmutableFunctionalTerm(
                                UNARY_FUNCTION,
                                PUSH_PREVENT_DISTINCT
                        ))),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, PUSH_PREVENT_DISTINCT), SUBSTITUTION_FACTORY.getSubstitution(PUSH_PREVENT_DISTINCT, TERM_FACTORY.getImmutableFunctionalTerm(
                                        SANITIZE_FUNCTION,
                                        B
                                ))),
                                dataNode
                        )
                )
        );

        optimizeAndCompare(initialQuery, IQ_FACTORY.createIQ(projectionAtom, expectedResult));
    }

    @Test
    public void testPreventMultiple() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, A);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(T1_AR2, ImmutableMap.of(0, A, 1, B));
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        Substitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X,
                TERM_FACTORY.getImmutableFunctionalTerm(UNSAFE_BINARY_FUNCTION,
                        TERM_FACTORY.getImmutableFunctionalTerm(KEEP_PREVENT_FUNCTION, B),
                        TERM_FACTORY.getImmutableFunctionalTerm(KEEP_PREVENT_FUNCTION, B)));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), substitution);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        dataNode
                ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                tree);

        IQTree expectedResult = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), SUBSTITUTION_FACTORY.getSubstitution()),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getImmutableFunctionalTerm(
                                        UNSAFE_BINARY_FUNCTION,
                                        TERM_FACTORY.getImmutableFunctionalTerm(KEEP_PREVENT_FUNCTION, B),
                                        TERM_FACTORY.getImmutableFunctionalTerm(KEEP_PREVENT_FUNCTION, B)
                                ))),
                                dataNode
                        )
                )
        );

        optimizeAndCompare(initialQuery, IQ_FACTORY.createIQ(projectionAtom, expectedResult));
    }

    @Test
    public void testPreventMultipleDifferent() {
        DistinctVariableOnlyDataAtom projectionAtom = ATOM_FACTORY.getDistinctVariableOnlyDataAtom(ANS1_AR2_PREDICATE, X, A);

        ExtensionalDataNode dataNode = IQ_FACTORY.createExtensionalDataNode(T1_AR2, ImmutableMap.of(0, A, 1, B));
        DistinctNode distinctNode = IQ_FACTORY.createDistinctNode();
        Substitution<ImmutableTerm> substitution = SUBSTITUTION_FACTORY.getSubstitution(X,
                TERM_FACTORY.getImmutableFunctionalTerm(BINARY_FUNCTION,
                        TERM_FACTORY.getImmutableFunctionalTerm(SANITIZE_FUNCTION, B),
                        TERM_FACTORY.getImmutableFunctionalTerm(SANITIZE_FUNCTION, B)));
        ConstructionNode constructionNode = IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), substitution);

        UnaryIQTree tree = IQ_FACTORY.createUnaryIQTree(
                constructionNode,
                IQ_FACTORY.createUnaryIQTree(
                        distinctNode,
                        dataNode
                ));

        IQ initialQuery = IQ_FACTORY.createIQ(
                projectionAtom,
                tree);

        IQTree expectedResult = IQ_FACTORY.createUnaryIQTree(
                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, X), SUBSTITUTION_FACTORY.getSubstitution(X, TERM_FACTORY.getImmutableFunctionalTerm(
                        BINARY_FUNCTION,
                        PUSH_PREVENT_DISTINCT,
                        PUSH_PREVENT_DISTINCT2
                ))),
                IQ_FACTORY.createUnaryIQTree(
                        IQ_FACTORY.createDistinctNode(),
                        IQ_FACTORY.createUnaryIQTree(
                                IQ_FACTORY.createConstructionNode(ImmutableSet.of(A, PUSH_PREVENT_DISTINCT, PUSH_PREVENT_DISTINCT2), SUBSTITUTION_FACTORY.getSubstitution(
                                        PUSH_PREVENT_DISTINCT, TERM_FACTORY.getImmutableFunctionalTerm(
                                            SANITIZE_FUNCTION,
                                            B
                                        ),
                                        PUSH_PREVENT_DISTINCT2, TERM_FACTORY.getImmutableFunctionalTerm(
                                                SANITIZE_FUNCTION,
                                                B
                                        )
                                )),
                                dataNode
                        )
                )
        );

        optimizeAndCompare(initialQuery, IQ_FACTORY.createIQ(projectionAtom, expectedResult));
    }

    private void optimizeAndCompare(IQ initialQuery, IQ expectedQuery) {
        IQ optimizedQuery = GENERAL_STRUCTURAL_AND_SEMANTIC_IQ_OPTIMIZER.optimize(initialQuery);
        LOGGER.debug("Initial query: {}", initialQuery);
        assertEquals(expectedQuery, optimizedQuery);
        LOGGER.debug("Optimized query: {}", optimizedQuery);
    }

    private static FunctionSymbol makeFunctionSymbol(String name, ImmutableList<TermType> types, DBTermType targetType) {
        return new AbstractTypedDBFunctionSymbol(name, types, targetType) {
            @Override
            protected boolean isAlwaysInjectiveInTheAbsenceOfNonInjectiveFunctionalTerms() {
                return true;
            }

            @Override
            public boolean canBePostProcessed(ImmutableList<? extends ImmutableTerm> arguments) {
                return false;
            }

            @Override
            public String getNativeDBString(ImmutableList<? extends ImmutableTerm> terms, Function<ImmutableTerm, String> termConverter, TermFactory termFactory) {
                return name;
            }
        };
    }

    private static DBTermType makeType(String name, TermTypeAncestry ancestry, boolean allowDistinct) {
        return new DBTermTypeImpl(name, ancestry, false, DBTermType.Category.OTHER) {

            @Override
            public boolean isPreventDistinctRecommended() {
                return !allowDistinct;
            }

            @Override
            public Optional<RDFDatatype> getNaturalRDFDatatype() {
                return Optional.empty();
            }

            @Override
            public boolean isNeedingIRISafeEncoding() {
                return false;
            }

            @Override
            public boolean areEqualitiesStrict() {
                return false;
            }

            @Override
            public Optional<Boolean> areEqualitiesStrict(DBTermType otherType) {
                return Optional.empty();
            }

            @Override
            public boolean areEqualitiesBetweenTwoDBAttributesStrict() {
                return false;
            }
        };
    }
}
