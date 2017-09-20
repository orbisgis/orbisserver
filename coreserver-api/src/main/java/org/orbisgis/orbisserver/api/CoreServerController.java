package org.orbisgis.orbisserver.api;

import org.orbisgis.orbisserver.api.service.ServiceFactory;

/**
 * @author Sylvain PALOMINOS
 */
public interface CoreServerController{

    void addServiceFactory(ServiceFactory serviceFactory);
}
