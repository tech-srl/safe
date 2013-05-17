package j2ee.structural.security;

import java.io.FilePermission;
import java.rmi.RemoteException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Permission;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.Security;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.security.auth.Subject;

public class HelloBean implements SessionBean {
  /**
   * 
   */
  private static final long serialVersionUID = -1005512060134465534L;

  public void hello1() throws Exception {
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

  public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
  }

  public void ejbRemove() throws EJBException, RemoteException {
  }

  public void ejbActivate() throws EJBException, RemoteException {
  }

  public void ejbPassivate() throws EJBException, RemoteException {
  }
}