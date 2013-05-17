package j2se.structural.accessibility.first_set;

import j2se.structural.accessibility.second_set.AccessibilityExample1;

public class AccessibilityExample3 extends AccessibilityExample1 {

  public void m3PublicUnreachable() {
    f1ProtectedToProtected = 3;
    f1PublicToProtected = 2;
    m1PublicToProtected();
    m1ProtectedToProtected();
  }
}
