package j2se.structural;

import java.util.Random;

public final class SCCPExample6 {

  public static void main(String[] args) {
    boolean cond = (new Random()).nextBoolean();
    Object x = null;
    while (x == null && cond) { // miss
      x = new Object();
    }
  }

}
