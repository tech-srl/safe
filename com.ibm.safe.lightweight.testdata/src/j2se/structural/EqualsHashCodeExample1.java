package j2se.structural;

public class EqualsHashCodeExample1 implements Comparable { // Hit ! ( should
                                                            // implement
                                                            // hashCode )

  public boolean equals(Object obj) {
    return false;
  }

  public int compareTo(Object foo) {
    return 1;
  }

  public boolean compareTo(String foo, String bar) {
    return foo.equals(bar);
  }

}
