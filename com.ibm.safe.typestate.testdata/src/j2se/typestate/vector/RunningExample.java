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
package j2se.typestate.vector;

import java.util.Random;
import java.util.Vector;

public final class RunningExample {

  public static void main(String[] args) {
    try {
      Object a = simple();
      apMust();
      apMustMustNot();
      Object x = apMustK();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Object simple() {
    Vector v1 = new Vector();
    v1.add(new Object());
    return v1.firstElement();

  }

  public static Object apMust() {
    Vector v = getOneOfManyVector();
    v.add(new Object());
    return v.firstElement();

  }

  private static Vector getOneOfManyVector() {
    if (random())
      return new Vector();
    else
      return new Vector();
  }

  public static Object apMustMustNot() {
    Container c1 = new Container();
    Container c2 = new Container();
    Container s;

    if (random()) {
      s = c1;
    } else {
      s = c2;
    }

    Vector v = s.vector;
    v.add(new Object());
    return v.firstElement();

  }

  public static Object apMustK() {

    Container1 c = new Container1();
    c.vector = new Vector();
    Vector v = c.vector;
    c.vector.removeAllElements();

    c.vector.add(new Object());

    return v.get(0);
  }

  private static boolean random() {
    return (new Random().nextBoolean());
  }

  private static void init(Vector v1, Vector v2) {
    v1.add(new Object());
    v2.removeAllElements();
  }

  private static class Container1 {
    Vector vector;
  }

  private static class Container {
    Vector vector;

    public Container() {
      vector = new Vector();
    }

    public Object getElement() {
      return vector.get(0);
    }

    public void addElemet() {
      vector.add(new Object());
    }

  }

}