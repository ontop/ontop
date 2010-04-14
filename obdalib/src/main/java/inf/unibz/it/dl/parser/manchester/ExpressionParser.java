// $ANTLR 3.1.1 /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g 2009-02-09 17:11:51

/* 
* This code has been auto generated from the file ClassExpression.g using ANTLRWorks 1.2.2
* Do not modify this file, modify the .g grammar instead.
*
* This parser includes 2 static methods that you can use to invoke the parser on a string input and get
* ClassExpressions and PropertyExpressions.
*/

package inf.unibz.it.dl.parser.manchester;

import inf.unibz.it.dl.domain.ClassExpression;
import inf.unibz.it.dl.domain.DataProperty;
import inf.unibz.it.dl.domain.ExistentialQuantification;
import inf.unibz.it.dl.domain.InverseProperty;
import inf.unibz.it.dl.domain.NamedConcept;
import inf.unibz.it.dl.domain.NamedProperty;
import inf.unibz.it.dl.domain.ObjectProperty;
import inf.unibz.it.dl.domain.PropertyExpression;
import inf.unibz.it.obda.api.controller.APIController;
import inf.unibz.it.obda.api.controller.APICoupler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;

public class ExpressionParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALPHAVAR", "ALPHA", "INT", "CHAR", "WS", "'('", "'some'", "')'", "'inverseof'"
    };
    public static final int WS=8;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__10=10;
    public static final int CHAR=7;
    public static final int ALPHAVAR=4;
    public static final int INT=6;
    public static final int EOF=-1;
    public static final int T__9=9;
    public static final int ALPHA=5;

    // delegates
    // delegators
    
    private APIController api = null;

        public ExpressionParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public ExpressionParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
        public void setController(APIController api) {
        	this.api = api;
        }
        

    public String[] getTokenNames() { return ExpressionParser.tokenNames; }
    public String getGrammarFileName() { return "/Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g"; }




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





    // $ANTLR start "basic_concept_exp"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:91:1: basic_concept_exp returns [ClassExpression value] : ( atomic_concept | existential_role ) ;
    public final ClassExpression basic_concept_exp() throws RecognitionException {
        ClassExpression value = null;

        NamedConcept atomic_concept1 = null;

        ExistentialQuantification existential_role2 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:92:2: ( ( atomic_concept | existential_role ) )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:92:4: ( atomic_concept | existential_role )
            {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:92:4: ( atomic_concept | existential_role )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==ALPHAVAR) ) {
                alt1=1;
            }
            else if ( ((LA1_0>=9 && LA1_0<=10)) ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:92:5: atomic_concept
                    {
                    pushFollow(FOLLOW_atomic_concept_in_basic_concept_exp45);
                    atomic_concept1=atomic_concept();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:92:22: existential_role
                    {
                    pushFollow(FOLLOW_existential_role_in_basic_concept_exp49);
                    existential_role2=existential_role();

                    state._fsp--;


                    }
                    break;

            }


            	
            		if (atomic_concept1 != null) {
            			value = atomic_concept1;
            		} else if (existential_role2 != null) {
            			value = existential_role2;
            		} else {
            			//throw error
            		}
            	

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "basic_concept_exp"


    // $ANTLR start "atomic_concept"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:105:1: atomic_concept returns [NamedConcept value] : ALPHAVAR ;
    public final NamedConcept atomic_concept() throws RecognitionException {
        NamedConcept value = null;

        Token ALPHAVAR3=null;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:106:2: ( ALPHAVAR )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:106:4: ALPHAVAR
            {
            ALPHAVAR3=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_atomic_concept69); 

            			APICoupler coupler = api.getCoupler();
            			URI uri = URI.create((ALPHAVAR3!=null?ALPHAVAR3.getText():null));
            			if (coupler.isNamedConcept(uri)) {
            				value = new NamedConcept(uri);
            			} else {
            				//throw error
            			}
            		

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "atomic_concept"


    // $ANTLR start "existential_role"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:117:1: existential_role returns [ExistentialQuantification value] : ( '(' 'some' ( atomic_property | '(' inverse_atomic_property ')' ) ')' | 'some' ( atomic_property | '(' inverse_atomic_property ')' ) );
    public final ExistentialQuantification existential_role() throws RecognitionException {
        ExistentialQuantification value = null;

        NamedProperty atomic_property4 = null;

        InverseProperty inverse_atomic_property5 = null;

        NamedProperty atomic_property6 = null;

        InverseProperty inverse_atomic_property7 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:118:2: ( '(' 'some' ( atomic_property | '(' inverse_atomic_property ')' ) ')' | 'some' ( atomic_property | '(' inverse_atomic_property ')' ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==9) ) {
                alt4=1;
            }
            else if ( (LA4_0==10) ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:118:5: '(' 'some' ( atomic_property | '(' inverse_atomic_property ')' ) ')'
                    {
                    match(input,9,FOLLOW_9_in_existential_role87); 
                    match(input,10,FOLLOW_10_in_existential_role89); 
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:118:17: ( atomic_property | '(' inverse_atomic_property ')' )
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==ALPHAVAR) ) {
                        alt2=1;
                    }
                    else if ( (LA2_0==9) ) {
                        alt2=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 2, 0, input);

                        throw nvae;
                    }
                    switch (alt2) {
                        case 1 :
                            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:118:19: atomic_property
                            {
                            pushFollow(FOLLOW_atomic_property_in_existential_role94);
                            atomic_property4=atomic_property();

                            state._fsp--;


                            }
                            break;
                        case 2 :
                            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:118:37: '(' inverse_atomic_property ')'
                            {
                            match(input,9,FOLLOW_9_in_existential_role98); 
                            pushFollow(FOLLOW_inverse_atomic_property_in_existential_role100);
                            inverse_atomic_property5=inverse_atomic_property();

                            state._fsp--;

                            match(input,11,FOLLOW_11_in_existential_role102); 

                            }
                            break;

                    }

                    match(input,11,FOLLOW_11_in_existential_role106); 

                    			if (atomic_property4 != null) {
                    				value = new ExistentialQuantification(atomic_property4);
                    			} else if (inverse_atomic_property5 != null) {
                    				value = new ExistentialQuantification(inverse_atomic_property5);
                    			} else {
                    				//throw error
                    			}
                    			

                    }
                    break;
                case 2 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:127:4: 'some' ( atomic_property | '(' inverse_atomic_property ')' )
                    {
                    match(input,10,FOLLOW_10_in_existential_role114); 
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:127:12: ( atomic_property | '(' inverse_atomic_property ')' )
                    int alt3=2;
                    int LA3_0 = input.LA(1);

                    if ( (LA3_0==ALPHAVAR) ) {
                        alt3=1;
                    }
                    else if ( (LA3_0==9) ) {
                        alt3=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 3, 0, input);

                        throw nvae;
                    }
                    switch (alt3) {
                        case 1 :
                            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:127:14: atomic_property
                            {
                            pushFollow(FOLLOW_atomic_property_in_existential_role119);
                            atomic_property6=atomic_property();

                            state._fsp--;


                            }
                            break;
                        case 2 :
                            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:127:32: '(' inverse_atomic_property ')'
                            {
                            match(input,9,FOLLOW_9_in_existential_role123); 
                            pushFollow(FOLLOW_inverse_atomic_property_in_existential_role125);
                            inverse_atomic_property7=inverse_atomic_property();

                            state._fsp--;

                            match(input,11,FOLLOW_11_in_existential_role127); 

                            }
                            break;

                    }


                    			if (atomic_property6 != null) {
                    				value = new ExistentialQuantification(atomic_property6);
                    			} else if (inverse_atomic_property7 != null) {
                    				value = new ExistentialQuantification(inverse_atomic_property7);
                    			} else {
                    				//throw error
                    			}
                    	

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "existential_role"


    // $ANTLR start "property_expression"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:138:1: property_expression returns [PropertyExpression value ] : ( atomic_property | inverse_atomic_property );
    public final PropertyExpression property_expression() throws RecognitionException {
        PropertyExpression value = null;

        NamedProperty atomic_property8 = null;

        InverseProperty inverse_atomic_property9 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:139:2: ( atomic_property | inverse_atomic_property )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==ALPHAVAR) ) {
                alt5=1;
            }
            else if ( (LA5_0==9||LA5_0==12) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:139:4: atomic_property
                    {
                    pushFollow(FOLLOW_atomic_property_in_property_expression148);
                    atomic_property8=atomic_property();

                    state._fsp--;

                     value = atomic_property8; 

                    }
                    break;
                case 2 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:140:5: inverse_atomic_property
                    {
                    pushFollow(FOLLOW_inverse_atomic_property_in_property_expression156);
                    inverse_atomic_property9=inverse_atomic_property();

                    state._fsp--;

                     value = inverse_atomic_property9; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "property_expression"


    // $ANTLR start "inverse_atomic_property"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:143:1: inverse_atomic_property returns [InverseProperty value] : ( '(' 'inverseof' atomic_property ')' | 'inverseof' atomic_property );
    public final InverseProperty inverse_atomic_property() throws RecognitionException {
        InverseProperty value = null;

        NamedProperty atomic_property10 = null;

        NamedProperty atomic_property11 = null;


        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:144:2: ( '(' 'inverseof' atomic_property ')' | 'inverseof' atomic_property )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==9) ) {
                alt6=1;
            }
            else if ( (LA6_0==12) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:144:4: '(' 'inverseof' atomic_property ')'
                    {
                    match(input,9,FOLLOW_9_in_inverse_atomic_property175); 
                    match(input,12,FOLLOW_12_in_inverse_atomic_property177); 
                    pushFollow(FOLLOW_atomic_property_in_inverse_atomic_property179);
                    atomic_property10=atomic_property();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_inverse_atomic_property181); 
                     
                    				if (atomic_property10 instanceof ObjectProperty) { 
                    					value = new InverseProperty((ObjectProperty)atomic_property10); 
                    				} else {
                    					//Error
                    				}
                    				
                    			

                    }
                    break;
                case 2 :
                    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:152:4: 'inverseof' atomic_property
                    {
                    match(input,12,FOLLOW_12_in_inverse_atomic_property188); 
                    pushFollow(FOLLOW_atomic_property_in_inverse_atomic_property190);
                    atomic_property11=atomic_property();

                    state._fsp--;

                     
                    				if (atomic_property11 instanceof ObjectProperty) { 
                    					value = new InverseProperty((ObjectProperty)atomic_property11); 
                    				} else {
                    					//Error
                    				}
                    				
                    			

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "inverse_atomic_property"


    // $ANTLR start "atomic_property"
    // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:163:1: atomic_property returns [NamedProperty value] : ALPHAVAR ;
    public final NamedProperty atomic_property() throws RecognitionException {
        NamedProperty value = null;

        Token ALPHAVAR12=null;

        try {
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:164:2: ( ALPHAVAR )
            // /Users/mariano/Documents/OBDA/obda_eclipse_workspace/inf.unibz.it.obda.api/src/inf/unibz/it/obda/api/parse/classexpression/Expression.g:164:4: ALPHAVAR
            {
            ALPHAVAR12=(Token)match(input,ALPHAVAR,FOLLOW_ALPHAVAR_in_atomic_property211); 

            			APICoupler coupler = api.getCoupler();
            			NamedProperty prop = null;
            			URI uri = URI.create((ALPHAVAR12!=null?ALPHAVAR12.getText():null));

            			if (coupler.isObjectProperty(uri)) {
            				prop = new ObjectProperty(uri);
            			} else if (coupler.isDatatypeProperty(uri)) {
            				prop = new DataProperty(uri);
            			} else {
            				//Throw exception
            			}
            			value = prop;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "atomic_property"

    // Delegated rules


 

    public static final BitSet FOLLOW_atomic_concept_in_basic_concept_exp45 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_existential_role_in_basic_concept_exp49 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_atomic_concept69 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_9_in_existential_role87 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_existential_role89 = new BitSet(new long[]{0x0000000000000210L});
    public static final BitSet FOLLOW_atomic_property_in_existential_role94 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_9_in_existential_role98 = new BitSet(new long[]{0x0000000000001200L});
    public static final BitSet FOLLOW_inverse_atomic_property_in_existential_role100 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_existential_role102 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_existential_role106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_10_in_existential_role114 = new BitSet(new long[]{0x0000000000000210L});
    public static final BitSet FOLLOW_atomic_property_in_existential_role119 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_9_in_existential_role123 = new BitSet(new long[]{0x0000000000001200L});
    public static final BitSet FOLLOW_inverse_atomic_property_in_existential_role125 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_existential_role127 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_atomic_property_in_property_expression148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_inverse_atomic_property_in_property_expression156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_9_in_inverse_atomic_property175 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_inverse_atomic_property177 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_atomic_property_in_inverse_atomic_property179 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_inverse_atomic_property181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_12_in_inverse_atomic_property188 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_atomic_property_in_inverse_atomic_property190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALPHAVAR_in_atomic_property211 = new BitSet(new long[]{0x0000000000000002L});

}