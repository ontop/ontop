// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/java/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA_tmp.g4 by ANTLR 4.7
package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.model.term.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDA_tmpParser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TurtleOBDA_tmpParser}.
 */
public interface TurtleOBDA_tmpListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(TurtleOBDA_tmpParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(TurtleOBDA_tmpParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#directiveStatement}.
	 * @param ctx the parse tree
	 */
	void enterDirectiveStatement(TurtleOBDA_tmpParser.DirectiveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#directiveStatement}.
	 * @param ctx the parse tree
	 */
	void exitDirectiveStatement(TurtleOBDA_tmpParser.DirectiveStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#triplesStatement}.
	 * @param ctx the parse tree
	 */
	void enterTriplesStatement(TurtleOBDA_tmpParser.TriplesStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#triplesStatement}.
	 * @param ctx the parse tree
	 */
	void exitTriplesStatement(TurtleOBDA_tmpParser.TriplesStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#directive}.
	 * @param ctx the parse tree
	 */
	void enterDirective(TurtleOBDA_tmpParser.DirectiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#directive}.
	 * @param ctx the parse tree
	 */
	void exitDirective(TurtleOBDA_tmpParser.DirectiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#base}.
	 * @param ctx the parse tree
	 */
	void enterBase(TurtleOBDA_tmpParser.BaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#base}.
	 * @param ctx the parse tree
	 */
	void exitBase(TurtleOBDA_tmpParser.BaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code prefixID_1}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#prefixID}.
	 * @param ctx the parse tree
	 */
	void enterPrefixID_1(TurtleOBDA_tmpParser.PrefixID_1Context ctx);
	/**
	 * Exit a parse tree produced by the {@code prefixID_1}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#prefixID}.
	 * @param ctx the parse tree
	 */
	void exitPrefixID_1(TurtleOBDA_tmpParser.PrefixID_1Context ctx);
	/**
	 * Enter a parse tree produced by the {@code prefixID_2}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#prefixID}.
	 * @param ctx the parse tree
	 */
	void enterPrefixID_2(TurtleOBDA_tmpParser.PrefixID_2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code prefixID_2}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#prefixID}.
	 * @param ctx the parse tree
	 */
	void exitPrefixID_2(TurtleOBDA_tmpParser.PrefixID_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#triples}.
	 * @param ctx the parse tree
	 */
	void enterTriples(TurtleOBDA_tmpParser.TriplesContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#triples}.
	 * @param ctx the parse tree
	 */
	void exitTriples(TurtleOBDA_tmpParser.TriplesContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#predicateObjectList}.
	 * @param ctx the parse tree
	 */
	void enterPredicateObjectList(TurtleOBDA_tmpParser.PredicateObjectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#predicateObjectList}.
	 * @param ctx the parse tree
	 */
	void exitPredicateObjectList(TurtleOBDA_tmpParser.PredicateObjectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#predicateObject}.
	 * @param ctx the parse tree
	 */
	void enterPredicateObject(TurtleOBDA_tmpParser.PredicateObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#predicateObject}.
	 * @param ctx the parse tree
	 */
	void exitPredicateObject(TurtleOBDA_tmpParser.PredicateObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#verb}.
	 * @param ctx the parse tree
	 */
	void enterVerb(TurtleOBDA_tmpParser.VerbContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#verb}.
	 * @param ctx the parse tree
	 */
	void exitVerb(TurtleOBDA_tmpParser.VerbContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#objectList}.
	 * @param ctx the parse tree
	 */
	void enterObjectList(TurtleOBDA_tmpParser.ObjectListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#objectList}.
	 * @param ctx the parse tree
	 */
	void exitObjectList(TurtleOBDA_tmpParser.ObjectListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#subject}.
	 * @param ctx the parse tree
	 */
	void enterSubject(TurtleOBDA_tmpParser.SubjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#subject}.
	 * @param ctx the parse tree
	 */
	void exitSubject(TurtleOBDA_tmpParser.SubjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#object}.
	 * @param ctx the parse tree
	 */
	void enterObject(TurtleOBDA_tmpParser.ObjectContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#object}.
	 * @param ctx the parse tree
	 */
	void exitObject(TurtleOBDA_tmpParser.ObjectContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#resource}.
	 * @param ctx the parse tree
	 */
	void enterResource(TurtleOBDA_tmpParser.ResourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#resource}.
	 * @param ctx the parse tree
	 */
	void exitResource(TurtleOBDA_tmpParser.ResourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#uriref}.
	 * @param ctx the parse tree
	 */
	void enterUriref(TurtleOBDA_tmpParser.UrirefContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#uriref}.
	 * @param ctx the parse tree
	 */
	void exitUriref(TurtleOBDA_tmpParser.UrirefContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#qname}.
	 * @param ctx the parse tree
	 */
	void enterQname(TurtleOBDA_tmpParser.QnameContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#qname}.
	 * @param ctx the parse tree
	 */
	void exitQname(TurtleOBDA_tmpParser.QnameContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#blank}.
	 * @param ctx the parse tree
	 */
	void enterBlank(TurtleOBDA_tmpParser.BlankContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#blank}.
	 * @param ctx the parse tree
	 */
	void exitBlank(TurtleOBDA_tmpParser.BlankContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(TurtleOBDA_tmpParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(TurtleOBDA_tmpParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(TurtleOBDA_tmpParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(TurtleOBDA_tmpParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typedLiteral_1}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#typedLiteral}.
	 * @param ctx the parse tree
	 */
	void enterTypedLiteral_1(TurtleOBDA_tmpParser.TypedLiteral_1Context ctx);
	/**
	 * Exit a parse tree produced by the {@code typedLiteral_1}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#typedLiteral}.
	 * @param ctx the parse tree
	 */
	void exitTypedLiteral_1(TurtleOBDA_tmpParser.TypedLiteral_1Context ctx);
	/**
	 * Enter a parse tree produced by the {@code typedLiteral_2}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#typedLiteral}.
	 * @param ctx the parse tree
	 */
	void enterTypedLiteral_2(TurtleOBDA_tmpParser.TypedLiteral_2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code typedLiteral_2}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#typedLiteral}.
	 * @param ctx the parse tree
	 */
	void exitTypedLiteral_2(TurtleOBDA_tmpParser.TypedLiteral_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#language}.
	 * @param ctx the parse tree
	 */
	void enterLanguage(TurtleOBDA_tmpParser.LanguageContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#language}.
	 * @param ctx the parse tree
	 */
	void exitLanguage(TurtleOBDA_tmpParser.LanguageContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#terms}.
	 * @param ctx the parse tree
	 */
	void enterTerms(TurtleOBDA_tmpParser.TermsContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#terms}.
	 * @param ctx the parse tree
	 */
	void exitTerms(TurtleOBDA_tmpParser.TermsContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(TurtleOBDA_tmpParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(TurtleOBDA_tmpParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterLiteral(TurtleOBDA_tmpParser.LiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitLiteral(TurtleOBDA_tmpParser.LiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(TurtleOBDA_tmpParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(TurtleOBDA_tmpParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#dataTypeString}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeString(TurtleOBDA_tmpParser.DataTypeStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#dataTypeString}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeString(TurtleOBDA_tmpParser.DataTypeStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#numericLiteral}.
	 * @param ctx the parse tree
	 */
	void enterNumericLiteral(TurtleOBDA_tmpParser.NumericLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#numericLiteral}.
	 * @param ctx the parse tree
	 */
	void exitNumericLiteral(TurtleOBDA_tmpParser.NumericLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#nodeID}.
	 * @param ctx the parse tree
	 */
	void enterNodeID(TurtleOBDA_tmpParser.NodeIDContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#nodeID}.
	 * @param ctx the parse tree
	 */
	void exitNodeID(TurtleOBDA_tmpParser.NodeIDContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#relativeURI}.
	 * @param ctx the parse tree
	 */
	void enterRelativeURI(TurtleOBDA_tmpParser.RelativeURIContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#relativeURI}.
	 * @param ctx the parse tree
	 */
	void exitRelativeURI(TurtleOBDA_tmpParser.RelativeURIContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#namespace}.
	 * @param ctx the parse tree
	 */
	void enterNamespace(TurtleOBDA_tmpParser.NamespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#namespace}.
	 * @param ctx the parse tree
	 */
	void exitNamespace(TurtleOBDA_tmpParser.NamespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#defaultNamespace}.
	 * @param ctx the parse tree
	 */
	void enterDefaultNamespace(TurtleOBDA_tmpParser.DefaultNamespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#defaultNamespace}.
	 * @param ctx the parse tree
	 */
	void exitDefaultNamespace(TurtleOBDA_tmpParser.DefaultNamespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(TurtleOBDA_tmpParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(TurtleOBDA_tmpParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#languageTag}.
	 * @param ctx the parse tree
	 */
	void enterLanguageTag(TurtleOBDA_tmpParser.LanguageTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#languageTag}.
	 * @param ctx the parse tree
	 */
	void exitLanguageTag(TurtleOBDA_tmpParser.LanguageTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(TurtleOBDA_tmpParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(TurtleOBDA_tmpParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#numericUnsigned}.
	 * @param ctx the parse tree
	 */
	void enterNumericUnsigned(TurtleOBDA_tmpParser.NumericUnsignedContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#numericUnsigned}.
	 * @param ctx the parse tree
	 */
	void exitNumericUnsigned(TurtleOBDA_tmpParser.NumericUnsignedContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#numericPositive}.
	 * @param ctx the parse tree
	 */
	void enterNumericPositive(TurtleOBDA_tmpParser.NumericPositiveContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#numericPositive}.
	 * @param ctx the parse tree
	 */
	void exitNumericPositive(TurtleOBDA_tmpParser.NumericPositiveContext ctx);
	/**
	 * Enter a parse tree produced by {@link TurtleOBDA_tmpParser#numericNegative}.
	 * @param ctx the parse tree
	 */
	void enterNumericNegative(TurtleOBDA_tmpParser.NumericNegativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link TurtleOBDA_tmpParser#numericNegative}.
	 * @param ctx the parse tree
	 */
	void exitNumericNegative(TurtleOBDA_tmpParser.NumericNegativeContext ctx);
}