/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.hibernate.jmx;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.hazelcast.hibernate.RegionCache;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;

import static com.hazelcast.internal.jmx.ManagementService.quote;

/**
 * This class implements a MBean to allow access to statistics of local region,
 * accessing to the LocalRegion entities that are registered in this class.
 * <p>
 * To avoid the overload caused for several requests, the value are calculated only if the second request comes not before 30 seconds after the first one.
 *
 * @author antonio.jimenez@summanetworks.com, created on 11/5/21.
 */
public class LocalCacheRegionStatus implements LocalCacheRegionStatusMBean {

    private static final ILogger log = Logger.getLogger(LocalCacheRegionStatus.class);
    public static final String TOTAL_ENTITIES_BY_REGIONS_MAP_KEY = "TOTAL_ENTITIES_BY_REGIONS";
    public static final String LIST_OF_REGIONS_KEY = "LIST_OF_REGIONS";
    public static final String LIST_OF_QUERIES_KEY = "LIST_OF_QUERIES";
    public static final String TOTAL_ENTITIES_KEY = "TOTAL_ENTITIES";
    public static final String TOTAL_ENTITIES_FOR_GIVEN_REGION_KEY = "TOTAL_ENTITIES_FOR_REGION_";
    public static final String TOTAL_QUERIES_RESULTS = "TOTAL_QUERIES_RESULTS";

    private ObjectName objectName;
    private static long REFRESH = 30_000;
    private Map<String, RegionCache> localEntitiesRegionCacheMap = new TreeMap<>();
    private Map<String, RegionCache> localQueriesRegionCacheMap = new TreeMap<>();
    private Map<String, Result> lastResultsMap = new HashMap<>();

    /**
     * Build MBean to allow access by JMX to the regions registered in it.
     *
     * @param name The name given to represent the MBean in JMX.
     */
    public LocalCacheRegionStatus(String name) {
        final Hashtable<String, String> properties = new Hashtable<>(2);
        properties.put("type", quote("LocalCacheRegion"));
        properties.put("name", quote("LocalCache-" + name));
        //properties.put("instance", quote(hazelcastInstance.getName()));
        try {
            objectName = new ObjectName("com.hazelcast.hibernate5", properties);
        } catch (MalformedObjectNameException e) {
            log.warning("Error building the ObjectName ", e);
            throw new IllegalArgumentException("Failed to create an ObjectName", e);
        }
    }

    /**
     * Register the MBean in the platform.
     */
    public void registerMBean() {
        log.info("Register MBean");
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            if (!server.isRegistered(objectName)) {
                server.registerMBean(this, objectName);
            }
            localEntitiesRegionCacheMap = new TreeMap<>();
            lastResultsMap = new HashMap<>();
        } catch (Exception e) {
            log.severe("Failed to register", e);
        }
    }

    /**
     * Unregister the MBean from the platform.
     */
    public void unregisterMBean() {
        log.info("Unregister MBean");
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            server.unregisterMBean(objectName);
        } catch (Exception e) {
            log.warning("Failed to unregister [{}]" + e.getMessage());
        } finally {
            localEntitiesRegionCacheMap.clear();
            localEntitiesRegionCacheMap = null;
            lastResultsMap.clear();
            lastResultsMap = null;
        }
    }

    /**
     * Register the given region cache to be accessible from JMX.
     *
     * @param region
     * @param cache
     */
    public void registerCache(String region, RegionCache cache) {
        if (localEntitiesRegionCacheMap != null) {
            localEntitiesRegionCacheMap.put(region, cache);
        }
    }

    public void registerQueryCache(String region, RegionCache cache) {
        if (localQueriesRegionCacheMap != null) {
            localQueriesRegionCacheMap.put(region, cache);
        }
    }

    /**
     * Total entities saved in cache. Result is saved for further requests, and updated after a TTL period.
     *
     * @return the int
     */
    @Override
    public long countAllEntities() {
        log.fine("totalEntities()");
        long result = 0;
        if (localEntitiesRegionCacheMap != null) {
            Result r = lastResultsMap.get(TOTAL_ENTITIES_KEY);
            if (r == null) {
                r = new Result();
                r.result = 0L;
                lastResultsMap.put(TOTAL_ENTITIES_KEY, r);
            }
            if ((r.timestamp + REFRESH) < System.currentTimeMillis()) {
                r.result = localEntitiesRegionCacheMap.entrySet().stream().mapToLong(value -> value.getValue().getElementCountInMemory()).sum();
                r.timestamp = System.currentTimeMillis();
            }
            result = (Long) r.result;
        }
        return result;
    }

    @Override
    public long countEntityRegions() {
        log.fine("totalRegions()");
        long result = localEntitiesRegionCacheMap != null ? localEntitiesRegionCacheMap.size() : 0;
        return result;
    }

    @Override
    public long countEntitiesForRegion(String regionName) {
        log.fine("totalEntitiesForRegion() Region-name: "+regionName);
        long result = -1;
        if (regionName != null && localEntitiesRegionCacheMap != null) {
            Result r = lastResultsMap.get(TOTAL_ENTITIES_FOR_GIVEN_REGION_KEY + regionName);
            if (r == null) {
                r = new Result();
                r.result = 0L;
                lastResultsMap.put(TOTAL_ENTITIES_FOR_GIVEN_REGION_KEY + regionName, r);
            }
            if ((r.timestamp + REFRESH) < System.currentTimeMillis()) {
                RegionCache cache = localEntitiesRegionCacheMap.get(regionName);
                r.result = cache.getElementCountInMemory();
                r.timestamp = System.currentTimeMillis();
            }
            result = (Long) r.result;
        }
        return result;
    }

    @Override
    public List<String> listEntityRegions() {
        log.fine("listRegions()");
        List<String> result = null;
        if (localEntitiesRegionCacheMap != null) {
            Result r = lastResultsMap.get(LIST_OF_REGIONS_KEY);
            if (r == null) {
                r = new Result();
                lastResultsMap.put(LIST_OF_REGIONS_KEY, r);
            }
            if ((r.timestamp + REFRESH) < System.currentTimeMillis()) {
                final List resultList = new ArrayList<String>(localEntitiesRegionCacheMap.size());
                localEntitiesRegionCacheMap.entrySet().stream().forEach(e -> {
                    resultList.add(e.getKey());
                });
                r.result = resultList;
                r.timestamp = System.currentTimeMillis();
            }
            result = (List<String>) r.result;
        }
        return result;
    }

    @Override
    public Map<String, Long> mapEntitiesByRegion() {
        log.fine("mapEntitiesByRegion()");
        Map<String, Long> result = null;
        if (localEntitiesRegionCacheMap != null) {
            Result r = lastResultsMap.get(TOTAL_ENTITIES_BY_REGIONS_MAP_KEY);
            if (r == null) {
                r = new Result();
                lastResultsMap.put(TOTAL_ENTITIES_BY_REGIONS_MAP_KEY, r);
            }
            if ((r.timestamp + REFRESH) < System.currentTimeMillis()) {
                final Map map = new HashMap<String, Long>(localEntitiesRegionCacheMap.size());
                localEntitiesRegionCacheMap.entrySet().stream().forEach(e -> {
                    map.put(e.getKey(), e.getValue().getElementCountInMemory());
                });
                r.result = map;
                r.timestamp = System.currentTimeMillis();
            }
            result = (HashMap<String, Long>) r.result;
        }

        return result;
    }

    @Override
    public long evictCacheOfRegion(String localRegion) {
        log.fine("cleanCacheOfRegion(): Region: "+localRegion);
        long count = -1;
        if(localRegion != null && localEntitiesRegionCacheMap != null) {
            RegionCache cache = localEntitiesRegionCacheMap.get(localRegion);
            if (cache != null) {
                count = cache.getElementCountInMemory();
                cache.evictData();
                //Only remove specific result. Total and MAP are refresh in later call if TTL expired.
                lastResultsMap.remove(TOTAL_ENTITIES_FOR_GIVEN_REGION_KEY + localRegion);
            }
        }
        return count;
    }

    @Override
    public long countQueryRegions() {
        log.fine("totalQueriesRegions()");
        long result = localQueriesRegionCacheMap != null ? localQueriesRegionCacheMap.size() : 0;
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * There is usually one region for cache, but if the regions is explored for more entries.
     */
    @Override
    public List<String> listQueryRegions() {
        log.fine("listQueriesRegions()");
        List<String> result = null;
        if (localQueriesRegionCacheMap != null) {
            Result r = lastResultsMap.get(LIST_OF_QUERIES_KEY);
            if (r == null) {
                r = new Result();
                lastResultsMap.put(LIST_OF_QUERIES_KEY, r);
            }
            if ((r.timestamp + REFRESH) < System.currentTimeMillis()) {
                final List resultList = new ArrayList<String>(localQueriesRegionCacheMap.size());
                localQueriesRegionCacheMap.entrySet().stream().forEach(e -> {
                    resultList.add(e.getKey());
                });
                r.result = resultList;
                r.timestamp = System.currentTimeMillis();
            }
            result = (List<String>) r.result;
        }
        return result;
    }

    @Override
    public long countAllQueries() {
        log.fine("totalQueries()");
        long result = 0;
        if (localQueriesRegionCacheMap != null) {
            Result r = lastResultsMap.get(TOTAL_QUERIES_RESULTS);
            if (r == null) {
                r = new Result();
                r.result = 0L;
                lastResultsMap.put(TOTAL_QUERIES_RESULTS, r);
            }
            if ((r.timestamp + REFRESH) < System.currentTimeMillis()) {
                r.result = localQueriesRegionCacheMap.entrySet().stream().mapToLong(value -> value.getValue().getElementCountInMemory()).sum();
                r.timestamp = System.currentTimeMillis();
            }
            result = (Long) r.result;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * TODO: Could be good to add a refresh time
     */
    @Override
    public long evictCacheOfAllQueries() {
        log.fine("cleanAllQueryCache()");
        long count = 0;
        for (RegionCache rc : localQueriesRegionCacheMap.values()) {
            count += rc.getElementCountInMemory();
            rc.evictData();
        }
        return count;
    }


    class Result {
        Object result = null;
        long timestamp = 0;
    }

}


