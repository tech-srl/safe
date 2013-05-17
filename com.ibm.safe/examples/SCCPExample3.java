/*********************************************************************
* Name: SCCPExample3.java
* Author: Eran Yahav (eyahav)
*********************************************************************/


public class SCCPExample3 {

    public static void main(String[] args) {

        String x = "Hello";
        String y = " world";
        String z;

        z = x + y;

        boolean q = false;

        if (q) {
            System.out.println("Condition held");
        }

        if (x == null) {
            x.getClass();
        }

        if (x != null) {
            x.getClass();
        }

        Object w = null;

        if (w != null) {
            w.getClass();
        }


        System.out.println(z);
    }
}
