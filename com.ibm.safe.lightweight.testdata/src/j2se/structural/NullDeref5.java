package j2se.structural;

import java.util.Random;

public class NullDeref5 {

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
      int i = x.hashCode();
    }
  }
}
