/**
* StrucrualExample1
* implements equals but does not implement hashCode
**/
public class StructuralExample1 {

    public static int testField;

    public static void main(String[] args) {
        //FileComponent f1 = new FileComponent();
        //f1.close();
        //f1.read();

        invokeExplicitGC();
        allocateNewString();
        implicitlyAllocateNewString();
        invokesNewString();
        redundantToString();

        invokeNotify();

        invokesNewBoolean();

        doubleEqualsStringComparison();

        conditionOverConstant();

        nullDeref();

        callEqualsDifferentTypes();

        System.exit(0);
    }

    public boolean equals(Object obj) {
        return false;
    }

    public static void allocateNewString() {
        String s = new String("foo");
        String q = new String("Q");

        System.out.println(s + q);
    }

    public static void implicitlyAllocateNewString() {
        String s = "foo";
        String q = "Q";

        System.out.println(s + q);
    }

    public static void redundantToString() {

        String foo = "foo";
        String aFoo = foo.toString();

        System.out.println(aFoo);
    }

    public static void invokesNewString() {

        String foo = new String();
        System.out.println(foo);
    }

    public static void invokesNewBoolean() {

        Boolean boo = new Boolean(true);
        Boolean koo = Boolean.TRUE;
        System.out.println(boo);
    }


    public static void invokeNotify() {
        String foo = new String();
        foo.notify();
    }



    public static void invokeExplicitGC() {
        System.gc();
    }


    public static void doubleEqualsStringComparison() {
        String foo = "foo";
        String bar = "bar";
        if (foo == bar) {
            System.out.println("Foo equals Bar");
        }
    }

    public static void conditionOverConstant() {
        boolean foo = false;
        if (foo) {
            System.out.println("Foo was true");
        }

        if (!foo) {
            System.out.println("Not foo was true");
        }
    }

    public static void nullDeref() {
        Object x = null;
        Object y = new Object();
        Object z;

        if (true) {
            z = x;
        } else {
            z = null;
        }
        System.out.println(z.getClass());
    }

    public static void callEqualsDifferentTypes() {
        Object x = new Object();
        String foo = "foo";

        System.out.println("Are these equal?" + foo.equals(x));
    }


}
