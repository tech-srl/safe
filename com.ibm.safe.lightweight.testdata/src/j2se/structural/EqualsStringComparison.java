package j2se.structural;

public final class EqualsStringComparison {

  public static void main(String[] args) {
    final String first = args[0];
    final String second = "-value"; //$NON-NLS-1$
    String third = "a";
    third = third + "b";

    if (first == second) { // do not report this
      System.out.print(args[1]);
    }

    if (first == third) { // report this
      System.out.println("hello world");
    }

  }

}
