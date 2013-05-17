package j2se.structural;

public final class NullDeref2 {

  public void foo() {
    Object x = new Object();
    Object z;

    int y = 1;

    if (y > 3) {
      z = x;
    } else {
      z = null;
    }
    System.out.println(z.getClass()); // potential null deref
  }

}
