package j2se.structural;

public class NullDeref3 {

  public static int foo(Integer p) {
    int x = 0;
    if (p != null) {
      if (p != null) { // should give a constant expression warning (but
        // doesn't currently)
        x = p.intValue(); // should not give a null deref warning
      } else {
      }
    } else {
      if (p == null) { // should give a constant expression warning
        x = p.intValue(); // (#1) expected null deref warning
      }
    }
    return x;
  }

  public static int bar(Integer q) {
    int x = 0;
    if (q == null) {
      if (q == null) { // should give a constant expression warning.
        x = q.intValue(); // (#2) expected null deref warning
      } else {
      }
    } else {
      if (q != null) { // should give a constant expression warning. (but
        // doesn't currently)
        q = null;
        x = q.intValue(); // (#3) expected null deref warning
      }
    }
    return x;
  }

  public static void baz(Integer q) {
    if (q != null) {
      System.err.println(q.toString());
    }
    System.err.println(q.toString()); // should give a warning under
    // "evidence-based" analysis
    // (#4) expected null deref warning
  }

  public static void zap(int i) {
    Integer q = null;
    try {
      if (i > 0) {
        System.out.println("something");
      }
      q = new Integer(0);
      System.out.println("something");
    } catch (Throwable t) {
      if (q == null) { // this is NOT a suspicious constant branch.
        System.err.println("it's null");
        System.err.println(q.doubleValue()); // (#5) expected null deref
        // warning
      }
    } finally {
      if (q == null) { // this is NOT a suspicious constant branch.
        System.err.println("it's null");
      }
      System.err.println(q.doubleValue()); // (#6) expected null deref warning
    }
  }

}
