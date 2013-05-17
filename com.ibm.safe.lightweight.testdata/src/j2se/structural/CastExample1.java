/*********************************************************************
 * Name: SCCPExample1.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.structural;

public class CastExample1 {

  public static void main(String[] args) {
    int x = 5;
    int y = 50;

    double d;

    double d2;

    d = x / y; // integer truncation, will get 0.0

    d2 = (double) x / (double) y; // get the desired 0.1

    System.out.println("d=" + d);
    System.out.println("d2=" + d2);

  }
}
