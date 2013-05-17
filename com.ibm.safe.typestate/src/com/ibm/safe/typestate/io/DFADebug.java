/*******************************************************************************
 * Copyright (c) 2004-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.typestate.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

/**
 * utility for creating dot and ps files form DFA (dotting)
 * 
 * @author yahave
 */
public class DFADebug {

  private static final String DOT_EXE_STRING = "C:/Progra~1/ATT/Graphviz/bin/dot.exe";

  /**
   * determines restrictions on generated image size. null = no restrictions
   * size should be specified as width, height for examples DOT_GRAPH_SIZE =
   * "10,10"
   */
  private static final String DOT_GRAPH_SIZE = null; // "7,7";

  private static final String DOT_IMAGE_FORMAT = "gif";

  private static String outputDir;

  public static String flattenFileName(String fileName) {
    return fileName.replace('/', '_');
  }

  /**
   * I'll be damned if I start playing around with billions lines of EMF garbage
   * wrapping the useful 20 lines of code here.
   */
  public static void dotifyImages(File dotFile, String imageFile) {
    String cmd = getDotExe() + " -T" + DOT_IMAGE_FORMAT + " -o " + imageFile + " -v " + dotFile.getAbsolutePath();
    if (DOT_GRAPH_SIZE != null) {
      cmd = cmd + " -Gsize=" + DOT_GRAPH_SIZE;
    }

    System.out.println("spawning process " + cmd);
    try {
      Process p = Runtime.getRuntime().exec(cmd);
      BufferedInputStream output = new BufferedInputStream(p.getInputStream());
      BufferedInputStream error = new BufferedInputStream(p.getErrorStream());
      boolean repeat = true;
      while (repeat) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
          // just ignore and continue
        }
        if (output.available() > 0) {
          byte[] data = new byte[output.available()];
          int nRead = output.read(data);
          System.err.println("read " + nRead + " bytes from output stream");
        }
        if (error.available() > 0) {
          byte[] data = new byte[error.available()];
          int nRead = error.read(data);
          System.err.println("read " + nRead + " bytes from error stream");
        }
        try {
          p.exitValue();
          // if we get here, the process has terminated
          repeat = false;
          System.out.println("process terminated with exit code " + p.exitValue());
        } catch (IllegalThreadStateException e) {
          // this means the process has not yet terminated.
          repeat = true;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Error running dot " + e);
    }
  }

  private static String getDotExe() {
    return DOT_EXE_STRING;
  }

  public static void setOutputDirectory(String directory) {
    if (directory.charAt(directory.length() - 1) != '/') {
      directory = directory + "/";
    }
    outputDir = directory;
  }

  public static String getOutputDir() {
    return outputDir;
  }

}
