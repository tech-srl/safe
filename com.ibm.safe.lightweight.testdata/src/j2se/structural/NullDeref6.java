package j2se.structural;

import java.util.Random;

public class NullDeref6 {

  void foo1() {
    boolean flag = (new Random()).nextBoolean();
    Object x = null;
    if (flag) {
      x = new Object();
    }
    if (flag) {
      x.toString();
    }
    if (flag) {
      x.hashCode();
    }

    boolean flag2 = (new Random()).nextBoolean();
    Object y = null;
    if (flag2) {
      y = new Object();
    }
    if (flag2) {
      y.toString();
    }
    if (flag2) {
      y.hashCode();
    }

    boolean flag3 = (new Random()).nextBoolean();
    Object z = null;
    if (flag3) {
      z = new Object();
    }
    if (flag3) {
      z.toString();
    }
    if (flag3) {
      z.hashCode();
    }

  }
}
