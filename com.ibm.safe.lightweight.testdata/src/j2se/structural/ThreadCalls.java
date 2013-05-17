package j2se.structural;

public final class ThreadCalls {

  public static void main(String[] args) {
    Thread currentThread = Thread.currentThread();
    test1(currentThread);
    test2(currentThread);
  }

  private static void test1(final Thread currentThread) {
    currentThread.destroy(); // Hit !
  }

  private static void test2(final Thread currentThread) {
    currentThread.stop(); // Hit !
  }

}
