package j2se.structural;

public final class SCCPExample5 {

  final static int N = 100;

  public static void main(String[] args) {
    boolean foo = false;
    if (foo) { // Hit !
      System.out.println("..."); //$NON-NLS-1$
    }
    if (!foo) { // Hit !
      System.out.println("..."); //$NON-NLS-1$
    }

    for (int i = 0; i < N; i++) { // miss
      System.out.println(">>>");
    }

    int x = 10;
    while (x > 0) { // miss
      x--;
    }

    int y = 10;
    while (y > 0) { // report constant (hit!)
      x++;
    }

  }

}
