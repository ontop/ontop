/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.parser.sparql;

import gr.uoa.di.madgik.sesame.functions.EHCoveredByFunc;
import gr.uoa.di.madgik.sesame.functions.EHCoversFunc;
import gr.uoa.di.madgik.sesame.functions.EHDisjointFunc;
import gr.uoa.di.madgik.sesame.functions.EHEqualsFunc;

import org.openrdf.query.parser.sparql.ast.*;

/**
 * Base class for visitors of the SPARQL AST.
 * 
 * @author arjohn
 */
public abstract class ASTVisitorBase implements SyntaxTreeBuilderVisitor {

	public Object visit(ASTAbs node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTUpdateSequence node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBindingValue node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTInlineData node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTUpdateContainer node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAdd node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBindingSet node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTClear node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCopy node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCreate node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDeleteClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDeleteData node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDeleteWhere node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDrop node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphOrDefault node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphRefAll node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTInfix node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTInsertClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTInsertData node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLoad node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTModify node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMove node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNow node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTYear node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMonth node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDay node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTHours node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTz node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMinutes node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSeconds node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTimezone node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAnd node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAskQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTAvg node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMD5 node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSHA1 node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSHA224 node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSHA256 node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSHA384 node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSHA512 node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBaseDecl node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBasicGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBind node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBindingsClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBlankNode node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBlankNodePropertyList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBNodeFunc node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTBound node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCeil node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCoalesce node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTConcat node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTContains node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCollection node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCompare node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTConstraint node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTConstruct node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTConstructQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTCount node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDatasetClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDatatype node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDescribe node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTDescribeQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTExistsFunc node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTEncodeForURI node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTFalse node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTFloor node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTFunctionCall node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGraphPatternGroup node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGroupClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGroupConcat node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTGroupCondition node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTHavingClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIf node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIn node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIRI node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIRIFunc node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsBlank node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsIRI node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsLiteral node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTIsNumeric node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLang node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLangMatches node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLimit node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTLowerCase node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMath node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMax node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMin node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTMinusGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNot node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNotExistsFunc node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNotIn node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTNumericLiteral node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTObjectList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOffset node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOptionalGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOrderClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTOrderCondition node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPathAlternative node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPathElt node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPathMod node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPathOneInPropertySet node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPathSequence node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPrefixDecl node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTProjectionElem node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPropertyList node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTPropertyListPath node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTQName node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTQueryContainer node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTRand node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTRDFLiteral node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTRegexExpression node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTReplace node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTRound node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSameTerm node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSpatialContain node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSpatialOverlap node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSpatialCrosses node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSpatialDisjoint node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSpatialEqual node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSpatialIntersects node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSpatialTouches node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSpatialWithin node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTEHContains node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTEHCoveredBy node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTEHCovers node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTEHDisjoint node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTEHEquals node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTEHInside node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTEHOverlap node, Object data)
			throws VisitorException
		{
			return node.childrenAccept(this, data);
		}

	public Object visit(ASTSample node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSelect node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSelectQuery node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTServiceGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStrAfter node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStrBefore node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStrDt node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStrEnds node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTString node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTUUID node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSTRUUID node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStrLang node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStrLen node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTStrStarts node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSubstr node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTSum node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTriplesSameSubject node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTriplesSameSubjectPath node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTQuadsNotTriples node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTTrue node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTUnionGraphPattern node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTUpdate node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTUpperCase node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTVar node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(ASTWhereClause node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

	public Object visit(SimpleNode node, Object data)
		throws VisitorException
	{
		return node.childrenAccept(this, data);
	}

}
