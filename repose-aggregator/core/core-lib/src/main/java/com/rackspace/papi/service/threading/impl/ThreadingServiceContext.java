package com.rackspace.papi.service.threading.impl;

import com.rackspace.papi.service.context.ServiceContext;
import com.rackspace.papi.service.threading.ThreadingService;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.ServletContextEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Component;

//TODO:Refactor SRP Violation - Remove ThreadingService logic to external class
@Component("threadingServiceContext")
public class ThreadingServiceContext implements ServiceContext<ThreadingService>, ThreadingService {

    public static final String SERVICE_NAME = "powerapi:/kernel/threading";
    private final Set<WeakReference<Thread>> liveThreadReferences;

    public ThreadingServiceContext() {
        liveThreadReferences = new HashSet<WeakReference<Thread>>();
    }

    //TODO: find a way to move the contextDestroyed stuff into @PreDestroy if we need this
    //TODO: find a way to auto-register the service using spring somehow
    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public ThreadingService getService() {
        return this;
    }

    //TODO: make this crap go away...
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

    //TODO: this thing doesn't need to know about a servlet context at all!
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    //Oh right, the whole point of this :|
    @Override
    public Thread newThread(Runnable r, String name) {
        final Thread t = new Thread(r, name);
        liveThreadReferences.add(new WeakReference<Thread>(t));

        return t;
    }
}
