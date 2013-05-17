/*********************************************************************
 * Name: NullDeref1.java
 * Description: An extremely simple example for null-dereference.
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.structural;

public class NullDeref1 {

  public static void main(String[] args) {
    Boolean x = Boolean.valueOf(true);
    Boolean y = null;

    boolean bx = x.booleanValue();
    boolean by = y.booleanValue();

    System.out.println("BX: " + bx); //$NON-NLS-1$
    System.out.println("BY: " + by); //$NON-NLS-1$
  }
}
