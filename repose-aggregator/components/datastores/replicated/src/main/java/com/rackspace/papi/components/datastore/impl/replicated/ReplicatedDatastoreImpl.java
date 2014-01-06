package com.rackspace.papi.components.datastore.impl.replicated;

import com.rackspace.papi.components.datastore.Datastore;
import com.rackspace.papi.components.datastore.DatastoreOperationException;
import com.rackspace.papi.components.datastore.impl.replicated.data.Operation;
import com.rackspace.papi.components.datastore.impl.replicated.data.Subscriber;
import com.rackspace.papi.components.datastore.impl.replicated.notification.in.ChannelledUpdateListener;
import com.rackspace.papi.components.datastore.impl.replicated.notification.out.UpdateNotifier;
import com.rackspace.papi.components.datastore.impl.replicated.subscriptions.UdpSubscriptionListener;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ReplicatedDatastoreImpl implements Datastore, ReplicatedDatastore, NotifiableDatastore {

    private static final Logger LOG = LoggerFactory.getLogger(ReplicatedDatastoreImpl.class);
    private final Cache cache;
    private final SubscriptionListener subscriptionListener;
    private final Thread subscriberThread;
    private final UpdateListener updateListener;
    private final Thread updateListenerThread;
    private final Notifier updateNotifier;

    public ReplicatedDatastoreImpl(String subscriptionAddress, int subscriptionPort, Cache ehCacheInstance)
            throws IOException {
        this(null, subscriptionAddress, subscriptionPort, ehCacheInstance, 0);
    }

    public ReplicatedDatastoreImpl(Set<Subscriber> subscribers, String address, int subscriptionPort,
            Cache ehCacheInstance, int maxQueueSize) throws IOException {
        LOG.info("Replicated Datastore Socket (udp): " + address + ":" + subscriptionPort);
        this.cache = ehCacheInstance;
        this.updateNotifier = new UpdateNotifier(subscribers, maxQueueSize);
        this.subscriptionListener = new UdpSubscriptionListener(this, updateNotifier, address, subscriptionPort);
        this.updateListener = new ChannelledUpdateListener(this, address);
        this.subscriberThread = new Thread((Runnable) subscriptionListener);
        this.updateListenerThread = new Thread(updateListener);
    }

    Notifier getUpdateNotifier() {
        return updateNotifier;
    }

    @Override
    public String getName() {
        return ReplicatedCacheDatastoreManager.REPLICATED_DISTRIBUTED;
    }

    @Override
    public void joinGroup() {
        updateNotifier.startNotifications();
        updateListenerThread.start();
        subscriberThread.start();
        subscriptionListener.join(updateListener.getAddress(), updateListener.getPort());
    }

    @Override
    public void leaveGroup() {
        updateListener.done();
        subscriptionListener.unsubscribe();
        updateListenerThread.interrupt();
        subscriberThread.interrupt();
        updateNotifier.stopNotifications();
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        updateNotifier.addSubscriber(subscriber);
    }

    @Override
    public void addSubscribers(Collection<Subscriber> subscribers) {
        updateNotifier.addSubscribers(subscribers);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        updateNotifier.removeSubscriber(subscriber);
    }

    @Override
    protected void finalize() throws Throwable {
        updateListener.done();
        super.finalize();
    }

    @Override
    public void sync(Subscriber subscriber) throws IOException {
        cache.evictExpiredElements();
        Map<Object, Element> all = cache.getAll(cache.getKeysWithExpiryCheck());

        if (all.isEmpty()) {
            return;
        }

        Set<Object> keySet = all.keySet();
        Collection<Element> values = all.values();

        Operation[] operation = new Operation[keySet.size()];
        String[] keys = new String[keySet.size()];
        byte[][] data = new byte[values.size()][];
        int[] ttl = new int[values.size()];

        int index = 0;
        for (Object key : keySet) {
            keys[index++] = (String) key;
            operation[index] = Operation.PUT;
        }

        index = 0;

        for (Element element : values) {

            data[index] = (byte[]) element.getValue();
            ttl[index] = element.getTimeToLive();
            index++;
        }

        updateNotifier.notifyNode(operation, subscriber, keys, data, ttl);

    }

    @Override
    public Serializable get(String key) throws DatastoreOperationException {
        final Element element = cache.get(key);

        if (element != null) {
            return element.getValue();
        }

        return null;
    }

    @Override
    public void put(String key, Serializable value) throws DatastoreOperationException {
        put(key, value, true);
    }

    @Override
    public void put(String key, Serializable value, boolean notify) throws DatastoreOperationException {
        try {
            cache.put(new Element(key, value));
            if (notify) {
                updateNotifier.notifyAllNodes(Operation.PUT, key, value);
            }
        } catch (IOException ex) {
            throw new DatastoreOperationException("Error adding key: " + key, ex);
        }
    }

    @Override
    public void put(String key, Serializable value, int ttl, TimeUnit timeUnit) throws DatastoreOperationException {
        put(key, value, ttl, timeUnit, true);
    }

    @Override
    public void put(String key, Serializable value, int ttl, TimeUnit timeUnit, boolean notify)
            throws DatastoreOperationException {
        try {
            final Element putMe = new Element(key, value);
            long convertedTtl = TimeUnit.SECONDS.convert(ttl, timeUnit);
            if (convertedTtl > Integer.MAX_VALUE) {
                convertedTtl = Integer.MAX_VALUE;
            }

            int seconds = (int) convertedTtl;
            putMe.setTimeToLive(seconds);
            cache.put(putMe);
            if (notify) {
                updateNotifier.notifyAllNodes(Operation.PUT, key, value, seconds);
            }
        } catch (IOException ex) {
            throw new DatastoreOperationException("Error adding key: " + key, ex);
        }
    }

    @Override
    public void patch(String key, byte[] value) throws DatastoreOperationException {
        throw new UnsupportedOperationException("PATCH is unsupported for " + this.getName());
    }

    @Override
    public void patch(String key, byte[] value, int ttl, TimeUnit timeUnit) throws DatastoreOperationException {
        throw new UnsupportedOperationException("PATCH is unsupported for " + this.getName());
    }

    @Override
    public boolean remove(String key) throws DatastoreOperationException {
        return remove(key, true);
    }

    @Override
    public boolean remove(String key, boolean notify) throws DatastoreOperationException {
        try {
            boolean result = cache.remove(key);
            if (notify) {
                updateNotifier.notifyAllNodes(Operation.REMOVE, key);
            }
            return result;
        } catch (IOException ex) {
            throw new DatastoreOperationException("Error removing key: " + key, ex);
        }
    }

    @Override
    public void removeAll() {
        cache.removeAll();
    }
}
