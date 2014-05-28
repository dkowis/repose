package com.rackspace.papi.service.event;

import com.rackspace.papi.commons.util.Destroyable;
import com.rackspace.papi.commons.util.thread.DestroyableThreadWrapper;
import com.rackspace.papi.service.event.common.EventDispatcher;
import com.rackspace.papi.service.event.common.EventService;
import com.rackspace.papi.service.threading.ThreadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

@Component("powerProxyEventKernel")
public class PowerProxyEventKernel implements Runnable, Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(PowerProxyEventKernel.class);
    private volatile boolean shouldContinue;
    private DestroyableThreadWrapper eventKernelThread;

    private final EventService eventManager;
    private final ThreadingService threadingService;

    @Inject
    public PowerProxyEventKernel(
            EventService eventManager,
            ThreadingService threadingService
    ){
        this.threadingService = threadingService;
        this.eventManager = eventManager;
    }

    @PostConstruct
    public void startThisMadness() {
        eventKernelThread = new DestroyableThreadWrapper(threadingService.newThread(this, "Event Kernel Thread"), this);
        eventKernelThread.start();
    }

    @PreDestroy
    public void maybeStopThisMadness() {
        eventKernelThread.destroy();
    }

    @Override
    public void run() {
        shouldContinue = true;

        try {
            while (shouldContinue) {
                final EventDispatcher dispatcher = eventManager.nextDispatcher();
                
                if (LOG.isDebugEnabled()) {
                    final Enum eventType = dispatcher.getEvent().type();
                    
                    LOG.debug("Dispatching event: " + eventType.getClass().getSimpleName() + "." + eventType.name());
                }
                
                try {
                    dispatcher.dispatch();
                } catch (Exception ex) {
                    LOG.error("Exception caught while dispatching event, \""
                            + dispatcher.getEvent().type().getClass().getSimpleName() + "$" + dispatcher.getEvent().type().name()
                            + "\" - Reason: " + ex.getMessage(), ex);
                }
            }
        } catch (InterruptedException ie) {
            LOG.warn("Event kernel received an interrupt. Exiting event kernel loop.", ie);
            shouldContinue = false;

            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() {
        shouldContinue = false;
    }
}
