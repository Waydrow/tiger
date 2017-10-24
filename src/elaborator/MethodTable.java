package elaborator;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

import ast.Ast.Dec;
import ast.Ast.Type;
import util.Todo;

public class MethodTable {

    private Hashtable<String, MethodBinding> table;

    public MethodTable() {
        this.table = new java.util.Hashtable<String, MethodBinding>();
    }

    public void put(String id, MethodBinding mb) {
        this.table.put(id, mb);
    }

    public void put(String id, LinkedList<Dec.T> formals, LinkedList<Dec.T> locals) {
        MethodBinding mb = this.table.get(id);
        mb.put(formals, locals);
    }

    public Type.T get(String mid, String id) {
        return this.table.get(mid).get(id);
    }

    /*private java.util.Hashtable<String, Type.T> table;

    public MethodTable() {
        this.table = new java.util.Hashtable<String, Type.T>();
    }

    // Duplication is not allowed
    public void put(LinkedList<Dec.T> formals,
                    LinkedList<Dec.T> locals) {
        for (Dec.T dec : formals) {
            Dec.DecSingle decc = (Dec.DecSingle) dec;
            if (this.table.get(decc.id) != null) {
                System.out.println("duplicated parameter: " + decc.id);
                System.exit(1);
            }
            this.table.put(decc.id, decc.type);
        }

        for (Dec.T dec : locals) {
            Dec.DecSingle decc = (Dec.DecSingle) dec;
            if (this.table.get(decc.id) != null) {
                System.out.println("duplicated variable: " + decc.id);
                System.exit(1);
            }
            this.table.put(decc.id, decc.type);
        }

    }

    // return null for non-existing keys
    public Type.T get(String id) {
        return this.table.get(id);
    }*/

    public void dump() {
        System.out.println("MethodTable");
        for(Map.Entry<String, MethodBinding> entry : this.table.entrySet()) {
            System.out.println(entry.getKey());
            entry.getValue().toString();
            System.out.println("\n");
        }
    }

    @Override
    public String toString() {
        return this.table.toString();
    }
}
