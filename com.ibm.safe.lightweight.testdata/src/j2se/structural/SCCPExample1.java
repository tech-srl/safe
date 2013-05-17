/*********************************************************************
 * Name: SCCPExample1.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.structural;

public class SCCPExample1 {

  public static void main(String[] args) {
    int x = 5;
    int y;
    int z;
    int w;
    int q;

    y = 3;

    if (x > y) {
      z = x + y;
    } else {
      z = x + y;
    }

    w = x * y;
    q = z;

    System.out.println("q=" + q);

  }
}
