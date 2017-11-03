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
        a = false;
        if (!a && p) {
            System.out.println(222);
        } else {
            a = true;
        }
        while (i<n){
        	sum = sum + i;
        	i = i + 1;
        }
        return sum;
    }

    public int test(int bbb) {
        int rr;
        System.out.println(0);
        return 0;
    }

    public boolean isFalse() {
        //int rr;
        //rr[1] = 0;
        return false;
    }
}

class Test extends Doit {
    Doit b;
    int ii;
    public int tt(boolean aa, int uu) {
        int qqq;
        qqq = 2;
        System.out.println(333);
        //qqq = b.test();
        return 1;
    }
    public int test() {
        ii = 1;
        return ii;
    }
}