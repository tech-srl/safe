package j2se.structural;

public class NullDeref13 {
  void foo2(Integer p) {
    if (p != null) { // evidence that p might be null
      System.out.print("here"); // filler to prevent dead-code elimination
    }
    System.out.println(p.toString()); // (#1) expected null deref warning
  }
}
