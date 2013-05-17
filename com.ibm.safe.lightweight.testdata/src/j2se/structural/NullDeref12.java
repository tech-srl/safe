package j2se.structural;

public class NullDeref12 {
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
}
