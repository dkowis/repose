package com.rackspace.papi.service.context.impl;

import com.rackspace.papi.service.context.ServiceContext;
import com.rackspace.papi.service.serviceclient.akka.AkkaServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContextEvent;

/**
 * Manages the {@link com.rackspace.papi.service.serviceclient.akka.AkkaServiceClient}
 */

public class AkkaServiceClientContext implements ServiceContext<AkkaServiceClient> {
    private static final Logger LOG = LoggerFactory.getLogger(AkkaServiceClientContext.class);

    public static final String SERVICE_NAME = "powerapi:/services/AkkaServiceClient";

    private final AkkaServiceClient akkaServiceClientService;


    @Autowired
    public AkkaServiceClientContext(AkkaServiceClient akkaServiceClientService) {
        this.akkaServiceClientService = akkaServiceClientService;

    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public AkkaServiceClient getService() {
        return akkaServiceClientService;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.debug("Initializing context for Akka Authentication Service");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.debug("Destroying context for Akka Authentication Service");
        akkaServiceClientService.shutdown();

    }

}
