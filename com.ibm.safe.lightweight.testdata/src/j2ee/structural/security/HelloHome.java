package j2ee.structural.security;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface HelloHome extends EJBHome {
  Hello create() throws RemoteException, CreateException;
}
