package j2se.structural;

import java.util.Random;

public class NullDeref8 {

  void foo1() {
    Object x = getSomething(); // x may be null
    if (x != null) {
      x.toString(); // this is safe
    }
    x.toString(); // this is not safe
  }

  private Object getSomething() {
    boolean val = (new Random()).nextBoolean();
    if (val) {
      return null;
    } else {
      return new Object();
    }

  }
}
