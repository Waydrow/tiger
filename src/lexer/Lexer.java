package lexer;

import static control.Control.ConLexer.dump;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import lexer.Token.Kind;
import util.Todo;

public class Lexer {
    String fname; // the input file name to be compiled
    InputStream fstream; // input stream for the above file

    // store keywords in Mini Java
    private Map<String, Kind> keywords = new HashMap<>();

    // line number
    private int lineNum;

    // next char
    private int lookahead;

    public Lexer(String fname, InputStream fstream) {
        this.fname = fname;
        this.fstream = fstream;
        this.lineNum = 1;
        this.lookahead = 0;

        // initialize keywords
        keywords.put("boolean", Kind.TOKEN_BOOLEAN);
        keywords.put("class", Kind.TOKEN_CLASS);
        keywords.put("else", Kind.TOKEN_ELSE);
        keywords.put("extends", Kind.TOKEN_EXTENDS);
        keywords.put("false", Kind.TOKEN_FALSE);
        keywords.put("if", Kind.TOKEN_IF);
        keywords.put("int", Kind.TOKEN_INT);
        keywords.put("length", Kind.TOKEN_LENGTH);
        keywords.put("main", Kind.TOKEN_MAIN);
        keywords.put("new", Kind.TOKEN_NEW);
        keywords.put("out", Kind.TOKEN_OUT);
        keywords.put("println", Kind.TOKEN_PRINTLN);
        keywords.put("public", Kind.TOKEN_PUBLIC);
        keywords.put("return", Kind.TOKEN_RETURN);
        keywords.put("static", Kind.TOKEN_STATIC);
        keywords.put("String", Kind.TOKEN_STRING);
        keywords.put("System", Kind.TOKEN_SYSTEM);
        keywords.put("this", Kind.TOKEN_THIS);
        keywords.put("true", Kind.TOKEN_TRUE);
        keywords.put("void", Kind.TOKEN_VOID);
        keywords.put("while", Kind.TOKEN_WHILE);
    }

    // When called, return the next token (refer to the code "Token.java")
    // from the input stream.
    // Return TOKEN_EOF when reaching the end of the input stream.
    private Token nextTokenInternal() throws Exception {
        int c;
        if (lookahead == 0) {
            c = this.fstream.read();
        } else {
            c = lookahead;
            //lookahead = this.fstream.read();
        }

//        c = this.fstream.read();
        if (-1 == c)
            // The value for "lineNum" is now "null",
            // you should modify this to an appropriate
            // line number for the "EOF" token.
            return new Token(Kind.TOKEN_EOF, this.lineNum);

        // skip all kinds of "blanks"
        while (' ' == c || '\t' == c || '\r' == c || c == '\n') {
            if (c == '\n') {
                this.lineNum += 1;
            }
            c = this.fstream.read();
        }

        lookahead = this.fstream.read();

        //test(c);

        if (-1 == c)
            return new Token(Kind.TOKEN_EOF, this.lineNum);

        switch (c) {
            case '+':
                return new Token(Kind.TOKEN_ADD, this.lineNum);
            case '&':
                c = this.fstream.read();
                return new Token(Kind.TOKEN_AND, this.lineNum);
            case '=':
                return new Token(Kind.TOKEN_ASSIGN, this.lineNum);
            case ',':
                return new Token(Kind.TOKEN_COMMER, this.lineNum);
            case '.':
                return new Token(Kind.TOKEN_DOT, this.lineNum);
            case '{':
                return new Token(Kind.TOKEN_LBRACE, this.lineNum);
            case '[':
                return new Token(Kind.TOKEN_LBRACK, this.lineNum);
            case '(':
                return new Token(Kind.TOKEN_LPAREN, this.lineNum);
            case '<':
                return new Token(Kind.TOKEN_LT, this.lineNum);
            case '!':
                return new Token(Kind.TOKEN_NOT, this.lineNum);
            case '}':
                return new Token(Kind.TOKEN_RBRACE, this.lineNum);
            case ']':
                return new Token(Kind.TOKEN_RBRACK, this.lineNum);
            case ')':
                return new Token(Kind.TOKEN_RPAREN, this.lineNum);
            case ';':
                return new Token(Kind.TOKEN_SEMI, this.lineNum);
            case '-':
                return new Token(Kind.TOKEN_SUB, this.lineNum);
            case '*':
                return new Token(Kind.TOKEN_TIMES, this.lineNum);
            default:
                // Lab 1, exercise 2: supply missing code to
                // lex other kinds of tokens.
                // Hint: think carefully about the basic
                // data structure and algorithms. The code
                // is not that much and may be less than 50 lines. If you
                // find you are writing a lot of code, you
                // are on the wrong way.

                // c is a letter
                String str = "";
                Token relToken = null; // result token
                if (Character.isLetter(c)) {
                    str += Character.toString((char) c);
                    while(Character.isLetter(lookahead) || Character.isDigit(lookahead) || (char) lookahead == '_') {
                        str += Character.toString((char) lookahead);
                        lookahead = this.fstream.read();
                    }
                    //System.out.println("hhh " + str);
                    Kind k = keywords.get(str);
                    if (k != null) {
                        relToken =  new Token(k, this.lineNum, str);
                    } else {
                        relToken =  new Token(Kind.TOKEN_ID, this.lineNum, str);
                    }
                } else if (Character.isDigit(c)) {
                    str = "";
                    str += Character.toString((char) c);
                    while(Character.isDigit(lookahead)) {
                        str += Character.toString((char) lookahead);
                        lookahead = this.fstream.read();
                    }
                    relToken =  new Token(Kind.TOKEN_NUM, this.lineNum, str);
                }
                return relToken;
        }
    }

    public Token nextToken() {
        Token t = null;

        try {
            t = this.nextTokenInternal();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (dump)
            System.out.println(t.toString());
        return t;
    }

    private void test(int c) {
        System.out.print("c: " + (char) c);
        System.out.println("   lookahead: " + (char) lookahead + "   lineNum: " + this.lineNum);
    }
}
