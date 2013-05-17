/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.properties;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.utils.SafeHome;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.io.CommandLine;

/**
 * A Set of Properties for SAFE
 * 
 * @author sjfink
 * 
 */
public class PropertiesManager {

  final static String PROPERTY_FILENAME = "safe.properties"; //$NON-NLS-1$

  /**
   * Descriptions of all registered properties.
   */
  final private static Map<String, IPropertyDescriptor> registry = HashMapFactory.make();

  static {
    registerProperties(CommonProperties.Props.values());
  }

  final private Properties p;

  PropertiesManager(Properties p) {
    this.p = p;
  }

  public PropertiesManager(PropertiesManager pm) {
    this.p = new Properties(pm.p);
  }

  protected PropertiesManager() {
    this.p = new Properties();
  }

  public static void registerProperties(IPropertyDescriptor[] props) {
    for (IPropertyDescriptor p : props) {
      registry.put(p.getName(), p);
    }
  }

  public static enum Type {
    STRING, INT, BOOLEAN, PATH
  }

  /**
   * Interface which describes the meta-deta for a particular property
   * 
   */
  public interface IPropertyDescriptor {
    /**
     * If "name" is the name, then the command-line option would be "-name"
     */
    public String getName();

    /**
     * String representation of default value, or null if none
     */
    public String getDefaultAsString();

    /**
     * Is it legal to set this property on the command-line?
     */
    public boolean isCommandLineOption();

    /**
     * A human-readable string describing what is the role of the property.
     */
    public String getDescription();

    public Type getType();

  }

  public boolean getBooleanValue(IPropertyDescriptor d) throws PropertiesException {
    if (!d.getType().equals(Type.BOOLEAN)) {
      Assertions.UNREACHABLE("property " + d.getName() + " is not a boolean property");
    }
    String v = p.getProperty(d.getName());
    if (v == null) {
      v = d.getDefaultAsString();
    }
    assert (v.equals("true") || v.equals("false"));
    return Boolean.valueOf(v);
  }

  public int getIntValue(IPropertyDescriptor d) throws PropertiesException {
    if (!d.getType().equals(Type.INT)) {
      Assertions.UNREACHABLE("property " + d.getName() + " is not a boolean property");
    }
    String v = p.getProperty(d.getName());
    if (v == null) {
      v = d.getDefaultAsString();
      if (v == null) {
        throw new PropertiesException("no known value for " + d.getName());
      }
    }
    return Integer.valueOf(v);
  }

  public String getStringValue(IPropertyDescriptor d) throws PropertiesException {
    if (!d.getType().equals(Type.STRING)) {
      Assertions.UNREACHABLE("property " + d.getName() + " is not a String property");
    }
    String result = p.getProperty(d.getName());
    if (result == null) {
      return d.getDefaultAsString();
    } else {
      return result;
    }
  }

  public String getPathValue(IPropertyDescriptor d) throws PropertiesException {
    if (!d.getType().equals(Type.PATH)) {
      Assertions.UNREACHABLE("property " + d.getName() + " is not a Path property");
    }
    String result = p.getProperty(d.getName());
    if (result == null) {
      result = d.getDefaultAsString();
      if (result == null) {
        return null;
      }
    }
    try {
      return resolveDelimitedPath(result);
    } catch (WalaException e) {
      throw new PropertiesException(e.getMessage());
    }
  }

  public void setBooleanValue(String key, boolean val) throws PropertiesException {
    IPropertyDescriptor d = registry.get(key);
    if (d == null) {
      throw new PropertiesException("unknown option : -" + key);
    }
    if (!d.getType().equals(Type.BOOLEAN)) {
      throw new PropertiesException("property " + d.getName() + " is not a boolean property");
    }

    p.put(key, String.valueOf(val));
  }

  public void setIntValue(String key, int val) throws PropertiesException {
    IPropertyDescriptor d = registry.get(key);
    if (d == null) {
      throw new PropertiesException("unknown option : -" + key);
    }
    if (!d.getType().equals(Type.INT)) {
      throw new PropertiesException("property " + d.getName() + " is not an int property");
    }

    p.put(key, String.valueOf(val));
  }

  public void setStringValue(String key, String val) throws PropertiesException {
    IPropertyDescriptor d = registry.get(key);
    if (d == null) {
      throw new PropertiesException("unknown option : -" + key);
    } else if (!d.getType().equals(Type.STRING)) {
      throw new PropertiesException("property " + d.getName() + " is not a String property");
    } else if (val == null) {
      throw new PropertiesException("new String value for " + d.getName() + " should not be null");
    }

    p.put(key, val);
  }

  public void setPathValue(String key, String val) throws PropertiesException {
    IPropertyDescriptor d = registry.get(key);
    if (d == null) {
      throw new PropertiesException("unknown option : -" + key);
    }
    if (!d.getType().equals(Type.PATH)) {
      throw new PropertiesException("property " + d.getName() + " is not a Path property");
    }
    if (val == null) {
      throw new PropertiesException("new String value for " + d.getName() + " should not be null");
    }

    try { // Test the path resolution here, so we catch any mistakes at the
      // source.
      resolveDelimitedPath(val);
    } catch (WalaException e) {
      throw new PropertiesException(e.getMessage());
    }

    // To be consistent with other property mechanisms, store the input
    // (unresolved) path. Feel free to change.
    p.put(key, val);
  }

  /**
   * load the safe.properties into a properties object
   * 
   * @throws PropertiesException
   */
  private static Properties loadPropertiesFromFile() throws PropertiesException {
    try {
      Properties result = WalaProperties.loadPropertiesFromFile(PropertiesManager.class.getClassLoader(), PROPERTY_FILENAME);
      return result;
    } catch (Exception e) {
      e.printStackTrace();
      throw new PropertiesException("Unable to set up safe properties ", e);
    }
  }

  /**
   * initialize a property manager based on the safe.properties file and the
   * command-line.
   * 
   * @throws PropertiesException
   * @throws WalaException
   */
  public static PropertiesManager initFromCommandLine(String[] args) throws PropertiesException, WalaException {
    Properties p = loadPropertiesFromFile();
    Properties cmd = CommandLine.parse(args);
    for (Object key : cmd.keySet()) {
      String s = (String) key;
      IPropertyDescriptor d = registry.get(s);
      if (d == null) {
        throw new PropertiesException("unknown command-line option : -" + s);
      }
      if (!d.isCommandLineOption()) {
        throw new PropertiesException("forbidden command-line option : -" + s);
      }
      p.put(s, cmd.getProperty(s));
    }
    return new PropertiesManager(p);
  }

  /**
   * initialize a property manager based on the safe.properties file and a
   * particular map
   * 
   * @throws PropertiesException
   */
  public static PropertiesManager initFromMap(Map<String, String> options) throws PropertiesException {
    Properties p = loadPropertiesFromFile();
    for (String key : options.keySet()) {
      IPropertyDescriptor d = registry.get(key);
      if (d == null) {
        throw new PropertiesException("unknown option : -" + key);
      }
      p.put(key, options.get(key));
    }
    return new PropertiesManager(p);
  }

  private static String resolveDelimitedPath(final String pathValue) throws WalaException {
    StringBuffer result = new StringBuffer();
    StringTokenizer tokenizer = new StringTokenizer(pathValue, ";");
    while (tokenizer.hasMoreTokens()) {
      String token = tokenizer.nextToken();
      result.append(resolvePath(token));
      if (tokenizer.hasMoreTokens()) {
        result.append(";");
      }
    }
    return result.toString();
  }

  private static String resolvePath(final String pathValue) throws WalaException {
    final File file = new File(pathValue);
    final String path;
    if (file.isAbsolute()) {
      path = file.getPath();
    } else {
      path = SafeHome.getSafeHomeDir(PropertiesManager.class.getClassLoader()).concat(File.separator).concat(pathValue);
    }

    final File targetPath = new File(path);
    final File fileToCheck = (targetPath.isDirectory()) ? targetPath : targetPath.getParentFile();
    if (fileToCheck != null && !fileToCheck.exists() && !fileToCheck.mkdirs()) {
      throw new WalaException("Could not resolve path " + path);
    }

    return targetPath.getPath();
  }
}
