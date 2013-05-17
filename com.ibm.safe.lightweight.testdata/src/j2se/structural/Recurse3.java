package j2se.structural;

/**
 * @author sfink
 * 
 */
public class Recurse3 {

  private class A {
    void foo() {
    }
  }

  private class B extends A {
    void foo() {
      foo();
    }
  }
}
