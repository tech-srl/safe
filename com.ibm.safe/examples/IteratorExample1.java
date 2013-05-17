/*********************************************************************
* Name: IteratorExample1.java
* Description: A correct usage of iterator.
* Expected Result: this case should not report any alarms.
* Author: Eran Yahav (eyahav)
*********************************************************************/


import java.util.*;

public class IteratorExample1 {

    public static void main(String[] args) {
        List l1 = new ArrayList();
        List l2 = new ArrayList();

        l1.add("foo");
        l1.add("moo");
        l1.add("zoo");

        for (Iterator it1 = l1.iterator(); it1.hasNext();) {
            System.out.println(it1.next());
        }
    }
}
