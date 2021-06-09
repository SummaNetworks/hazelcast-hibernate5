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

import java.util.List;
import java.util.Map;

/**
 * The interface for the MBean that allow check the status of local cache regions.
 *
 * @author antonio.jimenez@summanetworks.com, created on 11/5/21.
 */
public interface LocalCacheRegionStatusMBean {

    /**
     * Return the total entities count.
     *
     * Result is saved for further requests, and updated after a TTL period.
     *
     * @return the int
     */
    long countAllEntities();

    /**
     * Get the number of regions saved in cache.
     *
     * Result is saved for further requests, and updated after a TTL period.
     *
     * @return the int
     */
    long countEntityRegions();

    /**
     * List the regions for entities that are registered in the local cache.
     *
     * Result is saved for further requests, and updated after a TTL period.
     *
     * @return
     */
    List<String> listEntityRegions();

    /**
     * Calculate and return the total entities for the given region.
     *
     * Result is saved for further requests, and updated after a TTL period.
     *
     * @param regionName the region name
     * @return the int
     */
    long countEntitiesForRegion(String regionName);

    /**
     * Usually there is only one: 'default-query-results-region' so this method will return always 1.
     *
     * @return
     */
    long countQueryRegions();

    /**
     * List of all queries name that are cached.
     * Usually there is only one 'default-query-results-region'
     *
     * Result is saved for further requests, and updated after a TTL period.
     *
     * @return
     */
    List<String> listQueryRegions();

    /**
     * Return the number of result saved in the query cache.
     *
     * Result is saved for further requests, and updated after a TTL period.
     *
     * @return the number of queries in the cache.
     */
    long countAllQueries();

    /**
     * Total entities by region in a Map<String,Long>
     *
     * Result is saved for further requests, and updated after a TTL period.
     * @return the map with region-number of entities.
     */
    Map<String, Long> mapEntitiesByRegion();

    /**
     * Clean the given local region and return the element that it contains at the moment of remove.
     *
     * @param entityRegion
     * @return
     */
    long evictCacheOfRegion(String entityRegion);

    /**
     * Clean the cache for the queries of given cache region.
     *
     * @param queryRegion the region name of query cache.
     * @return the number of results removed.
     */
    long evictCacheOfQueryRegion(String queryRegion);
}
