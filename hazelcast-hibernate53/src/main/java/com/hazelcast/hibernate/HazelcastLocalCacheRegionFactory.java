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

package com.hazelcast.hibernate;

import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.hibernate.jmx.LocalCacheRegionStatus;
import com.hazelcast.hibernate.local.LocalRegionCache;
import com.hazelcast.hibernate.local.TimestampsRegionCache;
import com.hazelcast.logging.Logger;
import com.hazelcast.util.Clock;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.support.RegionNameQualifier;
import org.hibernate.cache.spi.support.StorageAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;

/**
 * Simple RegionFactory implementation to return Hazelcast based local Region implementations
 */
public class HazelcastLocalCacheRegionFactory extends AbstractHazelcastCacheRegionFactory {

    public static final String JMX_NAME_KEY = "hazelcast.local.region.jmx.name";

    LocalCacheRegionStatus mbean = null;

    public HazelcastLocalCacheRegionFactory() {
        Logger.getLogger(HazelcastLocalCacheRegionFactory.class).info("Creating factory.");
    }

    public HazelcastLocalCacheRegionFactory(final CacheKeysFactory cacheKeysFactory) {
        super(cacheKeysFactory);
    }

    public HazelcastLocalCacheRegionFactory(final HazelcastInstance instance) {
        super(instance);
    }

    protected void prepareForUse(final SessionFactoryOptions settings, final Map configValues) {
        super.prepareForUse(settings, configValues);
        //After prepare for use, do some extra steps:
        String jmxName = (String) configValues.get(JMX_NAME_KEY);
        if(jmxName != null){
            mbean = new LocalCacheRegionStatus(jmxName);
            mbean.registerMBean();
        }
    }

    @Override
    protected void releaseFromUse() {
        super.releaseFromUse();
        if(mbean != null){
            mbean.unregisterMBean();
        }
    }

    @Override
    protected RegionCache createRegionCache(final String unqualifiedRegionName,
                                            final SessionFactoryImplementor sessionFactory,
                                            final DomainDataRegionConfig regionConfig) {
        verifyStarted();
        assert !RegionNameQualifier.INSTANCE.isQualified(unqualifiedRegionName, sessionFactory.getSessionFactoryOptions());

        final String qualifiedRegionName = RegionNameQualifier.INSTANCE.qualify(
                unqualifiedRegionName,
                sessionFactory.getSessionFactoryOptions()
        );

        final LocalRegionCache regionCache = new LocalRegionCache(this, qualifiedRegionName, instance, regionConfig);
        cleanupService.registerCache(regionCache);
        if(mbean != null){
            mbean.registerCache(qualifiedRegionName, regionCache);
        }
        return regionCache;
    }

    @Override
    protected StorageAccess createQueryResultsRegionStorageAccess(final String regionName,
                                                                  final SessionFactoryImplementor sessionFactory) {
        HazelcastStorageAccessImpl storageAccess = (HazelcastStorageAccessImpl)
                super.createQueryResultsRegionStorageAccess(regionName, sessionFactory);
        if(mbean != null){
            storageAccess.getDelegate();
            mbean.registerQueryCache(storageAccess.getDelegate().getName(), storageAccess.getDelegate());
        }
        return storageAccess;
    }

    @Override
    protected RegionCache createTimestampsRegionCache(final String unqualifiedRegionName,
                                                      final SessionFactoryImplementor sessionFactory) {
        verifyStarted();
        assert !RegionNameQualifier.INSTANCE.isQualified(unqualifiedRegionName, sessionFactory.getSessionFactoryOptions());

        final String qualifiedRegionName = RegionNameQualifier.INSTANCE.qualify(
                unqualifiedRegionName,
                sessionFactory.getSessionFactoryOptions()
        );

        return new TimestampsRegionCache(this, qualifiedRegionName, instance);
    }

    public long nextTimestamp() {
         long result = instance == null ? Clock.currentTimeMillis()
                : HazelcastTimestamper.nextTimestamp(instance);
         return result;
    }

}
