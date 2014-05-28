package com.rackspace.papi.service.context.impl;

import com.rackspace.papi.commons.util.StringUtilities;
import com.rackspace.papi.commons.util.http.HttpsURLConnectionSslInitializer;
import com.rackspace.papi.domain.ReposeInstanceInfo;
import com.rackspace.papi.domain.ServicePorts;
import com.rackspace.papi.service.context.ContextAdapter;
import com.rackspace.papi.service.context.ServletContextAware;
import com.rackspace.papi.service.context.ServletContextHelper;
import com.rackspace.papi.service.context.banner.PapiBanner;
import com.rackspace.papi.service.deploy.ArtifactManagerServiceContext;
import com.rackspace.papi.service.threading.impl.ThreadingServiceContext;
import com.rackspace.papi.servlet.InitParameter;
import com.rackspace.papi.spring.SpringWithServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Map;

/**
 * TODO: THIS WHOLE CLASS SHOULD BE DELETED BY THE spring framework ContextLoaderListener
 * It will tie the application context lifecycle to the servlet context lifecycle, which is exactly what we want... I hope
 * But will it even work in valve?
 * # YES: http://stackoverflow.com/questions/10738816/deploying-a-servlet-programmatically-with-
 * TODO: THIS WHOLE CLASS SHOULD DIEEEEEE!!!!!!!!!!!!1111111111111
 */
//Lets see if I can haxor this into a bean, and like, make it go
@Component
public class PowerApiContextManager implements ServletContextListener {

   private static final Logger LOG = LoggerFactory.getLogger(PowerApiContextManager.class);
   private AbstractApplicationContext applicationContext;
   private ServicePorts ports;
   private boolean contextInitialized = false;
   private ReposeInstanceInfo instanceInfo;

   public PowerApiContextManager() {
       System.out.println("INITIALIZING STUPID CONTEXT THINGY");
   }

   public PowerApiContextManager setPorts(ServicePorts ports, ReposeInstanceInfo instanceInfo) {
      this.ports = ports;
      this.instanceInfo = instanceInfo;
      configurePorts(applicationContext);
      configureReposeInfo(applicationContext);
      return this;
   }

    //TODO: it seems that the "connectionFramework" is deprecated EITHER WAY, therefore don't care?
   private AbstractApplicationContext initApplicationContext(ServletContext servletContext) {
      final String connectionFrameworkProp = InitParameter.CONNECTION_FRAMEWORK.getParameterName();
      final String connectionFramework = System.getProperty(connectionFrameworkProp, servletContext.getInitParameter(connectionFrameworkProp));

      AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringWithServices.class);
      if (StringUtilities.isNotBlank(connectionFramework)) {
          LOG.warn("***DEPRECATED*** The ability to define the connection framework of jersey, ning, or apache has been deprecated!" +
                  " The default and only available connection framework is Apache HttpClient");
      } else {
          LOG.warn("***DEPRECATED*** The default connection framework has changed from Jersey to Apache HttpClient!");
      }

       //TODO: these thingies need to be called somewhere
      configurePorts(context);
      configureReposeInfo(context);

      Thread.currentThread().setName(instanceInfo.toString());
      context.getBean("exporter");

      return context;

   }

    //TODO: have to figure out a way to handle this... I'm not even sure why it's here
    // it's only to hide the spring stuff in here... May have to find a different way to add parameters
   private void configurePorts(ApplicationContext context) {
      if (ports == null || context == null) {
         return;
      }
      ServicePorts servicePorts = context.getBean("servicePorts", ServicePorts.class);
      servicePorts.clear();
      servicePorts.addAll(ports);
   }

    //TODO: this will have to be handled somewhere else also
    // Probably a standalone bean? -- Maybe not, it's initialized in two different places...
   private void configureReposeInfo(ApplicationContext context) {
      if (instanceInfo == null) {

         String clusterId = System.getProperty("repose-cluster-id");
         String nodeId = System.getProperty("repose-node-id");
         instanceInfo = new ReposeInstanceInfo(clusterId, nodeId);
      }
      if (context == null) {
         return;
      }


      ReposeInstanceInfo reposeInstanceInfo = context.getBean("reposeInstanceInfo", ReposeInstanceInfo.class);
      reposeInstanceInfo.setClusterId(instanceInfo.getClusterId());
      reposeInstanceInfo.setNodeId(instanceInfo.getNodeId());
   }

   private void intializeServices(ServletContextEvent sce) {
      ServletContextHelper helper = ServletContextHelper.getInstance(sce.getServletContext());
      ContextAdapter ca = helper.getPowerApiContext();

      ca.getContext(ConfigurationServiceContext.class).contextInitialized(sce);
      ca.getContext(ContainerServiceContext.class).contextInitialized(sce);
      ca.getContext(RoutingServiceContext.class).contextInitialized(sce);
      ca.getContext(LoggingServiceContext.class).contextInitialized(sce);
      PapiBanner.print(LOG);
      ca.getContext(ResponseMessageServiceContext.class).contextInitialized(sce);
      // TODO:Refactor - This service should be bound to a fitler-chain specific JNDI context
      ca.getContext(DatastoreServiceContext.class).contextInitialized(sce);
      ca.getContext(ClassLoaderServiceContext.class).contextInitialized(sce);
      ca.getContext(ArtifactManagerServiceContext.class).contextInitialized(sce);
      ca.getContext(FilterChainGCServiceContext.class).contextInitialized(sce);
      ca.getContext(RequestProxyServiceContext.class).contextInitialized(sce);
      ca.getContext(ReportingServiceContext.class).contextInitialized(sce);
      ca.getContext(RequestHeaderServiceContext.class).contextInitialized(sce);
      ca.getContext(ResponseHeaderServiceContext.class).contextInitialized(sce);
      ca.getContext(DistributedDatastoreServiceContext.class).contextInitialized(sce);
      ca.getContext( MetricsServiceContext.class ).contextInitialized( sce );
      ca.getContext(HttpConnectionPoolServiceContext.class).contextInitialized(sce);
      ca.getContext(AkkaServiceClientContext.class).contextInitialized(sce);

      // Start management server
      if (isManagementServerEnabled()) {
         ca.getContext(ManagementServiceContext.class).contextInitialized(sce);
      }

      Map<String, ServletContextAware> contextAwareBeans = applicationContext.getBeansOfType(ServletContextAware.class);

      for (ServletContextAware bean : contextAwareBeans.values()) {
         bean.contextInitialized(sce);
      }

   }

   private boolean isManagementServerEnabled() {
      return System.getProperty(InitParameter.MANAGEMENT_PORT.getParameterName()) != null;
   }

    /**
     * Given a couple TODOs, this entire method is unnecessary
     * @param sce
     */
   @Override
   public void contextInitialized(ServletContextEvent sce) {
      final ServletContext servletContext = sce.getServletContext();

       //TODO: find a home for this thingy
      final String insecureProp = InitParameter.INSECURE.getParameterName();
      final String insecure = System.getProperty(insecureProp, servletContext.getInitParameter(insecureProp));

      if (StringUtilities.nullSafeEqualsIgnoreCase(insecure, "true")) {
         new HttpsURLConnectionSslInitializer().allowAllServerCerts();
      }

      applicationContext = initApplicationContext(servletContext);

      //Allows Repose to set any header to pass to the origin service. Namely the "Via" header
       //TODO: this seems like a bad place to set a system property :(
      System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

      // Most bootstrap steps require or will try to load some kind of
      // configuration so we need to set our naming context in the servlet context
      // first before anything else
       //TODO: why not do this in spring? Put this into a bean of some sort instead
       //Replaced with the ContextLoaderListener. That will tie the spring lifecycle to the servlet lifecycle, which is
       // Exactly what we want.
      ServletContextHelper.configureInstance(
              servletContext,
              applicationContext);

      intializeServices(sce);
      servletContext.setAttribute("powerApiContextManager", this);
      contextInitialized = true;
   }

   public boolean isContextInitialized() {
      return contextInitialized;
   }

    /**
     * This entire method is unnecessary with the ContextLoaderListener
     * @param sce
     */
   @Override
   public void contextDestroyed(ServletContextEvent sce) {
      contextInitialized = false;


      Map<String, ServletContextAware> contextAwareBeans = applicationContext.getBeansOfType(ServletContextAware.class);

      for (ServletContextAware bean : contextAwareBeans.values()) {
         bean.contextDestroyed(sce);
      }

      LOG.info("Shutting down Spring application context");
       //So spring will automatically call PreDestroy on all things, we don't need to have a serviceRegistry...
      applicationContext.close();


   }
}
