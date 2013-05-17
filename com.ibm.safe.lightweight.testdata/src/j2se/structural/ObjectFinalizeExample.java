package j2se.structural;

public final class ObjectFinalizeExample {

  public static void main(String[] args) throws Throwable {
    final ObjectFinalizeExample instance = new ObjectFinalizeExample();
    instance.foo(args[0]);
    instance.finalize(); // Hit !
  }

  private void foo(final String value) {
    if (value.length() > 0) {
      try {
        finalize(); // Hit !
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

}
