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
/**
 * author: inbal
 */
package j2se.typestate.multipleobjects;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author inbal
 * 
 */
public class ReflectionTest {

  void makeReflectionCalls(String className, String method1, String method2) {
    try {
      Class foundClass = Class.forName(className);
      Object obj = foundClass.newInstance();
      Class[] parameterTypes = new Class[] { int.class };
      Constructor otherObjConst = foundClass.getConstructor(parameterTypes);
      Object[] args = new Object[1];
      args[0] = 2;
      Object otherObj = otherObjConst.newInstance(args);

      Method method = foundClass.getMethod(method1, new Class[] {});
      method.invoke(obj, null);
      Method otherMethod = foundClass.getMethod(method2, new Class[] { String.class });
      Object[] args2 = new Object[1];
      args2[0] = "hello";
      otherMethod.invoke(otherObj, args2);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    new ReflectionTest().makeReflectionCalls(args[0], args[1], args[2]);

  }

  public class TestClass {

    public TestClass() {

    }

    public TestClass(int i) {

      System.out.println("in constructor, got parameter " + i);
    }

    public void testMethod() {

      System.out.println("in empty testMethod");
    }

    public void testMethod2(String str) {

      System.out.println("in testMethod with String " + str);
    }
  }
}
