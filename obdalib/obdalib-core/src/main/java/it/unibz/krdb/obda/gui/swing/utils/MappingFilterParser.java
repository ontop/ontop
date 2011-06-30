// $ANTLR 3.3 Nov 30, 2010 12:50:56 MappingFilter.g 2011-06-30 14:09:08

package it.unibz.krdb.obda.gui.swing.utils;

import it.unibz.krdb.obda.gui.swing.treemodel.TreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingIDTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingStringTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingHeadVariableTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingSQLStringTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingFunctorTreeModelFilter;
import it.unibz.krdb.obda.gui.swing.treemodel.MappingPredicateTreeModelFilter;
import it.unibz.krdb.obda.model.OBDAMappingAxiom;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class MappingFilterParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SEMI", "NOT", "COLON", "ID", "TEXT", "TARGET", "SOURCE", "FUNCT", "PRED", "STRING", "COMMA", "UNDERSCORE", "DASH", "ALPHA", "DIGIT", "ALPHANUM", "CHAR", "WS"
    };
    public static final int EOF=-1;
    public static final int SEMI=4;
    public static final int NOT=5;
    public static final int COLON=6;
    public static final int ID=7;
    public static final int TEXT=8;
    public static final int TARGET=9;
    public static final int SOURCE=10;
    public static final int FUNCT=11;
    public static final int PRED=12;
    public static final int STRING=13;
    public static final int COMMA=14;
    public static final int UNDERSCORE=15;
    public static final int DASH=16;
    public static final int ALPHA=17;
    public static final int DIGIT=18;
    public static final int ALPHANUM=19;
    public static final int CHAR=20;
    public static final int WS=21;

    // delegates
    // delegators


        public MappingFilterParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public MappingFilterParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return MappingFilterParser.tokenNames; }
    public String getGrammarFileName() { return "MappingFilter.g"; }


    private static String stripLeadingAndTrailingQuotes(String str)
    {
      if (str.startsWith("\"")) {
        str = str.substring(1, str.length());
      }
      if (str.endsWith("\"")) {
        str = str.substring(0, str.length() - 1);
      }
      return str;
    }



    // $ANTLR start "parse"
    // MappingFilter.g:52:1: parse returns [ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList] : f1= filter ( SEMI f2= filter )* EOF ;
    public final ArrayList<TreeModelFilter<OBDAMappingAxiom>> parse() throws RecognitionException {
        ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList = null;

        TreeModelFilter<OBDAMappingAxiom> f1 = null;

        TreeModelFilter<OBDAMappingAxiom> f2 = null;



          filterList = new ArrayList<TreeModelFilter<OBDAMappingAxiom>>();

        try {
            // MappingFilter.g:56:3: (f1= filter ( SEMI f2= filter )* EOF )
            // MappingFilter.g:56:5: f1= filter ( SEMI f2= filter )* EOF
            {
            pushFollow(FOLLOW_filter_in_parse55);
            f1=filter();

            state._fsp--;

             filterList.add(f1); 
            // MappingFilter.g:56:47: ( SEMI f2= filter )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==SEMI) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // MappingFilter.g:56:48: SEMI f2= filter
            	    {
            	    match(input,SEMI,FOLLOW_SEMI_in_parse60); 
            	    pushFollow(FOLLOW_filter_in_parse64);
            	    f2=filter();

            	    state._fsp--;

            	     filterList.add(f2); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_parse70); 

            }

        }
        catch (RecognitionException e) {
             
                  throw e;
                
        }
        finally {
        }
        return filterList;
    }
    // $ANTLR end "parse"


    // $ANTLR start "filter"
    // MappingFilter.g:61:1: filter returns [TreeModelFilter<OBDAMappingAxiom> value] : ( (not= NOT )? ( type COLON )? keyword ) ;
    public final TreeModelFilter<OBDAMappingAxiom> filter() throws RecognitionException {
        TreeModelFilter<OBDAMappingAxiom> value = null;

        Token not=null;
        TreeModelFilter<OBDAMappingAxiom> type1 = null;

        MappingFilterParser.keyword_return keyword2 = null;


        try {
            // MappingFilter.g:62:3: ( ( (not= NOT )? ( type COLON )? keyword ) )
            // MappingFilter.g:62:5: ( (not= NOT )? ( type COLON )? keyword )
            {
            // MappingFilter.g:62:5: ( (not= NOT )? ( type COLON )? keyword )
            // MappingFilter.g:62:6: (not= NOT )? ( type COLON )? keyword
            {
            // MappingFilter.g:62:9: (not= NOT )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==NOT) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // MappingFilter.g:62:9: not= NOT
                    {
                    not=(Token)match(input,NOT,FOLLOW_NOT_in_filter96); 

                    }
                    break;

            }

            // MappingFilter.g:62:15: ( type COLON )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( ((LA3_0>=ID && LA3_0<=PRED)) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // MappingFilter.g:62:16: type COLON
                    {
                    pushFollow(FOLLOW_type_in_filter100);
                    type1=type();

                    state._fsp--;

                    match(input,COLON,FOLLOW_COLON_in_filter102); 

                    }
                    break;

            }

            pushFollow(FOLLOW_keyword_in_filter106);
            keyword2=keyword();

            state._fsp--;


            }


                  value = type1;
                  if (value == null) {
                    value = new MappingStringTreeModelFilter();
                  }
                  
                  // Register the keyword.
                  value.addStringFilter((keyword2!=null?input.toString(keyword2.start,keyword2.stop):null));
                  
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
        }
        return value;
    }
    // $ANTLR end "filter"


    // $ANTLR start "type"
    // MappingFilter.g:78:1: type returns [TreeModelFilter<OBDAMappingAxiom> value] : ( ID | TEXT | TARGET | SOURCE | FUNCT | PRED );
    public final TreeModelFilter<OBDAMappingAxiom> type() throws RecognitionException {
        TreeModelFilter<OBDAMappingAxiom> value = null;

        try {
            // MappingFilter.g:79:3: ( ID | TEXT | TARGET | SOURCE | FUNCT | PRED )
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
                    // MappingFilter.g:79:5: ID
                    {
                    match(input,ID,FOLLOW_ID_in_type126); 
                     value = new MappingIDTreeModelFilter(); 

                    }
                    break;
                case 2 :
                    // MappingFilter.g:80:5: TEXT
                    {
                    match(input,TEXT,FOLLOW_TEXT_in_type139); 
                     value = new MappingStringTreeModelFilter(); 

                    }
                    break;
                case 3 :
                    // MappingFilter.g:81:5: TARGET
                    {
                    match(input,TARGET,FOLLOW_TARGET_in_type150); 
                     value = new MappingHeadVariableTreeModelFilter(); 

                    }
                    break;
                case 4 :
                    // MappingFilter.g:82:5: SOURCE
                    {
                    match(input,SOURCE,FOLLOW_SOURCE_in_type159); 
                     value = new MappingSQLStringTreeModelFilter(); 

                    }
                    break;
                case 5 :
                    // MappingFilter.g:83:5: FUNCT
                    {
                    match(input,FUNCT,FOLLOW_FUNCT_in_type168); 
                     value = new MappingFunctorTreeModelFilter(); 

                    }
                    break;
                case 6 :
                    // MappingFilter.g:84:5: PRED
                    {
                    match(input,PRED,FOLLOW_PRED_in_type178); 
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
        }
        return value;
    }
    // $ANTLR end "type"

    public static class keyword_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "keyword"
    // MappingFilter.g:87:1: keyword : STRING ( COMMA STRING )* ;
    public final MappingFilterParser.keyword_return keyword() throws RecognitionException {
        MappingFilterParser.keyword_return retval = new MappingFilterParser.keyword_return();
        retval.start = input.LT(1);

        try {
            // MappingFilter.g:88:3: ( STRING ( COMMA STRING )* )
            // MappingFilter.g:88:5: STRING ( COMMA STRING )*
            {
            match(input,STRING,FOLLOW_STRING_in_keyword198); 
            // MappingFilter.g:88:12: ( COMMA STRING )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==COMMA) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // MappingFilter.g:88:13: COMMA STRING
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_keyword201); 
            	    match(input,STRING,FOLLOW_STRING_in_keyword203); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "keyword"

    // Delegated rules


 

    public static final BitSet FOLLOW_filter_in_parse55 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_SEMI_in_parse60 = new BitSet(new long[]{0x0000000000003FA0L});
    public static final BitSet FOLLOW_filter_in_parse64 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EOF_in_parse70 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_filter96 = new BitSet(new long[]{0x0000000000003FA0L});
    public static final BitSet FOLLOW_type_in_filter100 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_filter102 = new BitSet(new long[]{0x0000000000003FA0L});
    public static final BitSet FOLLOW_keyword_in_filter106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_type126 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_type139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TARGET_in_type150 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOURCE_in_type159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FUNCT_in_type168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRED_in_type178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_keyword198 = new BitSet(new long[]{0x0000000000004002L});
    public static final BitSet FOLLOW_COMMA_in_keyword201 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_STRING_in_keyword203 = new BitSet(new long[]{0x0000000000004002L});

}