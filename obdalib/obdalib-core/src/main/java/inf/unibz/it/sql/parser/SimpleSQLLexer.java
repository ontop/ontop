// $ANTLR 3.1.2 /home/obda/SimpleSQL.g 2009-09-09 10:41:25

package inf.unibz.it.sql.parser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SimpleSQLLexer extends Lexer {
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__66=66;
    public static final int T__67=67;
    public static final int T__64=64;
    public static final int STAR=17;
    public static final int T__65=65;
    public static final int LSQUARE=10;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int MOD=19;
    public static final int BITWISEXOR=22;
    public static final int Exponent=33;
    public static final int T__61=61;
    public static final int T__60=60;
    public static final int EOF=-1;
    public static final int Identifier=9;
    public static final int LPAREN=6;
    public static final int NOTEQUAL=24;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int GREATERTHANOREQUALTO=27;
    public static final int RPAREN=7;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int Number=4;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int COMMA=5;
    public static final int DateLiteral=13;
    public static final int T__59=59;
    public static final int EQUAL=23;
    public static final int TILDE=15;
    public static final int PLUS=16;
    public static final int DOT=12;
    public static final int T__50=50;
    public static final int DIVIDE=18;
    public static final int GREATERTHAN=28;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int LESSTHAN=26;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__80=80;
    public static final int T__47=47;
    public static final int T__81=81;
    public static final int T__44=44;
    public static final int T__82=82;
    public static final int T__45=45;
    public static final int T__83=83;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int AMPERSAND=20;
    public static final int SEMICOLON=30;
    public static final int RSQUARE=11;
    public static final int MINUS=14;
    public static final int T__85=85;
    public static final int Digit=32;
    public static final int T__84=84;
    public static final int T__87=87;
    public static final int T__86=86;
    public static final int BITWISEOR=21;
    public static final int T__88=88;
    public static final int StringLiteral=8;
    public static final int COLON=29;
    public static final int T__71=71;
    public static final int WS=34;
    public static final int T__72=72;
    public static final int T__35=35;
    public static final int T__70=70;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int LESSTHANOREQUALTO=25;
    public static final int T__76=76;
    public static final int T__75=75;
    public static final int T__74=74;
    public static final int T__73=73;
    public static final int Letter=31;
    public static final int T__79=79;
    public static final int T__78=78;
    public static final int T__77=77;

    // delegates
    // delegators

    public SimpleSQLLexer() {;} 
    public SimpleSQLLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public SimpleSQLLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/obda/SimpleSQL.g"; }

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:7:7: ( 'select' )
            // /home/obda/SimpleSQL.g:7:9: 'select'
            {
            match("select"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:8:7: ( 'SELECT' )
            // /home/obda/SimpleSQL.g:8:9: 'SELECT'
            {
            match("SELECT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:9:7: ( 'all' )
            // /home/obda/SimpleSQL.g:9:9: 'all'
            {
            match("all"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:10:7: ( 'distinct' )
            // /home/obda/SimpleSQL.g:10:9: 'distinct'
            {
            match("distinct"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:11:7: ( 'ALL' )
            // /home/obda/SimpleSQL.g:11:9: 'ALL'
            {
            match("ALL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:12:7: ( 'DISTINCT' )
            // /home/obda/SimpleSQL.g:12:9: 'DISTINCT'
            {
            match("DISTINCT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:13:7: ( 'where' )
            // /home/obda/SimpleSQL.g:13:9: 'where'
            {
            match("where"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:14:7: ( 'WHERE' )
            // /home/obda/SimpleSQL.g:14:9: 'WHERE'
            {
            match("WHERE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:15:7: ( 'limit' )
            // /home/obda/SimpleSQL.g:15:9: 'limit'
            {
            match("limit"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:16:7: ( 'LIMIT' )
            // /home/obda/SimpleSQL.g:16:9: 'LIMIT'
            {
            match("LIMIT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "T__45"
    public final void mT__45() throws RecognitionException {
        try {
            int _type = T__45;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:17:7: ( 'order' )
            // /home/obda/SimpleSQL.g:17:9: 'order'
            {
            match("order"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__45"

    // $ANTLR start "T__46"
    public final void mT__46() throws RecognitionException {
        try {
            int _type = T__46;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:18:7: ( 'by' )
            // /home/obda/SimpleSQL.g:18:9: 'by'
            {
            match("by"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__46"

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:19:7: ( 'ORDER' )
            // /home/obda/SimpleSQL.g:19:9: 'ORDER'
            {
            match("ORDER"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "T__48"
    public final void mT__48() throws RecognitionException {
        try {
            int _type = T__48;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:20:7: ( 'BY' )
            // /home/obda/SimpleSQL.g:20:9: 'BY'
            {
            match("BY"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__48"

    // $ANTLR start "T__49"
    public final void mT__49() throws RecognitionException {
        try {
            int _type = T__49;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:21:7: ( 'asc' )
            // /home/obda/SimpleSQL.g:21:9: 'asc'
            {
            match("asc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__49"

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:22:7: ( 'desc' )
            // /home/obda/SimpleSQL.g:22:9: 'desc'
            {
            match("desc"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__50"

    // $ANTLR start "T__51"
    public final void mT__51() throws RecognitionException {
        try {
            int _type = T__51;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:23:7: ( 'ASC' )
            // /home/obda/SimpleSQL.g:23:9: 'ASC'
            {
            match("ASC"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__51"

    // $ANTLR start "T__52"
    public final void mT__52() throws RecognitionException {
        try {
            int _type = T__52;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:24:7: ( 'DESC' )
            // /home/obda/SimpleSQL.g:24:9: 'DESC'
            {
            match("DESC"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__52"

    // $ANTLR start "T__53"
    public final void mT__53() throws RecognitionException {
        try {
            int _type = T__53;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:25:7: ( 'and' )
            // /home/obda/SimpleSQL.g:25:9: 'and'
            {
            match("and"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__53"

    // $ANTLR start "T__54"
    public final void mT__54() throws RecognitionException {
        try {
            int _type = T__54;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:26:7: ( 'or' )
            // /home/obda/SimpleSQL.g:26:9: 'or'
            {
            match("or"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__54"

    // $ANTLR start "T__55"
    public final void mT__55() throws RecognitionException {
        try {
            int _type = T__55;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:27:7: ( 'AND' )
            // /home/obda/SimpleSQL.g:27:9: 'AND'
            {
            match("AND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__55"

    // $ANTLR start "T__56"
    public final void mT__56() throws RecognitionException {
        try {
            int _type = T__56;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:28:7: ( 'OR' )
            // /home/obda/SimpleSQL.g:28:9: 'OR'
            {
            match("OR"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__56"

    // $ANTLR start "T__57"
    public final void mT__57() throws RecognitionException {
        try {
            int _type = T__57;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:29:7: ( 'group' )
            // /home/obda/SimpleSQL.g:29:9: 'group'
            {
            match("group"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__57"

    // $ANTLR start "T__58"
    public final void mT__58() throws RecognitionException {
        try {
            int _type = T__58;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:30:7: ( 'not' )
            // /home/obda/SimpleSQL.g:30:9: 'not'
            {
            match("not"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__58"

    // $ANTLR start "T__59"
    public final void mT__59() throws RecognitionException {
        try {
            int _type = T__59;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:31:7: ( 'NOT' )
            // /home/obda/SimpleSQL.g:31:9: 'NOT'
            {
            match("NOT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__59"

    // $ANTLR start "T__60"
    public final void mT__60() throws RecognitionException {
        try {
            int _type = T__60;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:32:7: ( 'is not' )
            // /home/obda/SimpleSQL.g:32:9: 'is not'
            {
            match("is not"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__60"

    // $ANTLR start "T__61"
    public final void mT__61() throws RecognitionException {
        try {
            int _type = T__61;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:33:7: ( 'IS NOT' )
            // /home/obda/SimpleSQL.g:33:9: 'IS NOT'
            {
            match("IS NOT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__61"

    // $ANTLR start "T__62"
    public final void mT__62() throws RecognitionException {
        try {
            int _type = T__62;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:34:7: ( 'like' )
            // /home/obda/SimpleSQL.g:34:9: 'like'
            {
            match("like"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__62"

    // $ANTLR start "T__63"
    public final void mT__63() throws RecognitionException {
        try {
            int _type = T__63;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:35:7: ( 'LIKE' )
            // /home/obda/SimpleSQL.g:35:9: 'LIKE'
            {
            match("LIKE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__63"

    // $ANTLR start "T__64"
    public final void mT__64() throws RecognitionException {
        try {
            int _type = T__64;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:36:7: ( 'in' )
            // /home/obda/SimpleSQL.g:36:9: 'in'
            {
            match("in"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__64"

    // $ANTLR start "T__65"
    public final void mT__65() throws RecognitionException {
        try {
            int _type = T__65;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:37:7: ( 'IN' )
            // /home/obda/SimpleSQL.g:37:9: 'IN'
            {
            match("IN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__65"

    // $ANTLR start "T__66"
    public final void mT__66() throws RecognitionException {
        try {
            int _type = T__66;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:38:7: ( 'is null' )
            // /home/obda/SimpleSQL.g:38:9: 'is null'
            {
            match("is null"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__66"

    // $ANTLR start "T__67"
    public final void mT__67() throws RecognitionException {
        try {
            int _type = T__67;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:39:7: ( 'IS NULL' )
            // /home/obda/SimpleSQL.g:39:9: 'IS NULL'
            {
            match("IS NULL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__67"

    // $ANTLR start "T__68"
    public final void mT__68() throws RecognitionException {
        try {
            int _type = T__68;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:40:7: ( 'is not null' )
            // /home/obda/SimpleSQL.g:40:9: 'is not null'
            {
            match("is not null"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__68"

    // $ANTLR start "T__69"
    public final void mT__69() throws RecognitionException {
        try {
            int _type = T__69;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:41:7: ( 'IS NOT NULL' )
            // /home/obda/SimpleSQL.g:41:9: 'IS NOT NULL'
            {
            match("IS NOT NULL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__69"

    // $ANTLR start "T__70"
    public final void mT__70() throws RecognitionException {
        try {
            int _type = T__70;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:42:7: ( 'as' )
            // /home/obda/SimpleSQL.g:42:9: 'as'
            {
            match("as"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__70"

    // $ANTLR start "T__71"
    public final void mT__71() throws RecognitionException {
        try {
            int _type = T__71;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:43:7: ( 'from' )
            // /home/obda/SimpleSQL.g:43:9: 'from'
            {
            match("from"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__71"

    // $ANTLR start "T__72"
    public final void mT__72() throws RecognitionException {
        try {
            int _type = T__72;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:44:7: ( 'FROM' )
            // /home/obda/SimpleSQL.g:44:9: 'FROM'
            {
            match("FROM"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__72"

    // $ANTLR start "T__73"
    public final void mT__73() throws RecognitionException {
        try {
            int _type = T__73;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:45:7: ( 'AS' )
            // /home/obda/SimpleSQL.g:45:9: 'AS'
            {
            match("AS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__73"

    // $ANTLR start "T__74"
    public final void mT__74() throws RecognitionException {
        try {
            int _type = T__74;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:46:7: ( 'case' )
            // /home/obda/SimpleSQL.g:46:9: 'case'
            {
            match("case"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__74"

    // $ANTLR start "T__75"
    public final void mT__75() throws RecognitionException {
        try {
            int _type = T__75;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:47:7: ( 'when' )
            // /home/obda/SimpleSQL.g:47:9: 'when'
            {
            match("when"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__75"

    // $ANTLR start "T__76"
    public final void mT__76() throws RecognitionException {
        try {
            int _type = T__76;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:48:7: ( 'then' )
            // /home/obda/SimpleSQL.g:48:9: 'then'
            {
            match("then"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__76"

    // $ANTLR start "T__77"
    public final void mT__77() throws RecognitionException {
        try {
            int _type = T__77;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:49:7: ( 'else' )
            // /home/obda/SimpleSQL.g:49:9: 'else'
            {
            match("else"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__77"

    // $ANTLR start "T__78"
    public final void mT__78() throws RecognitionException {
        try {
            int _type = T__78;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:50:7: ( 'end' )
            // /home/obda/SimpleSQL.g:50:9: 'end'
            {
            match("end"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__78"

    // $ANTLR start "T__79"
    public final void mT__79() throws RecognitionException {
        try {
            int _type = T__79;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:51:7: ( 'any' )
            // /home/obda/SimpleSQL.g:51:9: 'any'
            {
            match("any"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__79"

    // $ANTLR start "T__80"
    public final void mT__80() throws RecognitionException {
        try {
            int _type = T__80;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:52:7: ( 'exists' )
            // /home/obda/SimpleSQL.g:52:9: 'exists'
            {
            match("exists"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__80"

    // $ANTLR start "T__81"
    public final void mT__81() throws RecognitionException {
        try {
            int _type = T__81;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:53:7: ( 'some' )
            // /home/obda/SimpleSQL.g:53:9: 'some'
            {
            match("some"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__81"

    // $ANTLR start "T__82"
    public final void mT__82() throws RecognitionException {
        try {
            int _type = T__82;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:54:7: ( 'ANY' )
            // /home/obda/SimpleSQL.g:54:9: 'ANY'
            {
            match("ANY"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__82"

    // $ANTLR start "T__83"
    public final void mT__83() throws RecognitionException {
        try {
            int _type = T__83;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:55:7: ( 'EXISTS' )
            // /home/obda/SimpleSQL.g:55:9: 'EXISTS'
            {
            match("EXISTS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__83"

    // $ANTLR start "T__84"
    public final void mT__84() throws RecognitionException {
        try {
            int _type = T__84;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:56:7: ( 'SOME' )
            // /home/obda/SimpleSQL.g:56:9: 'SOME'
            {
            match("SOME"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__84"

    // $ANTLR start "T__85"
    public final void mT__85() throws RecognitionException {
        try {
            int _type = T__85;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:57:7: ( 'true' )
            // /home/obda/SimpleSQL.g:57:9: 'true'
            {
            match("true"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__85"

    // $ANTLR start "T__86"
    public final void mT__86() throws RecognitionException {
        try {
            int _type = T__86;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:58:7: ( 'false' )
            // /home/obda/SimpleSQL.g:58:9: 'false'
            {
            match("false"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__86"

    // $ANTLR start "T__87"
    public final void mT__87() throws RecognitionException {
        try {
            int _type = T__87;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:59:7: ( 'TRUE' )
            // /home/obda/SimpleSQL.g:59:9: 'TRUE'
            {
            match("TRUE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__87"

    // $ANTLR start "T__88"
    public final void mT__88() throws RecognitionException {
        try {
            int _type = T__88;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:60:7: ( 'FALSE' )
            // /home/obda/SimpleSQL.g:60:9: 'FALSE'
            {
            match("FALSE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__88"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:601:5: ( '.' )
            // /home/obda/SimpleSQL.g:601:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:602:7: ( ':' )
            // /home/obda/SimpleSQL.g:602:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:603:7: ( ',' )
            // /home/obda/SimpleSQL.g:603:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "SEMICOLON"
    public final void mSEMICOLON() throws RecognitionException {
        try {
            int _type = SEMICOLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:604:11: ( ';' )
            // /home/obda/SimpleSQL.g:604:13: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMICOLON"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:606:8: ( '(' )
            // /home/obda/SimpleSQL.g:606:10: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:607:8: ( ')' )
            // /home/obda/SimpleSQL.g:607:10: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "LSQUARE"
    public final void mLSQUARE() throws RecognitionException {
        try {
            int _type = LSQUARE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:608:9: ( '[' )
            // /home/obda/SimpleSQL.g:608:11: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LSQUARE"

    // $ANTLR start "RSQUARE"
    public final void mRSQUARE() throws RecognitionException {
        try {
            int _type = RSQUARE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:609:9: ( ']' )
            // /home/obda/SimpleSQL.g:609:11: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RSQUARE"

    // $ANTLR start "EQUAL"
    public final void mEQUAL() throws RecognitionException {
        try {
            int _type = EQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:611:7: ( '=' )
            // /home/obda/SimpleSQL.g:611:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUAL"

    // $ANTLR start "NOTEQUAL"
    public final void mNOTEQUAL() throws RecognitionException {
        try {
            int _type = NOTEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:612:10: ( '<>' )
            // /home/obda/SimpleSQL.g:612:12: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOTEQUAL"

    // $ANTLR start "LESSTHANOREQUALTO"
    public final void mLESSTHANOREQUALTO() throws RecognitionException {
        try {
            int _type = LESSTHANOREQUALTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:613:19: ( '<=' )
            // /home/obda/SimpleSQL.g:613:21: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSTHANOREQUALTO"

    // $ANTLR start "LESSTHAN"
    public final void mLESSTHAN() throws RecognitionException {
        try {
            int _type = LESSTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:614:10: ( '<' )
            // /home/obda/SimpleSQL.g:614:12: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSTHAN"

    // $ANTLR start "GREATERTHANOREQUALTO"
    public final void mGREATERTHANOREQUALTO() throws RecognitionException {
        try {
            int _type = GREATERTHANOREQUALTO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:615:22: ( '>=' )
            // /home/obda/SimpleSQL.g:615:24: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATERTHANOREQUALTO"

    // $ANTLR start "GREATERTHAN"
    public final void mGREATERTHAN() throws RecognitionException {
        try {
            int _type = GREATERTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:616:13: ( '>' )
            // /home/obda/SimpleSQL.g:616:15: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATERTHAN"

    // $ANTLR start "DIVIDE"
    public final void mDIVIDE() throws RecognitionException {
        try {
            int _type = DIVIDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:618:8: ( '/' )
            // /home/obda/SimpleSQL.g:618:10: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DIVIDE"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:619:6: ( '+' )
            // /home/obda/SimpleSQL.g:619:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:620:7: ( '-' )
            // /home/obda/SimpleSQL.g:620:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "STAR"
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:621:6: ( '*' )
            // /home/obda/SimpleSQL.g:621:8: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STAR"

    // $ANTLR start "MOD"
    public final void mMOD() throws RecognitionException {
        try {
            int _type = MOD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:622:5: ( '%' )
            // /home/obda/SimpleSQL.g:622:7: '%'
            {
            match('%'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MOD"

    // $ANTLR start "AMPERSAND"
    public final void mAMPERSAND() throws RecognitionException {
        try {
            int _type = AMPERSAND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:624:11: ( '&' )
            // /home/obda/SimpleSQL.g:624:13: '&'
            {
            match('&'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AMPERSAND"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:625:7: ( '~' )
            // /home/obda/SimpleSQL.g:625:9: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "BITWISEOR"
    public final void mBITWISEOR() throws RecognitionException {
        try {
            int _type = BITWISEOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:626:11: ( '|' )
            // /home/obda/SimpleSQL.g:626:13: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISEOR"

    // $ANTLR start "BITWISEXOR"
    public final void mBITWISEXOR() throws RecognitionException {
        try {
            int _type = BITWISEXOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:627:12: ( '^' )
            // /home/obda/SimpleSQL.g:627:14: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BITWISEXOR"

    // $ANTLR start "Letter"
    public final void mLetter() throws RecognitionException {
        try {
            // /home/obda/SimpleSQL.g:636:5: ( 'a' .. 'z' | '_' )
            // /home/obda/SimpleSQL.g:
            {
            if ( input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Letter"

    // $ANTLR start "Digit"
    public final void mDigit() throws RecognitionException {
        try {
            // /home/obda/SimpleSQL.g:641:5: ( '0' .. '9' )
            // /home/obda/SimpleSQL.g:642:5: '0' .. '9'
            {
            matchRange('0','9'); 

            }

        }
        finally {
        }
    }
    // $ANTLR end "Digit"

    // $ANTLR start "Exponent"
    public final void mExponent() throws RecognitionException {
        try {
            // /home/obda/SimpleSQL.g:647:5: ( 'e' ( PLUS | MINUS )? ( Digit )+ )
            // /home/obda/SimpleSQL.g:648:5: 'e' ( PLUS | MINUS )? ( Digit )+
            {
            match('e'); 
            // /home/obda/SimpleSQL.g:648:9: ( PLUS | MINUS )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='+'||LA1_0=='-') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/obda/SimpleSQL.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            // /home/obda/SimpleSQL.g:648:25: ( Digit )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:648:26: Digit
            	    {
            	    mDigit(); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

        }
        finally {
        }
    }
    // $ANTLR end "Exponent"

    // $ANTLR start "StringLiteral"
    public final void mStringLiteral() throws RecognitionException {
        try {
            int _type = StringLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:652:5: ( '\\'' (~ '\\'' )* '\\'' ( '\\'' (~ '\\'' )* '\\'' )* )
            // /home/obda/SimpleSQL.g:653:5: '\\'' (~ '\\'' )* '\\'' ( '\\'' (~ '\\'' )* '\\'' )*
            {
            match('\''); 
            // /home/obda/SimpleSQL.g:653:10: (~ '\\'' )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='\u0000' && LA3_0<='&')||(LA3_0>='(' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:653:11: ~ '\\''
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\''); 
            // /home/obda/SimpleSQL.g:653:24: ( '\\'' (~ '\\'' )* '\\'' )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\'') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:653:26: '\\'' (~ '\\'' )* '\\''
            	    {
            	    match('\''); 
            	    // /home/obda/SimpleSQL.g:653:31: (~ '\\'' )*
            	    loop4:
            	    do {
            	        int alt4=2;
            	        int LA4_0 = input.LA(1);

            	        if ( ((LA4_0>='\u0000' && LA4_0<='&')||(LA4_0>='(' && LA4_0<='\uFFFF')) ) {
            	            alt4=1;
            	        }


            	        switch (alt4) {
            	    	case 1 :
            	    	    // /home/obda/SimpleSQL.g:653:32: ~ '\\''
            	    	    {
            	    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
            	    	        input.consume();

            	    	    }
            	    	    else {
            	    	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	    	        recover(mse);
            	    	        throw mse;}


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop4;
            	        }
            	    } while (true);

            	    match('\''); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "StringLiteral"

    // $ANTLR start "DateLiteral"
    public final void mDateLiteral() throws RecognitionException {
        try {
            int _type = DateLiteral;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:657:5: ( '#' (~ '#' )* '#' )
            // /home/obda/SimpleSQL.g:658:5: '#' (~ '#' )* '#'
            {
            match('#'); 
            // /home/obda/SimpleSQL.g:658:9: (~ '#' )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='\u0000' && LA6_0<='\"')||(LA6_0>='$' && LA6_0<='\uFFFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:658:10: ~ '#'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\"')||(input.LA(1)>='$' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match('#'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DateLiteral"

    // $ANTLR start "Number"
    public final void mNumber() throws RecognitionException {
        try {
            int _type = Number;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:665:5: ( ( Digit )+ ( DOT ( Digit )* ( Exponent )? | Exponent )? )
            // /home/obda/SimpleSQL.g:666:5: ( Digit )+ ( DOT ( Digit )* ( Exponent )? | Exponent )?
            {
            // /home/obda/SimpleSQL.g:666:5: ( Digit )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='0' && LA7_0<='9')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:666:6: Digit
            	    {
            	    mDigit(); 

            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);

            // /home/obda/SimpleSQL.g:666:14: ( DOT ( Digit )* ( Exponent )? | Exponent )?
            int alt10=3;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='.') ) {
                alt10=1;
            }
            else if ( (LA10_0=='e') ) {
                alt10=2;
            }
            switch (alt10) {
                case 1 :
                    // /home/obda/SimpleSQL.g:666:16: DOT ( Digit )* ( Exponent )?
                    {
                    mDOT(); 
                    // /home/obda/SimpleSQL.g:666:20: ( Digit )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( ((LA8_0>='0' && LA8_0<='9')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // /home/obda/SimpleSQL.g:666:21: Digit
                    	    {
                    	    mDigit(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);

                    // /home/obda/SimpleSQL.g:666:29: ( Exponent )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0=='e') ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // /home/obda/SimpleSQL.g:666:30: Exponent
                            {
                            mExponent(); 

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/obda/SimpleSQL.g:666:43: Exponent
                    {
                    mExponent(); 

                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Number"

    // $ANTLR start "Identifier"
    public final void mIdentifier() throws RecognitionException {
        try {
            int _type = Identifier;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:670:5: ( Letter ( Letter | Digit )* )
            // /home/obda/SimpleSQL.g:671:4: Letter ( Letter | Digit )*
            {
            mLetter(); 
            // /home/obda/SimpleSQL.g:671:11: ( Letter | Digit )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>='0' && LA11_0<='9')||LA11_0=='_'||(LA11_0>='a' && LA11_0<='z')) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /home/obda/SimpleSQL.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Identifier"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/obda/SimpleSQL.g:674:5: ( ( ' ' | '\\r' | '\\t' | '\\n' ) )
            // /home/obda/SimpleSQL.g:674:8: ( ' ' | '\\r' | '\\t' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // /home/obda/SimpleSQL.g:1:8: ( T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | DOT | COLON | COMMA | SEMICOLON | LPAREN | RPAREN | LSQUARE | RSQUARE | EQUAL | NOTEQUAL | LESSTHANOREQUALTO | LESSTHAN | GREATERTHANOREQUALTO | GREATERTHAN | DIVIDE | PLUS | MINUS | STAR | MOD | AMPERSAND | TILDE | BITWISEOR | BITWISEXOR | StringLiteral | DateLiteral | Number | Identifier | WS )
        int alt12=82;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // /home/obda/SimpleSQL.g:1:10: T__35
                {
                mT__35(); 

                }
                break;
            case 2 :
                // /home/obda/SimpleSQL.g:1:16: T__36
                {
                mT__36(); 

                }
                break;
            case 3 :
                // /home/obda/SimpleSQL.g:1:22: T__37
                {
                mT__37(); 

                }
                break;
            case 4 :
                // /home/obda/SimpleSQL.g:1:28: T__38
                {
                mT__38(); 

                }
                break;
            case 5 :
                // /home/obda/SimpleSQL.g:1:34: T__39
                {
                mT__39(); 

                }
                break;
            case 6 :
                // /home/obda/SimpleSQL.g:1:40: T__40
                {
                mT__40(); 

                }
                break;
            case 7 :
                // /home/obda/SimpleSQL.g:1:46: T__41
                {
                mT__41(); 

                }
                break;
            case 8 :
                // /home/obda/SimpleSQL.g:1:52: T__42
                {
                mT__42(); 

                }
                break;
            case 9 :
                // /home/obda/SimpleSQL.g:1:58: T__43
                {
                mT__43(); 

                }
                break;
            case 10 :
                // /home/obda/SimpleSQL.g:1:64: T__44
                {
                mT__44(); 

                }
                break;
            case 11 :
                // /home/obda/SimpleSQL.g:1:70: T__45
                {
                mT__45(); 

                }
                break;
            case 12 :
                // /home/obda/SimpleSQL.g:1:76: T__46
                {
                mT__46(); 

                }
                break;
            case 13 :
                // /home/obda/SimpleSQL.g:1:82: T__47
                {
                mT__47(); 

                }
                break;
            case 14 :
                // /home/obda/SimpleSQL.g:1:88: T__48
                {
                mT__48(); 

                }
                break;
            case 15 :
                // /home/obda/SimpleSQL.g:1:94: T__49
                {
                mT__49(); 

                }
                break;
            case 16 :
                // /home/obda/SimpleSQL.g:1:100: T__50
                {
                mT__50(); 

                }
                break;
            case 17 :
                // /home/obda/SimpleSQL.g:1:106: T__51
                {
                mT__51(); 

                }
                break;
            case 18 :
                // /home/obda/SimpleSQL.g:1:112: T__52
                {
                mT__52(); 

                }
                break;
            case 19 :
                // /home/obda/SimpleSQL.g:1:118: T__53
                {
                mT__53(); 

                }
                break;
            case 20 :
                // /home/obda/SimpleSQL.g:1:124: T__54
                {
                mT__54(); 

                }
                break;
            case 21 :
                // /home/obda/SimpleSQL.g:1:130: T__55
                {
                mT__55(); 

                }
                break;
            case 22 :
                // /home/obda/SimpleSQL.g:1:136: T__56
                {
                mT__56(); 

                }
                break;
            case 23 :
                // /home/obda/SimpleSQL.g:1:142: T__57
                {
                mT__57(); 

                }
                break;
            case 24 :
                // /home/obda/SimpleSQL.g:1:148: T__58
                {
                mT__58(); 

                }
                break;
            case 25 :
                // /home/obda/SimpleSQL.g:1:154: T__59
                {
                mT__59(); 

                }
                break;
            case 26 :
                // /home/obda/SimpleSQL.g:1:160: T__60
                {
                mT__60(); 

                }
                break;
            case 27 :
                // /home/obda/SimpleSQL.g:1:166: T__61
                {
                mT__61(); 

                }
                break;
            case 28 :
                // /home/obda/SimpleSQL.g:1:172: T__62
                {
                mT__62(); 

                }
                break;
            case 29 :
                // /home/obda/SimpleSQL.g:1:178: T__63
                {
                mT__63(); 

                }
                break;
            case 30 :
                // /home/obda/SimpleSQL.g:1:184: T__64
                {
                mT__64(); 

                }
                break;
            case 31 :
                // /home/obda/SimpleSQL.g:1:190: T__65
                {
                mT__65(); 

                }
                break;
            case 32 :
                // /home/obda/SimpleSQL.g:1:196: T__66
                {
                mT__66(); 

                }
                break;
            case 33 :
                // /home/obda/SimpleSQL.g:1:202: T__67
                {
                mT__67(); 

                }
                break;
            case 34 :
                // /home/obda/SimpleSQL.g:1:208: T__68
                {
                mT__68(); 

                }
                break;
            case 35 :
                // /home/obda/SimpleSQL.g:1:214: T__69
                {
                mT__69(); 

                }
                break;
            case 36 :
                // /home/obda/SimpleSQL.g:1:220: T__70
                {
                mT__70(); 

                }
                break;
            case 37 :
                // /home/obda/SimpleSQL.g:1:226: T__71
                {
                mT__71(); 

                }
                break;
            case 38 :
                // /home/obda/SimpleSQL.g:1:232: T__72
                {
                mT__72(); 

                }
                break;
            case 39 :
                // /home/obda/SimpleSQL.g:1:238: T__73
                {
                mT__73(); 

                }
                break;
            case 40 :
                // /home/obda/SimpleSQL.g:1:244: T__74
                {
                mT__74(); 

                }
                break;
            case 41 :
                // /home/obda/SimpleSQL.g:1:250: T__75
                {
                mT__75(); 

                }
                break;
            case 42 :
                // /home/obda/SimpleSQL.g:1:256: T__76
                {
                mT__76(); 

                }
                break;
            case 43 :
                // /home/obda/SimpleSQL.g:1:262: T__77
                {
                mT__77(); 

                }
                break;
            case 44 :
                // /home/obda/SimpleSQL.g:1:268: T__78
                {
                mT__78(); 

                }
                break;
            case 45 :
                // /home/obda/SimpleSQL.g:1:274: T__79
                {
                mT__79(); 

                }
                break;
            case 46 :
                // /home/obda/SimpleSQL.g:1:280: T__80
                {
                mT__80(); 

                }
                break;
            case 47 :
                // /home/obda/SimpleSQL.g:1:286: T__81
                {
                mT__81(); 

                }
                break;
            case 48 :
                // /home/obda/SimpleSQL.g:1:292: T__82
                {
                mT__82(); 

                }
                break;
            case 49 :
                // /home/obda/SimpleSQL.g:1:298: T__83
                {
                mT__83(); 

                }
                break;
            case 50 :
                // /home/obda/SimpleSQL.g:1:304: T__84
                {
                mT__84(); 

                }
                break;
            case 51 :
                // /home/obda/SimpleSQL.g:1:310: T__85
                {
                mT__85(); 

                }
                break;
            case 52 :
                // /home/obda/SimpleSQL.g:1:316: T__86
                {
                mT__86(); 

                }
                break;
            case 53 :
                // /home/obda/SimpleSQL.g:1:322: T__87
                {
                mT__87(); 

                }
                break;
            case 54 :
                // /home/obda/SimpleSQL.g:1:328: T__88
                {
                mT__88(); 

                }
                break;
            case 55 :
                // /home/obda/SimpleSQL.g:1:334: DOT
                {
                mDOT(); 

                }
                break;
            case 56 :
                // /home/obda/SimpleSQL.g:1:338: COLON
                {
                mCOLON(); 

                }
                break;
            case 57 :
                // /home/obda/SimpleSQL.g:1:344: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 58 :
                // /home/obda/SimpleSQL.g:1:350: SEMICOLON
                {
                mSEMICOLON(); 

                }
                break;
            case 59 :
                // /home/obda/SimpleSQL.g:1:360: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 60 :
                // /home/obda/SimpleSQL.g:1:367: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 61 :
                // /home/obda/SimpleSQL.g:1:374: LSQUARE
                {
                mLSQUARE(); 

                }
                break;
            case 62 :
                // /home/obda/SimpleSQL.g:1:382: RSQUARE
                {
                mRSQUARE(); 

                }
                break;
            case 63 :
                // /home/obda/SimpleSQL.g:1:390: EQUAL
                {
                mEQUAL(); 

                }
                break;
            case 64 :
                // /home/obda/SimpleSQL.g:1:396: NOTEQUAL
                {
                mNOTEQUAL(); 

                }
                break;
            case 65 :
                // /home/obda/SimpleSQL.g:1:405: LESSTHANOREQUALTO
                {
                mLESSTHANOREQUALTO(); 

                }
                break;
            case 66 :
                // /home/obda/SimpleSQL.g:1:423: LESSTHAN
                {
                mLESSTHAN(); 

                }
                break;
            case 67 :
                // /home/obda/SimpleSQL.g:1:432: GREATERTHANOREQUALTO
                {
                mGREATERTHANOREQUALTO(); 

                }
                break;
            case 68 :
                // /home/obda/SimpleSQL.g:1:453: GREATERTHAN
                {
                mGREATERTHAN(); 

                }
                break;
            case 69 :
                // /home/obda/SimpleSQL.g:1:465: DIVIDE
                {
                mDIVIDE(); 

                }
                break;
            case 70 :
                // /home/obda/SimpleSQL.g:1:472: PLUS
                {
                mPLUS(); 

                }
                break;
            case 71 :
                // /home/obda/SimpleSQL.g:1:477: MINUS
                {
                mMINUS(); 

                }
                break;
            case 72 :
                // /home/obda/SimpleSQL.g:1:483: STAR
                {
                mSTAR(); 

                }
                break;
            case 73 :
                // /home/obda/SimpleSQL.g:1:488: MOD
                {
                mMOD(); 

                }
                break;
            case 74 :
                // /home/obda/SimpleSQL.g:1:492: AMPERSAND
                {
                mAMPERSAND(); 

                }
                break;
            case 75 :
                // /home/obda/SimpleSQL.g:1:502: TILDE
                {
                mTILDE(); 

                }
                break;
            case 76 :
                // /home/obda/SimpleSQL.g:1:508: BITWISEOR
                {
                mBITWISEOR(); 

                }
                break;
            case 77 :
                // /home/obda/SimpleSQL.g:1:518: BITWISEXOR
                {
                mBITWISEXOR(); 

                }
                break;
            case 78 :
                // /home/obda/SimpleSQL.g:1:529: StringLiteral
                {
                mStringLiteral(); 

                }
                break;
            case 79 :
                // /home/obda/SimpleSQL.g:1:543: DateLiteral
                {
                mDateLiteral(); 

                }
                break;
            case 80 :
                // /home/obda/SimpleSQL.g:1:555: Number
                {
                mNumber(); 

                }
                break;
            case 81 :
                // /home/obda/SimpleSQL.g:1:562: Identifier
                {
                mIdentifier(); 

                }
                break;
            case 82 :
                // /home/obda/SimpleSQL.g:1:573: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA12_eotS =
        "\1\uffff\1\62\1\uffff\2\62\2\uffff\1\62\1\uffff\1\62\1\uffff\2\62"+
        "\2\uffff\2\62\1\uffff\1\62\1\uffff\1\62\1\uffff\3\62\13\uffff\1"+
        "\132\1\134\16\uffff\2\62\2\uffff\1\62\1\141\3\62\1\uffff\1\147\3"+
        "\uffff\2\62\1\uffff\1\160\1\161\1\163\3\62\1\167\2\uffff\2\62\2"+
        "\uffff\6\62\5\uffff\2\62\1\u0083\1\u0084\1\uffff\1\u0085\1\u0086"+
        "\2\62\4\uffff\3\62\2\uffff\1\62\4\uffff\1\62\1\u008f\3\uffff\6\62"+
        "\1\u0098\2\62\1\u009b\4\uffff\1\62\1\u009d\1\62\1\u009f\1\62\1\u00a1"+
        "\2\62\3\uffff\1\u00a8\1\62\1\u00aa\1\u00ab\1\u00ac\1\u00ad\1\uffff"+
        "\2\62\1\uffff\1\62\1\uffff\1\u00b1\1\uffff\1\u00b2\1\uffff\1\u00b3"+
        "\1\u00b4\5\uffff\1\u00b7\4\uffff\1\62\1\u00b9\1\62\4\uffff\1\u00bc"+
        "\1\u00be\1\uffff\1\u00bf\1\uffff\1\62\5\uffff\1\u00c1\1\uffff";
    static final String DFA12_eofS =
        "\u00c2\uffff";
    static final String DFA12_minS =
        "\1\11\1\145\1\105\1\154\1\145\1\114\1\105\1\150\1\uffff\1\151\1"+
        "\111\1\162\1\171\1\122\1\uffff\1\162\1\157\1\uffff\1\156\1\116\1"+
        "\141\1\101\1\141\1\150\1\154\13\uffff\2\75\16\uffff\1\154\1\155"+
        "\2\uffff\1\154\1\60\1\144\2\163\1\uffff\1\103\1\104\2\uffff\1\145"+
        "\1\153\1\113\2\60\1\104\1\157\1\164\1\40\1\60\1\40\1\uffff\1\157"+
        "\1\154\2\uffff\1\163\1\145\1\165\1\163\1\144\1\151\5\uffff\2\145"+
        "\2\60\1\uffff\2\60\1\164\1\143\4\uffff\1\156\1\151\1\145\2\uffff"+
        "\1\145\4\uffff\1\165\1\60\1\156\1\uffff\1\116\1\155\1\163\1\145"+
        "\1\156\2\145\1\60\1\163\1\143\1\60\4\uffff\1\151\1\60\1\145\1\60"+
        "\1\164\1\60\1\162\1\160\1\uffff\1\157\1\117\1\60\1\145\4\60\1\uffff"+
        "\2\164\1\uffff\1\156\1\uffff\1\60\1\uffff\1\60\1\uffff\2\60\1\164"+
        "\1\uffff\1\124\2\uffff\1\60\4\uffff\1\163\1\60\1\143\4\uffff\2\40"+
        "\1\uffff\1\60\1\uffff\1\164\5\uffff\1\60\1\uffff";
    static final String DFA12_maxS =
        "\1\176\1\157\1\117\1\163\1\151\1\123\1\111\1\150\1\uffff\1\151\1"+
        "\111\1\162\1\171\1\122\1\uffff\1\162\1\157\1\uffff\1\163\1\123\1"+
        "\162\1\122\1\141\1\162\1\170\13\uffff\1\76\1\75\16\uffff\1\154\1"+
        "\155\2\uffff\1\154\1\172\1\171\2\163\1\uffff\1\103\1\131\2\uffff"+
        "\1\145\1\155\1\115\2\172\1\104\1\157\1\164\1\40\1\172\1\40\1\uffff"+
        "\1\157\1\154\2\uffff\1\163\1\145\1\165\1\163\1\144\1\151\5\uffff"+
        "\2\145\2\172\1\uffff\2\172\1\164\1\143\4\uffff\1\162\1\151\1\145"+
        "\2\uffff\1\145\4\uffff\1\165\1\172\1\156\1\uffff\1\116\1\155\1\163"+
        "\1\145\1\156\2\145\1\172\1\163\1\143\1\172\4\uffff\1\151\1\172\1"+
        "\145\1\172\1\164\1\172\1\162\1\160\1\uffff\1\165\1\125\1\172\1\145"+
        "\4\172\1\uffff\2\164\1\uffff\1\156\1\uffff\1\172\1\uffff\1\172\1"+
        "\uffff\2\172\1\164\1\uffff\1\124\2\uffff\1\172\4\uffff\1\163\1\172"+
        "\1\143\4\uffff\2\40\1\uffff\1\172\1\uffff\1\164\5\uffff\1\172\1"+
        "\uffff";
    static final String DFA12_acceptS =
        "\10\uffff\1\10\5\uffff\1\16\2\uffff\1\31\7\uffff\1\61\1\65\1\67"+
        "\1\70\1\71\1\72\1\73\1\74\1\75\1\76\1\77\2\uffff\1\105\1\106\1\107"+
        "\1\110\1\111\1\112\1\113\1\114\1\115\1\116\1\117\1\120\1\121\1\122"+
        "\2\uffff\1\2\1\62\5\uffff\1\5\2\uffff\1\6\1\22\13\uffff\1\37\2\uffff"+
        "\1\46\1\66\6\uffff\1\100\1\101\1\102\1\103\1\104\4\uffff\1\44\4"+
        "\uffff\1\21\1\47\1\25\1\60\3\uffff\1\12\1\35\1\uffff\1\24\1\14\1"+
        "\15\1\26\3\uffff\1\36\13\uffff\1\3\1\17\1\23\1\55\10\uffff\1\30"+
        "\10\uffff\1\54\2\uffff\1\57\1\uffff\1\20\1\uffff\1\51\1\uffff\1"+
        "\34\3\uffff\1\40\1\uffff\1\41\1\45\1\uffff\1\50\1\52\1\63\1\53\3"+
        "\uffff\1\7\1\11\1\13\1\27\2\uffff\1\64\1\uffff\1\1\1\uffff\1\42"+
        "\1\32\1\43\1\33\1\56\1\uffff\1\4";
    static final String DFA12_specialS =
        "\u00c2\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\63\2\uffff\1\63\22\uffff\1\63\2\uffff\1\60\1\uffff\1\52\1"+
            "\53\1\57\1\37\1\40\1\51\1\47\1\35\1\50\1\33\1\46\12\61\1\34"+
            "\1\36\1\44\1\43\1\45\2\uffff\1\5\1\16\1\uffff\1\6\1\31\1\25"+
            "\2\uffff\1\23\2\uffff\1\12\1\uffff\1\21\1\15\3\uffff\1\2\1\32"+
            "\2\uffff\1\10\3\uffff\1\41\1\uffff\1\42\1\56\1\62\1\uffff\1"+
            "\3\1\14\1\26\1\4\1\30\1\24\1\17\1\62\1\22\2\62\1\11\1\62\1\20"+
            "\1\13\3\62\1\1\1\27\2\62\1\7\3\62\1\uffff\1\55\1\uffff\1\54",
            "\1\64\11\uffff\1\65",
            "\1\66\11\uffff\1\67",
            "\1\70\1\uffff\1\72\4\uffff\1\71",
            "\1\74\3\uffff\1\73",
            "\1\75\1\uffff\1\77\4\uffff\1\76",
            "\1\101\3\uffff\1\100",
            "\1\102",
            "",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\106",
            "\1\107",
            "",
            "\1\110",
            "\1\111",
            "",
            "\1\113\4\uffff\1\112",
            "\1\115\4\uffff\1\114",
            "\1\117\20\uffff\1\116",
            "\1\121\20\uffff\1\120",
            "\1\122",
            "\1\123\11\uffff\1\124",
            "\1\125\1\uffff\1\126\11\uffff\1\127",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\131\1\130",
            "\1\133",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\135",
            "\1\136",
            "",
            "",
            "\1\137",
            "\12\62\45\uffff\1\62\1\uffff\2\62\1\140\27\62",
            "\1\142\24\uffff\1\143",
            "\1\144",
            "\1\145",
            "",
            "\1\146",
            "\1\150\24\uffff\1\151",
            "",
            "",
            "\1\152",
            "\1\154\1\uffff\1\153",
            "\1\156\1\uffff\1\155",
            "\12\62\45\uffff\1\62\1\uffff\3\62\1\157\26\62",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\162",
            "\1\164",
            "\1\165",
            "\1\166",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\170",
            "",
            "\1\171",
            "\1\172",
            "",
            "",
            "\1\173",
            "\1\174",
            "\1\175",
            "\1\176",
            "\1\177",
            "\1\u0080",
            "",
            "",
            "",
            "",
            "",
            "\1\u0081",
            "\1\u0082",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u0087",
            "\1\u0088",
            "",
            "",
            "",
            "",
            "\1\u008a\3\uffff\1\u0089",
            "\1\u008b",
            "\1\u008c",
            "",
            "",
            "\1\u008d",
            "",
            "",
            "",
            "",
            "\1\u008e",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u0090",
            "",
            "\1\u0091",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\u0097",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u0099",
            "\1\u009a",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "",
            "",
            "",
            "",
            "\1\u009c",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u009e",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u00a0",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u00a2",
            "\1\u00a3",
            "",
            "\1\u00a4\5\uffff\1\u00a5",
            "\1\u00a6\5\uffff\1\u00a7",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u00a9",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "",
            "\1\u00ae",
            "\1\u00af",
            "",
            "\1\u00b0",
            "",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u00b5",
            "",
            "\1\u00b6",
            "",
            "",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "",
            "",
            "",
            "",
            "\1\u00b8",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "\1\u00ba",
            "",
            "",
            "",
            "",
            "\1\u00bb",
            "\1\u00bd",
            "",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            "",
            "\1\u00c0",
            "",
            "",
            "",
            "",
            "",
            "\12\62\45\uffff\1\62\1\uffff\32\62",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | T__63 | T__64 | T__65 | T__66 | T__67 | T__68 | T__69 | T__70 | T__71 | T__72 | T__73 | T__74 | T__75 | T__76 | T__77 | T__78 | T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | DOT | COLON | COMMA | SEMICOLON | LPAREN | RPAREN | LSQUARE | RSQUARE | EQUAL | NOTEQUAL | LESSTHANOREQUALTO | LESSTHAN | GREATERTHANOREQUALTO | GREATERTHAN | DIVIDE | PLUS | MINUS | STAR | MOD | AMPERSAND | TILDE | BITWISEOR | BITWISEXOR | StringLiteral | DateLiteral | Number | Identifier | WS );";
        }
    }
 

}