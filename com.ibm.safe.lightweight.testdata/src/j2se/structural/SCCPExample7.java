package j2se.structural;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public final class SCCPExample7 {

  public static void main(String[] args) {

    boolean checksumMatches = true;
    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {

      fis = new FileInputStream("someFile");
      fis.close();
      fis = null;

      fos = new FileOutputStream("YetAnotherFile");
      fos.write(42);
      fos.close();
      fos = null;

    } catch (Exception e) {
      throw new RuntimeException("bad program, no soup for you!");
    } finally {
      if (fis != null) {
        try {
          fis.close();
        } catch (IOException e) {
        }
      }
      if (fos != null) {
        try {
          fos.close();
        } catch (IOException e) {
        }
      }
    }
  }

}
