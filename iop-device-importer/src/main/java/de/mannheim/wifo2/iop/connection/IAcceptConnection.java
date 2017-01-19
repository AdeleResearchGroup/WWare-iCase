package de.mannheim.wifo2.iop.connection;

/**
 * Classes implementing this interface must provide
 * a constructor with the following arguments:
 * 1. ICallback callback
 * 2. Class<IConnection> clazz (class of client connections)
 * 3. int port
 * @author Max
 *
 */
public interface IAcceptConnection extends IConnection {

}
