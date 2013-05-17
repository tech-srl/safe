package j2se.structural.accessibility.second_set;

/*
 * Default class that should stay default
 */
class AccessibilityExample4 {
  /*
   * Unreached method
   */
  public void m4PublicUnreached() {
    AccessibilityExample1 test1 = new AccessibilityExample1();
    test1.f1DefaultToDefault = 5;
    test1.f1ProtectedToDefault = 6;
    test1.f1PublicToDefault = 6;
  }
}
