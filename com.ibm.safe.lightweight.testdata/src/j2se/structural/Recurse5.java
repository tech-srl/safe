package j2se.structural;

/**
 * @author sfink
 * 
 */
public class Recurse5 {

  private class A {
    void foo(A a) {
      a.foo(a);
    }
  }

  private class B extends A {
    void foo(A a) {
      foo(a);
    }
  }
}
