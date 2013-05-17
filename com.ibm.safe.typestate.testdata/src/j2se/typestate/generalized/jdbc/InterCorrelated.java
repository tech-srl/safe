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
package j2se.typestate.generalized.jdbc;

// InterCorrelated.java

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

class InterCorrelated {

  public static void main(String[] args) {
    ConnectionManager cm = new ConnectionManager();

    try {
      Connection aConnection = cm.getConnection();
      Statement stmt1 = aConnection.createStatement();
      boolean closed1 = false;
      int id = 42;
      int firstBalance = 0;
      int anotherBalance = 0;
      String query = "SELECT balance FROM accounts WHERE  id = " + id + " ORDER BY balance";

      ResultSet rs1 = stmt1.executeQuery(query);

      if (rs1.next())
        firstBalance = rs1.getInt(1);

      if (firstBalance < 200) {
        stmt1.close();
        closed1 = true;
      }

      Connection aConnection2 = cm.getConnection();
      Statement stmt2 = aConnection2.createStatement();
      boolean closed2 = false;
      id = 447;
      int aBalance = 0;

      ResultSet rs2 = stmt2.executeQuery(query);

      if (rs2.next())
        firstBalance = rs2.getInt(1);

      if (!closed1) {
        while (rs1.next()) {
          anotherBalance = rs1.getInt(1);
        }
      }

      if (firstBalance < 200) {
        stmt2.close();
        closed2 = true;
      }

      Connection aConnection3 = cm.getConnection();
      Statement stmt3 = aConnection3.createStatement();
      boolean closed3 = false;
      id = 447;
      aBalance = 0;

      ResultSet rs3 = stmt3.executeQuery(query);

      while (rs3.next()) {
        aBalance = rs3.getInt(1);
      }

      if (aBalance < 200) {
        stmt3.close();
        closed3 = true;
      }

      Connection aConnection4 = cm.getConnection();
      Statement stmt4 = aConnection4.createStatement();
      boolean closed4 = false;
      id = 447;
      aBalance = 0;

      ResultSet rs4 = stmt4.executeQuery(query);

      while (rs4.next()) {
        aBalance = rs4.getInt(1);
      }

      if (aBalance < 200) {
        stmt4.close();
        closed4 = true;
      }

      if (!closed1)
        stmt1.close();

      if (!closed2)
        stmt2.close();

      if (!closed3)
        stmt3.close();

      if (!closed4)
        stmt4.close();

    } catch (java.sql.SQLException e) {
      throw new RuntimeException("SQL Error executing query");
    }
  }
}
