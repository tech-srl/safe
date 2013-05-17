package j2se.structural;

public class NullDeref11 {
  class T {
    public void deref() {
    }

    public T getT() {
      return new T();
    }
  }

  /* should not report NPE */
  public void testDeref1(T x) {
    x.deref();
  }

  /* should report NPE */
  public void testDeref2(T x) {
    if (x != null) {
      System.out.println("to prevent from eliminating this dead code");
    }
    x.deref();
  }

  /* should not report NPE */
  public void testDeref3(T x) {
    T y = x.getT();
    y.deref();
  }

  /* should report NPE */
  public void testDeref4(T x) {
    T y = x.getT();
    if (y != null) {
      System.out.println("to prevent from eliminating this dead code");
    }
    y.deref();
  }
}
