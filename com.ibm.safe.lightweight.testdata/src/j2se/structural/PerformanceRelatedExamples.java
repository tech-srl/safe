package j2se.structural;

public final class PerformanceRelatedExamples {

  public static void main(String[] args) {
    final String first = new String(); // Hit !
    final String second = new String(args[0]); // Hit !

    if (first.equals(second)) {
      final Boolean trueValue = new Boolean(true); // Hit !
      System.out.println(trueValue);
    }
  }

}
