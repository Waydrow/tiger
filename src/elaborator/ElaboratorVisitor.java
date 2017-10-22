package elaborator;

import java.util.LinkedList;

import ast.Ast;
import ast.Ast.Class;
import ast.Ast.Exp.False;
import ast.Ast.Exp.Id;
import ast.Ast.Exp.Length;
import ast.Ast.Exp.Lt;
import ast.Ast.Exp.NewIntArray;
import ast.Ast.Exp.NewObject;
import ast.Ast.Exp.Not;
import ast.Ast.Exp.Num;
import ast.Ast.Exp.Sub;
import ast.Ast.Exp.This;
import ast.Ast.Exp.Times;
import ast.Ast.Exp.True;
import ast.Ast.MainClass;
import ast.Ast.Class.ClassSingle;
import ast.Ast.Dec;
import ast.Ast.Dec.DecSingle;
import ast.Ast.Exp;
import ast.Ast.Exp.Add;
import ast.Ast.Exp.And;
import ast.Ast.Exp.ArraySelect;
import ast.Ast.Exp.Call;
import ast.Ast.Method;
import ast.Ast.Method.MethodSingle;
import ast.Ast.Program.ProgramSingle;
import ast.Ast.Stm;
import ast.Ast.Stm.Assign;
import ast.Ast.Stm.AssignArray;
import ast.Ast.Stm.Block;
import ast.Ast.Stm.If;
import ast.Ast.Stm.Print;
import ast.Ast.Stm.While;
import ast.Ast.Type;
import ast.Ast.Type.ClassType;
import control.Control.ConAst;

public class ElaboratorVisitor implements ast.Visitor {
    public ClassTable classTable; // symbol table for class
    public MethodTable methodTable; // symbol table for each method
    public String currentClass; // the class name being elaborated
    public Type.T type; // type of the expression being elaborated
    private Boolean hasError; // whether or not has error
    private int totalError; // total num of errors

    public ElaboratorVisitor() {
        this.classTable = new ClassTable();
        this.methodTable = new MethodTable();
        this.currentClass = null;
        this.type = null;
        this.hasError = false;
        this.totalError = 0;
    }

    private void error(String msg) {
        this.hasError = true;
        this.totalError += 1;
        System.out.print("Error: ");
        System.out.println(msg);
        System.out.println("current class: " + this.currentClass + "\n");
        //System.exit(1);
    }

    // /////////////////////////////////////////////////////
    // expressions
    @Override
    public void visit(Add e) {
        e.left.accept(this);
        Type.T type = this.type;
        e.right.accept(this);
        if ((!type.toString().equals("@int")) || (!this.type.toString().equals("@int"))) {
            error("Add: only add by 2 int type data");
        }
        if (!this.type.toString().equals(type.toString())) {
            error("Add: left's type must be same with right's type");
        }
        this.type = new Type.Int();
        return;
    }

    @Override
    public void visit(And e) {
        e.left.accept(this);
        Type.T type = this.type;
        e.right.accept(this);
        if ((!type.toString().equals("@boolean")) || (!this.type.toString().equals("@boolean"))) {
            error("And: only and by 2 boolean type data");
        }
        if (!this.type.toString().equals(type.toString())) {
            error("And: left's type must be same with right's type");
        }
        this.type = new Type.Boolean();
        return;
    }

    @Override
    public void visit(ArraySelect e) {
        e.array.accept(this);
        e.index.accept(this);
        if (!(this.type instanceof Type.Int)) {
            error("ArraySelect: index must be int");
        }
        this.type = new Type.Int();
        return;
    }

    @Override
    public void visit(Call e) {
        Type.T leftty;
        Type.ClassType ty = null;

        e.exp.accept(this);
        leftty = this.type;
        if (leftty instanceof ClassType) {
            ty = (ClassType) leftty;
            e.type = ty.id;
        } else
            error("Call: left exp must be class type");
        MethodType mty = this.classTable.getm(ty.id, e.id);
        java.util.LinkedList<Type.T> argsty = new LinkedList<Type.T>();
        for (Exp.T a : e.args) {
            a.accept(this);
            argsty.addLast(this.type);
        }
        if (mty.argsType.size() != argsty.size())
            error("Call: num of func's parameters is wrong");
        for (int i = 0; i < argsty.size(); i++) {
            Dec.DecSingle dec = (Dec.DecSingle) mty.argsType.get(i);
            if (dec.type.toString().equals(argsty.get(i).toString()))
                ;
            else
                error("Call: type of func's parameters is wrong");
        }
        this.type = mty.retType;
        e.at = argsty;
        e.rt = this.type;
        return;
    }

    @Override
    public void visit(False e) {
        this.type = new Type.Boolean();
        return;
    }

    @Override
    public void visit(Id e) {
        // first look up the id in method table
        Type.T type = this.methodTable.get(e.id);
        // if search failed, then s.id must be a class field.
        if (type == null) {
            type = this.classTable.get(this.currentClass, e.id);
            // mark this id as a field id, this fact will be
            // useful in later phase.
            e.isField = true;
        }
        if (type == null)
            error("Id not found, must be declared first");
        this.type = type;
        // record this type on this node for future use.
        e.type = type;
        return;
    }

    @Override
    public void visit(Length e) {
        e.array.accept(this);
        if (!(this.type instanceof Type.IntArray)) {
            error("Length: must be array int type");
        }
        this.type = new Type.Int();
        return;
    }

    @Override
    public void visit(Lt e) {
        e.left.accept(this);
        Type.T ty = this.type;
        e.right.accept(this);
        if (!this.type.toString().equals(ty.toString()))
            error("Lt: left's type must be same with right's type");
        this.type = new Type.Boolean();
        return;
    }

    @Override
    public void visit(NewIntArray e) {
        e.exp.accept(this);
        if (!(this.type instanceof Type.Int)) {
            error("new int[e], e must be int");
        }
        this.type = new Type.IntArray();
        return;
    }

    @Override
    public void visit(NewObject e) {
        this.type = new Type.ClassType(e.id);
        return;
    }

    @Override
    public void visit(Not e) {
        e.exp.accept(this);
        if (!(this.type instanceof Type.Boolean)) {
            error("Not exp must be Boolean");
        }
        this.type = new Type.Boolean();
        return;
    }

    @Override
    public void visit(Num e) {
        this.type = new Type.Int();
        return;
    }

    @Override
    public void visit(Sub e) {
        e.left.accept(this);
        Type.T leftty = this.type;
        e.right.accept(this);
        if ((!leftty.toString().equals("@int")) || (!this.type.toString().equals("@int"))) {
            error("SubExp: only sub by 2 int type data");
        }
        if (!this.type.toString().equals(leftty.toString()))
            error("SubExp: left's type must be same with right's type");
        this.type = new Type.Int();
        return;
    }

    @Override
    public void visit(This e) {
        this.type = new Type.ClassType(this.currentClass);
        return;
    }

    @Override
    public void visit(Times e) {
        e.left.accept(this);
        Type.T leftty = this.type;
        e.right.accept(this);
        if ((!leftty.toString().equals("@int")) || (!this.type.toString().equals("@int"))) {
            error("Times: only times by 2 int type data");
        }
        if (!this.type.toString().equals(leftty.toString()))
            error("Times: left's type must be same with right's type");
        this.type = new Type.Int();
        return;
    }

    @Override
    public void visit(True e) {
        this.type = new Type.Boolean();
        return;
    }

    // statements
    @Override
    public void visit(Assign s) {
        // first look up the id in method table
        Type.T type = this.methodTable.get(s.id);
        // if search failed, then s.id must
        if (type == null)
            type = this.classTable.get(this.currentClass, s.id);
        if (type == null)
            error("Assign: cannot found left id");
        s.exp.accept(this);
        s.type = type;
        if(!this.type.toString().equals(type.toString())) {
            error("Assign: type of left and right is different");
        }
        return;
    }

    @Override
    public void visit(AssignArray s) {
        Type.T type = this.methodTable.get(s.id);
        if (type == null) {
            type = this.classTable.get(this.currentClass, s.id);
        }
        if (type == null) {
            error("AssignArray: left id cannot be found");
        }
        s.index.accept(this);
        if (!(this.type instanceof Type.Int)) {
            error("AssignArray: index must be int");
        }
        s.exp.accept(this);
        if (!(this.type instanceof Type.Int)) {
            error("AssignArray: right's type must be int");
        }
        return;
    }

    @Override
    public void visit(Block s) {
        for (Stm.T stm : s.stms) {
            stm.accept(this);
        }
        return;
    }

    @Override
    public void visit(If s) {
        s.condition.accept(this);
        if (!this.type.toString().equals("@boolean"))
            error("If: condition must be boolean");
        s.thenn.accept(this);
        s.elsee.accept(this);
        return;
    }

    @Override
    public void visit(Print s) {
        s.exp.accept(this);
        if (!this.type.toString().equals("@int"))
            error("Print: only can print int data");
        return;
    }

    @Override
    public void visit(While s) {
        s.condition.accept(this);
        if (!this.type.toString().equals("@boolean")) {
            error("While: condition must be boolean");
        }
        s.body.accept(this);
        return;
    }

    // type
    @Override
    public void visit(Type.Boolean t) {
        return;
    }

    @Override
    public void visit(Type.ClassType t) {
        ClassBinding cb = this.classTable.get(t.id);
        if (cb == null) {
            error("Dec: " + t.id + " type isn't exist");
        }
        return;
    }

    @Override
    public void visit(Type.Int t) {
        return;
    }

    @Override
    public void visit(Type.IntArray t) {
        return;
    }

    // dec
    @Override
    public void visit(Dec.DecSingle d) {
        d.type.accept(this);
        return;
    }

    // method
    @Override
    public void visit(Method.MethodSingle m) {
        // construct the method table
        this.methodTable.put(m.formals, m.locals);

        m.retType.accept(this);

        for (Dec.T dec : m.formals) {
            dec.accept(this);
        }

        for (Dec.T dec : m.locals) {
            dec.accept(this);
        }

        if (ConAst.elabMethodTable)
            this.methodTable.dump();

        for (Stm.T s : m.stms)
            s.accept(this);
        m.retExp.accept(this);
        if (!this.type.toString().equals(m.retType.toString())) {
            error("Function " + m.id + ": return type is wrong");
        }
        return;
    }

    // class
    @Override
    public void visit(Class.ClassSingle c) {
        this.currentClass = c.id;

        for (Dec.T dec : c.decs) {
            dec.accept(this);
        }

        for (Method.T m : c.methods) {
            m.accept(this);
        }
        return;
    }

    // main class
    @Override
    public void visit(MainClass.MainClassSingle c) {
        this.currentClass = c.id;
        // "main" has an argument "arg" of type "String[]", but
        // one has no chance to use it. So it's safe to skip it...

        c.stm.accept(this);
        return;
    }

    // ////////////////////////////////////////////////////////
    // step 1: build class table
    // class table for Main class
    private void buildMainClass(MainClass.MainClassSingle main) {
        this.classTable.put(main.id, new ClassBinding(null));
    }

    // class table for normal classes
    private void buildClass(ClassSingle c) {
        this.classTable.put(c.id, new ClassBinding(c.extendss));
        for (Dec.T dec : c.decs) {
            Dec.DecSingle d = (Dec.DecSingle) dec;
            this.classTable.put(c.id, d.id, d.type);
        }
        for (Method.T method : c.methods) {
            MethodSingle m = (MethodSingle) method;
            this.classTable.put(c.id, m.id, new MethodType(m.retType, m.formals));
        }
    }

    // step 1: end
    // ///////////////////////////////////////////////////

    // program
    @Override
    public void visit(ProgramSingle p) {
        // ////////////////////////////////////////////////
        // step 1: build a symbol table for class (the class table)
        // a class table is a mapping from class names to class bindings
        // classTable: className -> ClassBinding{extends, fields, methods}
        buildMainClass((MainClass.MainClassSingle) p.mainClass);
        for (Class.T c : p.classes) {
            buildClass((ClassSingle) c);
        }

        // we can double check that the class table is OK!
        if (control.Control.ConAst.elabClassTable) {
            this.classTable.dump();
        }

        // ////////////////////////////////////////////////
        // step 2: elaborate each class in turn, under the class table
        // built above.
        p.mainClass.accept(this);
        for (Class.T c : p.classes) {
            c.accept(this);
        }
        if (this.hasError) {
            System.out.println("There are " + this.totalError + " errors.");
            System.exit(1);
        } else {
            System.out.println("Type checking successfully!");
        }
    }
}
