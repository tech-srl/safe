package j2se.structural;

public final class SuspiciousOverriddingExamples {

  public boolean equal(final Object rhs) { // Hit !
    // ...
    return false;
  }

  public boolean equals(final String rhs) { // Hit !
    // ...
    return false;
  }

  public int hashcode() { // Hit !
    return 1;
  }

  public String tostring() { // Hit !
    return getClass().getName();
  }

}
