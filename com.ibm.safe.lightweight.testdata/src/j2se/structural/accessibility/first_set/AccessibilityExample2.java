package j2se.structural.accessibility.first_set;

import j2se.structural.accessibility.second_set.AccessibilityExample1;

/*
 * Public class that could be made default
 */
public class AccessibilityExample2 {
  void m2DefaultUnreachable() {
    AccessibilityExample1 test = new AccessibilityExample1();
    test.f1PublicToPublic = 5;
    test.m1PublicToPublic();
  }
}
