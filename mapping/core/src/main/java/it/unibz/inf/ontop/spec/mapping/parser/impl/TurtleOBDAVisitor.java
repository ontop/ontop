// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/java/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA.g4 by ANTLR 4.7
package it.unibz.inf.ontop.spec.mapping.parser.impl;

import it.unibz.inf.ontop.model.term.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TurtleOBDAParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TurtleOBDAVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(TurtleOBDAParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#directiveStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiveStatement(TurtleOBDAParser.DirectiveStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#triplesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriplesStatement(TurtleOBDAParser.TriplesStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#directive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirective(TurtleOBDAParser.DirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#base}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBase(TurtleOBDAParser.BaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#prefixID}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixID(TurtleOBDAParser.PrefixIDContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#prefix}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefix(TurtleOBDAParser.PrefixContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#triples}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriples(TurtleOBDAParser.TriplesContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#predicateObjectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateObjectList(TurtleOBDAParser.PredicateObjectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#predicateObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateObject(TurtleOBDAParser.PredicateObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#verb}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVerb(TurtleOBDAParser.VerbContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#objectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectList(TurtleOBDAParser.ObjectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#subject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubject(TurtleOBDAParser.SubjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(TurtleOBDAParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(TurtleOBDAParser.ResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#uriref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUriref(TurtleOBDAParser.UrirefContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#prefixedName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixedName(TurtleOBDAParser.PrefixedNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#blank}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlank(TurtleOBDAParser.BlankContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(TurtleOBDAParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(TurtleOBDAParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typedLiteral_1}
	 * labeled alternative in {@link TurtleOBDAParser#typedLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedLiteral_1(TurtleOBDAParser.TypedLiteral_1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code typedLiteral_2}
	 * labeled alternative in {@link TurtleOBDAParser#typedLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedLiteral_2(TurtleOBDAParser.TypedLiteral_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#language}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLanguage(TurtleOBDAParser.LanguageContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#terms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerms(TurtleOBDAParser.TermsContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(TurtleOBDAParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(TurtleOBDAParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#stringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(TurtleOBDAParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#dataTypeString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeString(TurtleOBDAParser.DataTypeStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#numericLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(TurtleOBDAParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#languageTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLanguageTag(TurtleOBDAParser.LanguageTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(TurtleOBDAParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#numericUnsigned}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericUnsigned(TurtleOBDAParser.NumericUnsignedContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#numericPositive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericPositive(TurtleOBDAParser.NumericPositiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDAParser#numericNegative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericNegative(TurtleOBDAParser.NumericNegativeContext ctx);
}