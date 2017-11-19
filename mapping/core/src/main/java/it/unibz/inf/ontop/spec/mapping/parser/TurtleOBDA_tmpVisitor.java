// Generated from /home/jcorman/workspace/ontop/v3/mapping/core/src/main/java/it/unibz/inf/ontop/spec/mapping/parser/impl/TurtleOBDA_tmp.g4 by ANTLR 4.7
package it.unibz.inf.ontop.spec.mapping.parser;

import it.unibz.inf.ontop.model.term.*;
import static it.unibz.inf.ontop.model.OntopModelSingletons.TERM_FACTORY;

import it.unibz.inf.ontop.spec.mapping.parser.impl.TurtleOBDA_tmpParser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TurtleOBDA_tmpParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TurtleOBDA_tmpVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(TurtleOBDA_tmpParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#directiveStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirectiveStatement(TurtleOBDA_tmpParser.DirectiveStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#triplesStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriplesStatement(TurtleOBDA_tmpParser.TriplesStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#directive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDirective(TurtleOBDA_tmpParser.DirectiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#base}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBase(TurtleOBDA_tmpParser.BaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code prefixID_1}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#prefixID}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixID_1(TurtleOBDA_tmpParser.PrefixID_1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code prefixID_2}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#prefixID}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrefixID_2(TurtleOBDA_tmpParser.PrefixID_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#triples}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTriples(TurtleOBDA_tmpParser.TriplesContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#predicateObjectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateObjectList(TurtleOBDA_tmpParser.PredicateObjectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#predicateObject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateObject(TurtleOBDA_tmpParser.PredicateObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#verb}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVerb(TurtleOBDA_tmpParser.VerbContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#objectList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectList(TurtleOBDA_tmpParser.ObjectListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#subject}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubject(TurtleOBDA_tmpParser.SubjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#object}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObject(TurtleOBDA_tmpParser.ObjectContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#resource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResource(TurtleOBDA_tmpParser.ResourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#uriref}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUriref(TurtleOBDA_tmpParser.UrirefContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#qname}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQname(TurtleOBDA_tmpParser.QnameContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#blank}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlank(TurtleOBDA_tmpParser.BlankContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#variable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariable(TurtleOBDA_tmpParser.VariableContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(TurtleOBDA_tmpParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typedLiteral_1}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#typedLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedLiteral_1(TurtleOBDA_tmpParser.TypedLiteral_1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code typedLiteral_2}
	 * labeled alternative in {@link TurtleOBDA_tmpParser#typedLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypedLiteral_2(TurtleOBDA_tmpParser.TypedLiteral_2Context ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#language}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLanguage(TurtleOBDA_tmpParser.LanguageContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#terms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerms(TurtleOBDA_tmpParser.TermsContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(TurtleOBDA_tmpParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteral(TurtleOBDA_tmpParser.LiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#stringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(TurtleOBDA_tmpParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#dataTypeString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeString(TurtleOBDA_tmpParser.DataTypeStringContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#numericLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(TurtleOBDA_tmpParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#nodeID}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNodeID(TurtleOBDA_tmpParser.NodeIDContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#relativeURI}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelativeURI(TurtleOBDA_tmpParser.RelativeURIContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#namespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamespace(TurtleOBDA_tmpParser.NamespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#defaultNamespace}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultNamespace(TurtleOBDA_tmpParser.DefaultNamespaceContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(TurtleOBDA_tmpParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#languageTag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLanguageTag(TurtleOBDA_tmpParser.LanguageTagContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(TurtleOBDA_tmpParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#numericUnsigned}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericUnsigned(TurtleOBDA_tmpParser.NumericUnsignedContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#numericPositive}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericPositive(TurtleOBDA_tmpParser.NumericPositiveContext ctx);
	/**
	 * Visit a parse tree produced by {@link TurtleOBDA_tmpParser#numericNegative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericNegative(TurtleOBDA_tmpParser.NumericNegativeContext ctx);
}