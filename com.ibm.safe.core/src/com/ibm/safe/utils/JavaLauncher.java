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
package com.ibm.safe.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.wala.util.WalaException;

/**
 * A Java process launcher
 */
public class JavaLauncher {

  protected static final String PROGRAM_ARGS_EDEFAULT = "";

  protected String programArgs = PROGRAM_ARGS_EDEFAULT;

  protected static final String MAIN_CLASS_EDEFAULT = "";

  protected String mainClass = MAIN_CLASS_EDEFAULT;

  protected List<String> classpathEntries = new ArrayList<String>();

  protected int maxHeap = 800;

  public String getProgramArgs() {
    return programArgs;
  }

  public void setProgramArgs(String newProgramArgs) {
    programArgs = newProgramArgs;
  }

  public String getMainClass() {
    return mainClass;
  }

  public void setMainClass(String newMainClass) {
    mainClass = newMainClass;
  }

  public List<String> getClasspathEntries() {
    return classpathEntries;
  }

  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (programArgs: ");
    result.append(programArgs);
    result.append(", mainClass: ");
    result.append(mainClass);
    result.append(", classpathEntries: ");
    result.append(classpathEntries);
    result.append(')');
    return result.toString();
  }

  /**
   * @return the string that identifies the java executable file
   */
  protected String getJavaExe() {
    String java = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java";
    return java;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.capa.core.EAnalysisEngine#processImpl()
   */
  public void process() throws WalaException {

    String cp = makeClasspath();

    String maxHeapString = " -Xmx" + maxHeap + "M ";

    String cmd = getJavaExe() + maxHeapString + cp + getMainClass() + " " + getProgramArgs();

    // TODO: factor out the following!
    Process p = spawnProcess(cmd);
    Thread d1 = drainStdOut(p);
    Thread d2 = drainStdErr(p);
    try {
      d1.join();
      d2.join();
    } catch (InterruptedException e) {
      throw new WalaException("Internal error", e);
    }
  }

  private String makeClasspath() {
    if (getClasspathEntries().isEmpty()) {
      return "";
    } else {
      StringBuffer sb = new StringBuffer();
      sb.append(" -classpath ");
      for (Iterator<String> it = getClasspathEntries().iterator(); it.hasNext();) {
        sb.append(it.next());
        if (it.hasNext()) {
          sb.append(File.pathSeparator);
        }
      }
      sb.append(" ");
      return sb.toString();
    }
  }

  /**
   * @param cmd
   * @throws WalaException
   */
  protected Process spawnProcess(String cmd) throws WalaException {
    System.out.println("spawning process " + cmd);
    String[] env = getEnv() == null ? null : buildEnv(getEnv());
    try {
      Process p = Runtime.getRuntime().exec(cmd, env, getWorkingDir());
      return p;
    } catch (IOException e) {
      e.printStackTrace();
      throw new WalaException("IOException in " + getClass());
    }
  }

  private String[] buildEnv(Map<String, String> env) {
    String[] result = new String[env.size()];
    int i = 0;
    for (Iterator<Map.Entry<String, String>> it = env.entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, String> e = it.next();
      result[i++] = e.getKey() + "=" + e.getValue();
    }
    return result;
  }

  protected Thread drainStdOut(Process p) {
    final BufferedInputStream output = new BufferedInputStream(p.getInputStream());
    Thread result = new Drainer(p) {
      void drain() throws IOException {
        drainAndPrint(output, System.out);
      }
    };
    result.start();
    return result;
  }

  protected Drainer captureStdOut(Process p) {
    final BufferedInputStream output = new BufferedInputStream(p.getInputStream());
    final ByteArrayOutputStream b = new ByteArrayOutputStream();
    Drainer result = new Drainer(p) {
      void drain() throws IOException {
        drainAndCatch(output, b);
      }
    };
    result.setCapture(b);
    result.start();
    return result;
  }

  protected Thread drainStdErr(Process p) {
    final BufferedInputStream err = new BufferedInputStream(p.getErrorStream());
    Thread result = new Drainer(p) {
      void drain() throws IOException {
        drainAndPrint(err, System.err);
      }
    };
    result.start();
    return result;
  }

  /**
   * @author sfink
   * 
   * A thread that runs in a loop, performing the drain() action until a process
   * terminates
   */
  abstract class Drainer extends Thread {

    private final Process p;

    private ByteArrayOutputStream capture;

    abstract void drain() throws IOException;

    Drainer(Process p) {
      this.p = p;
    }

    public void run() {
      try {
        boolean repeat = true;
        while (repeat) {
          try {
            Thread.sleep(500);
          } catch (InterruptedException e1) {
            e1.printStackTrace();
            // just ignore and continue
          }
          drain();
          try {
            p.exitValue();
            // if we get here, the process has terminated
            repeat = false;
            drain();
            System.out.println("process terminated with exit code " + p.exitValue());
          } catch (IllegalThreadStateException e) {
            // this means the process has not yet terminated.
            repeat = true;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public ByteArrayOutputStream getCapture() {
      return capture;
    }

    public void setCapture(ByteArrayOutputStream capture) {
      this.capture = capture;
    }
  }

  private void drainAndPrint(BufferedInputStream s, PrintStream p) throws IOException {
    if (s.available() > 0) {
      byte[] data = new byte[s.available()];
      s.read(data);
      p.print(new String(data));
    }
  }

  private void drainAndCatch(BufferedInputStream s, ByteArrayOutputStream b) throws IOException {
    if (s.available() > 0) {
      byte[] data = new byte[s.available()];
      int nRead = s.read(data);
      b.write(data, 0, nRead);
    }
  }

  public Map<String, String> getEnv() {
    return env;
  }

  protected static final File WORKING_DIR_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getWorkingDir() <em>Working Dir</em>}'
   * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #getWorkingDir()
   * @generated
   * @ordered
   */
  protected File workingDir = WORKING_DIR_EDEFAULT;

  /**
   * The default value of the '{@link #getEnv() <em>Env</em>}' attribute. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #getEnv()
   * @generated
   * @ordered
   */
  protected static final Map<String, String> ENV_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getEnv() <em>Env</em>}' attribute. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #getEnv()
   * @generated
   * @ordered
   */
  protected Map<String, String> env = ENV_EDEFAULT;

  public File getWorkingDir() {
    return workingDir;
  }

  public void setWorkingDir(File newWorkingDir) {
    workingDir = newWorkingDir;
  }

  public void setEnv(Map<String, String> newEnv) {
    env = newEnv;
  }

  /**
   * Max heap size in MB
   * 
   * @param maxHeap
   */
  public void setMaxHeap(int maxHeapSize) {
    this.maxHeap = maxHeapSize;
  }

}
