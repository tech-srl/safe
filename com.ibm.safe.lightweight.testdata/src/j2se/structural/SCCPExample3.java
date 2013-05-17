/*********************************************************************
 * Name: SCCPExample3.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.structural;

public class SCCPExample3 {

  public static void main(String[] args) {

    String x = "Hello";
    String y = " world";
    String z;

    z = x + y;

    boolean q = false;

    if (q) { // suspicious condition over a constant value
      System.out.println("Condition held");
    }

    if (x == null) { // suspicious condition over a constant value
      x.getClass();
    }

    if (x != null) { // suspicious condition over a constant value
      x.getClass();
    }

    Object w = null;

    if (w != null) { // suspicious condition over a constant value
      w.getClass();
    }

    System.out.println(z);
  }
}
