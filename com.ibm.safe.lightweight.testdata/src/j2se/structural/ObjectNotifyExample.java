package j2se.structural;

import java.util.Stack;

public final class ObjectNotifyExample {

  public static void main(String[] args) {
    final Stack collection = new Stack();
    // ...
    collection.pop().notify(); // Hit !
    // ...
  }

}
