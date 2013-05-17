package j2se.structural;

public class NullDeref16 {

  public int eval(Integer aComponent) {
    assert aComponent != null;

    int result = aComponent.intValue();

    return result;
  }
}
