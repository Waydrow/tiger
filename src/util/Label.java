package util;

public class Label {
    private static int count = 0;
    private int i;

    public Label() {
        i = count++;
    }

    @Override
    public String toString() {
        return "L_" + (Integer.toString(this.i));
    }
}
