package j2se.structural;

public final class IgnoreReturnedValueExample {

  public static void main(final String[] args) {
    final String thing = " some literal string ";
    String s = thing.trim();
    thing.trim();
    System.out.println(s);
  }

}
