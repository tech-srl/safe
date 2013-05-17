package j2se.structural;

/**
 * Should not produce a hit.
 */
public final class CloneableExample2 implements Cloneable {

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

}
