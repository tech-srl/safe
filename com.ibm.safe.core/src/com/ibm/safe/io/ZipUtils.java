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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class ZipUtils {

  /**
   * Uncompress a given zip file named <i>zipFileName</i> over directory named
   * <i>directory</i>. Note that files unzipped have a verification of
   * existence, and if the verdict is positive then no overwritting is realized.
   * 
   * @param zipFileName
   *            The zip file to unzip.
   * @param directory
   *            The directory where to unzip the file.
   * @throws IOException
   *             Occurs if zip file can't be read properly or some writting
   *             issues happen when creating the unzipped files on disk.
   */
  public static void uncompress(final String zipFileName, final String directory) throws IOException {
    final ZipInputStream zipIs = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFileName)));

    ZipEntry entry;
    int count;
    final byte data[] = new byte[BUFFER_SIZE];
    while ((entry = zipIs.getNextEntry()) != null) {
      final File file = new File(directory, entry.getName());
      if (meetRequirements(file)) {
        final BufferedOutputStream dest = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);
        while ((count = zipIs.read(data, 0 /* offset */, BUFFER_SIZE /* length */)) != -1) {
          dest.write(data, 0, count);
        }
        dest.flush();
        dest.close();
      }
    }
    zipIs.close();
  }

  private static boolean meetRequirements(final File file) {
    assert (file != null);
    final File parentFile = file.getParentFile();
    assert (parentFile != null);
    return (!file.exists()) && (parentFile.exists() || parentFile.mkdirs());
  }

  private final static int BUFFER_SIZE = 2048;

}
