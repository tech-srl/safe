package j2se.structural;

public class EqualsHashCodeExample2 implements Comparable { // Hit ! ( should
                                                            // implement equals
                                                            // )

  public int hashCode() {
    return 1;
  }

  public int compareTo(Object foo) {
    return 1;
  }

  public boolean compareTo(String foo, String bar) {
    return foo.equals(bar);
  }

}
