// $ANTLR 3.4 C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g 2012-05-18 10:51:12

package org.semanticweb.ontop.protege4.utils;

/*
 * #%L
 * ontop-protege4
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.semanticweb.ontop.model.OBDAMappingAxiom;
import org.semanticweb.ontop.protege4.gui.treemodels.MappingFunctorTreeModelFilter;
import org.semanticweb.ontop.protege4.gui.treemodels.MappingHeadVariableTreeModelFilter;
import org.semanticweb.ontop.protege4.gui.treemodels.MappingIDTreeModelFilter;
import org.semanticweb.ontop.protege4.gui.treemodels.MappingPredicateTreeModelFilter;
import org.semanticweb.ontop.protege4.gui.treemodels.MappingSQLStringTreeModelFilter;
import org.semanticweb.ontop.protege4.gui.treemodels.MappingStringTreeModelFilter;
import org.semanticweb.ontop.protege4.gui.treemodels.TreeModelFilter;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class MappingFilterParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALPHA", "ALPHANUM", "CHAR", "COLON", "COMMA", "DASH", "DIGIT", "ECHAR", "FUNCT", "ID", "NOT", "PRED", "SEMI", "SOURCE", "STRING", "STRING_WITH_QUOTE", "STRING_WITH_QUOTE_DOUBLE", "TARGET", "TEXT", "UNDERSCORE", "WS"
    };

    public static final int EOF=-1;
    public static final int ALPHA=4;
    public static final int ALPHANUM=5;
    public static final int CHAR=6;
    public static final int COLON=7;
    public static final int COMMA=8;
    public static final int DASH=9;
    public static final int DIGIT=10;
    public static final int ECHAR=11;
    public static final int FUNCT=12;
    public static final int ID=13;
    public static final int NOT=14;
    public static final int PRED=15;
    public static final int SEMI=16;
    public static final int SOURCE=17;
    public static final int STRING=18;
    public static final int STRING_WITH_QUOTE=19;
    public static final int STRING_WITH_QUOTE_DOUBLE=20;
    public static final int TARGET=21;
    public static final int TEXT=22;
    public static final int UNDERSCORE=23;
    public static final int WS=24;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public MappingFilterParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public MappingFilterParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

    public String[] getTokenNames() { return MappingFilterParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g"; }



    // $ANTLR start "parse"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:42:1: parse returns [ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList] : f1= filter ( SEMI f2= filter )* EOF ;
    public final ArrayList<TreeModelFilter<OBDAMappingAxiom>> parse() throws RecognitionException {
        ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList = null;


        TreeModelFilter<OBDAMappingAxiom> f1 =null;

        TreeModelFilter<OBDAMappingAxiom> f2 =null;



          filterList = new ArrayList<TreeModelFilter<OBDAMappingAxiom>>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:46:3: (f1= filter ( SEMI f2= filter )* EOF )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:46:5: f1= filter ( SEMI f2= filter )* EOF
            {
            pushFollow(FOLLOW_filter_in_parse49);
            f1=filter();

            state._fsp--;


             filterList.add(f1); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:46:47: ( SEMI f2= filter )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==SEMI) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:46:48: SEMI f2= filter
            	    {
            	    match(input,SEMI,FOLLOW_SEMI_in_parse54); 

            	    pushFollow(FOLLOW_filter_in_parse58);
            	    f2=filter();

            	    state._fsp--;


            	     filterList.add(f2); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            match(input,EOF,FOLLOW_EOF_in_parse64); 

            }

        }
        catch (RecognitionException e) {
             
                  throw e;
                
        }

        finally {
        	// do for sure before leaving
        }
        return filterList;
    }
    // $ANTLR end "parse"



    // $ANTLR start "filter"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:51:1: filter returns [TreeModelFilter<OBDAMappingAxiom> value] : ( (not= NOT )? ( type COLON )? keyword ) ;
    public final TreeModelFilter<OBDAMappingAxiom> filter() throws RecognitionException {
        TreeModelFilter<OBDAMappingAxiom> value = null;


        Token not=null;
        TreeModelFilter<OBDAMappingAxiom> type1 =null;

        String[] keyword2 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:3: ( ( (not= NOT )? ( type COLON )? keyword ) )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:5: ( (not= NOT )? ( type COLON )? keyword )
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:5: ( (not= NOT )? ( type COLON )? keyword )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:6: (not= NOT )? ( type COLON )? keyword
            {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:9: (not= NOT )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==NOT) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:9: not= NOT
                    {
                    not=(Token)match(input,NOT,FOLLOW_NOT_in_filter90); 

                    }
                    break;

            }


            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:15: ( type COLON )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( ((LA3_0 >= FUNCT && LA3_0 <= ID)||LA3_0==PRED||LA3_0==SOURCE||(LA3_0 >= TARGET && LA3_0 <= TEXT)) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:52:16: type COLON
                    {
                    pushFollow(FOLLOW_type_in_filter94);
                    type1=type();

                    state._fsp--;


                    match(input,COLON,FOLLOW_COLON_in_filter96); 

                    }
                    break;

            }


            pushFollow(FOLLOW_keyword_in_filter100);
            keyword2=keyword();

            state._fsp--;


            }



                  value = type1;
                  if (value == null) {
                    value = new MappingStringTreeModelFilter();
                  }
                  
                  // Register the keyword.
                  value.addStringFilter(keyword2);
                  
                  // Register the negation.
                  if (not != null) {
                    value.putNegation();
                  }
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "filter"



    // $ANTLR start "type"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:68:1: type returns [TreeModelFilter<OBDAMappingAxiom> value] : ( ID | TEXT | TARGET | SOURCE | FUNCT | PRED );
    public final TreeModelFilter<OBDAMappingAxiom> type() throws RecognitionException {
        TreeModelFilter<OBDAMappingAxiom> value = null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:69:3: ( ID | TEXT | TARGET | SOURCE | FUNCT | PRED )
            int alt4=6;
            switch ( input.LA(1) ) {
            case ID:
                {
                alt4=1;
                }
                break;
            case TEXT:
                {
                alt4=2;
                }
                break;
            case TARGET:
                {
                alt4=3;
                }
                break;
            case SOURCE:
                {
                alt4=4;
                }
                break;
            case FUNCT:
                {
                alt4=5;
                }
                break;
            case PRED:
                {
                alt4=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }

            switch (alt4) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:69:5: ID
                    {
                    match(input,ID,FOLLOW_ID_in_type120); 

                     value = new MappingIDTreeModelFilter(); 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:70:5: TEXT
                    {
                    match(input,TEXT,FOLLOW_TEXT_in_type133); 

                     value = new MappingStringTreeModelFilter(); 

                    }
                    break;
                case 3 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:71:5: TARGET
                    {
                    match(input,TARGET,FOLLOW_TARGET_in_type144); 

                     value = new MappingHeadVariableTreeModelFilter(); 

                    }
                    break;
                case 4 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:72:5: SOURCE
                    {
                    match(input,SOURCE,FOLLOW_SOURCE_in_type153); 

                     value = new MappingSQLStringTreeModelFilter(); 

                    }
                    break;
                case 5 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:73:5: FUNCT
                    {
                    match(input,FUNCT,FOLLOW_FUNCT_in_type162); 

                     value = new MappingFunctorTreeModelFilter(); 

                    }
                    break;
                case 6 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:74:5: PRED
                    {
                    match(input,PRED,FOLLOW_PRED_in_type172); 

                     value = new MappingPredicateTreeModelFilter(); 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "type"



    // $ANTLR start "keyword"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:77:1: keyword returns [String[] value] : t1= input ( COMMA t2= input )* ;
    public final String[] keyword() throws RecognitionException {
        String[] value = null;


        String t1 =null;

        String t2 =null;



          List<String> inputList = new LinkedList<String>();

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:81:3: (t1= input ( COMMA t2= input )* )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:81:5: t1= input ( COMMA t2= input )*
            {
            pushFollow(FOLLOW_input_in_keyword203);
            t1=input();

            state._fsp--;


             inputList.add(t1); 

            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:81:44: ( COMMA t2= input )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==COMMA) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:81:45: COMMA t2= input
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_keyword208); 

            	    pushFollow(FOLLOW_input_in_keyword212);
            	    t2=input();

            	    state._fsp--;


            	     inputList.add(t2); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);



                  value = inputList.toArray(new String[inputList.size()]);
                

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "keyword"



    // $ANTLR start "input"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:86:1: input returns [String value] : ( unquoted_string | quoted_string );
    public final String input() throws RecognitionException {
        String value = null;


        String unquoted_string3 =null;

        String quoted_string4 =null;


        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:87:3: ( unquoted_string | quoted_string )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==STRING) ) {
                alt6=1;
            }
            else if ( ((LA6_0 >= STRING_WITH_QUOTE && LA6_0 <= STRING_WITH_QUOTE_DOUBLE)) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }
            switch (alt6) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:87:5: unquoted_string
                    {
                    pushFollow(FOLLOW_unquoted_string_in_input238);
                    unquoted_string3=unquoted_string();

                    state._fsp--;


                     value = unquoted_string3; 

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:88:5: quoted_string
                    {
                    pushFollow(FOLLOW_quoted_string_in_input246);
                    quoted_string4=quoted_string();

                    state._fsp--;


                     value = quoted_string4; 

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "input"



    // $ANTLR start "unquoted_string"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:91:1: unquoted_string returns [String value] : STRING ;
    public final String unquoted_string() throws RecognitionException {
        String value = null;


        Token STRING5=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:92:3: ( STRING )
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:92:5: STRING
            {
            STRING5=(Token)match(input,STRING,FOLLOW_STRING_in_unquoted_string267); 

             value = (STRING5!=null?STRING5.getText():null); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "unquoted_string"



    // $ANTLR start "quoted_string"
    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:95:1: quoted_string returns [String value] : ( STRING_WITH_QUOTE_DOUBLE | STRING_WITH_QUOTE );
    public final String quoted_string() throws RecognitionException {
        String value = null;


        Token STRING_WITH_QUOTE_DOUBLE6=null;
        Token STRING_WITH_QUOTE7=null;

        try {
            // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:96:3: ( STRING_WITH_QUOTE_DOUBLE | STRING_WITH_QUOTE )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==STRING_WITH_QUOTE_DOUBLE) ) {
                alt7=1;
            }
            else if ( (LA7_0==STRING_WITH_QUOTE) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:96:5: STRING_WITH_QUOTE_DOUBLE
                    {
                    STRING_WITH_QUOTE_DOUBLE6=(Token)match(input,STRING_WITH_QUOTE_DOUBLE,FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_quoted_string288); 


                          String str = (STRING_WITH_QUOTE_DOUBLE6!=null?STRING_WITH_QUOTE_DOUBLE6.getText():null);
                          str = str.substring(1, str.length()-1);
                          value = str.trim();
                        

                    }
                    break;
                case 2 :
                    // C:\\Project\\Obdalib\\obdalib-parent\\obdalib-core\\src\\main\\java\\it\\unibz\\krdb\\obda\\gui\\swing\\utils\\MappingFilter.g:101:5: STRING_WITH_QUOTE
                    {
                    STRING_WITH_QUOTE7=(Token)match(input,STRING_WITH_QUOTE,FOLLOW_STRING_WITH_QUOTE_in_quoted_string296); 


                          String str = (STRING_WITH_QUOTE7!=null?STRING_WITH_QUOTE7.getText():null);
                          str = str.substring(1, str.length()-1);
                          value = str.trim();
                        

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return value;
    }
    // $ANTLR end "quoted_string"

    // Delegated rules


 

    public static final BitSet FOLLOW_filter_in_parse49 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_SEMI_in_parse54 = new BitSet(new long[]{0x00000000007EF000L});
    public static final BitSet FOLLOW_filter_in_parse58 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EOF_in_parse64 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_filter90 = new BitSet(new long[]{0x00000000007EB000L});
    public static final BitSet FOLLOW_type_in_filter94 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_COLON_in_filter96 = new BitSet(new long[]{0x00000000001C0000L});
    public static final BitSet FOLLOW_keyword_in_filter100 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_type120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_type133 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TARGET_in_type144 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOURCE_in_type153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FUNCT_in_type162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRED_in_type172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_input_in_keyword203 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_COMMA_in_keyword208 = new BitSet(new long[]{0x00000000001C0000L});
    public static final BitSet FOLLOW_input_in_keyword212 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_unquoted_string_in_input238 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quoted_string_in_input246 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_unquoted_string267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_DOUBLE_in_quoted_string288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_WITH_QUOTE_in_quoted_string296 = new BitSet(new long[]{0x0000000000000002L});

}
