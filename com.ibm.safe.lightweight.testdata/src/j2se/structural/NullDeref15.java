package j2se.structural;

public class NullDeref15 {

  public int eval(Integer aComponent) {
    int result = 0;

    if (!(aComponent.toString().length() == 0)) {
      result = 2;

      if (aComponent > 15) {
        result--;
      }
    }

    return result;
  }
}
