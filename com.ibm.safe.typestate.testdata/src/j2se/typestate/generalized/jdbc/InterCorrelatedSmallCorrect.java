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

// InterCorrelatedSmallCorrect

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

class InterCorrelatedSmallCorrect {

  public static void printBalances(int id, int threshold) throws java.sql.SQLException {

    String maxQuery = "SELECT MAX(balance) FROM accounts WHERE id= " + id;
    String minQuery = "SELECT MIN(balance) FROM accounts WHERE id= " + id;
    String balancesQuery = "SELECT balance FROM accounts WHERE id = " + id + " ORDER BY balance DESC";

    ConnectionManager cm = new ConnectionManager();

    Connection connection1 = cm.getConnection();
    Statement stmt1 = cm.createStatement(connection1);
    boolean closed1 = false;
    int maxBalance1 = 0;

    ResultSet maxRs = stmt1.executeQuery(maxQuery);
    if (maxRs.next())
      maxBalance1 = maxRs.getInt(1);
    maxRs.close();

    ResultSet rs1 = stmt1.executeQuery(balancesQuery);

    if (maxBalance1 < threshold) {
      stmt1.close();
      closed1 = true;
    }

    Connection connection2 = cm.getConnection();
    Statement stmt2 = cm.createStatement(connection2);
    boolean closed2 = false;
    int maxBalance2 = 0, minBalance2 = 0;

    ResultSet maxRs2 = stmt2.executeQuery(maxQuery);
    if (maxRs2.next())
      maxBalance2 = maxRs2.getInt(1);
    maxRs2.close();

    ResultSet minRs2 = stmt2.executeQuery(minQuery);
    if (minRs2.next())
      minBalance2 = minRs2.getInt(1);
    minRs2.close();

    ResultSet rs2 = stmt2.executeQuery(balancesQuery);

    if (maxBalance2 < threshold) {
      stmt2.close();
      closed2 = true;
    }

    if (minBalance2 > maxBalance1) {
      if (!closed2)
        while (rs2.next())
          printBalanceRecord(rs2.getInt(1));
      if (!closed1)
        while (rs1.next())
          printBalanceRecord(rs1.getInt(1));
    } else {
      if (!closed1)
        while (rs1.next())
          printBalanceRecord(rs1.getInt(1));
      if (!closed2)
        while (rs2.next())
          printBalanceRecord(rs2.getInt(1));
    }

    Connection connection3 = cm.getConnection();
    Statement stmt3 = cm.createStatement(connection2);
    boolean closed3 = false;
    int maxBalance3 = 0;

    ResultSet maxRs3 = stmt3.executeQuery(maxQuery);
    if (maxRs3.next())
      maxBalance3 = maxRs3.getInt(1);
    maxRs3.close();

    ResultSet rs3 = stmt3.executeQuery(balancesQuery);

    if (maxBalance3 < threshold) {
      stmt3.close();
      closed3 = true;
    }

    Connection connection4 = cm.getConnection();
    Statement stmt4 = cm.createStatement(connection2);
    boolean closed4 = false;
    int maxBalance4 = 0;

    ResultSet maxRs4 = stmt4.executeQuery(maxQuery);
    if (maxRs4.next())
      maxBalance4 = maxRs4.getInt(1);
    maxRs4.close();

    ResultSet rs4 = stmt4.executeQuery(balancesQuery);

    if (maxBalance4 < threshold) {
      stmt4.close();
      closed4 = true;
    }

    if (!closed3)
      while (rs3.next())
        printBalanceRecord(rs3.getInt(1));

    if (!closed4)
      while (rs4.next())
        printBalanceRecord(rs4.getInt(1));

    if (!closed1)
      stmt1.close();

    if (!closed2)
      stmt2.close();

    if (!closed3)
      stmt3.close();

    if (!closed4)
      stmt4.close();

  }

  public static void main(String[] args) {
    try {
      printBalances(42, 200);
    } catch (java.sql.SQLException e) {
      throw new RuntimeException("SQL Error executing query");
    }
  }

  public static void printBalanceRecord(int value) {
    System.out.println("Balance: " + value);
  }

}
