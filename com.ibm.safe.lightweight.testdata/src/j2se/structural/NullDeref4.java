package j2se.structural;

public class NullDeref4 {

  // Simple examples; variable correlation is not required

  void foo1(Integer p) {
    System.out.println(p.toString());
  }

  void foo2(Integer p) {
    if (p != null) { // evidence that p might be null
      System.out.print("here"); // filler to prevent dead-code elimination
    }
    System.out.println(p.toString()); // (#1) expected null deref warning
  }

  void foo3(Integer p) {
    if (p == null) { // same as foo2, only sense of conditional reversed
      System.out.print("here");
    }
    System.out.println(p.toString()); // (#2) expected null deref warning
  }

  void foo4(Integer p) {
    if (p == null) {
      p = new Integer(3); // make p non-null in the null case
    }
    System.out.println(p.toString());
  }

  void foo5(Integer p) {
    if (p != null) {
      System.out.println(p.toString());
    }
  }

  void foo6(Integer p, boolean nondet) {
    if (nondet) {
      if (p != null) { // evidence is on some paths, not all
        System.out.print("here");
      }
    }
    System.out.println(p.toString()); // (#3) expected null deref warning
  }

  void foo7(Integer p) {
    if (p == null) {
      System.out.println(p.toString()); // (#4) expected null deref warning
    }
  }

  void foo8(Integer p) {
    if (p == null) {
      System.out.print("here");
    }
    System.out.println(p.toString()); // (#5) expected null deref warning
    System.out.println(p.toString()); // (#6) expected null deref warning
    // should not warn here, because dominated by previous warning. currently
    // does warn. need to fix.
  }

  void foo9(Integer p) {
    Integer q = null;
    try {
      System.out.println(p.toString()); // a possible exception-throwing stmt
    } catch (Exception e) {
      q = new Integer(3);
    } // catch block may or may not execute
    System.out.println(q.toString()); // TODO: is not reported under optimistic
                                      // eval
  }

  void foo10(Integer p, boolean nondet1, boolean nondet2) {
    Integer q = null;
    if (nondet1) {
      q = new Integer(3);
    } else if (nondet2) {
      q = new Integer(4);
    } // q not assigned if both nondet1 and nondet2 are false
    System.out.println(q.toString()); // (#8) expected
  }

  void foo11() {
    Integer p = null;
    Integer q = null;
    if (p == null) { // this is constant branch
      q = new Integer(3);
    }
    System.out.println(q.toString()); // reported with pessimistic eval
  }

  void foo12(Integer p) {
    Integer q = null;
    if (p != null) { // a nondet branch
      q = new Integer(3);
    }
    System.out.println(q.toString()); // (#7) expected null deref warning
  }

  // Examples that involve correlated variables
}
