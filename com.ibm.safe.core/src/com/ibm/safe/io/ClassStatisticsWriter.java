/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.io;

import java.io.FileWriter;

import com.ibm.safe.metrics.ProgramStatistics;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class ClassStatisticsWriter {
  public static void write(String fileName, ProgramStatistics progStats) {
    try {
      FileWriter fw = new FileWriter(fileName, false);

      fw.write(writeClassStats(progStats));

      fw.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error writing result file");
    }
  }

  public static String writeClassStats(ProgramStatistics progStats) {
    return progStats.toString();
  }

}