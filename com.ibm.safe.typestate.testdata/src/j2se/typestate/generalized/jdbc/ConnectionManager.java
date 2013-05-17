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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

class ConnectionManager {
  protected List connections = new ArrayList();

  public ConnectionManager() {
    init();
  }

  public static void main(String[] args) {
    ConnectionManager cm = new ConnectionManager();
    System.out.println(cm.toString());
  }

  private void init() {
    String driverName = "something";
    String dbUrl = "another thing";
    try {
      Class.forName(driverName);
      // create a bunch of connections, put them in the set
      for (int i = 0; i < 10; i++) {
        Connection conn = DriverManager.getConnection(dbUrl);
        connections.add(conn);
      }
    } catch (java.sql.SQLException e) {
      throw new RuntimeException("cannot create connections");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("driver class not found");
    }
  }

  public Connection getConnection() {
    if (!connections.isEmpty()) {
      return (Connection) connections.remove(0);
    } else {
      throw new RuntimeException("out of connections");
    }
  }

  public Statement createStatement(Connection connection) {
    try {
      return connection.createStatement();
    } catch (java.sql.SQLException e) {
      throw new RuntimeException("cannot create statement");
    }
  }

}
