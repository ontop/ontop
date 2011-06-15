// $ANTLR 3.3 Nov 30, 2010 12:50:56 MappingFilter.g 2011-06-15 16:54:43

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "COMMA", "NOT", "COLON", "ID", "TEXT", "TARGET", "SOURCE", "FUNCT", "PRED", "STRING_LITERAL", "QUOTE_DOUBLE", "WS"
    };
    public static final int EOF=-1;
    public static final int COMMA=4;
    public static final int NOT=5;
    public static final int COLON=6;
    public static final int ID=7;
    public static final int TEXT=8;
    public static final int TARGET=9;
    public static final int SOURCE=10;
    public static final int FUNCT=11;
    public static final int PRED=12;
    public static final int STRING_LITERAL=13;
    public static final int QUOTE_DOUBLE=14;
    public static final int WS=15;

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
    // MappingFilter.g:52:1: parse returns [ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList] : f1= filter ( COMMA f2= filter )* EOF ;
    public final ArrayList<TreeModelFilter<OBDAMappingAxiom>> parse() throws RecognitionException {
        ArrayList<TreeModelFilter<OBDAMappingAxiom>> filterList = null;

        TreeModelFilter<OBDAMappingAxiom> f1 = null;

        TreeModelFilter<OBDAMappingAxiom> f2 = null;



          filterList = new ArrayList<TreeModelFilter<OBDAMappingAxiom>>();

        try {
            // MappingFilter.g:56:3: (f1= filter ( COMMA f2= filter )* EOF )
            // MappingFilter.g:56:5: f1= filter ( COMMA f2= filter )* EOF
            {
            pushFollow(FOLLOW_filter_in_parse55);
            f1=filter();

            state._fsp--;

             filterList.add(f1); 
            // MappingFilter.g:56:47: ( COMMA f2= filter )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==COMMA) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // MappingFilter.g:56:48: COMMA f2= filter
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_parse60); 
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
    // MappingFilter.g:61:1: filter returns [TreeModelFilter<OBDAMappingAxiom> value] : ( (not= NOT )? type COLON keyword ) ;
    public final TreeModelFilter<OBDAMappingAxiom> filter() throws RecognitionException {
        TreeModelFilter<OBDAMappingAxiom> value = null;

        Token not=null;
        TreeModelFilter<OBDAMappingAxiom> type1 = null;

        MappingFilterParser.keyword_return keyword2 = null;


        try {
            // MappingFilter.g:62:3: ( ( (not= NOT )? type COLON keyword ) )
            // MappingFilter.g:62:5: ( (not= NOT )? type COLON keyword )
            {
            // MappingFilter.g:62:5: ( (not= NOT )? type COLON keyword )
            // MappingFilter.g:62:6: (not= NOT )? type COLON keyword
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

            pushFollow(FOLLOW_type_in_filter99);
            type1=type();

            state._fsp--;

            match(input,COLON,FOLLOW_COLON_in_filter101); 
            pushFollow(FOLLOW_keyword_in_filter103);
            keyword2=keyword();

            state._fsp--;


            }


                  value = type1;
                  String keyword = stripLeadingAndTrailingQuotes((keyword2!=null?input.toString(keyword2.start,keyword2.stop):null));    
                  if (not != null) {
                    value.putNegation();
                  }
                  value.addStringFilter(keyword);
                

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
    // MappingFilter.g:72:1: type returns [TreeModelFilter<OBDAMappingAxiom> value] : ( ID | TEXT | TARGET | SOURCE | FUNCT | PRED );
    public final TreeModelFilter<OBDAMappingAxiom> type() throws RecognitionException {
        TreeModelFilter<OBDAMappingAxiom> value = null;

        try {
            // MappingFilter.g:73:3: ( ID | TEXT | TARGET | SOURCE | FUNCT | PRED )
            int alt3=6;
            switch ( input.LA(1) ) {
            case ID:
                {
                alt3=1;
                }
                break;
            case TEXT:
                {
                alt3=2;
                }
                break;
            case TARGET:
                {
                alt3=3;
                }
                break;
            case SOURCE:
                {
                alt3=4;
                }
                break;
            case FUNCT:
                {
                alt3=5;
                }
                break;
            case PRED:
                {
                alt3=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // MappingFilter.g:73:5: ID
                    {
                    match(input,ID,FOLLOW_ID_in_type123); 
                     value = new MappingIDTreeModelFilter(); 

                    }
                    break;
                case 2 :
                    // MappingFilter.g:74:5: TEXT
                    {
                    match(input,TEXT,FOLLOW_TEXT_in_type136); 
                     value = new MappingStringTreeModelFilter(); 

                    }
                    break;
                case 3 :
                    // MappingFilter.g:75:5: TARGET
                    {
                    match(input,TARGET,FOLLOW_TARGET_in_type147); 
                     value = new MappingHeadVariableTreeModelFilter(); 

                    }
                    break;
                case 4 :
                    // MappingFilter.g:76:5: SOURCE
                    {
                    match(input,SOURCE,FOLLOW_SOURCE_in_type156); 
                     value = new MappingSQLStringTreeModelFilter(); 

                    }
                    break;
                case 5 :
                    // MappingFilter.g:77:5: FUNCT
                    {
                    match(input,FUNCT,FOLLOW_FUNCT_in_type165); 
                     value = new MappingFunctorTreeModelFilter(); 

                    }
                    break;
                case 6 :
                    // MappingFilter.g:78:5: PRED
                    {
                    match(input,PRED,FOLLOW_PRED_in_type175); 
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
    // MappingFilter.g:81:1: keyword : STRING_LITERAL ;
    public final MappingFilterParser.keyword_return keyword() throws RecognitionException {
        MappingFilterParser.keyword_return retval = new MappingFilterParser.keyword_return();
        retval.start = input.LT(1);

        try {
            // MappingFilter.g:82:3: ( STRING_LITERAL )
            // MappingFilter.g:82:5: STRING_LITERAL
            {
            match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_keyword195); 

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
    public static final BitSet FOLLOW_COMMA_in_parse60 = new BitSet(new long[]{0x0000000000001FA0L});
    public static final BitSet FOLLOW_filter_in_parse64 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EOF_in_parse70 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_filter96 = new BitSet(new long[]{0x0000000000001FA0L});
    public static final BitSet FOLLOW_type_in_filter99 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_COLON_in_filter101 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_keyword_in_filter103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_type123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TEXT_in_type136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TARGET_in_type147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SOURCE_in_type156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FUNCT_in_type165 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRED_in_type175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_LITERAL_in_keyword195 = new BitSet(new long[]{0x0000000000000002L});

}