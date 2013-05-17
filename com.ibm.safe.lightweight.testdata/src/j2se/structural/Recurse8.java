package j2se.structural;

import java.util.Random;

/**
 * @author sfink
 * 
 */
public class Recurse8 {

  public static void foo() {

    if ((new Random().nextBoolean())) {
      foo();
    } else {
      foo();
    }

  }
}