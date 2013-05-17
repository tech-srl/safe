package j2se.structural;

import java.util.Random;

/**
 * @author sfink
 * 
 */
public class Recurse7 {

  public static void foo() {
    try {
      if ((new Random().nextBoolean())) {
        foo();
      }
    } finally {
      foo();
    }
  }
}