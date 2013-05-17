package j2se.structural;

import java.util.Random;

public class NullDeref10 {

  void foo1() {
    boolean flag = (new Random()).nextBoolean();
    Object x = null;
    if (x == null) {
      x = new Object();
    }
    x.toString();

    if (flag) {
      x.toString();
    }

  }
}
