class Sum { 
	public static void main(String[] a) {
        System.out.println(new Doit().doit(101));
    }
}

// hello
/* hello//* */

class Doit { // xx

    int ii;
    Doit bb;

    public int doit(int n) {
        int sum;
        int i;
        boolean a;
        boolean p;
        int[] b;
        
        i = 0;
        sum = 0;
        a = false;
        p = true;
        b = new int[2];
        b[1] = 3;
        i = b[1];
        sum = b.length;
        if (!a && p) {
            System.out.println(222);
        } else {
            a = true;
        }
        while (i<n){
        	sum = sum + i;
        	i = i+1;
        }
        return sum;
    }

    public int test(int bbb) {
        int rr;
        System.out.println(0);
        return 0;
    }
}

class Test extends Doit {
    int b;

    public int tt(boolean aa, int uu) {
        System.out.println(333);
        return 0;
    }
}