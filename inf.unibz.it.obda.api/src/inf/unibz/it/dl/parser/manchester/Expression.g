grammar Expression;

@header {
/* 
* This code has been auto generated from the file ClassExpression.g using ANTLRWorks 1.2.2
* Do not modify this file, modify the .g grammar instead.
*
* This parser includes 2 static methods that you can use to invoke the parser on a string input and get
* ClassExpressions and PropertyExpressions.
*/

package inf.unibz.it.obda.api.parse.classexpression;

import inf.unibz.it.obda.api.domain.*;
import inf.unibz.it.obda.api.controller.*;
import java.net.URI;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

}

@lexer::header {

package inf.unibz.it.obda.api.parse.classexpression;

}

@lexer::members{
}

@members{


	public static ClassExpression parseClassExpression(String expression) {
		ExpressionParser parser = null;
		ClassExpression result = null;
		byte currentBytes[] = expression.getBytes();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
		ANTLRInputStream inputst = null;
		try {
			inputst = new ANTLRInputStream(byteArrayInputStream);

		} catch (IOException e) {
			e.printStackTrace(System.err);
			return null;
		}
		ExpressionLexer lexer = new ExpressionLexer(inputst);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		parser = new ExpressionParser(tokens);
		try {
			result = parser.basic_concept_exp();
		} catch (RecognitionException e) {
			return null;
		}
		return result;
	}
	
	public static PropertyExpression parsePropertyExpression(String expression) {
		ExpressionParser parser = null;
		PropertyExpression result = null;
		byte currentBytes[] = expression.getBytes();
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(currentBytes);
		ANTLRInputStream inputst = null;
		try {
			inputst = new ANTLRInputStream(byteArrayInputStream);

		} catch (IOException e) {
			e.printStackTrace(System.err);
			return null;
		}
		ExpressionLexer lexer = new ExpressionLexer(inputst);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		parser = new ExpressionParser(tokens);
		try {
			result = parser.property_expression();
		} catch (RecognitionException e) {
			return null;
		}
		return result;
	}


}

basic_concept_exp returns [ClassExpression value]	
	:	(atomic_concept | existential_role) {
	
		if ($atomic_concept.value != null) {
			$value = $atomic_concept.value;
		} else if ($existential_role.value != null) {
			$value = $existential_role.value;
		} else {
			//throw error
		}
	}
	;


atomic_concept returns [NamedConcept value] 
	:	ALPHAVAR {
			APICoupler coupler = APIController.getController().getCoupler();
			URI uri = URI.create($ALPHAVAR.text);
			if (coupler.isNamedConcept(uri)) {
				$value = new NamedConcept(uri);
			} else {
				//throw error
			}
		}
	;

existential_role returns [ExistentialQuantification value]
	:	 '(' 'some'  ( atomic_property | '(' inverse_atomic_property ')' ) ')'	 {
			if ($atomic_property.value != null) {
				$value = new ExistentialQuantification($atomic_property.value);
			} else if ($inverse_atomic_property.value != null) {
				$value = new ExistentialQuantification($inverse_atomic_property.value);
			} else {
				//throw error
			}
			}
	| 'some'  ( atomic_property | '(' inverse_atomic_property ')' )  {
			if ($atomic_property.value != null) {
				$value = new ExistentialQuantification($atomic_property.value);
			} else if ($inverse_atomic_property.value != null) {
				$value = new ExistentialQuantification($inverse_atomic_property.value);
			} else {
				//throw error
			}
	}
	;
	
property_expression returns [PropertyExpression value ]
	:	atomic_property { $value = $atomic_property.value; }
	| 	inverse_atomic_property { $value = $inverse_atomic_property.value; }
	;	
	
inverse_atomic_property returns [InverseProperty value]
	:	'(' 'inverseof' atomic_property ')' { 
				if ($atomic_property.value instanceof ObjectProperty) { 
					$value = new InverseProperty((ObjectProperty)$atomic_property.value); 
				} else {
					//Error
				}
				
			}
	| 'inverseof' atomic_property  { 
				if ($atomic_property.value instanceof ObjectProperty) { 
					$value = new InverseProperty((ObjectProperty)$atomic_property.value); 
				} else {
					//Error
				}
				
			}
	
	;

atomic_property returns [NamedProperty value] 
	:	ALPHAVAR {
			APICoupler coupler = APIController.getController().getCoupler();
			NamedProperty prop = null;
			URI uri = URI.create($ALPHAVAR.text);

			if (coupler.isObjectProperty(uri)) {
				prop = new ObjectProperty(uri);
			} else if (coupler.isDatatypeProperty(uri)) {
				prop = new DataProperty(uri);
			} else {
				//Throw exception
			}
			$value = prop;}		
	;

ALPHAVAR 	:	  (ALPHA | INT | CHAR)+ ; 	

fragment
CHAR 		:	('_'|'-'|'*'|'&'|'@'|'!'|'#'|'%'|'+'|'='|':'|'.');

fragment
ALPHA 		:   ('a'..'z'|'A'..'Z')+ ;

fragment
INT 		:   '0'..'9'+ ; 

WS  		:   (' '|'\t'|('\r'|'\r\n'))+ {skip();};

