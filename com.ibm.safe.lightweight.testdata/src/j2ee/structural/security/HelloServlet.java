package j2ee.structural.security;

import java.io.FilePermission;
import java.io.IOException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Security;

import javax.security.auth.Subject;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet implements Servlet {
  /**
   * 
   */
  private static final long serialVersionUID = -1059788981323765239L;

  public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    System.loadLibrary("a");
    Runtime.getRuntime().loadLibrary("b");

    System.setSecurityManager(new SecurityManager());

    PrivilegedAction pa = new PrivilegedAction() {
      public Object run() {
        return null;
      }
    };
    PrivilegedExceptionAction pea = new PrivilegedExceptionAction() {
      public Object run() {
        return null;
      }
    };
    AccessControlContext context = AccessController.getContext();
    Subject s = new Subject();

    AccessController.doPrivileged(pa);
    try {
      AccessController.doPrivileged(pea);
    } catch (PrivilegedActionException e) {
    }

    AccessController.doPrivileged(pa, context);
    try {
      AccessController.doPrivileged(pea, context);
    } catch (PrivilegedActionException e) {
    }

    Policy.setPolicy(Policy.getPolicy());

    Security.setProperty("a", "b");

    Subject.doAs(s, pa);
    try {
      Subject.doAs(s, pea);
    } catch (PrivilegedActionException e) {
    }
    Subject.doAsPrivileged(s, pa, context);
    try {
      Subject.doAsPrivileged(s, pea, context);
    } catch (PrivilegedActionException e) {
    }

    SecurityManager sm = System.getSecurityManager();
    Permission p = new FilePermission("a", "read");
    AccessController.checkPermission(p);
  }
}
