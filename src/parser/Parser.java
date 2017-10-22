package parser;

import ast.Ast;
import ast.Visitor;
import lexer.Lexer;
import lexer.Token;
import lexer.Token.Kind;

import javax.sound.sampled.Line;
import java.util.LinkedList;

public class Parser {
    Lexer lexer;
    Token current;
    boolean rollback;
    Kind rollbackKind;
    String rollbackLexeme;

    public Parser(String fname, java.io.InputStream fstream) {
        lexer = new Lexer(fname, fstream);
//        fstream.mark(1);
        //System.out.println(fstream.markSupported() ? "TTTT" : "FFFF");
        current = lexer.nextToken();
        rollback = false;
        rollbackLexeme = null;
    }

    // /////////////////////////////////////////////
    // utility methods to connect the lexer
    // and the parser.

    private void advance() {
        current = lexer.nextToken();
    }

    private void eatToken(Kind kind) {
        if (kind == current.kind)
            advance();
        else {
            System.out.println("Expects: " + kind.toString());
            System.out.println("But got: " + current.kind.toString() + " " + current.lexeme);
            System.out.println("At line " + current.lineNum);
            System.exit(1);
        }
    }

    private void error() {
        System.out.println("Syntax error: compilation aborting...");
        System.out.println("Current Token: " + current.toString());
        System.exit(1);
        return;
    }

    // ////////////////////////////////////////////////////////////
    // below are method for parsing.

    // A bunch of parsing methods to parse expressions. The messy
    // parts are to deal with precedence and associativity.

    // ExpList -> Exp ExpRest*
    // ->
    // ExpRest -> , Exp
    private LinkedList<Ast.Exp.T> parseExpList() {
        LinkedList<Ast.Exp.T> expList = new LinkedList<>();
        if (current.kind == Kind.TOKEN_RPAREN)
            return expList;
        expList.add(parseExp());
        while (current.kind == Kind.TOKEN_COMMER) {
            advance();
            expList.add(parseExp());
        }
        return expList;
    }

    // AtomExp -> (exp)
    // -> INTEGER_LITERAL
    // -> true
    // -> false
    // -> this
    // -> id
    // -> new int [exp]
    // -> new id ()
    private Ast.Exp.T parseAtomExp() {
        String id;
        switch (current.kind) {
            case TOKEN_LPAREN:
                advance();
                Ast.Exp.T e = parseExp();
                eatToken(Kind.TOKEN_RPAREN);
                return e;
            case TOKEN_NUM:
                int num = Integer.parseInt(current.lexeme);
                advance();
                return new Ast.Exp.Num(num);
            case TOKEN_TRUE:
                advance();
                return new Ast.Exp.True();
            case TOKEN_FALSE:
                advance();
                return new Ast.Exp.False();
            case TOKEN_THIS:
                advance();
                return new Ast.Exp.This();
            case TOKEN_ID:
                id = current.lexeme;
                advance();
                return new Ast.Exp.Id(id);
            case TOKEN_NEW: {
                advance();
                switch (current.kind) {
                    case TOKEN_INT:
                        advance();
                        eatToken(Kind.TOKEN_LBRACK);
                        Ast.Exp.T ee = parseExp();
                        eatToken(Kind.TOKEN_RBRACK);
                        return new Ast.Exp.NewIntArray(ee);
                    case TOKEN_ID:
                        id = current.lexeme;
                        advance();
                        eatToken(Kind.TOKEN_LPAREN);
                        eatToken(Kind.TOKEN_RPAREN);
                        return new Ast.Exp.NewObject(id);
                    default:
                        System.out.println("Func: parseAtomExp 2");
                        error();
                        return null;
                }
            }
            default:
                System.out.println("Function: parseAtomExp 1");
                error();
                return null;
        }
    }

    // NotExp -> AtomExp
    // -> AtomExp .id (expList)
    // -> AtomExp [exp]
    // -> AtomExp .length
    private Ast.Exp.T parseNotExp() {
        Ast.Exp.T atomExp = parseAtomExp();
        while (current.kind == Kind.TOKEN_DOT || current.kind == Kind.TOKEN_LBRACK) {
            if (current.kind == Kind.TOKEN_DOT) {
                advance();
                if (current.kind == Kind.TOKEN_LENGTH) {
                    advance();
                    return new Ast.Exp.Length(atomExp);
                }
                String id = current.lexeme;
                eatToken(Kind.TOKEN_ID);
                eatToken(Kind.TOKEN_LPAREN);
                LinkedList<Ast.Exp.T> args = parseExpList();
                eatToken(Kind.TOKEN_RPAREN);
                return new Ast.Exp.Call(atomExp, id, args);
            } else {
                advance();
                Ast.Exp.T index = parseExp();
                eatToken(Kind.TOKEN_RBRACK);
                return new Ast.Exp.ArraySelect(atomExp, index);
            }
        }
        return atomExp;
    }

    // TimesExp -> ! TimesExp
    // -> NotExp
    private Ast.Exp.T parseTimesExp() {
        /*if (current.kind == Kind.TOKEN_LPAREN || current.kind == Kind.TOKEN_NUM
                || current.kind == Kind.TOKEN_TRUE || current.kind == Kind.TOKEN_FALSE
                || current.kind == Kind.TOKEN_THIS || current.kind == Kind.TOKEN_ID
                || current.kind == Kind.TOKEN_NEW) {
            Ast.Exp.T notExp = parseNotExp();
            return new Ast.Exp.Not(notExp);
        }*/
        while (current.kind == Kind.TOKEN_NOT) {
            advance();
            return new Ast.Exp.Not(parseTimesExp());
        }
        return parseNotExp();
    }

    // AddSubExp -> TimesExp * TimesExp
    // -> TimesExp
    private Ast.Exp.T parseAddSubExp() {
        Ast.Exp.T left = parseTimesExp();
        while (current.kind == Kind.TOKEN_TIMES) {
            advance();
            Ast.Exp.T right = parseTimesExp();
            return new Ast.Exp.Times(left, right);
        }
        return left;
    }

    // LtExp -> AddSubExp + AddSubExp
    // -> AddSubExp - AddSubExp
    // -> AddSubExp
    private Ast.Exp.T parseLtExp() {
        Ast.Exp.T left = parseAddSubExp();
        while (current.kind == Kind.TOKEN_ADD || current.kind == Kind.TOKEN_SUB) {
            if (current.kind == Kind.TOKEN_ADD) {
                advance();
                Ast.Exp.T right = parseAddSubExp();
                return new Ast.Exp.Add(left, right);
            } else {
                advance();
                Ast.Exp.T right = parseAddSubExp();
                return new Ast.Exp.Sub(left, right);
            }
        }
        return left;
    }

    // AndExp -> LtExp < LtExp
    // -> LtExp
    private Ast.Exp.T parseAndExp() {
        Ast.Exp.T left = parseLtExp();
        while (current.kind == Kind.TOKEN_LT) {
            advance();
            Ast.Exp.T right = parseLtExp();
            return new Ast.Exp.Lt(left, right);
        }
        return left;
    }

    // Exp -> AndExp && AndExp
    // -> AndExp
    private Ast.Exp.T parseExp() {
        Ast.Exp.T andExp = parseAndExp();
        while (current.kind == Kind.TOKEN_AND) {
            advance();
            Ast.Exp.T andExpp = parseAndExp();
            return new Ast.Exp.And(andExp, andExpp);
        }
        return andExp;
    }

    // Statement -> { Statement* }
    // -> if ( Exp ) Statement else Statement
    // -> while ( Exp ) Statement
    // -> System.out.println ( Exp ) ;
    // -> id = Exp ;
    // -> id [ Exp ]= Exp ;
    private Ast.Stm.T parseStatement() {
        // Lab1. Exercise 4: Fill in the missing code
        // to parse a statement.
//        new util.Todo();
        switch (current.kind) {
            case TOKEN_LBRACE:
                advance();
                LinkedList<Ast.Stm.T> stms = parseStatements();
                eatToken(Kind.TOKEN_RBRACE);
                return new Ast.Stm.Block(stms);
            case TOKEN_IF:
                advance();
                eatToken(Kind.TOKEN_LPAREN);
                Ast.Exp.T condition = parseExp();
                eatToken(Kind.TOKEN_RPAREN);
                Ast.Stm.T thenn = parseStatement();
                eatToken(Kind.TOKEN_ELSE);
                Ast.Stm.T elsee = parseStatement();
                return new Ast.Stm.If(condition, thenn, elsee);
            case TOKEN_WHILE:
                advance();
                eatToken(Kind.TOKEN_LPAREN);
                Ast.Exp.T cc = parseExp();
                eatToken(Kind.TOKEN_RPAREN);
                Ast.Stm.T body = parseStatement();
                return new Ast.Stm.While(cc, body);
            case TOKEN_SYSTEM:
                advance();
                eatToken(Kind.TOKEN_DOT);
                eatToken(Kind.TOKEN_OUT);
                eatToken(Kind.TOKEN_DOT);
                eatToken(Kind.TOKEN_PRINTLN);
                eatToken(Kind.TOKEN_LPAREN);
                Ast.Exp.T bb = parseExp();
                eatToken(Kind.TOKEN_RPAREN);
                eatToken(Kind.TOKEN_SEMI);
                return new Ast.Stm.Print(bb);
            case TOKEN_ID:
                String id;
                if (rollback) {
                    // deal with backtracking
                    rollback = false;
                    current.kind = rollbackKind;
                    id = rollbackLexeme;
                    //System.out.println("aaa " + id);
                } else {
                    id = current.lexeme;
                    advance();
                }
                if (current.kind == Kind.TOKEN_LBRACK) {
                    eatToken(Kind.TOKEN_LBRACK);
                    Ast.Exp.T exp = parseExp();
                    eatToken(Kind.TOKEN_RBRACK);
                    eatToken(Kind.TOKEN_ASSIGN);
                    Ast.Exp.T expp = parseExp();
                    eatToken(Kind.TOKEN_SEMI);
                    return new Ast.Stm.AssignArray(id, exp, expp);
                } else {
                    eatToken(Kind.TOKEN_ASSIGN);
                    Ast.Exp.T exppp = parseExp();
                    eatToken(Kind.TOKEN_SEMI);
                    return new Ast.Stm.Assign(id, exppp);
                }
            default:
                System.out.println("Func: parseStatement");
                error();
                return null;
        }
    }

    // Statements -> Statement Statements
    // ->
    private LinkedList<Ast.Stm.T> parseStatements() {
        LinkedList<Ast.Stm.T> stms = new LinkedList<>();
        //System.out.println("current kind: " + current.kind.toString());
        while (current.kind == Kind.TOKEN_LBRACE || current.kind == Kind.TOKEN_IF
                || current.kind == Kind.TOKEN_WHILE
                || current.kind == Kind.TOKEN_SYSTEM || current.kind == Kind.TOKEN_ID) {
            stms.add(parseStatement());
        }
        return stms;
    }

    // Type -> int []
    // -> boolean
    // -> int
    // -> id
    private Ast.Type.T parseType() {
        // Lab1. Exercise 4: Fill in the missing code
        // to parse a type.
//        new util.Todo();
        switch (current.kind) {
            case TOKEN_INT:
                advance();
                if (current.kind == Kind.TOKEN_LBRACK) {
                    eatToken(Kind.TOKEN_LBRACK);
                    eatToken(Kind.TOKEN_RBRACK);
                    return new Ast.Type.IntArray();
                }
                return new Ast.Type.Int();
            case TOKEN_BOOLEAN:
                advance();
                return new Ast.Type.Boolean();
            case TOKEN_ID:
                String id = current.lexeme;
                advance();
                return new Ast.Type.ClassType(id);
            default:
                System.out.println(current.toString());
                System.out.println("aaa");
                error();
                return null;
        }
    }

    // VarDecl -> Type id ;
    private Ast.Dec.T parseVarDecl() {
        // to parse the "Type" nonterminal in this method, instead of writing
        // a fresh one.
        rollbackLexeme = current.lexeme;
        Ast.Type.T type = parseType();
        if (current.kind == Kind.TOKEN_ID) {
            String id = current.lexeme;
            eatToken(Kind.TOKEN_ID);
            eatToken(Kind.TOKEN_SEMI);
            return new Ast.Dec.DecSingle(type, id);
        } else {
            // deal with the following situation
            // int i;
            // i = 0;
            // prevent backtracking
            rollback = true;
            rollbackKind = current.kind;
            current.kind = Kind.TOKEN_ID;
            return null;
        }
    }

    // VarDecls -> VarDecl VarDecls
    // ->
    private LinkedList<Ast.Dec.T> parseVarDecls() {
        LinkedList<Ast.Dec.T> decs = new LinkedList<>();
        while (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
                || current.kind == Kind.TOKEN_ID) {
            if (rollback) {
                return decs;
            }
            Ast.Dec.T dec = parseVarDecl();
            if (dec != null) {
                decs.add(dec);
            }
        }
        return decs;
    }

    // FormalList -> Type id FormalRest*
    // ->
    // FormalRest -> , Type id
    private LinkedList<Ast.Dec.T> parseFormalList() {
        LinkedList<Ast.Dec.T> formals = new LinkedList<>();
        if (current.kind == Kind.TOKEN_INT || current.kind == Kind.TOKEN_BOOLEAN
                || current.kind == Kind.TOKEN_ID) {
            Ast.Type.T type;
            String id;
            type = parseType();
            id = current.lexeme;
            eatToken(Kind.TOKEN_ID);
            formals.add(new Ast.Dec.DecSingle(type, id));
            while (current.kind == Kind.TOKEN_COMMER) {
                advance();
                type = parseType();
                id = current.lexeme;
                eatToken(Kind.TOKEN_ID);
                formals.add(new Ast.Dec.DecSingle(type, id));
            }
        }
        return formals;
    }

    // Method -> public Type id ( FormalList )
    // { VarDecl* Statement* return Exp ;}
    private Ast.Method.T parseMethod() {
        // Lab1. Exercise 4: Fill in the missing code
        // to parse a method.
//        new util.Todo();
        eatToken(Kind.TOKEN_PUBLIC);
        Ast.Type.T retType = parseType();
        String id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LPAREN);
        LinkedList<Ast.Dec.T> formals = parseFormalList();
        eatToken(Kind.TOKEN_RPAREN);
        eatToken(Kind.TOKEN_LBRACE);
//        System.out.println("hhhh");
        LinkedList<Ast.Dec.T> locals = parseVarDecls();
        LinkedList<Ast.Stm.T> stms = parseStatements();
        //System.out.println("aaa");
        eatToken(Kind.TOKEN_RETURN);
        Ast.Exp.T retExp = parseExp();
        eatToken(Kind.TOKEN_SEMI);
        eatToken(Kind.TOKEN_RBRACE);
        return new Ast.Method.MethodSingle(retType, id, formals, locals, stms, retExp);
    }

    // MethodDecls -> MethodDecl MethodDecls
    // ->
    private LinkedList<Ast.Method.T> parseMethodDecls() {
        LinkedList<Ast.Method.T> methods = new LinkedList<>();
        while (current.kind == Kind.TOKEN_PUBLIC) {
            Ast.Method.T method = parseMethod();
            methods.add(method);
        }
        return methods;
    }

    // ClassDecl -> class id { VarDecl* MethodDecl* }
    // -> class id extends id { VarDecl* MethodDecl* }
    private Ast.Class.T parseClassDecl() {
        eatToken(Kind.TOKEN_CLASS);
        String id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        String extendss = null;
        if (current.kind == Kind.TOKEN_EXTENDS) {
            eatToken(Kind.TOKEN_EXTENDS);
            extendss = current.lexeme;
            eatToken(Kind.TOKEN_ID);
        }
        eatToken(Kind.TOKEN_LBRACE);
        LinkedList<Ast.Dec.T> decs = parseVarDecls();
        LinkedList<Ast.Method.T> methods = parseMethodDecls();
        eatToken(Kind.TOKEN_RBRACE);
        return new Ast.Class.ClassSingle(id, extendss, decs, methods);
    }

    // ClassDecls -> ClassDecl ClassDecls
    // ->
    private LinkedList<Ast.Class.T> parseClassDecls() {
        LinkedList<Ast.Class.T> classes = new LinkedList<>();
        while (current.kind == Kind.TOKEN_CLASS) {
            classes.add(parseClassDecl());
        }
        return classes;
    }

    // MainClass -> class id
    // {
    // public static void main ( String [] id )
    // {
    // Statement
    // }
    // }
    private Ast.MainClass.T parseMainClass() {
        // Lab1. Exercise 4: Fill in the missing code
        // to parse a main class as described by the
        // grammar above.
//        new util.Todo();
        eatToken(Kind.TOKEN_CLASS);
        String id = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_LBRACE);
        eatToken(Kind.TOKEN_PUBLIC);
        eatToken(Kind.TOKEN_STATIC);
        eatToken(Kind.TOKEN_VOID);
        eatToken(Kind.TOKEN_MAIN);
        eatToken(Kind.TOKEN_LPAREN);
        eatToken(Kind.TOKEN_STRING);
        eatToken(Kind.TOKEN_LBRACK);
        eatToken(Kind.TOKEN_RBRACK);
        String args = current.lexeme;
        eatToken(Kind.TOKEN_ID);
        eatToken(Kind.TOKEN_RPAREN);
        eatToken(Kind.TOKEN_LBRACE);
        Ast.Stm.T stm = parseStatement();
        eatToken(Kind.TOKEN_RBRACE);
        eatToken(Kind.TOKEN_RBRACE);
        return new Ast.MainClass.MainClassSingle(id, args, stm);
    }


    // Program -> MainClass ClassDecl*
    private Ast.Program.T parseProgram() {
        Ast.MainClass.T main = parseMainClass();
        LinkedList<Ast.Class.T> classes = parseClassDecls();
        eatToken(Kind.TOKEN_EOF);
        return new Ast.Program.ProgramSingle(main, classes);
    }


    public ast.Ast.Program.T parse() {
        return parseProgram();
        //return null;
    }
}
