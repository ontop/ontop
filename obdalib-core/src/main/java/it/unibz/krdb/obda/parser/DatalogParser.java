// $ANTLR 3.5.1 /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g 2014-10-07 15:45:47

package it.unibz.krdb.obda.parser;

import it.unibz.krdb.obda.model.CQIE;
import it.unibz.krdb.obda.model.DatalogProgram;
import it.unibz.krdb.obda.model.Function;
import it.unibz.krdb.obda.model.Term;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.Predicate;
import it.unibz.krdb.obda.model.URIConstant;
import it.unibz.krdb.obda.model.ValueConstant;
import it.unibz.krdb.obda.model.Variable;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAVocabulary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.TokenStream;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@SuppressWarnings("all")
public class DatalogParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ALPHA", "ALPHANUM", "AMPERSAND", 
		"APOSTROPHE", "ASTERISK", "AT", "BACKSLASH", "BASE", "CARET", "COLON", 
		"COMMA", "DASH", "DIGIT", "DOLLAR", "DOT", "DOUBLE_SLASH", "EQUALS", "EXCLAMATION", 
		"GREATER", "HASH", "ID", "ID_CORE", "ID_PLAIN", "ID_START", "IMPLIES", 
		"INV_IMPLIES", "LESS", "LPAREN", "LSQ_BRACKET", "PERCENT", "PLUS", "PREFIX", 
		"QUESTION", "QUOTE_DOUBLE", "QUOTE_SINGLE", "REFERENCE", "RPAREN", "RSQ_BRACKET", 
		"SCHEMA", "SEMI", "SLASH", "STRING_LITERAL", "STRING_LITERAL2", "STRING_PREFIX", 
		"STRING_URI", "TILDE", "UNDERSCORE", "URI_PATH", "WS"
	};
	public static final int EOF=-1;
	public static final int ALPHA=4;
	public static final int ALPHANUM=5;
	public static final int AMPERSAND=6;
	public static final int APOSTROPHE=7;
	public static final int ASTERISK=8;
	public static final int AT=9;
	public static final int BACKSLASH=10;
	public static final int BASE=11;
	public static final int CARET=12;
	public static final int COLON=13;
	public static final int COMMA=14;
	public static final int DASH=15;
	public static final int DIGIT=16;
	public static final int DOLLAR=17;
	public static final int DOT=18;
	public static final int DOUBLE_SLASH=19;
	public static final int EQUALS=20;
	public static final int EXCLAMATION=21;
	public static final int GREATER=22;
	public static final int HASH=23;
	public static final int ID=24;
	public static final int ID_CORE=25;
	public static final int ID_PLAIN=26;
	public static final int ID_START=27;
	public static final int IMPLIES=28;
	public static final int INV_IMPLIES=29;
	public static final int LESS=30;
	public static final int LPAREN=31;
	public static final int LSQ_BRACKET=32;
	public static final int PERCENT=33;
	public static final int PLUS=34;
	public static final int PREFIX=35;
	public static final int QUESTION=36;
	public static final int QUOTE_DOUBLE=37;
	public static final int QUOTE_SINGLE=38;
	public static final int REFERENCE=39;
	public static final int RPAREN=40;
	public static final int RSQ_BRACKET=41;
	public static final int SCHEMA=42;
	public static final int SEMI=43;
	public static final int SLASH=44;
	public static final int STRING_LITERAL=45;
	public static final int STRING_LITERAL2=46;
	public static final int STRING_PREFIX=47;
	public static final int STRING_URI=48;
	public static final int TILDE=49;
	public static final int UNDERSCORE=50;
	public static final int URI_PATH=51;
	public static final int WS=52;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public DatalogParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public DatalogParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return DatalogParser.tokenNames; }
	@Override public String getGrammarFileName() { return "/Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g"; }


	/** Constants */
	private static final String OBDA_DEFAULT_URI = "obda-default";
	private static final String OBDA_BASE_URI = "obda-base";
	private static final String OBDA_SELECT_ALL = "obda-select-all";

	/** Map of directives */
	private HashMap<String, String> directives = new HashMap<String, String>();

	/** Set of variable terms */
	private HashSet<Variable> variables = new HashSet<Variable>();

	/** A factory to construct the predicates and terms */
	private static final OBDADataFactory dfac = OBDADataFactoryImpl.getInstance();

	/** Select all flag */
	private boolean isSelectAll = false;

	public HashMap<String, String> getDirectives() {
	    return directives;
	}



	// $ANTLR start "parse"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:103:1: parse returns [DatalogProgram value] : prog EOF ;
	public final DatalogProgram parse() throws RecognitionException {
		DatalogProgram value = null;


		DatalogProgram prog1 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:104:3: ( prog EOF )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:104:5: prog EOF
			{
			pushFollow(FOLLOW_prog_in_parse54);
			prog1=prog();
			state._fsp--;
			if (state.failed) return value;
			match(input,EOF,FOLLOW_EOF_in_parse56); if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      value = prog1;
			    }
			}

		}
		catch (RecognitionException e) {
			 
			      throw e;
			    
		}

		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "parse"



	// $ANTLR start "prog"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:111:1: prog returns [DatalogProgram value] : ( base )? ( directive )* ( rule )+ ;
	public final DatalogProgram prog() throws RecognitionException {
		DatalogProgram value = null;


		CQIE rule2 =null;


		  value = dfac.getDatalogProgram();
		  CQIE rule = null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:116:3: ( ( base )? ( directive )* ( rule )+ )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:116:5: ( base )? ( directive )* ( rule )+
			{
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:116:5: ( base )?
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0==BASE) ) {
				alt1=1;
			}
			switch (alt1) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:116:5: base
					{
					pushFollow(FOLLOW_base_in_prog88);
					base();
					state._fsp--;
					if (state.failed) return value;
					}
					break;

			}

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:117:5: ( directive )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==PREFIX) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:117:5: directive
					{
					pushFollow(FOLLOW_directive_in_prog95);
					directive();
					state._fsp--;
					if (state.failed) return value;
					}
					break;

				default :
					break loop2;
				}
			}

			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:118:5: ( rule )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0==COLON||LA3_0==ID_PLAIN||(LA3_0 >= IMPLIES && LA3_0 <= INV_IMPLIES)||(LA3_0 >= STRING_PREFIX && LA3_0 <= STRING_URI)) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:118:6: rule
					{
					pushFollow(FOLLOW_rule_in_prog104);
					rule2=rule();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      rule = rule2;
					      if (isSelectAll) {
					        List<Term> variableList = new Vector<Term>();
					        variableList.addAll(variables); // Import all the data from the Set to a Vector.
					         
					        // Get the head atom
					        Function head = rule.getHead();
					        String name = head.getPredicate().getName();
					        int size = variableList.size(); 
					        
					        // Get the predicate atom
					        Predicate predicate = dfac.getPredicate(name, size);
					        Function newhead = dfac.getFunction(predicate, variableList);
					        rule.updateHead(newhead);
					        
					        isSelectAll = false;  
					      }
					        
					      value.appendRule(rule);
					    }
					}
					break;

				default :
					if ( cnt3 >= 1 ) break loop3;
					if (state.backtracking>0) {state.failed=true; return value;}
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
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
	// $ANTLR end "prog"



	// $ANTLR start "directive"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:141:1: directive : PREFIX prefix LESS uriref GREATER ;
	public final void directive() throws RecognitionException {
		ParserRuleReturnScope prefix3 =null;
		ParserRuleReturnScope uriref4 =null;


		  String prefix = "";
		  String uriref = "";

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:146:3: ( PREFIX prefix LESS uriref GREATER )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:146:5: PREFIX prefix LESS uriref GREATER
			{
			match(input,PREFIX,FOLLOW_PREFIX_in_directive128); if (state.failed) return;
			pushFollow(FOLLOW_prefix_in_directive130);
			prefix3=prefix();
			state._fsp--;
			if (state.failed) return;
			match(input,LESS,FOLLOW_LESS_in_directive132); if (state.failed) return;
			pushFollow(FOLLOW_uriref_in_directive134);
			uriref4=uriref();
			state._fsp--;
			if (state.failed) return;
			match(input,GREATER,FOLLOW_GREATER_in_directive136); if (state.failed) return;
			if ( state.backtracking==0 ) {
			      prefix = (prefix3!=null?input.toString(prefix3.start,prefix3.stop):null);
			      prefix = prefix.substring(0, prefix.length()-1);  
			      if (prefix == null || prefix == "") // if there is no prefix id put the default name.
			        prefix = OBDA_DEFAULT_URI;

			      uriref = (uriref4!=null?input.toString(uriref4.start,uriref4.stop):null);
			      
			      directives.put(prefix, uriref);
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
	}
	// $ANTLR end "directive"



	// $ANTLR start "base"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:158:1: base : BASE LESS uriref GREATER ;
	public final void base() throws RecognitionException {
		ParserRuleReturnScope uriref5 =null;


		  String prefix = OBDA_BASE_URI; // prefix name for the base
		  String uriref = "";

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:163:3: ( BASE LESS uriref GREATER )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:163:5: BASE LESS uriref GREATER
			{
			match(input,BASE,FOLLOW_BASE_in_base156); if (state.failed) return;
			match(input,LESS,FOLLOW_LESS_in_base158); if (state.failed) return;
			pushFollow(FOLLOW_uriref_in_base160);
			uriref5=uriref();
			state._fsp--;
			if (state.failed) return;
			match(input,GREATER,FOLLOW_GREATER_in_base162); if (state.failed) return;
			if ( state.backtracking==0 ) {
			      uriref = (uriref5!=null?input.toString(uriref5.start,uriref5.stop):null);      
			      directives.put(prefix, uriref);
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
	}
	// $ANTLR end "base"



	// $ANTLR start "rule"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:169:1: rule returns [CQIE value] : ( ( ( head )? INV_IMPLIES )=> datalog_syntax_rule | ( ( body )? IMPLIES )=> swirl_syntax_rule );
	public final CQIE rule() throws RecognitionException {
		CQIE value = null;


		CQIE datalog_syntax_rule6 =null;
		CQIE swirl_syntax_rule7 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:170:3: ( ( ( head )? INV_IMPLIES )=> datalog_syntax_rule | ( ( body )? IMPLIES )=> swirl_syntax_rule )
			int alt4=2;
			int LA4_0 = input.LA(1);
			if ( (LA4_0==INV_IMPLIES) && (synpred1_Datalog())) {
				alt4=1;
			}
			else if ( (LA4_0==STRING_URI) ) {
				int LA4_2 = input.LA(2);
				if ( (synpred1_Datalog()) ) {
					alt4=1;
				}
				else if ( (synpred2_Datalog()) ) {
					alt4=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return value;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 4, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA4_0==ID_PLAIN) ) {
				int LA4_3 = input.LA(2);
				if ( (synpred1_Datalog()) ) {
					alt4=1;
				}
				else if ( (synpred2_Datalog()) ) {
					alt4=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return value;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 4, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA4_0==COLON||LA4_0==STRING_PREFIX) ) {
				int LA4_4 = input.LA(2);
				if ( (synpred1_Datalog()) ) {
					alt4=1;
				}
				else if ( (synpred2_Datalog()) ) {
					alt4=2;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return value;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 4, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}
			else if ( (LA4_0==IMPLIES) && (synpred2_Datalog())) {
				alt4=2;
			}

			switch (alt4) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:170:5: ( ( head )? INV_IMPLIES )=> datalog_syntax_rule
					{
					pushFollow(FOLLOW_datalog_syntax_rule_in_rule191);
					datalog_syntax_rule6=datalog_syntax_rule();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { 
					      value = datalog_syntax_rule6;
					    }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:173:5: ( ( body )? IMPLIES )=> swirl_syntax_rule
					{
					pushFollow(FOLLOW_swirl_syntax_rule_in_rule207);
					swirl_syntax_rule7=swirl_syntax_rule();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = swirl_syntax_rule7;
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
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "rule"



	// $ANTLR start "datalog_syntax_rule"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:178:1: datalog_syntax_rule returns [CQIE value] : ( INV_IMPLIES body | datalog_syntax_alt );
	public final CQIE datalog_syntax_rule() throws RecognitionException {
		CQIE value = null;


		List<Function> body8 =null;
		CQIE datalog_syntax_alt9 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:179:3: ( INV_IMPLIES body | datalog_syntax_alt )
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==INV_IMPLIES) ) {
				alt5=1;
			}
			else if ( (LA5_0==COLON||LA5_0==ID_PLAIN||(LA5_0 >= STRING_PREFIX && LA5_0 <= STRING_URI)) ) {
				alt5=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}

			switch (alt5) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:179:5: INV_IMPLIES body
					{
					match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_datalog_syntax_rule226); if (state.failed) return value;
					pushFollow(FOLLOW_body_in_datalog_syntax_rule228);
					body8=body();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = dfac.getCQIE(null, body8);
					    }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:182:5: datalog_syntax_alt
					{
					pushFollow(FOLLOW_datalog_syntax_alt_in_datalog_syntax_rule236);
					datalog_syntax_alt9=datalog_syntax_alt();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = datalog_syntax_alt9;
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
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "datalog_syntax_rule"



	// $ANTLR start "datalog_syntax_alt"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:187:1: datalog_syntax_alt returns [CQIE value] : ( ( head INV_IMPLIES body )=> head INV_IMPLIES body | head INV_IMPLIES );
	public final CQIE datalog_syntax_alt() throws RecognitionException {
		CQIE value = null;


		Function head10 =null;
		List<Function> body11 =null;
		Function head12 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:188:3: ( ( head INV_IMPLIES body )=> head INV_IMPLIES body | head INV_IMPLIES )
			int alt6=2;
			switch ( input.LA(1) ) {
			case STRING_URI:
				{
				int LA6_1 = input.LA(2);
				if ( (synpred3_Datalog()) ) {
					alt6=1;
				}
				else if ( (true) ) {
					alt6=2;
				}

				}
				break;
			case ID_PLAIN:
				{
				int LA6_2 = input.LA(2);
				if ( (synpred3_Datalog()) ) {
					alt6=1;
				}
				else if ( (true) ) {
					alt6=2;
				}

				}
				break;
			case COLON:
			case STRING_PREFIX:
				{
				int LA6_3 = input.LA(2);
				if ( (synpred3_Datalog()) ) {
					alt6=1;
				}
				else if ( (true) ) {
					alt6=2;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 6, 0, input);
				throw nvae;
			}
			switch (alt6) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:188:5: ( head INV_IMPLIES body )=> head INV_IMPLIES body
					{
					pushFollow(FOLLOW_head_in_datalog_syntax_alt264);
					head10=head();
					state._fsp--;
					if (state.failed) return value;
					match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_datalog_syntax_alt266); if (state.failed) return value;
					pushFollow(FOLLOW_body_in_datalog_syntax_alt268);
					body11=body();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = dfac.getCQIE(head10, body11);
					    }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:191:5: head INV_IMPLIES
					{
					pushFollow(FOLLOW_head_in_datalog_syntax_alt276);
					head12=head();
					state._fsp--;
					if (state.failed) return value;
					match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_datalog_syntax_alt278); if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = dfac.getCQIE(head12, new LinkedList<Function>());
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
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "datalog_syntax_alt"



	// $ANTLR start "swirl_syntax_rule"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:196:1: swirl_syntax_rule returns [CQIE value] : ( IMPLIES head | swirl_syntax_alt );
	public final CQIE swirl_syntax_rule() throws RecognitionException {
		CQIE value = null;


		Function head13 =null;
		CQIE swirl_syntax_alt14 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:197:3: ( IMPLIES head | swirl_syntax_alt )
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==IMPLIES) ) {
				alt7=1;
			}
			else if ( (LA7_0==COLON||LA7_0==ID_PLAIN||(LA7_0 >= STRING_PREFIX && LA7_0 <= STRING_URI)) ) {
				alt7=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}

			switch (alt7) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:197:5: IMPLIES head
					{
					match(input,IMPLIES,FOLLOW_IMPLIES_in_swirl_syntax_rule297); if (state.failed) return value;
					pushFollow(FOLLOW_head_in_swirl_syntax_rule299);
					head13=head();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = dfac.getCQIE(head13, new LinkedList<Function>());
					    }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:200:5: swirl_syntax_alt
					{
					pushFollow(FOLLOW_swirl_syntax_alt_in_swirl_syntax_rule307);
					swirl_syntax_alt14=swirl_syntax_alt();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = swirl_syntax_alt14;
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
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "swirl_syntax_rule"



	// $ANTLR start "swirl_syntax_alt"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:205:1: swirl_syntax_alt returns [CQIE value] : ( ( body IMPLIES head )=> body IMPLIES head | body IMPLIES );
	public final CQIE swirl_syntax_alt() throws RecognitionException {
		CQIE value = null;


		Function head15 =null;
		List<Function> body16 =null;
		List<Function> body17 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:206:3: ( ( body IMPLIES head )=> body IMPLIES head | body IMPLIES )
			int alt8=2;
			switch ( input.LA(1) ) {
			case STRING_URI:
				{
				int LA8_1 = input.LA(2);
				if ( (synpred4_Datalog()) ) {
					alt8=1;
				}
				else if ( (true) ) {
					alt8=2;
				}

				}
				break;
			case ID_PLAIN:
				{
				int LA8_2 = input.LA(2);
				if ( (synpred4_Datalog()) ) {
					alt8=1;
				}
				else if ( (true) ) {
					alt8=2;
				}

				}
				break;
			case COLON:
			case STRING_PREFIX:
				{
				int LA8_3 = input.LA(2);
				if ( (synpred4_Datalog()) ) {
					alt8=1;
				}
				else if ( (true) ) {
					alt8=2;
				}

				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}
			switch (alt8) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:206:5: ( body IMPLIES head )=> body IMPLIES head
					{
					pushFollow(FOLLOW_body_in_swirl_syntax_alt335);
					body16=body();
					state._fsp--;
					if (state.failed) return value;
					match(input,IMPLIES,FOLLOW_IMPLIES_in_swirl_syntax_alt337); if (state.failed) return value;
					pushFollow(FOLLOW_head_in_swirl_syntax_alt339);
					head15=head();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = dfac.getCQIE(head15, body16);
					    }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:209:5: body IMPLIES
					{
					pushFollow(FOLLOW_body_in_swirl_syntax_alt347);
					body17=body();
					state._fsp--;
					if (state.failed) return value;
					match(input,IMPLIES,FOLLOW_IMPLIES_in_swirl_syntax_alt349); if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = dfac.getCQIE(null, body17);
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
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "swirl_syntax_alt"



	// $ANTLR start "head"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:214:1: head returns [Function value] : atom ;
	public final Function head() throws RecognitionException {
		Function value = null;


		Function atom18 =null;


		  value = null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:218:3: ( atom )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:218:5: atom
			{
			pushFollow(FOLLOW_atom_in_head373);
			atom18=atom();
			state._fsp--;
			if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      value = atom18;
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
	// $ANTLR end "head"



	// $ANTLR start "body"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:223:1: body returns [List<Function> value] : a1= atom ( ( COMMA | CARET ) a2= atom )* ;
	public final List<Function> body() throws RecognitionException {
		List<Function> value = null;


		Function a1 =null;
		Function a2 =null;


		  value = new LinkedList<Function>();

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:227:3: (a1= atom ( ( COMMA | CARET ) a2= atom )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:227:5: a1= atom ( ( COMMA | CARET ) a2= atom )*
			{
			pushFollow(FOLLOW_atom_in_body399);
			a1=atom();
			state._fsp--;
			if (state.failed) return value;
			if ( state.backtracking==0 ) { value.add(a1); }
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:227:40: ( ( COMMA | CARET ) a2= atom )*
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==CARET||LA9_0==COMMA) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:227:41: ( COMMA | CARET ) a2= atom
					{
					if ( input.LA(1)==CARET||input.LA(1)==COMMA ) {
						input.consume();
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return value;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_atom_in_body412);
					a2=atom();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value.add(a2); }
					}
					break;

				default :
					break loop9;
				}
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
	// $ANTLR end "body"



	// $ANTLR start "atom"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:230:1: atom returns [Function value] : predicate LPAREN ( terms )? RPAREN ;
	public final Function atom() throws RecognitionException {
		Function value = null;


		String predicate19 =null;
		Vector<Term> terms20 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:231:3: ( predicate LPAREN ( terms )? RPAREN )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:231:5: predicate LPAREN ( terms )? RPAREN
			{
			pushFollow(FOLLOW_predicate_in_atom433);
			predicate19=predicate();
			state._fsp--;
			if (state.failed) return value;
			match(input,LPAREN,FOLLOW_LPAREN_in_atom435); if (state.failed) return value;
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:231:22: ( terms )?
			int alt10=2;
			int LA10_0 = input.LA(1);
			if ( (LA10_0==ASTERISK||LA10_0==COLON||LA10_0==DOLLAR||LA10_0==ID_PLAIN||LA10_0==QUESTION||(LA10_0 >= STRING_LITERAL && LA10_0 <= STRING_URI)) ) {
				alt10=1;
			}
			switch (alt10) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:231:22: terms
					{
					pushFollow(FOLLOW_terms_in_atom437);
					terms20=terms();
					state._fsp--;
					if (state.failed) return value;
					}
					break;

			}

			match(input,RPAREN,FOLLOW_RPAREN_in_atom440); if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      String uri = predicate19;
			      
			      Vector<Term> elements = terms20;
			      if (elements == null)
			        elements = new Vector<Term>();
			      Predicate predicate = dfac.getPredicate(uri, elements.size());
			      
			      Vector<Term> terms = terms20;
			      if (terms == null)
			        terms = new Vector<Term>();
			        
			      value = dfac.getFunction(predicate, terms);
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
	// $ANTLR end "atom"



	// $ANTLR start "predicate"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:247:1: predicate returns [String value] : ( full_name | plain_name | qualified_name );
	public final String predicate() throws RecognitionException {
		String value = null;


		String full_name21 =null;
		String plain_name22 =null;
		String qualified_name23 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:248:3: ( full_name | plain_name | qualified_name )
			int alt11=3;
			switch ( input.LA(1) ) {
			case STRING_URI:
				{
				alt11=1;
				}
				break;
			case ID_PLAIN:
				{
				alt11=2;
				}
				break;
			case COLON:
			case STRING_PREFIX:
				{
				alt11=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}
			switch (alt11) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:248:5: full_name
					{
					pushFollow(FOLLOW_full_name_in_predicate460);
					full_name21=full_name();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = full_name21; }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:249:5: plain_name
					{
					pushFollow(FOLLOW_plain_name_in_predicate473);
					plain_name22=plain_name();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = plain_name22; }
					}
					break;
				case 3 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:250:5: qualified_name
					{
					pushFollow(FOLLOW_qualified_name_in_predicate485);
					qualified_name23=qualified_name();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = qualified_name23; }
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
	// $ANTLR end "predicate"



	// $ANTLR start "terms"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:253:1: terms returns [Vector<Term> elements] : t1= term ( COMMA t2= term )* ;
	public final Vector<Term> terms() throws RecognitionException {
		Vector<Term> elements = null;


		Term t1 =null;
		Term t2 =null;


		  elements = new Vector<Term>();

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:257:3: (t1= term ( COMMA t2= term )* )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:257:5: t1= term ( COMMA t2= term )*
			{
			pushFollow(FOLLOW_term_in_terms511);
			t1=term();
			state._fsp--;
			if (state.failed) return elements;
			if ( state.backtracking==0 ) { elements.add(t1); }
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:257:43: ( COMMA t2= term )*
			loop12:
			while (true) {
				int alt12=2;
				int LA12_0 = input.LA(1);
				if ( (LA12_0==COMMA) ) {
					alt12=1;
				}

				switch (alt12) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:257:44: COMMA t2= term
					{
					match(input,COMMA,FOLLOW_COMMA_in_terms516); if (state.failed) return elements;
					pushFollow(FOLLOW_term_in_terms520);
					t2=term();
					state._fsp--;
					if (state.failed) return elements;
					if ( state.backtracking==0 ) { elements.add(t2); }
					}
					break;

				default :
					break loop12;
				}
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
		return elements;
	}
	// $ANTLR end "terms"



	// $ANTLR start "term"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:260:1: term returns [Term value] : ( variable_term | literal_term | object_term | uri_term );
	public final Term term() throws RecognitionException {
		Term value = null;


		Variable variable_term24 =null;
		ValueConstant literal_term25 =null;
		Function object_term26 =null;
		URIConstant uri_term27 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:261:3: ( variable_term | literal_term | object_term | uri_term )
			int alt13=4;
			switch ( input.LA(1) ) {
			case ASTERISK:
			case DOLLAR:
			case QUESTION:
				{
				alt13=1;
				}
				break;
			case STRING_LITERAL:
			case STRING_LITERAL2:
				{
				alt13=2;
				}
				break;
			case STRING_URI:
				{
				int LA13_3 = input.LA(2);
				if ( (LA13_3==LPAREN) ) {
					alt13=3;
				}
				else if ( (LA13_3==COMMA||LA13_3==RPAREN) ) {
					alt13=4;
				}

				else {
					if (state.backtracking>0) {state.failed=true; return value;}
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 13, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case COLON:
			case ID_PLAIN:
			case STRING_PREFIX:
				{
				alt13=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 13, 0, input);
				throw nvae;
			}
			switch (alt13) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:261:5: variable_term
					{
					pushFollow(FOLLOW_variable_term_in_term543);
					variable_term24=variable_term();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = variable_term24; }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:262:5: literal_term
					{
					pushFollow(FOLLOW_literal_term_in_term551);
					literal_term25=literal_term();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = literal_term25; }
					}
					break;
				case 3 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:263:5: object_term
					{
					pushFollow(FOLLOW_object_term_in_term560);
					object_term26=object_term();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = object_term26; }
					}
					break;
				case 4 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:264:5: uri_term
					{
					pushFollow(FOLLOW_uri_term_in_term570);
					uri_term27=uri_term();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = uri_term27; }
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
	// $ANTLR end "term"



	// $ANTLR start "variable_term"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:267:1: variable_term returns [Variable value] : ( ( DOLLAR | QUESTION ) id | ASTERISK );
	public final Variable variable_term() throws RecognitionException {
		Variable value = null;


		ParserRuleReturnScope id28 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:268:3: ( ( DOLLAR | QUESTION ) id | ASTERISK )
			int alt14=2;
			int LA14_0 = input.LA(1);
			if ( (LA14_0==DOLLAR||LA14_0==QUESTION) ) {
				alt14=1;
			}
			else if ( (LA14_0==ASTERISK) ) {
				alt14=2;
			}

			else {
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 14, 0, input);
				throw nvae;
			}

			switch (alt14) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:268:5: ( DOLLAR | QUESTION ) id
					{
					if ( input.LA(1)==DOLLAR||input.LA(1)==QUESTION ) {
						input.consume();
						state.errorRecovery=false;
						state.failed=false;
					}
					else {
						if (state.backtracking>0) {state.failed=true; return value;}
						MismatchedSetException mse = new MismatchedSetException(null,input);
						throw mse;
					}
					pushFollow(FOLLOW_id_in_variable_term601);
					id28=id();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { 
					      value = dfac.getVariable((id28!=null?input.toString(id28.start,id28.stop):null));
					      variables.add(value); // collect the variable terms.
					    }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:272:5: ASTERISK
					{
					match(input,ASTERISK,FOLLOW_ASTERISK_in_variable_term609); if (state.failed) return value;
					if ( state.backtracking==0 ) {
					      value = dfac.getVariable(OBDA_SELECT_ALL);
					      isSelectAll = true;
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
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "variable_term"



	// $ANTLR start "literal_term"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:278:1: literal_term returns [ValueConstant value] : string ;
	public final ValueConstant literal_term() throws RecognitionException {
		ValueConstant value = null;


		ParserRuleReturnScope string29 =null;


		  String literal = "";

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:282:3: ( string )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:282:5: string
			{
			pushFollow(FOLLOW_string_in_literal_term633);
			string29=string();
			state._fsp--;
			if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      literal = (string29!=null?input.toString(string29.start,string29.stop):null);
			      literal = literal.substring(1, literal.length()-1); // removes the quote signs.
			      
			      if (literal.charAt(0) == '<' && literal.charAt(literal.length()-1) == '>') {
			      	literal = directives.get("") + literal.substring(1,literal.length()-1);
			      } else {
			      for (String prefix: directives.keySet()) {
			      	literal = literal.replaceAll("&" + prefix + ";", directives.get(prefix));
			      	}
			      }
			      
			      value = dfac.getConstantLiteral(literal);
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
	// $ANTLR end "literal_term"



	// $ANTLR start "object_term"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:298:1: object_term returns [Function value] : function LPAREN terms RPAREN ;
	public final Function object_term() throws RecognitionException {
		Function value = null;


		String function30 =null;
		Vector<Term> terms31 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:299:3: ( function LPAREN terms RPAREN )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:299:5: function LPAREN terms RPAREN
			{
			pushFollow(FOLLOW_function_in_object_term655);
			function30=function();
			state._fsp--;
			if (state.failed) return value;
			match(input,LPAREN,FOLLOW_LPAREN_in_object_term657); if (state.failed) return value;
			pushFollow(FOLLOW_terms_in_object_term659);
			terms31=terms();
			state._fsp--;
			if (state.failed) return value;
			match(input,RPAREN,FOLLOW_RPAREN_in_object_term661); if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      String functionName = function30;
			      int arity = terms31.size();
			    
			      Predicate functionSymbol = null;  	
			      if (functionName.equals(OBDAVocabulary.RDFS_LITERAL_URI)) {
			    	functionSymbol = dfac.getDataTypePredicateLiteral();
			      } else if (functionName.equals(OBDAVocabulary.XSD_STRING_URI)) {
			    	functionSymbol = dfac.getDataTypePredicateString();
			      } else if (functionName.equals(OBDAVocabulary.XSD_INTEGER_URI)) {
			     	functionSymbol = dfac.getDataTypePredicateInteger();
			      } else if (functionName.equals(OBDAVocabulary.XSD_LONG_URI)) {
			     	functionSymbol = dfac.getDataTypePredicateLong();
			      } else if (functionName.equals(OBDAVocabulary.XSD_DECIMAL_URI)) {
			    	functionSymbol = dfac.getDataTypePredicateDecimal();
			      } else if (functionName.equals(OBDAVocabulary.XSD_DOUBLE_URI)) {
			    	functionSymbol = dfac.getDataTypePredicateDouble();
			      } else if (functionName.equals(OBDAVocabulary.XSD_DATETIME_URI)) {
			    	functionSymbol = dfac.getDataTypePredicateDateTime();
			      } else if (functionName.equals(OBDAVocabulary.XSD_BOOLEAN_URI)) {
			    	functionSymbol = dfac.getDataTypePredicateBoolean();
			      } else if (functionName.equals(OBDAVocabulary.QUEST_URI)) {
			        functionSymbol = dfac.getUriTemplatePredicate(arity);
			      } else {
			        functionSymbol = dfac.getPredicate(functionName, arity);
			      }
			      value = dfac.getFunction(functionSymbol, terms31);
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
	// $ANTLR end "object_term"



	// $ANTLR start "uri_term"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:329:1: uri_term returns [URIConstant value] : uri ;
	public final URIConstant uri_term() throws RecognitionException {
		URIConstant value = null;


		ParserRuleReturnScope uri32 =null;


		  String uriText = "";

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:333:3: ( uri )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:333:5: uri
			{
			pushFollow(FOLLOW_uri_in_uri_term687);
			uri32=uri();
			state._fsp--;
			if (state.failed) return value;
			if ( state.backtracking==0 ) { 
			      uriText = (uri32!=null?input.toString(uri32.start,uri32.stop):null);      
			      value = dfac.getConstantURI(uriText);
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
	// $ANTLR end "uri_term"



	// $ANTLR start "function"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:339:1: function returns [String value] : ( full_name | plain_name | qualified_name );
	public final String function() throws RecognitionException {
		String value = null;


		String full_name33 =null;
		String plain_name34 =null;
		String qualified_name35 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:340:3: ( full_name | plain_name | qualified_name )
			int alt15=3;
			switch ( input.LA(1) ) {
			case STRING_URI:
				{
				alt15=1;
				}
				break;
			case ID_PLAIN:
				{
				alt15=2;
				}
				break;
			case COLON:
			case STRING_PREFIX:
				{
				alt15=3;
				}
				break;
			default:
				if (state.backtracking>0) {state.failed=true; return value;}
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}
			switch (alt15) {
				case 1 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:340:5: full_name
					{
					pushFollow(FOLLOW_full_name_in_function708);
					full_name33=full_name();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = full_name33; }
					}
					break;
				case 2 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:341:5: plain_name
					{
					pushFollow(FOLLOW_plain_name_in_function721);
					plain_name34=plain_name();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = plain_name34; }
					}
					break;
				case 3 :
					// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:342:5: qualified_name
					{
					pushFollow(FOLLOW_qualified_name_in_function733);
					qualified_name35=qualified_name();
					state._fsp--;
					if (state.failed) return value;
					if ( state.backtracking==0 ) { value = qualified_name35; }
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
	// $ANTLR end "function"



	// $ANTLR start "qualified_name"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:345:1: qualified_name returns [String value] : prefix id ;
	public final String qualified_name() throws RecognitionException {
		String value = null;


		ParserRuleReturnScope prefix36 =null;
		ParserRuleReturnScope id37 =null;


		  String prefix = "";
		  String uriref = "";

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:350:3: ( prefix id )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:350:5: prefix id
			{
			pushFollow(FOLLOW_prefix_in_qualified_name757);
			prefix36=prefix();
			state._fsp--;
			if (state.failed) return value;
			pushFollow(FOLLOW_id_in_qualified_name759);
			id37=id();
			state._fsp--;
			if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      prefix = (prefix36!=null?input.toString(prefix36.start,prefix36.stop):null);
			      prefix = prefix.substring(0, prefix.length()-1);
			      if (prefix != null)
			        uriref = directives.get(prefix);
			      else
			        uriref = directives.get(OBDA_DEFAULT_URI);
			      if (uriref == null) {
			      	  //System.out.println("Unknown prefix");          
			          throw new RecognitionException();
			      }
			      
			      String uri = uriref + (id37!=null?input.toString(id37.start,id37.stop):null); // creates the complete Uri string
			      value = uri;
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
	// $ANTLR end "qualified_name"



	// $ANTLR start "plain_name"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:367:1: plain_name returns [String value] : id ;
	public final String plain_name() throws RecognitionException {
		String value = null;


		ParserRuleReturnScope id38 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:368:3: ( id )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:368:5: id
			{
			pushFollow(FOLLOW_id_in_plain_name778);
			id38=id();
			state._fsp--;
			if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      String uriref = directives.get(OBDA_BASE_URI);      
			      String uri = uriref + (id38!=null?input.toString(id38.start,id38.stop):null); // creates the complete Uri string
			      value = uri;
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
	// $ANTLR end "plain_name"



	// $ANTLR start "full_name"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:375:1: full_name returns [String value] : uri ;
	public final String full_name() throws RecognitionException {
		String value = null;


		ParserRuleReturnScope uri39 =null;

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:376:3: ( uri )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:376:5: uri
			{
			pushFollow(FOLLOW_uri_in_full_name797);
			uri39=uri();
			state._fsp--;
			if (state.failed) return value;
			if ( state.backtracking==0 ) {
			      value = (uri39!=null?input.toString(uri39.start,uri39.stop):null);
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
	// $ANTLR end "full_name"


	public static class uri_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "uri"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:381:1: uri : STRING_URI ;
	public final DatalogParser.uri_return uri() throws RecognitionException {
		DatalogParser.uri_return retval = new DatalogParser.uri_return();
		retval.start = input.LT(1);

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:382:3: ( STRING_URI )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:382:5: STRING_URI
			{
			match(input,STRING_URI,FOLLOW_STRING_URI_in_uri812); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "uri"


	public static class uriref_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "uriref"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:385:1: uriref : STRING_URI ;
	public final DatalogParser.uriref_return uriref() throws RecognitionException {
		DatalogParser.uriref_return retval = new DatalogParser.uriref_return();
		retval.start = input.LT(1);

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:386:3: ( STRING_URI )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:386:5: STRING_URI
			{
			match(input,STRING_URI,FOLLOW_STRING_URI_in_uriref825); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "uriref"


	public static class id_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "id"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:389:1: id : ID_PLAIN ;
	public final DatalogParser.id_return id() throws RecognitionException {
		DatalogParser.id_return retval = new DatalogParser.id_return();
		retval.start = input.LT(1);

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:390:3: ( ID_PLAIN )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:390:5: ID_PLAIN
			{
			match(input,ID_PLAIN,FOLLOW_ID_PLAIN_in_id838); if (state.failed) return retval;
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "id"


	public static class prefix_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "prefix"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:393:1: prefix : ( STRING_PREFIX | COLON );
	public final DatalogParser.prefix_return prefix() throws RecognitionException {
		DatalogParser.prefix_return retval = new DatalogParser.prefix_return();
		retval.start = input.LT(1);

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:394:3: ( STRING_PREFIX | COLON )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( input.LA(1)==COLON||input.LA(1)==STRING_PREFIX ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "prefix"


	public static class string_return extends ParserRuleReturnScope {
	};


	// $ANTLR start "string"
	// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:398:1: string : ( STRING_LITERAL | STRING_LITERAL2 );
	public final DatalogParser.string_return string() throws RecognitionException {
		DatalogParser.string_return retval = new DatalogParser.string_return();
		retval.start = input.LT(1);

		try {
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:399:3: ( STRING_LITERAL | STRING_LITERAL2 )
			// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:
			{
			if ( (input.LA(1) >= STRING_LITERAL && input.LA(1) <= STRING_LITERAL2) ) {
				input.consume();
				state.errorRecovery=false;
				state.failed=false;
			}
			else {
				if (state.backtracking>0) {state.failed=true; return retval;}
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "string"

	// $ANTLR start synpred1_Datalog
	public final void synpred1_Datalog_fragment() throws RecognitionException {
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:170:5: ( ( head )? INV_IMPLIES )
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:170:6: ( head )? INV_IMPLIES
		{
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:170:6: ( head )?
		int alt16=2;
		int LA16_0 = input.LA(1);
		if ( (LA16_0==COLON||LA16_0==ID_PLAIN||(LA16_0 >= STRING_PREFIX && LA16_0 <= STRING_URI)) ) {
			alt16=1;
		}
		switch (alt16) {
			case 1 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:170:6: head
				{
				pushFollow(FOLLOW_head_in_synpred1_Datalog184);
				head();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_synpred1_Datalog187); if (state.failed) return;
		}

	}
	// $ANTLR end synpred1_Datalog

	// $ANTLR start synpred2_Datalog
	public final void synpred2_Datalog_fragment() throws RecognitionException {
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:173:5: ( ( body )? IMPLIES )
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:173:6: ( body )? IMPLIES
		{
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:173:6: ( body )?
		int alt17=2;
		int LA17_0 = input.LA(1);
		if ( (LA17_0==COLON||LA17_0==ID_PLAIN||(LA17_0 >= STRING_PREFIX && LA17_0 <= STRING_URI)) ) {
			alt17=1;
		}
		switch (alt17) {
			case 1 :
				// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:173:6: body
				{
				pushFollow(FOLLOW_body_in_synpred2_Datalog200);
				body();
				state._fsp--;
				if (state.failed) return;
				}
				break;

		}

		match(input,IMPLIES,FOLLOW_IMPLIES_in_synpred2_Datalog203); if (state.failed) return;
		}

	}
	// $ANTLR end synpred2_Datalog

	// $ANTLR start synpred3_Datalog
	public final void synpred3_Datalog_fragment() throws RecognitionException {
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:188:5: ( head INV_IMPLIES body )
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:188:6: head INV_IMPLIES body
		{
		pushFollow(FOLLOW_head_in_synpred3_Datalog256);
		head();
		state._fsp--;
		if (state.failed) return;
		match(input,INV_IMPLIES,FOLLOW_INV_IMPLIES_in_synpred3_Datalog258); if (state.failed) return;
		pushFollow(FOLLOW_body_in_synpred3_Datalog260);
		body();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred3_Datalog

	// $ANTLR start synpred4_Datalog
	public final void synpred4_Datalog_fragment() throws RecognitionException {
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:206:5: ( body IMPLIES head )
		// /Users/Sarah/Projects/ontop/obdalib-core/src/main/java/it/unibz/krdb/obda/parser/Datalog.g:206:6: body IMPLIES head
		{
		pushFollow(FOLLOW_body_in_synpred4_Datalog327);
		body();
		state._fsp--;
		if (state.failed) return;
		match(input,IMPLIES,FOLLOW_IMPLIES_in_synpred4_Datalog329); if (state.failed) return;
		pushFollow(FOLLOW_head_in_synpred4_Datalog331);
		head();
		state._fsp--;
		if (state.failed) return;
		}

	}
	// $ANTLR end synpred4_Datalog

	// Delegated rules

	public final boolean synpred4_Datalog() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred4_Datalog_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred1_Datalog() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred1_Datalog_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred3_Datalog() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred3_Datalog_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}
	public final boolean synpred2_Datalog() {
		state.backtracking++;
		int start = input.mark();
		try {
			synpred2_Datalog_fragment(); // can never throw exception
		} catch (RecognitionException re) {
			System.err.println("impossible: "+re);
		}
		boolean success = !state.failed;
		input.rewind(start);
		state.backtracking--;
		state.failed=false;
		return success;
	}



	public static final BitSet FOLLOW_prog_in_parse54 = new BitSet(new long[]{0x0000000000000000L});
	public static final BitSet FOLLOW_EOF_in_parse56 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_base_in_prog88 = new BitSet(new long[]{0x0001800834002000L});
	public static final BitSet FOLLOW_directive_in_prog95 = new BitSet(new long[]{0x0001800834002000L});
	public static final BitSet FOLLOW_rule_in_prog104 = new BitSet(new long[]{0x0001800034002002L});
	public static final BitSet FOLLOW_PREFIX_in_directive128 = new BitSet(new long[]{0x0000800000002000L});
	public static final BitSet FOLLOW_prefix_in_directive130 = new BitSet(new long[]{0x0000000040000000L});
	public static final BitSet FOLLOW_LESS_in_directive132 = new BitSet(new long[]{0x0001000000000000L});
	public static final BitSet FOLLOW_uriref_in_directive134 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_GREATER_in_directive136 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BASE_in_base156 = new BitSet(new long[]{0x0000000040000000L});
	public static final BitSet FOLLOW_LESS_in_base158 = new BitSet(new long[]{0x0001000000000000L});
	public static final BitSet FOLLOW_uriref_in_base160 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_GREATER_in_base162 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_datalog_syntax_rule_in_rule191 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_swirl_syntax_rule_in_rule207 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INV_IMPLIES_in_datalog_syntax_rule226 = new BitSet(new long[]{0x0001800004002000L});
	public static final BitSet FOLLOW_body_in_datalog_syntax_rule228 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_datalog_syntax_alt_in_datalog_syntax_rule236 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_head_in_datalog_syntax_alt264 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_INV_IMPLIES_in_datalog_syntax_alt266 = new BitSet(new long[]{0x0001800004002000L});
	public static final BitSet FOLLOW_body_in_datalog_syntax_alt268 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_head_in_datalog_syntax_alt276 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_INV_IMPLIES_in_datalog_syntax_alt278 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMPLIES_in_swirl_syntax_rule297 = new BitSet(new long[]{0x0001800004002000L});
	public static final BitSet FOLLOW_head_in_swirl_syntax_rule299 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_swirl_syntax_alt_in_swirl_syntax_rule307 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_body_in_swirl_syntax_alt335 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_IMPLIES_in_swirl_syntax_alt337 = new BitSet(new long[]{0x0001800004002000L});
	public static final BitSet FOLLOW_head_in_swirl_syntax_alt339 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_body_in_swirl_syntax_alt347 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_IMPLIES_in_swirl_syntax_alt349 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_atom_in_head373 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_atom_in_body399 = new BitSet(new long[]{0x0000000000005002L});
	public static final BitSet FOLLOW_set_in_body404 = new BitSet(new long[]{0x0001800004002000L});
	public static final BitSet FOLLOW_atom_in_body412 = new BitSet(new long[]{0x0000000000005002L});
	public static final BitSet FOLLOW_predicate_in_atom433 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_LPAREN_in_atom435 = new BitSet(new long[]{0x0001E11004022100L});
	public static final BitSet FOLLOW_terms_in_atom437 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_RPAREN_in_atom440 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_full_name_in_predicate460 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_plain_name_in_predicate473 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualified_name_in_predicate485 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_term_in_terms511 = new BitSet(new long[]{0x0000000000004002L});
	public static final BitSet FOLLOW_COMMA_in_terms516 = new BitSet(new long[]{0x0001E01004022100L});
	public static final BitSet FOLLOW_term_in_terms520 = new BitSet(new long[]{0x0000000000004002L});
	public static final BitSet FOLLOW_variable_term_in_term543 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_literal_term_in_term551 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_object_term_in_term560 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_uri_term_in_term570 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_set_in_variable_term595 = new BitSet(new long[]{0x0000000004000000L});
	public static final BitSet FOLLOW_id_in_variable_term601 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ASTERISK_in_variable_term609 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_in_literal_term633 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_function_in_object_term655 = new BitSet(new long[]{0x0000000080000000L});
	public static final BitSet FOLLOW_LPAREN_in_object_term657 = new BitSet(new long[]{0x0001E01004022100L});
	public static final BitSet FOLLOW_terms_in_object_term659 = new BitSet(new long[]{0x0000010000000000L});
	public static final BitSet FOLLOW_RPAREN_in_object_term661 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_uri_in_uri_term687 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_full_name_in_function708 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_plain_name_in_function721 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_qualified_name_in_function733 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_prefix_in_qualified_name757 = new BitSet(new long[]{0x0000000004000000L});
	public static final BitSet FOLLOW_id_in_qualified_name759 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_id_in_plain_name778 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_uri_in_full_name797 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_URI_in_uri812 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_URI_in_uriref825 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ID_PLAIN_in_id838 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_head_in_synpred1_Datalog184 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_INV_IMPLIES_in_synpred1_Datalog187 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_body_in_synpred2_Datalog200 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_IMPLIES_in_synpred2_Datalog203 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_head_in_synpred3_Datalog256 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_INV_IMPLIES_in_synpred3_Datalog258 = new BitSet(new long[]{0x0001800004002000L});
	public static final BitSet FOLLOW_body_in_synpred3_Datalog260 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_body_in_synpred4_Datalog327 = new BitSet(new long[]{0x0000000010000000L});
	public static final BitSet FOLLOW_IMPLIES_in_synpred4_Datalog329 = new BitSet(new long[]{0x0001800004002000L});
	public static final BitSet FOLLOW_head_in_synpred4_Datalog331 = new BitSet(new long[]{0x0000000000000002L});
}
