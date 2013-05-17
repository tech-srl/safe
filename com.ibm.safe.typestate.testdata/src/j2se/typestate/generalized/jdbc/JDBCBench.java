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

/* 
 *  This is a sample implementation of the Transaction Processing Performance 
 *  Council Benchmark B coded in Java and ANSI SQL2. 
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

class JDBCBench {

  /* tpc bm b scaling rules */
  public static int tps = 1; /* the tps scaling factor: here it is 1 */

  public static int nbranches = 1; /* number of branches in 1 tps db */

  public static int ntellers = 10; /* number of tellers in 1 tps db */

  public static int naccounts = 100000; /* number of accounts in 1 tps db */

  public static int nhistory = 864000; /* number of history recs in 1 tps db */

  public final static int TELLER = 0;

  public final static int BRANCH = 1;

  public final static int ACCOUNT = 2;

  private Connection Conn = null;

  int failed_transactions = 0;

  int transaction_count = 0;

  static int n_clients = 10;

  static int n_txn_per_client = 10;

  long start_time = 0;

  static boolean innodb = false;

  static boolean verbose = false;

  MemoryWatcherThread MemoryWatcher;

  /*
   * main program, creates a 1-tps database: i.e. 1 branch, 10 tellers,... runs
   * one TPC BM B transaction
   */

  public static void main(String[] Args) {
    String DriverName = "";
    String DBUrl = "";
    boolean initialize_dataset = false;

    for (int i = 0; i < Args.length; i++) {
      if (Args[i].equals("-clients")) {
        if (i + 1 < Args.length) {
          i++;
          n_clients = Integer.parseInt(Args[i]);
        }
      } else if (Args[i].equals("-driver")) {
        if (i + 1 < Args.length) {
          i++;
          DriverName = Args[i];
        }
      } else if (Args[i].equals("-url")) {
        if (i + 1 < Args.length) {
          i++;
          DBUrl = Args[i];
        }
      } else if (Args[i].equals("-tpc")) {
        if (i + 1 < Args.length) {
          i++;
          n_txn_per_client = Integer.parseInt(Args[i]);
        }
      } else if (Args[i].equals("-init")) {
        initialize_dataset = true;
      } else if (Args[i].equals("-v")) {
        verbose = true;
      } else if (Args[i].equals("-innodb")) {
        innodb = true;
      }
    }

    if (DriverName.length() == 0 || DBUrl.length() == 0) {
      System.out.println("usage: java JDBCBench -driver [driver_class_name] -url [url_to_db] [-v] [-init] [-tpc n] [-clients]");
      System.out.println();
      System.out.println("-v 		verbose error messages");
      System.out.println("-init 	initialize the tables");
      System.out.println("-tpc	transactions per client");
      System.out.println("-clients    number of simultaneous clients");
      System.exit(-1);
    }

    System.out.println("*********************************************************");
    System.out.println("* JDBCBench v1.0                                        *");
    System.out.println("*********************************************************");
    System.out.println();
    System.out.println("Driver: " + DriverName);
    System.out.println("URL:" + DBUrl);
    System.out.println();
    System.out.println("Number of clients: " + n_clients);
    System.out.println("Number of transactions per client: " + n_txn_per_client);
    System.out.println();

    try {
      Class.forName(DriverName);
      Connection C = DriverManager.getConnection(DBUrl);

      JDBCBench Me = new JDBCBench(C, initialize_dataset);
    } catch (Exception E) {
      System.out.println(E.getMessage());
      E.printStackTrace();
    }
  }

  public JDBCBench(Connection C, boolean init) {
    try {
      Conn = C;
      if (init) {
        System.out.println("Start: " + (new java.util.Date()).toString());
        System.out.print("Initializing dataset...");
        createDatabase();
        System.out.println("done.\n");
        System.out.println("Complete: " + (new java.util.Date()).toString());
      }
      System.out.println("* Starting Benchmark Run *");
      MemoryWatcher = new MemoryWatcherThread();
      MemoryWatcher.start();

      start_time = System.currentTimeMillis();

      for (int i = 0; i < n_clients; i++) {
        Thread Client = new ClientThread(n_txn_per_client);
        Client.start();
      }
    } catch (Exception E) {
      System.out.println(E.getMessage());
      E.printStackTrace();
    }
  }

  public void reportDone() {
    n_clients--;

    if (n_clients <= 0) {
      MemoryWatcher.interrupt();

      long end_time = System.currentTimeMillis();
      double completion_time = ((double) end_time - (double) start_time) / 1000;
      System.out.println("* Benchmark finished *");
      System.out.println("\n* Benchmark Report *");
      System.out.println("--------------------\n");
      System.out.println("Time to execute " + transaction_count + " transactions: " + completion_time + " seconds.");
      System.out.println("Max/Min memory usage: " + MemoryWatcher.max + " / " + MemoryWatcher.min + " kb");
      System.out.println(failed_transactions + " / " + transaction_count + " failed to complete.");
      System.out.println("Transaction rate: " + (transaction_count - failed_transactions) / completion_time + " txn/sec.");
    }

  }

  public synchronized void incrementTransactionCount() {
    transaction_count++;
  }

  public synchronized void incrementFailedTransactionCount() {
    failed_transactions++;
  }

  /*
   * createDatabase() - Creates and Initializes a scaled database.
   */

  void createDatabase() throws Exception {

    try {
      Statement Stmt = Conn.createStatement();

      String s = Conn.getMetaData().getDatabaseProductName();
      System.out.println("DBMS: " + s);

      String Query = "CREATE TABLE branches (";
      Query += "Bid         INT NOT NULL, PRIMARY KEY(Bid), ";
      Query += "Bbalance    INT,";
      Query += "filler      CHAR(88))"; /* pad to 100 bytes */
      if (innodb)
        Query += " TYPE = InnoDB";
      Stmt.execute(Query);
      Stmt.clearWarnings();

      Query = "CREATE TABLE tellers ( ";
      Query += "Tid         INT NOT NULL, PRIMARY KEY(Tid),";
      Query += "Bid         INT,";
      Query += "Tbalance    INT,";
      Query += "filler      CHAR(84))"; /* pad to 100 bytes */
      if (innodb)
        Query += " TYPE = InnoDB";

      Stmt.execute(Query);
      Stmt.clearWarnings();

      Query = "CREATE TABLE accounts ( ";
      Query += "Aid         INT NOT NULL, PRIMARY KEY(Aid), ";
      Query += "Bid         INT, ";
      Query += "Abalance    INT, ";
      Query += "filler      CHAR(84))"; /* pad to 100 bytes */
      if (innodb)
        Query += " TYPE = InnoDB";

      Stmt.execute(Query);
      Stmt.clearWarnings();

      Query = "CREATE TABLE history ( ";
      Query += "Tid         INT, ";
      Query += "Bid         INT, ";
      Query += "Aid         INT, ";
      Query += "delta       INT, ";
      Query += "time        TIMESTAMP, ";
      Query += "filler      CHAR(22))"; /* pad to 50 bytes */
      if (innodb)
        Query += " TYPE = InnoDB";

      Stmt.execute(Query);
      Stmt.clearWarnings();

      /*
       * prime database using TPC BM B scaling rules. Note that for each branch
       * and teller: branch_id = teller_id / ntellers branch_id = account_id /
       * naccounts
       */

      for (int i = 0; i < nbranches * tps; i++) {
        Query = "INSERT INTO branches(Bid,Bbalance) VALUES (" + i + ",0)";
        Stmt.executeUpdate(Query);
        Stmt.clearWarnings();
      }
      for (int i = 0; i < ntellers * tps; i++) {
        Query = "INSERT INTO tellers(Tid,Bid,Tbalance) VALUES (" + i + "," + i / ntellers + ",0)";
        Stmt.executeUpdate(Query);
        Stmt.clearWarnings();
      }
      for (int i = 0; i < naccounts * tps; i++) {
        Query = "INSERT INTO accounts(Aid,Bid,Abalance) VALUES (" + i + "," + i / naccounts + ",0)";
        Stmt.executeUpdate(Query);
        Stmt.clearWarnings();
      }
    } catch (Exception E) {
      System.out.println(E.getMessage());
      E.printStackTrace();
    }

  } /* end of CreateDatabase */

  public static int getRandomInt(int lo, int hi) {
    int ret = 0;

    ret = (int) (Math.random() * (hi - lo + 1));
    ret += lo;

    return ret;
  }

  public static int getRandomID(int type) {
    int min, max, num;

    max = min = 0;
    num = naccounts;

    switch (type) {
    case TELLER:
      min += nbranches;
      num = ntellers;
      /* FALLTHROUGH */
    case BRANCH:
      if (type == BRANCH)
        num = nbranches;
      min += naccounts;
      /* FALLTHROUGH */
    case ACCOUNT:
      max = min + num - 1;
    }
    return (getRandomInt(min, max));
  }

  class ClientThread extends Thread {
    int ntrans = 0;

    public ClientThread(int number_of_txns) {
      ntrans = number_of_txns;
    }

    public void run() {
      while (ntrans-- > 0) {

        int account = JDBCBench.getRandomID(ACCOUNT);
        int branch = JDBCBench.getRandomID(BRANCH);
        int teller = JDBCBench.getRandomID(TELLER);
        int delta = JDBCBench.getRandomInt(0, 1000);

        doOne(account, branch, teller, delta);
        incrementTransactionCount();
      }
      reportDone();
    }

    /*
     * doOne() - Executes a single TPC BM B transaction.
     */

    int doOne(int bid, int tid, int aid, int delta) {
      try {
        Statement Stmt = Conn.createStatement();

        String Query = "UPDATE accounts ";
        Query += "SET     Abalance = Abalance + " + delta + " ";
        Query += "WHERE   Aid = " + aid;

        Stmt.executeUpdate(Query);
        Stmt.clearWarnings();

        Query = "SELECT Abalance ";
        Query += "FROM   accounts ";
        Query += "WHERE  Aid = " + aid;

        ResultSet RS = Stmt.executeQuery(Query);
        Stmt.clearWarnings();

        int aBalance = 0;

        while (RS.next()) {
          aBalance = RS.getInt(1);
        }

        Query = "UPDATE tellers ";
        Query += "SET    Tbalance = Tbalance + " + delta + " ";
        Query += "WHERE  Tid = " + tid;

        Stmt.executeUpdate(Query);
        Stmt.clearWarnings();

        Query = "UPDATE branches ";
        Query += "SET    Bbalance = Bbalance + " + delta + " ";
        Query += "WHERE  Bid = " + bid;

        Stmt.executeUpdate(Query);
        Stmt.clearWarnings();

        Query = "INSERT INTO history(Tid, Bid, Aid, delta) ";
        Query += "VALUES (";
        Query += tid + ",";
        Query += bid + ",";
        Query += aid + ",";
        Query += delta + ")";

        Stmt.executeUpdate(Query);
        Stmt.clearWarnings();

        return aBalance;
      } catch (SQLException E) {
        if (verbose) {
          System.out.println("Transaction failed: " + E.getMessage());
          E.printStackTrace();
        }
        incrementFailedTransactionCount();
      }
      return 0;

    } /* end of DoOne */

  }

  class MemoryWatcherThread extends Thread {
    long min = 0;

    long max = 0;

    public void run() {
      min = Runtime.getRuntime().freeMemory();

      for (;;) {
        long currentFree = Runtime.getRuntime().freeMemory();
        long currentAlloc = Runtime.getRuntime().totalMemory();
        long used = currentAlloc - currentFree;

        if (used < min)
          min = used;
        if (used > max)
          max = used;

        try {
          sleep(100);
        } catch (InterruptedException E) {
        }
      }
    }
  }

}
