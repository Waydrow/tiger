package elaborator;

import ast.Ast;
import ast.Ast.Dec;
import ast.Ast.Type;

import java.util.Hashtable;
import java.util.LinkedList;

public class MethodBinding {

    private java.util.Hashtable<String, Type.T> fields;

    public MethodBinding() {
        this.fields = new Hashtable<>();
    }

    // Duplication is not allowed
    public void put(LinkedList<Dec.T> formals,
                    LinkedList<Dec.T> locals) {
        for (Dec.T dec : formals) {
            Dec.DecSingle decc = (Dec.DecSingle) dec;
            if (this.fields.get(decc.id) != null) {
                System.out.println("duplicated parameter: " + decc.id);
                System.exit(1);
            }
            this.fields.put(decc.id, decc.type);
        }

        for (Dec.T dec : locals) {
            Dec.DecSingle decc = (Dec.DecSingle) dec;
            if (this.fields.get(decc.id) != null) {
                System.out.println("duplicated variable: " + decc.id);
                System.exit(1);
            }
            this.fields.put(decc.id, decc.type);
        }

    }

    public Type.T get(String id) {
        return this.fields.get(id);
    }

    @Override
    public String toString() {
        return this.fields.toString();
    }
}
