package j2se.structural.accessibility.second_set;

/*
 * Public class that should be made default
 */
public class AccessibilityExample5 extends AccessibilityExample1 {
  protected void m5DefaultUnreached() {
    m1ProtectedToDefault();
    m1PublicToDefault();
    m1DefaultToDefault();
  }
}
