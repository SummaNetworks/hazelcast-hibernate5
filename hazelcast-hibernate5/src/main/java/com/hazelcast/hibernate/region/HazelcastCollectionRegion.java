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

package com.hazelcast.hibernate.region;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.RegionCache;
import com.hazelcast.hibernate.access.NonStrictReadWriteAccessDelegate;
import com.hazelcast.hibernate.access.ReadOnlyAccessDelegate;
import com.hazelcast.hibernate.access.ReadWriteAccessDelegate;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;

import java.util.Properties;

/**
 * An collection region implementation based upon Hazelcast IMap with basic concurrency / transactional support
 * by supplying CollectionRegionAccessStrategy
 *
 * @param <Cache> implementation type of RegionCache
 */
public final class HazelcastCollectionRegion<Cache extends RegionCache> extends AbstractTransactionalDataRegion<Cache>
        implements CollectionRegion {

    public HazelcastCollectionRegion(final HazelcastInstance instance,
                                     final String regionName, final Properties props,
                                     final CacheDataDescription metadata, final Cache cache) {
        super(instance, regionName, props, metadata, cache);
    }

    @Override
    public CollectionRegionAccessStrategy buildAccessStrategy(final AccessType accessType) throws CacheException {
        if (AccessType.READ_ONLY.equals(accessType)) {
            return new CollectionRegionAccessStrategyAdapter(
                    new ReadOnlyAccessDelegate<HazelcastCollectionRegion>(this, props));
        }
        if (AccessType.NONSTRICT_READ_WRITE.equals(accessType)) {
            return new CollectionRegionAccessStrategyAdapter(
                    new NonStrictReadWriteAccessDelegate<HazelcastCollectionRegion>(this, props));
        }
        if (AccessType.READ_WRITE.equals(accessType)) {
            return new CollectionRegionAccessStrategyAdapter(
                    new ReadWriteAccessDelegate<HazelcastCollectionRegion>(this, props));
        }
        throw new CacheException("AccessType \"" + accessType + "\" is not currently supported by Hazelcast.");
    }
}
