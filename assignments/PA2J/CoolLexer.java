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


class CoolLexer implements java_cup.runtime.Scanner {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 128;
	private final int YY_EOF = 129;

/*  Stuff enclosed in %{ %} is copied verbatim to the lexer class
 *  definition, all the extra variables/functions you want to use in the
 *  lexer actions should go here.  Don't remove or modify anything that
 *  was there initially.  */
    // Integer for counting nested comments
    int nestedComments = 0;
    boolean escaped = false;
    boolean nullInString = false;
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
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private boolean yy_at_bol;
	private int yy_lexical_state;

	CoolLexer (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	CoolLexer (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private CoolLexer () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;

/*  Stuff enclosed in %init{ %init} is copied verbatim to the lexer
 *  class constructor, all the extra initialization you want to do should
 *  go here.  Don't remove or modify anything that was there initially. 
 */
    // empty for now
	}

	private boolean yy_eof_done = false;
	private final int INLINE_COMMENTS = 3;
	private final int STRING = 1;
	private final int YYINITIAL = 0;
	private final int BLOCK_COMMENTS = 2;
	private final int yy_state_dtrans[] = {
		0,
		49,
		53,
		58
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NO_ANCHOR,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NO_ANCHOR,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NO_ANCHOR,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NO_ANCHOR,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NO_ANCHOR,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NO_ANCHOR,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NOT_ACCEPT,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NO_ANCHOR,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NO_ANCHOR,
		/* 58 */ YY_NO_ANCHOR,
		/* 59 */ YY_NO_ANCHOR,
		/* 60 */ YY_NO_ANCHOR,
		/* 61 */ YY_NO_ANCHOR,
		/* 62 */ YY_NO_ANCHOR,
		/* 63 */ YY_NO_ANCHOR,
		/* 64 */ YY_NO_ANCHOR,
		/* 65 */ YY_NO_ANCHOR,
		/* 66 */ YY_NO_ANCHOR,
		/* 67 */ YY_NO_ANCHOR,
		/* 68 */ YY_NO_ANCHOR,
		/* 69 */ YY_NO_ANCHOR,
		/* 70 */ YY_NO_ANCHOR,
		/* 71 */ YY_NO_ANCHOR,
		/* 72 */ YY_NO_ANCHOR,
		/* 73 */ YY_NO_ANCHOR,
		/* 74 */ YY_NO_ANCHOR,
		/* 75 */ YY_NO_ANCHOR,
		/* 76 */ YY_NO_ANCHOR,
		/* 77 */ YY_NO_ANCHOR,
		/* 78 */ YY_NO_ANCHOR,
		/* 79 */ YY_NO_ANCHOR,
		/* 80 */ YY_NO_ANCHOR,
		/* 81 */ YY_NO_ANCHOR,
		/* 82 */ YY_NO_ANCHOR,
		/* 83 */ YY_NO_ANCHOR,
		/* 84 */ YY_NO_ANCHOR,
		/* 85 */ YY_NO_ANCHOR,
		/* 86 */ YY_NO_ANCHOR,
		/* 87 */ YY_NO_ANCHOR,
		/* 88 */ YY_NO_ANCHOR,
		/* 89 */ YY_NO_ANCHOR,
		/* 90 */ YY_NO_ANCHOR,
		/* 91 */ YY_NO_ANCHOR,
		/* 92 */ YY_NO_ANCHOR,
		/* 93 */ YY_NO_ANCHOR,
		/* 94 */ YY_NO_ANCHOR,
		/* 95 */ YY_NO_ANCHOR,
		/* 96 */ YY_NO_ANCHOR,
		/* 97 */ YY_NO_ANCHOR,
		/* 98 */ YY_NO_ANCHOR,
		/* 99 */ YY_NO_ANCHOR,
		/* 100 */ YY_NO_ANCHOR,
		/* 101 */ YY_NO_ANCHOR,
		/* 102 */ YY_NO_ANCHOR,
		/* 103 */ YY_NO_ANCHOR,
		/* 104 */ YY_NO_ANCHOR,
		/* 105 */ YY_NO_ANCHOR,
		/* 106 */ YY_NO_ANCHOR,
		/* 107 */ YY_NO_ANCHOR,
		/* 108 */ YY_NO_ANCHOR,
		/* 109 */ YY_NO_ANCHOR,
		/* 110 */ YY_NO_ANCHOR,
		/* 111 */ YY_NO_ANCHOR,
		/* 112 */ YY_NO_ANCHOR,
		/* 113 */ YY_NO_ANCHOR,
		/* 114 */ YY_NO_ANCHOR,
		/* 115 */ YY_NO_ANCHOR,
		/* 116 */ YY_NO_ANCHOR,
		/* 117 */ YY_NO_ANCHOR,
		/* 118 */ YY_NO_ANCHOR,
		/* 119 */ YY_NO_ANCHOR,
		/* 120 */ YY_NO_ANCHOR,
		/* 121 */ YY_NO_ANCHOR,
		/* 122 */ YY_NO_ANCHOR,
		/* 123 */ YY_NO_ANCHOR,
		/* 124 */ YY_NO_ANCHOR,
		/* 125 */ YY_NO_ANCHOR,
		/* 126 */ YY_NO_ANCHOR,
		/* 127 */ YY_NO_ANCHOR,
		/* 128 */ YY_NO_ANCHOR,
		/* 129 */ YY_NO_ANCHOR,
		/* 130 */ YY_NO_ANCHOR,
		/* 131 */ YY_NO_ANCHOR,
		/* 132 */ YY_NO_ANCHOR,
		/* 133 */ YY_NO_ANCHOR,
		/* 134 */ YY_NO_ANCHOR,
		/* 135 */ YY_NO_ANCHOR,
		/* 136 */ YY_NO_ANCHOR,
		/* 137 */ YY_NO_ANCHOR,
		/* 138 */ YY_NO_ANCHOR,
		/* 139 */ YY_NO_ANCHOR,
		/* 140 */ YY_NO_ANCHOR,
		/* 141 */ YY_NO_ANCHOR,
		/* 142 */ YY_NO_ANCHOR,
		/* 143 */ YY_NO_ANCHOR,
		/* 144 */ YY_NO_ANCHOR,
		/* 145 */ YY_NO_ANCHOR,
		/* 146 */ YY_NO_ANCHOR,
		/* 147 */ YY_NO_ANCHOR,
		/* 148 */ YY_NO_ANCHOR,
		/* 149 */ YY_NO_ANCHOR,
		/* 150 */ YY_NO_ANCHOR,
		/* 151 */ YY_NO_ANCHOR,
		/* 152 */ YY_NO_ANCHOR,
		/* 153 */ YY_NO_ANCHOR,
		/* 154 */ YY_NO_ANCHOR,
		/* 155 */ YY_NO_ANCHOR,
		/* 156 */ YY_NO_ANCHOR,
		/* 157 */ YY_NO_ANCHOR,
		/* 158 */ YY_NO_ANCHOR,
		/* 159 */ YY_NO_ANCHOR,
		/* 160 */ YY_NO_ANCHOR,
		/* 161 */ YY_NO_ANCHOR,
		/* 162 */ YY_NO_ANCHOR,
		/* 163 */ YY_NO_ANCHOR,
		/* 164 */ YY_NO_ANCHOR,
		/* 165 */ YY_NO_ANCHOR,
		/* 166 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,130,
"8:9,28,1,28:2,3,8:18,28,8,2,8:5,4,6,5,54,52,7,55,53,29:10,56,50,51,48,49,8," +
"59,30,31,32,33,34,14,31,35,36,31:2,37,31,38,39,40,31,41,42,19,43,44,45,31:3" +
",8:4,46,8,11,47,9,22,13,27,47,17,15,47:2,10,47,16,21,23,47,18,12,25,26,20,2" +
"4,47:3,57,8,58,60,8,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,167,
"0,1,2,1,2,3,4,1,5,1,6,7,8,1,9,1:12,10,11,12,11,1:3,11:7,10,11:7,13,1:3,14,1" +
",15,1:2,16,1,17,18,19,11,10,20,10:8,11,10:5,21,22,23,24,25,1,26,27,28,29,30" +
",31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55" +
",56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80" +
",81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,11,10,96,97,98,99,100,101,102" +
",103,104")[0];

	private int yy_nxt[][] = unpackFromString(105,61,
"1,2,3,4,5,6,7,8,9,10,116,156:2,158,11,61,118,156:2,62,156,83,156,160,162,16" +
"4,156,86,4,60,157:2,159,157,161,157,84,117,119,87,163,157:4,165,9,156,12,9," +
"13,14,15,16,17,18,19,20,21,22,23,-1:62,4,-1,4,-1:24,4,-1:37,24,-1:61,25,-1:" +
"61,26,-1:62,156,166,120,156:16,-1,156,120,156:6,166,156:10,-1:22,157:6,27,1" +
"57:12,-1,157:7,27,157:11,-1:62,31,-1:18,32,-1:40,33,-1:21,157:19,-1,157:19," +
"-1:22,156:19,-1,156:19,-1:22,156:8,144,156:10,-1,156:6,144,156:12,-1:13,1,5" +
"0,51,52:58,1,54,80:2,55,81,85,80:54,-1:5,56,-1:55,1,59,82,-1,82:57,-1:29,60" +
",-1:40,156:3,128,156,28,156,29,156:10,28,-1,156:9,29,156:3,128,156:5,-1:22," +
"157:8,121,157:10,-1,157:6,121,157:12,-1:22,157:8,143,157:10,-1,157:6,143,15" +
"7:12,-1:15,80:2,-1:3,80:54,-1:6,57,-1:56,82,-1,82:57,-1:9,156:5,30,156:12,3" +
"0,-1,156:19,-1:22,157:3,131,157,64,157,65,157:10,64,-1,157:9,65,157:3,131,1" +
"57:5,-1:22,156:2,138,156:3,63,156:12,-1,156,138,156:5,63,156:11,-1:22,157:5" +
",66,157:12,66,-1,157:19,-1:22,156:10,34,156:5,34,156:2,-1,156:19,-1:22,157:" +
"10,67,157:5,67,157:2,-1,157:19,-1:22,156:15,35,156:3,-1,156:16,35,156:2,-1:" +
"22,157:15,68,157:3,-1,157:16,68,157:2,-1:22,156:10,36,156:5,36,156:2,-1,156" +
":19,-1:22,157:10,69,157:5,69,157:2,-1,157:19,-1:22,156:4,37,156:14,-1,156:5" +
",37,156:13,-1:22,157:7,41,157:11,-1,157:9,41,157:9,-1:22,156:14,38,156:4,-1" +
",156:11,38,156:7,-1:22,157:4,70,157:14,-1,157:5,70,157:13,-1:22,156:4,39,15" +
"6:14,-1,156:5,39,156:13,-1:22,157:4,72,157:14,-1,157:5,72,157:13,-1:22,40,1" +
"56:18,-1,156:3,40,156:15,-1:22,73,157:18,-1,157:3,73,157:15,-1:22,156,42,15" +
"6:17,-1,156:8,42,156:10,-1:22,157:14,71,157:4,-1,157:11,71,157:7,-1:22,156:" +
"7,74,156:11,-1,156:9,74,156:9,-1:22,157,75,157:17,-1,157:8,75,157:10,-1:22," +
"156:4,43,156:14,-1,156:5,43,156:13,-1:22,157:3,76,157:15,-1,157:13,76,157:5" +
",-1:22,156:3,44,156:15,-1,156:13,44,156:5,-1:22,157:4,77,157:14,-1,157:5,77" +
",157:13,-1:22,156:4,45,156:14,-1,156:5,45,156:13,-1:22,157:13,78,157:5,-1,1" +
"57:4,78,157:14,-1:22,156:4,46,156:14,-1,156:5,46,156:13,-1:22,157:3,79,157:" +
"15,-1,157:13,79,157:5,-1:22,156:13,47,156:5,-1,156:4,47,156:14,-1:22,156:3," +
"48,156:15,-1,156:13,48,156:5,-1:22,156:4,88,156:7,122,156:6,-1,156:5,88,156" +
":4,122,156:8,-1:22,157:4,89,157:7,133,157:6,-1,157:5,89,157:4,133,157:8,-1:" +
"22,156:4,90,156:7,92,156:6,-1,156:5,90,156:4,92,156:8,-1:22,157:4,91,157:7," +
"93,157:6,-1,157:5,91,157:4,93,157:8,-1:22,156:3,94,156:15,-1,156:13,94,156:" +
"5,-1:22,157:4,95,157:14,-1,157:5,95,157:13,-1:22,156:12,96,156:6,-1,156:10," +
"96,156:8,-1:22,157:2,139,157:16,-1,157,139,157:17,-1:22,156:3,98,156:15,-1," +
"156:13,98,156:5,-1:22,157:3,97,157:15,-1,157:13,97,157:5,-1:22,156:2,100,15" +
"6:16,-1,156,100,156:17,-1:22,157:3,99,157:15,-1,157:13,99,157:5,-1:22,156:1" +
"1,142,156:7,-1,156:15,142,156:3,-1:22,157:2,101,157:16,-1,157,101,157:17,-1" +
":22,156:12,102,156:6,-1,156:10,102,156:8,-1:22,157:11,141,157:7,-1,157:15,1" +
"41,157:3,-1:22,156:6,146,156:12,-1,156:7,146,156:11,-1:22,157:12,103,157:6," +
"-1,157:10,103,157:8,-1:22,156:4,104,156:14,-1,156:5,104,156:13,-1:22,157:12" +
",105,157:6,-1,157:10,105,157:8,-1:22,156:17,106,156,-1,156:14,106,156:4,-1:" +
"22,157:6,145,157:12,-1,157:7,145,157:11,-1:22,156,148,156:17,-1,156:8,148,1" +
"56:10,-1:22,157:3,107,157:15,-1,157:13,107,157:5,-1:22,156:3,108,156:15,-1," +
"156:13,108,156:5,-1:22,157:12,147,157:6,-1,157:10,147,157:8,-1:22,156:12,15" +
"0,156:6,-1,156:10,150,156:8,-1:22,157:4,149,157:14,-1,157:5,149,157:13,-1:2" +
"2,156:4,152,156:14,-1,156:5,152,156:13,-1:22,157,109,157:17,-1,157:8,109,15" +
"7:10,-1:22,156,110,156:17,-1,156:8,110,156:10,-1:22,157:6,111,157:12,-1,157" +
":7,111,157:11,-1:22,156:3,112,156:15,-1,156:13,112,156:5,-1:22,157:9,151,15" +
"7:9,-1,157:12,151,157:6,-1:22,156:6,114,156:12,-1,156:7,114,156:11,-1:22,15" +
"7:6,153,157:12,-1,157:7,153,157:11,-1:22,156:9,154,156:9,-1,156:12,154,156:" +
"6,-1:22,157:10,113,157:5,113,157:2,-1,157:19,-1:22,156:6,155,156:12,-1,156:" +
"7,155,156:11,-1:22,156:10,115,156:5,115,156:2,-1,156:19,-1:22,156,124,156,1" +
"26,156:15,-1,156:8,124,156:4,126,156:5,-1:22,157,123,125,157:16,-1,157,125," +
"157:6,123,157:10,-1:22,156:12,130,156:6,-1,156:10,130,156:8,-1:22,157,127,1" +
"57,129,157:15,-1,157:8,127,157:4,129,157:5,-1:22,156:8,132,156:10,-1,156:6," +
"132,156:12,-1:22,157:12,135,157:6,-1,157:10,135,157:8,-1:22,156:8,134,136,1" +
"56:9,-1,156:6,134,156:5,136,156:6,-1:22,157:8,137,157:10,-1,157:6,137,157:1" +
"2,-1:22,156:2,140,156:16,-1,156,140,156:17,-1:13");

	public java_cup.runtime.Symbol next_token ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {

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
        case BLOCK_COMMENTS:
            yybegin(YYINITIAL); 
            return new Symbol(TokenConstants.ERROR, "EOF in comment.");
    }
    return new Symbol(TokenConstants.EOF);
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 0:
						{ /*Integer*/ return new Symbol(TokenConstants.INT_CONST, AbstractTable.inttable.addString(yytext())); }
					case -2:
						break;
					case 1:
						
					case -3:
						break;
					case 2:
						{ curr_lineno++; }
					case -4:
						break;
					case 3:
						{
    /* new string, clean string buffer */
    string_buf.delete(0, string_buf.length());
    nullInString=false;
    yybegin(STRING); 
}
					case -5:
						break;
					case 4:
						{ /*White space*/ }
					case -6:
						break;
					case 5:
						{ return new Symbol(TokenConstants.LPAREN); }
					case -7:
						break;
					case 6:
						{ return new Symbol(TokenConstants.MULT);   }
					case -8:
						break;
					case 7:
						{ return new Symbol(TokenConstants.RPAREN); }
					case -9:
						break;
					case 8:
						{ return new Symbol(TokenConstants.MINUS);  }
					case -10:
						break;
					case 9:
						{ /* This rule should be the very last
                            in your lexical specification and
                            will match match everything not
                            matched by other lexical rules. */
                    return new Symbol(TokenConstants.ERROR, new String(yytext())); }
					case -11:
						break;
					case 10:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -12:
						break;
					case 11:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -13:
						break;
					case 12:
						{ return new Symbol(TokenConstants.EQ);     }
					case -14:
						break;
					case 13:
						{ return new Symbol(TokenConstants.SEMI);   }
					case -15:
						break;
					case 14:
						{ return new Symbol(TokenConstants.LT);     }
					case -16:
						break;
					case 15:
						{ return new Symbol(TokenConstants.COMMA);  }
					case -17:
						break;
					case 16:
						{ return new Symbol(TokenConstants.DIV);    }
					case -18:
						break;
					case 17:
						{ return new Symbol(TokenConstants.PLUS);   }
					case -19:
						break;
					case 18:
						{ return new Symbol(TokenConstants.DOT);    }
					case -20:
						break;
					case 19:
						{ return new Symbol(TokenConstants.COLON);  }
					case -21:
						break;
					case 20:
						{ return new Symbol(TokenConstants.LBRACE); }
					case -22:
						break;
					case 21:
						{ return new Symbol(TokenConstants.RBRACE); }
					case -23:
						break;
					case 22:
						{ return new Symbol(TokenConstants.AT);     }
					case -24:
						break;
					case 23:
						{ return new Symbol(TokenConstants.NEG);    }
					case -25:
						break;
					case 24:
						{ yybegin(BLOCK_COMMENTS); }
					case -26:
						break;
					case 25:
						{ return new Symbol(TokenConstants.ERROR,"Mismatched '*)'"); }
					case -27:
						break;
					case 26:
						{ yybegin(INLINE_COMMENTS); }
					case -28:
						break;
					case 27:
						{ return new Symbol(TokenConstants.FI); }
					case -29:
						break;
					case 28:
						{ return new Symbol(TokenConstants.IF); }
					case -30:
						break;
					case 29:
						{ return new Symbol(TokenConstants.IN); }
					case -31:
						break;
					case 30:
						{ return new Symbol(TokenConstants.OF); }
					case -32:
						break;
					case 31:
						{ /* Sample lexical rule for "=>" arrow.
                                     Further lexical rules should be defined
                                     here, after the last %% separator */
                                  return new Symbol(TokenConstants.DARROW); }
					case -33:
						break;
					case 32:
						{ return new Symbol(TokenConstants.ASSIGN); }
					case -34:
						break;
					case 33:
						{ return new Symbol(TokenConstants.LE);     }
					case -35:
						break;
					case 34:
						{ return new Symbol(TokenConstants.LET); }
					case -36:
						break;
					case 35:
						{ return new Symbol(TokenConstants.NEW); }
					case -37:
						break;
					case 36:
						{ return new Symbol(TokenConstants.NOT); }
					case -38:
						break;
					case 37:
						{ return new Symbol(TokenConstants.CASE); }
					case -39:
						break;
					case 38:
						{ return new Symbol(TokenConstants.LOOP); }
					case -40:
						break;
					case 39:
						{ return new Symbol(TokenConstants.ELSE); }
					case -41:
						break;
					case 40:
						{ return new Symbol(TokenConstants.ESAC); }
					case -42:
						break;
					case 41:
						{ return new Symbol(TokenConstants.THEN); }
					case -43:
						break;
					case 42:
						{ return new Symbol(TokenConstants.POOL); }
					case -44:
						break;
					case 43:
						{ return new Symbol(TokenConstants.BOOL_CONST, new Boolean(true)); }
					case -45:
						break;
					case 44:
						{ return new Symbol(TokenConstants.CLASS); }
					case -46:
						break;
					case 45:
						{ return new Symbol(TokenConstants.WHILE); }
					case -47:
						break;
					case 46:
						{ return new Symbol(TokenConstants.BOOL_CONST, new Boolean(false)); }
					case -48:
						break;
					case 47:
						{ return new Symbol(TokenConstants.ISVOID); }
					case -49:
						break;
					case 48:
						{ return new Symbol(TokenConstants.INHERITS); }
					case -50:
						break;
					case 50:
						{
    // if an escape character appears before a \n, we must put \n into the string
    curr_lineno++;
    if(string_buf.length()>0 && escaped) {
        escaped = false;
        string_buf.setCharAt(string_buf.length()-1, '\n');
    } else {
        yybegin(YYINITIAL);
        return new Symbol(TokenConstants.ERROR, "Unterminated string constant");
    }
}
					case -51:
						break;
					case 51:
						{ 
    // if an escape character appears before a quote, we must put a quote into the string
    if(string_buf.length()>0 && escaped) {
        escaped = false;
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
					case -52:
						break;
					case 52:
						{
    // process all character one at a time, except for \n or "
    if (string_buf.length() == 0) {
        string_buf.append(yytext());
        if (yytext().charAt(0) == '\\') {
            escaped = true;
        }
    }else {
        if(yytext().charAt(0)=='\0') {
            nullInString=true;
        } else if (escaped == false && yytext().charAt(0) == '\\') {
            escaped = true;
            string_buf.append(yytext().charAt(0));
        } else if (escaped) {
            int length=string_buf.length();
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
            case '\\':
                // escpae an escape character, in the string it is just a single \
                break;
            default:
                string_buf.setCharAt(length-1, yytext().charAt(0));
            }
            escaped = false;
        }else {
            string_buf.append(yytext());
        }
    }
}
					case -53:
						break;
					case 53:
						{ /* in block comments, skip all character except for * ( ) \n */ }
					case -54:
						break;
					case 54:
						{ curr_lineno++; }
					case -55:
						break;
					case 55:
						{ /* skill single * ( ) as well */}
					case -56:
						break;
					case 56:
						{ nestedComments++; }
					case -57:
						break;
					case 57:
						{ 
    if (nestedComments == 0) {
        yybegin(YYINITIAL);
    }else {
        nestedComments--;
    }
}
					case -58:
						break;
					case 58:
						{ }
					case -59:
						break;
					case 59:
						{ curr_lineno++; yybegin(YYINITIAL); }
					case -60:
						break;
					case 60:
						{ /*Integer*/ return new Symbol(TokenConstants.INT_CONST, AbstractTable.inttable.addString(yytext())); }
					case -61:
						break;
					case 61:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -62:
						break;
					case 62:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -63:
						break;
					case 63:
						{ return new Symbol(TokenConstants.FI); }
					case -64:
						break;
					case 64:
						{ return new Symbol(TokenConstants.IF); }
					case -65:
						break;
					case 65:
						{ return new Symbol(TokenConstants.IN); }
					case -66:
						break;
					case 66:
						{ return new Symbol(TokenConstants.OF); }
					case -67:
						break;
					case 67:
						{ return new Symbol(TokenConstants.LET); }
					case -68:
						break;
					case 68:
						{ return new Symbol(TokenConstants.NEW); }
					case -69:
						break;
					case 69:
						{ return new Symbol(TokenConstants.NOT); }
					case -70:
						break;
					case 70:
						{ return new Symbol(TokenConstants.CASE); }
					case -71:
						break;
					case 71:
						{ return new Symbol(TokenConstants.LOOP); }
					case -72:
						break;
					case 72:
						{ return new Symbol(TokenConstants.ELSE); }
					case -73:
						break;
					case 73:
						{ return new Symbol(TokenConstants.ESAC); }
					case -74:
						break;
					case 74:
						{ return new Symbol(TokenConstants.THEN); }
					case -75:
						break;
					case 75:
						{ return new Symbol(TokenConstants.POOL); }
					case -76:
						break;
					case 76:
						{ return new Symbol(TokenConstants.CLASS); }
					case -77:
						break;
					case 77:
						{ return new Symbol(TokenConstants.WHILE); }
					case -78:
						break;
					case 78:
						{ return new Symbol(TokenConstants.ISVOID); }
					case -79:
						break;
					case 79:
						{ return new Symbol(TokenConstants.INHERITS); }
					case -80:
						break;
					case 80:
						{ /* in block comments, skip all character except for * ( ) \n */ }
					case -81:
						break;
					case 81:
						{ /* skill single * ( ) as well */}
					case -82:
						break;
					case 82:
						{ }
					case -83:
						break;
					case 83:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -84:
						break;
					case 84:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -85:
						break;
					case 85:
						{ /* skill single * ( ) as well */}
					case -86:
						break;
					case 86:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -87:
						break;
					case 87:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -88:
						break;
					case 88:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -89:
						break;
					case 89:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -90:
						break;
					case 90:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -91:
						break;
					case 91:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -92:
						break;
					case 92:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -93:
						break;
					case 93:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -94:
						break;
					case 94:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -95:
						break;
					case 95:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -96:
						break;
					case 96:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -97:
						break;
					case 97:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -98:
						break;
					case 98:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -99:
						break;
					case 99:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -100:
						break;
					case 100:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -101:
						break;
					case 101:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -102:
						break;
					case 102:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -103:
						break;
					case 103:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -104:
						break;
					case 104:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -105:
						break;
					case 105:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -106:
						break;
					case 106:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -107:
						break;
					case 107:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -108:
						break;
					case 108:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -109:
						break;
					case 109:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -110:
						break;
					case 110:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -111:
						break;
					case 111:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -112:
						break;
					case 112:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -113:
						break;
					case 113:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -114:
						break;
					case 114:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -115:
						break;
					case 115:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -116:
						break;
					case 116:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -117:
						break;
					case 117:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -118:
						break;
					case 118:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -119:
						break;
					case 119:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -120:
						break;
					case 120:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -121:
						break;
					case 121:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -122:
						break;
					case 122:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -123:
						break;
					case 123:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -124:
						break;
					case 124:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -125:
						break;
					case 125:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -126:
						break;
					case 126:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -127:
						break;
					case 127:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -128:
						break;
					case 128:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -129:
						break;
					case 129:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -130:
						break;
					case 130:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -131:
						break;
					case 131:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -132:
						break;
					case 132:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -133:
						break;
					case 133:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -134:
						break;
					case 134:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -135:
						break;
					case 135:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -136:
						break;
					case 136:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -137:
						break;
					case 137:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -138:
						break;
					case 138:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -139:
						break;
					case 139:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -140:
						break;
					case 140:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -141:
						break;
					case 141:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -142:
						break;
					case 142:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -143:
						break;
					case 143:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -144:
						break;
					case 144:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -145:
						break;
					case 145:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -146:
						break;
					case 146:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -147:
						break;
					case 147:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -148:
						break;
					case 148:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -149:
						break;
					case 149:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -150:
						break;
					case 150:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -151:
						break;
					case 151:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -152:
						break;
					case 152:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -153:
						break;
					case 153:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -154:
						break;
					case 154:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -155:
						break;
					case 155:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -156:
						break;
					case 156:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -157:
						break;
					case 157:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -158:
						break;
					case 158:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -159:
						break;
					case 159:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -160:
						break;
					case 160:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -161:
						break;
					case 161:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -162:
						break;
					case 162:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -163:
						break;
					case 163:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -164:
						break;
					case 164:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -165:
						break;
					case 165:
						{ /*Typed identifiers*/ return new Symbol(TokenConstants.TYPEID, AbstractTable.idtable.addString(yytext())); }
					case -166:
						break;
					case 166:
						{ /*Object identifiers*/ return new Symbol(TokenConstants.OBJECTID, AbstractTable.idtable.addString(yytext())); }
					case -167:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
