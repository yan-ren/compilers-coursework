/*
 *  The scanner definition for COOL.
 */

/*
Reference:
THeory of Compilation: 
http://cs.haifa.ac.il/courses/compilers/BILAL/Tutorials/JLex_CUP_tools.pdf

JLex user manual: 
https://www.cs.princeton.edu/~appel/modern/java/JLex/current/manual.html#SECTION1

java_cup.runtime.Symbol API: 
https://web.stanford.edu/class/archive/cs/cs143/cs143.1112/javadoc/java_cup/
*/
import java_cup.runtime.Symbol;

%%
/* JLex directives */
%{

/*  Stuff enclosed in %{ %} is copied verbatim to the lexer class
 *  definition, all the extra variables/functions you want to use in the
 *  lexer actions should go here.  Don't remove or modify anything that
 *  was there initially.  */

    // Integer for counting nested comments
    int nestedComments = 0;

    bool nullInString = false;
    // Max size of string constants
    static int MAX_STR_CONST = 1025;

    // For assembling string constants
    StringBuffer string_buf = new StringBuffer();

    private int curr_lineno = 1;
    int get_curr_lineno() {
	    return curr_lineno;
    }

    private AbstractSymbol filename;

    void set_filename(String fname) {
	    filename = AbstractTable.stringtable.addString(fname);
    }

    AbstractSymbol curr_filename() {
	    return filename;
    }
%}

%init{

/*  Stuff enclosed in %init{ %init} is copied verbatim to the lexer
 *  class constructor, all the extra initialization you want to do should
 *  go here.  Don't remove or modify anything that was there initially. 
 */

    // empty for now
%init}

%eofval{

/*  Stuff enclosed in %eofval{ %eofval} specifies java code that is
 *  executed when end-of-file is reached.  If you use multiple lexical
 *  states and want to do something special if an EOF is encountered in
 *  one of those states, place your code in the switch statement.
 *  Ultimately, you should return the EOF symbol, or your lexer won't
 *  work.  
 */

    switch(yy_lexical_state) {
        case YYINITIAL:
        /* nothing special to do in the initial state */
            break;
        case STRING:
            yybegin(YYINITIAL); 
            return new Symbol(TokenConstants.ERROR, "EOF in string constant");
        case BLOCK_COMMENT:
            yybegin(YYINITIAL); 
            return new Symbol(TokenConstants.ERROR, "EOF in comment.");
    }
    return new Symbol(TokenConstants.EOF);
%eofval}

%class CoolLexer
%cup

%state STRING
%state BLOCK_COMMENTS
%state INLINE_COMMENTS

%%
/*regular expression rules*/
/*The Cool Reference Manual 10.2 Strings

> If a string contains an unescaped newline, report that error as ‘‘Unterminated string constant’’
and resume lexing at the beginning of the next line—we assume the programmer simply forgot the
close-quote.

> String constant too long

String contains null character

> EOF in string constant

\c -> c

\b backspace
\t tab
\n newline
\f formfeed
*/
<YYINITIAL> \n          { curr_lineno++; }
<YYINITIAL> \"   {
    /* new string, clean string buffer */
    string_buf.delete(0, string_buf.length());
    nullInString=false;
    yybegin(STRING); 
}

<STRING> \" { 
    // if an escape character appears before a quote, we must put a quote into the string
    if(string_buf.length()>0 && string_buf.charAt(string_buf.length()-1)=='\\') {
        string_buf.setCharAt(string_buf.length()-1, '\"');
    } else {
        // it is the end of string
        if(string_buf.length()>=MAX_STR_CONST) {
            yybegin(YYINITIAL);
            return new Symbol(TokenConstants.ERROR, "String constant too long");
        } else if(nullInString) {
            yybegin(YYINITIAL);
            return new Symbol(TokenConstants.ERROR, "String contains null character");
        } else {
            yybegin(YYINITIAL); 
            return new Symbol(TokenConstants.STR_CONST, AbstractTable.stringtable.addString(string_buf.toString())); 
        }
    }
}

<STRING> \n {
    // if an escape character appears before a \n, we must put \n into the string
    if(string_buf.length()>0 && string_buf.charAt(string_buf.length()-1)=='\\') {
        curr_lineno++;
        string_buf.setCharAt(string_buf.length()-1, '\n');
    } else {
        curr_lineno++;
        yybegin(YYINITIAL);
        return new Symbol(TokenConstants.ERROR, "Unterminated string constant");
    }
}

<STRING> [^\n\"] {
    // process all character one at a time, except for \n or "
    if (string_buf.length() == 0) {
        string_buf.append(yytext());
    }else {
        if(yytext().charAt(0)=='\0') {
            nullInString=true;
        } else {
            int length=string_buf.length();
            // previous char is escape
            if(string_buf.charAt(string_buf.length()-1)=='\\') {
                switch(yytext().charAt(0)) {
                case 'b':
                    string_buf.setCharAt(length-1, '\b');
                    break;
                case 't':
                    string_buf.setCharAt(length-1, '\t');
                    break;
                case 'n':
                    string_buf.setCharAt(length-1, '\n');
                    break;
                case 'f':
                    string_buf.setCharAt(length-1, '\f');
                    break;
                default:
                    string_buf.setCharAt(length-1, yytext().charAt(0));
                }
            }else {
                string_buf.append(yytext());
            }
        }
    }
}

/*The Cool Reference Manual 10.3 Comments*/
<YYINITIAL> "(*"                { yybegin(BLOCK_COMMENT); }
<YYINITIAL> "*)"                { return new Symbol(TokenConstants.ERROR,"Mismatched '*)'"); }

<BLOCK_COMMENT> "(*"            { nestedComments++ }
<BLOCK_COMMENT> "*)"            { 
    if nestedComments == 0 {
        yybegin(YYINITIAL);
    }else {
        nestedComments--;
    }
}
<BLOCK_COMMENT> [^\*\(\)\n]*     { /* in block comments, skip all character except for * ( ) \n */ }
<BLOCK_COMMENT> [\(\)\*]         { /* skill single * ( ) as well */}
<BLOCK_COMMENT> \n               { curr_lineno++; }

<YYINITIAL> "--"                { yybegin(INLINE_COMMENTS); }
<INLINE_COMMENTS> \n            { curr_lineno++; yybegin(YYINITIAL); }
<INLINE_COMMENTS> .*            { }

/*The Cool Reference Manual 10.4 Keywords*/
<YYINITIAL> [cC][lL][aA][sS][sS]             { return new Symbol(TokenConstants.CLASS); }
<YYINITIAL> [eE][lL][sS][eE]                 { return new Symbol(TokenConstants.ELSE); }
<YYINITIAL> [fF][iI]                         { return new Symbol(TokenConstants.FI); }
<YYINITIAL> [iI][fF]                         { return new Symbol(TokenConstants.IF); }
<YYINITIAL> [iI][nN]                         { return new Symbol(TokenConstants.IN); }
<YYINITIAL> [iI][nN][hH][eE][rR][iI][tT][sS] { return new Symbol(TokenConstants.INHERITS); }
<YYINITIAL> [iI][sS][vV][oO][iI][dD]         { return new Symbol(TokenConstants.ISVOID); }
<YYINITIAL> [lL][eE][tT]                     { return new Symbol(TokenConstants.LET); }
<YYINITIAL> [lL][oO][oO][pP]                 { return new Symbol(TokenConstants.LOOP); }
<YYINITIAL> [pP][oO][oO][lL]                 { return new Symbol(TokenConstants.POOL); }
<YYINITIAL> [tT][hH][eE][nN]                 { return new Symbol(TokenConstants.THEN); }
<YYINITIAL> [wW][hH][iI][lL][eE]             { return new Symbol(TokenConstants.WHILE); }
<YYINITIAL> [cC][aA][sS][eE]                 { return new Symbol(TokenConstants.CASE); }
<YYINITIAL> [eE][sS][aA][cC]                 { return new Symbol(TokenConstants.ESAC); }
<YYINITIAL> [nN][eE][wW]                     { return new Symbol(TokenConstants.NEW); }
<YYINITIAL> [oO][fF]                         { return new Symbol(TokenConstants.OF); }
<YYINITIAL> [nN][oO][tT]                     { return new Symbol(TokenConstants.NOT); }
<YYINITIAL> [t][rR][uU][eE]                  { return new Symbol(TokenConstants.BOOL_CONST, new Boolean(true)); }
<YYINITIAL> [f][aA][lL][sS][eE]              { return new Symbol(TokenConstants.BOOL_CONST, new Boolean(false)); }

/*The Cool Reference Manual 10.5 White Space*/
<YYINITIAL> [ \n\f\r\t\v]+ {}

/*The Cool Reference Manual 10.1 Integers, Identifiers, and Special Notation*/
/*Integer*/
<YYINITIAL> [0-9]*             { return new Symbol(TokenConstants.INT_CONST, AbstractTable.inttable.addString(yytext())); }

/*Typed identifiers*/
<YYINITIAL> [A-Z][_a-zA-Z0-9]* { return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }

/*Object identifiers*/
<YYINITIAL> [a-z][_a-zA-Z0-9]* { return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }

/*The Cool Reference Manual Figure 1*/
<YYINITIAL>"=>"			{ /* Sample lexical rule for "=>" arrow.
                                     Further lexical rules should be defined
                                     here, after the last %% separator */
                                  return new Symbol(TokenConstants.DARROW); }
<YYINITIAL> "*"   { return new Symbol(TokenConstants.MULT);   }
<YYINITIAL> "("   { return new Symbol(TokenConstants.LPAREN); }
<YYINITIAL> ";"   { return new Symbol(TokenConstants.SEMI);   }
<YYINITIAL> "-"   { return new Symbol(TokenConstants.MINUS);  }
<YYINITIAL> ")"   { return new Symbol(TokenConstants.RPAREN); }
<YYINITIAL> "<"   { return new Symbol(TokenConstants.LT);     }
<YYINITIAL> ","   { return new Symbol(TokenConstants.COMMA);  }
<YYINITIAL> "/"   { return new Symbol(TokenConstants.DIV);    }
<YYINITIAL> "+"   { return new Symbol(TokenConstants.PLUS);   }
<YYINITIAL> "<-"  { return new Symbol(TokenConstants.ASSIGN); }
<YYINITIAL> "."   { return new Symbol(TokenConstants.DOT);    }
<YYINITIAL> "<="  { return new Symbol(TokenConstants.LE);     }
<YYINITIAL> "="   { return new Symbol(TokenConstants.EQ);     }
<YYINITIAL> ":"   { return new Symbol(TokenConstants.COLON);  }
<YYINITIAL> "{"   { return new Symbol(TokenConstants.LBRACE); }
<YYINITIAL> "}"   { return new Symbol(TokenConstants.RBRACE); }
<YYINITIAL> "@"   { return new Symbol(TokenConstants.AT);     }
<YYINITIAL> "~"   { return new Symbol(TokenConstants.NEG);    }

.                 { /* This rule should be the very last
                            in your lexical specification and
                            will match match everything not
                            matched by other lexical rules. */
                        System.err.println("LEXER BUG - UNMATCHED: " + yytext()); }
