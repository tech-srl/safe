package j2se.structural;

public final class SystemGcExample {

  public static void main(String[] args) {
    if (args[0].length() > 0) {
      // ...
      System.gc(); // Hit !
    }
    // ...
  }

}
