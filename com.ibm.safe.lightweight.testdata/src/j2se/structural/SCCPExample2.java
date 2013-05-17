/*********************************************************************
 * Name: SCCPExample2.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.structural;

public class SCCPExample2 {

  public static void main(String[] args) {

    Object x = new Object();
    Object y;
    Object z;

    y = null;

    if (y == null) { // suspicious condition over constant value
      System.out.println(y.getClass());
      z = y;
    }

    System.out.println("is x x ==y ?" + (x == y));

  }
}
