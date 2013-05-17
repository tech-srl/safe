package j2se.structural;

public class EqualsHashCodeExample3 implements Comparable {

  public boolean equals(Object obj) {
    return false;
  }

  public int hashCode() {
    return 1;
  }

  public int compareTo(Object rhs) {
    return 0;
  }

}

class EqualsHashCodeExample3Child extends EqualsHashCodeExample3 { // Hit ! (
                                                                    // should
                                                                    // implement
                                                                    // compareTo
                                                                    // )

  public boolean equals(Object obj) {
    return false;
  }

  public int hashCode() {
    return 1;
  }

}