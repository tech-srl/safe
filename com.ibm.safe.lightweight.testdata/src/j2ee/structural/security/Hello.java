package j2ee.structural.security;

import java.rmi.RemoteException;

import javax.ejb.EJBObject;

public interface Hello extends EJBObject {
  public String hello1() throws RemoteException;
}
