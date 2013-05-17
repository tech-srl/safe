/*********************************************************************
 * Name: SCCPExample3.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.structural;

/**
 * Test that the signature for the match which appears in a constructor should
 * not display <init> for the method name.
 */
public class SCCPExample4 {

  public SCCPExample4(final int value, final boolean flag, final String str) {
    Object x = null;

    // ...

    if (x == null) { // suspicious condition over constant value
      c = x.getClass();
    }
  }

  public static void main(final String[] args) {
    final SCCPExample4 instance = new SCCPExample4(4, args.length > 0, "My String");
    // ...
  }

  private Object x;

  private Class c;

}