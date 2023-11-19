package it.unibz.inf.ontop.query.translation.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unibz.inf.ontop.exception.MinorOntopInternalBugException;
import it.unibz.inf.ontop.exception.OntopUnsupportedKGQueryException;
import it.unibz.inf.ontop.model.term.*;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbolFactory;
import it.unibz.inf.ontop.model.term.functionsymbol.LangSPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.term.functionsymbol.SPARQLFunctionSymbol;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.TermTypeInference;
import it.unibz.inf.ontop.model.type.TypeFactory;
import it.unibz.inf.ontop.model.vocabulary.AGG;
import it.unibz.inf.ontop.model.vocabulary.SPARQL;
import it.unibz.inf.ontop.model.vocabulary.XPathFunction;
import it.unibz.inf.ontop.model.vocabulary.XSD;
import it.unibz.inf.ontop.utils.ImmutableCollectors;
import org.apache.commons.rdf.api.RDF;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.*;

import java.util.Optional;
import java.util.Set;

public class RDF4JValueExprTranslator {

    private final Set<Variable> knownVariables;
    private final ImmutableMap<Variable, GroundTerm> externalBindings;
    private final boolean treatBNodeAsVariable;

    private final TermFactory termFactory;
    private final RDF rdfFactory;
    private final FunctionSymbolFactory functionSymbolFactory;
    private final RDF4JValueTranslator valueTranslator;

    public RDF4JValueExprTranslator(Set<Variable> knownVariables,
                                    ImmutableMap<Variable, GroundTerm> externalBindings,
                                    boolean treatBNodeAsVariable,
                                    TermFactory termFactory,
                                    RDF rdfFactory,
                                    TypeFactory typeFactory,
                                    FunctionSymbolFactory functionSymbolFactory) {
        this.knownVariables = knownVariables;
        this.externalBindings = externalBindings;
        this.treatBNodeAsVariable = treatBNodeAsVariable;
        this.termFactory = termFactory;
        this.rdfFactory = rdfFactory;
        this.functionSymbolFactory = functionSymbolFactory;
        this.valueTranslator = new RDF4JValueTranslator(termFactory, rdfFactory, typeFactory);
    }


    /**
     * @param expr           expression
     * @return term
     */
    public ImmutableTerm getTerm(ValueExpr expr) {

        if (expr instanceof Var)
            return translateRDF4JVar((Var) expr, false);
        if (expr instanceof ValueConstant) {
            Value v = ((ValueConstant) expr).getValue();
            return valueTranslator.getTermForLiteralOrIri(v);
        }
        if (expr instanceof Bound) {
            // BOUND (Sec 17.4.1.1)
            // xsd:boolean  BOUND (variable var)
            Var v = ((Bound) expr).getArg();
            Variable var = termFactory.getVariable(v.getName());
            return knownVariables.contains(var)
                    ? getFunctionalTerm(SPARQL.BOUND, var)
                    : termFactory.getRDFLiteralConstant("false", XSD.BOOLEAN);
        }
        if (expr instanceof UnaryValueOperator) {
            return getTerm((UnaryValueOperator) expr);
        }
        if (expr instanceof BinaryValueOperator) {
            return getTerm((BinaryValueOperator) expr);
        }
        if (expr instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expr;

            ImmutableList<ImmutableTerm> terms = f.getArgs().stream()
                    .map(this::getTerm)
                    .collect(ImmutableCollectors.toList());

            String functionName = extractFunctionName(f.getURI());

            Optional<SPARQLFunctionSymbol> optionalFunctionSymbol = functionSymbolFactory.getSPARQLFunctionSymbol(
                    functionName, terms.size());

            if (optionalFunctionSymbol.isPresent()) {
                return termFactory.getImmutableFunctionalTerm(optionalFunctionSymbol.get(), terms);
            }
        }
        if (expr instanceof NAryValueOperator) {
            return getTerm((NAryValueOperator) expr);
        }
        if (expr instanceof BNodeGenerator) {
            Optional<ImmutableTerm> term = Optional.ofNullable(((BNodeGenerator) expr).getNodeIdExpr())
                    .map(this::getTerm);

            return term
                    .map(t -> getFunctionalTerm(SPARQL.BNODE, t))
                    .orElseGet(() -> getFunctionalTerm(SPARQL.BNODE));
        }
        if (expr instanceof If) {
            If ifExpr = (If) expr;

            return getFunctionalTerm(SPARQL.IF,
                    convertToXsdBooleanTerm(getTerm(ifExpr.getCondition())),
                    getTerm(ifExpr.getResult()),
                    getTerm(ifExpr.getAlternative()));
        }
        // other subclasses
        // SubQueryValueOperator
        // ValueExprTripleRef
        throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
    }

    private ImmutableTerm getTerm(UnaryValueOperator expr) {

        if (expr.getArg() == null) {
            if (expr instanceof Count)  // O-ary count
                return getFunctionalTerm(SPARQL.COUNT);

            throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
        }

        ImmutableTerm term = getTerm(expr.getArg());

        if (expr instanceof AbstractAggregateOperator) {
            AbstractAggregateOperator aggExpr = (AbstractAggregateOperator) expr;
            if (aggExpr instanceof Count) { //Unary count
                return getAggregateFunctionalTerm(SPARQL.COUNT, aggExpr.isDistinct(), term);
            }
            if (aggExpr instanceof Avg) {
                return getAggregateFunctionalTerm(SPARQL.AVG, aggExpr.isDistinct(), term);
            }
            if (aggExpr instanceof Sum) {
                return getAggregateFunctionalTerm(SPARQL.SUM, aggExpr.isDistinct(), term);
            }
            if (aggExpr instanceof Min) {
                return getFunctionalTerm(SPARQL.MIN, term);
            }
            if (aggExpr instanceof Max) {
                return getFunctionalTerm(SPARQL.MAX, term);
            }
            if (aggExpr instanceof Sample) {
                return getFunctionalTerm(SPARQL.SAMPLE, term);
            }
            if (aggExpr instanceof GroupConcat) {
                String separator = Optional.ofNullable(((GroupConcat) aggExpr).getSeparator())
                        .map(e -> ((ValueConstant) e).getValue().stringValue())
                        .orElse(" "); // Default separator

                return termFactory.getImmutableFunctionalTerm(
                        functionSymbolFactory.getSPARQLGroupConcatFunctionSymbol(separator, aggExpr.isDistinct()),
                        term);
            }
            if (aggExpr instanceof AggregateFunctionCall) {
                AggregateFunctionCall call = (AggregateFunctionCall) aggExpr;
                if(call.getIRI().startsWith(AGG.PREFIX)) {
                    return getAggregateFunctionalTerm(call.getIRI(), aggExpr.isDistinct(), term);
                }
            }
            throw new RuntimeException("Unreachable: all subclasses covered");
        }
        if (expr instanceof Not) {
            return getFunctionalTerm(XPathFunction.NOT.getIRIString(), convertToXsdBooleanTerm(term));
        }
        if (expr instanceof IsNumeric) {
            // isNumeric (Sec 17.4.2.4) for checking whether the argument is a numeric value
            return getFunctionalTerm(SPARQL.IS_NUMERIC, term);
        }
        if (expr instanceof IsLiteral) {
            return getFunctionalTerm(SPARQL.IS_LITERAL, term);
        }
        if (expr instanceof IsURI) {
            return getFunctionalTerm(SPARQL.IS_IRI, term);
        }
        if (expr instanceof Str) {
            return getFunctionalTerm(SPARQL.STR, term);
        }
        if (expr instanceof Datatype) {
            return getFunctionalTerm(SPARQL.DATATYPE, term);
        }
        if (expr instanceof IsBNode) {
            return getFunctionalTerm(SPARQL.IS_BLANK, term);
        }
        if (expr instanceof Lang) {
            if (expr.getArg() instanceof Var)
                return getFunctionalTerm(SPARQL.LANG, term);

            throw new RuntimeException(new OntopUnsupportedKGQueryException("A variable or a value is expected in " + expr));
        }
        if (expr instanceof IRIFunction) {
            // IRIFunction: IRI (Sec 17.4.2.8) for constructing IRIs
            Optional<org.apache.commons.rdf.api.IRI> optionalBaseIRI = Optional.ofNullable(((IRIFunction) expr).getBaseURI())
                    .map(rdfFactory::createIRI);

            SPARQLFunctionSymbol functionSymbol = optionalBaseIRI
                    .map(functionSymbolFactory::getIRIFunctionSymbol)
                    .orElseGet(functionSymbolFactory::getIRIFunctionSymbol);

            return termFactory.getImmutableFunctionalTerm(functionSymbol, term);
        }
        // subclasses missing:
        //  - IsResource
        //   - LocalName
        //   - Namespace
        //   - Label
        //   - Like
        throw new RuntimeException(new OntopUnsupportedKGQueryException("The expression " + expr + " is not supported yet!"));
    }

    private ImmutableTerm getTerm(BinaryValueOperator expr) {

        ImmutableTerm term1 = getTerm(expr.getLeftArg());
        ImmutableTerm term2 = getTerm(expr.getRightArg());

        if (expr instanceof And) {
            return getFunctionalTerm(SPARQL.LOGICAL_AND, convertToXsdBooleanTerm(term1), convertToXsdBooleanTerm(term2));
        }
        if (expr instanceof Or) {
            return getFunctionalTerm(SPARQL.LOGICAL_OR, convertToXsdBooleanTerm(term1), convertToXsdBooleanTerm(term2));
        }
        if (expr instanceof SameTerm) {
            // sameTerm (Sec 17.4.1.8)
            // Corresponds to the STRICT equality (same lexical value, same type)
            return getFunctionalTerm(SPARQL.SAME_TERM, term1, term2);
        }
        if (expr instanceof Regex) {
            // REGEX (Sec 17.4.3.14)
            // xsd:boolean  REGEX (string literal text, simple literal pattern)
            // xsd:boolean  REGEX (string literal text, simple literal pattern, simple literal flags)
            Regex reg = (Regex) expr;
            return (reg.getFlagsArg() != null)
                    ? getFunctionalTerm(SPARQL.REGEX, term1, term2, getTerm(reg.getFlagsArg()))
                    : getFunctionalTerm(SPARQL.REGEX, term1, term2);
        }
        if (expr instanceof Compare) {
            switch (((Compare) expr).getOperator()) {
                case EQ:
                    return getFunctionalTerm(SPARQL.EQ, term1, term2);
                case LT:
                    return getFunctionalTerm(SPARQL.LESS_THAN, term1, term2);
                case GT:
                    return getFunctionalTerm(SPARQL.GREATER_THAN, term1, term2);
                case NE:
                    return getFunctionalTerm(XPathFunction.NOT.getIRIString(), getFunctionalTerm(SPARQL.EQ, term1, term2));
                case LE:
                    return getFunctionalTerm(XPathFunction.NOT.getIRIString(), getFunctionalTerm(SPARQL.GREATER_THAN, term1, term2));
                case GE:
                    return getFunctionalTerm(XPathFunction.NOT.getIRIString(), getFunctionalTerm(SPARQL.LESS_THAN, term1, term2));
                default:
                    throw new RuntimeException(new OntopUnsupportedKGQueryException("Unsupported operator: " + expr));
            }
        }
        if (expr instanceof MathExpr) {
            return getFunctionalTerm(NumericalOperations.get(((MathExpr) expr).getOperator()), term1, term2);
        }
        /*
         * Restriction: the first argument must be LANG(...) and the second  a constant
         * (for guaranteeing that the langMatches logic is not delegated to the native query)
         */
        if (expr instanceof LangMatches) {
            if (!(term1 instanceof ImmutableFunctionalTerm
                    && ((ImmutableFunctionalTerm) term1).getFunctionSymbol() instanceof LangSPARQLFunctionSymbol)
                    || !(term2 instanceof RDFConstant)) {
                throw new RuntimeException(new OntopUnsupportedKGQueryException("The function langMatches is " +
                        "only supported with lang(..) function for the first argument and a constant for the second"));
            }

            return getFunctionalTerm(SPARQL.LANG_MATCHES, term1, term2);
        }
        throw new RuntimeException("Unreachable: all subclasses covered");
    }

    private ImmutableTerm getTerm(NAryValueOperator expr) {

        ImmutableList<ImmutableTerm> terms = expr.getArguments().stream()
                .map(this::getTerm)
                .collect(ImmutableCollectors.toList());

        if (expr instanceof Coalesce) {
            SPARQLFunctionSymbol functionSymbol = functionSymbolFactory.getRequiredSPARQLFunctionSymbol(
                    SPARQL.COALESCE, terms.size());
            return termFactory.getImmutableFunctionalTerm(functionSymbol, terms);
        }
        if (expr instanceof ListMemberOperator) {
            if (terms.size() < 2)
                throw new MinorOntopInternalBugException("Was not expecting a ListMemberOperator from RDF4J with less than 2 terms");

            ImmutableTerm firstArgument = terms.get(0);
            return terms.stream()
                    .skip(1)
                    .map(t -> getFunctionalTerm(SPARQL.EQ, firstArgument, t))
                    .reduce((e1, e2) -> getFunctionalTerm(SPARQL.LOGICAL_OR, e1, e2))
                    .orElseThrow(() -> new MinorOntopInternalBugException("Cannot happen because there are at least 2 terms"));
        }
        throw new RuntimeException("Unreachable: all subclasses covered");
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 0));
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName, ImmutableTerm t) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 1), t);
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName, ImmutableTerm t1, ImmutableTerm t2) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 2), t1, t2);
    }

    private ImmutableFunctionalTerm getFunctionalTerm(String functionName, ImmutableTerm t1, ImmutableTerm t2, ImmutableTerm t3) {
        return termFactory.getImmutableFunctionalTerm(functionSymbolFactory.getRequiredSPARQLFunctionSymbol(functionName, 3), t1, t2, t3);
    }

    private ImmutableFunctionalTerm getAggregateFunctionalTerm(String officialName, boolean isDistinct, ImmutableTerm t) {
        return termFactory.getImmutableFunctionalTerm(
                isDistinct
                        ? functionSymbolFactory.getRequiredSPARQLDistinctAggregateFunctionSymbol(officialName, 1)
                        : functionSymbolFactory.getRequiredSPARQLFunctionSymbol(officialName, 1),
                t);
    }


    private ImmutableTerm convertToXsdBooleanTerm(ImmutableTerm term) {

        return term.inferType()
                .flatMap(TermTypeInference::getTermType)
                .filter(t -> t instanceof RDFDatatype)
                .map(t -> (RDFDatatype) t)
                .filter(t -> t.isA(XSD.BOOLEAN))
                .isPresent()
                    ? term
                    : termFactory.getSPARQLEffectiveBooleanValue(term);
    }

    private static final ImmutableMap<MathExpr.MathOp, String> NumericalOperations =
            ImmutableMap.<MathExpr.MathOp, String>builder()
                    .put(MathExpr.MathOp.PLUS, SPARQL.NUMERIC_ADD)
                    .put(MathExpr.MathOp.MINUS, SPARQL.NUMERIC_SUBTRACT)
                    .put(MathExpr.MathOp.MULTIPLY, SPARQL.NUMERIC_MULTIPLY)
                    .put(MathExpr.MathOp.DIVIDE, SPARQL.NUMERIC_DIVIDE)
                    .build();


    /**
     * Changes some function names when RDF4J abuses the SPARQL standard (i.e. is too tightly-coupled)
     *
     * The typical example is the YEAR() function which is replaced by RDF4J by fn:year-from-dateTime because
     * the SPARQL 1.1 specification has only consider the case of xsd:dateTime, not xsd:date.
     * Obviously, all the major implementations also support the case of xsd:date and use the fun:year-from-date when
     * appropriated.
     *
     * This method reverses fn:year-from-dateTime into YEAR, as it now maps to a function symbol that accepts
     * both xsd:date and xsd:dateTime.
     *
     */
    private String extractFunctionName(String uri) {

        if (uri.equals(XPathFunction.YEAR_FROM_DATETIME.getIRIString()))
            return SPARQL.YEAR;
        else if (uri.equals(XPathFunction.MONTH_FROM_DATETIME.getIRIString()))
            return SPARQL.MONTH;
        else if (uri.equals(XPathFunction.DAY_FROM_DATETIME.getIRIString()))
            return SPARQL.DAY;
        else
            return uri;
    }


    /**
     * Translates a RDF4J "Var" (which can be a variable or a constant) into a Ontop term.
     */
    public VariableOrGroundTerm translateRDF4JVar(Var v, boolean leafNode) {
        // If this "Var" is a constant
        if ((v.hasValue()))
            return valueTranslator.getTermForLiteralOrIri(v.getValue());

        if (v.isAnonymous() && !treatBNodeAsVariable)
            return termFactory.getConstantBNode(v.getName());

        // Otherwise, this "Var" is a variable
        Variable var = termFactory.getVariable(v.getName());
        // If the subtree is empty, create a variable
        if (leafNode)
            return var;
        // Otherwise, check whether the variable is projected
        return knownVariables.contains(var)
                ? var
                : Optional.ofNullable(externalBindings.get(var))
                        .orElseGet(termFactory::getNullConstant);
    }
}
