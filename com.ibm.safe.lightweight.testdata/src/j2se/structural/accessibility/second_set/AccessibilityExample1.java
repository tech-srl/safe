package j2se.structural.accessibility.second_set;

/*
 * Public class that should stay public
 */
public class AccessibilityExample1 {
  /*
   * Unreferenced fields.
   */
  public int f1PublicUnreferenced; // tested

  protected int f1ProtectedUnreferenced; // tested

  int f1DefaultUnreferenced; // tested

  private int f1PrivateUnreferenced; // tested

  public int f1PublicToPublic; // tested

  public int f1PublicToProtected; // tested

  public int f1PublicToDefault; // tested

  public int f1PublicToPrivate; // tested

  protected int f1ProtectedToProtected; // tested

  protected int f1ProtectedToDefault; // tested

  protected int f1ProtectedToPrivate; // tested

  int f1DefaultToDefault; // tested

  int f1DefaultToPrivate; // tested

  int f1PrivateToPrivate; // tested

  public static int f1PublicStaticNonfinal; // tested

  protected static int f1ProtectedStaticNonfinal; // tested

  static int f1DefaultStaticNonfinal; // tested

  public void m1PublicUnreached() {
  } // tested

  protected void m1ProtectedUnreached() {
  } // tested

  void m1DefaultUnreached() {
  } // tested

  private void m1PrivateUnreached() {
  } // tested

  public void m1PublicToPublic() { // tested
    f1PublicToPrivate = 0;
    f1ProtectedToPrivate = 1;
    f1DefaultToPrivate = 0;
    f1PrivateToPrivate = 0;
    m1PublicToPrivate();
    m1ProtectedToPrivate();
    m1DefaultToPrivate();
    m1PrivateToPrivate();
  }

  public void m1PublicToProtected() {
  } // tested

  public void m1PublicToDefault() {
  } // tested

  public void m1PublicToPrivate() {
  } // tested

  protected void m1ProtectedToProtected() {
  } // tested

  protected void m1ProtectedToDefault() {
  } // tested

  protected void m1ProtectedToPrivate() {
  } // tested

  void m1DefaultToDefault() {
  } // tested

  void m1DefaultToPrivate() {
  } // tested

  private void m1PrivateToPrivate() {
  } // tested
}