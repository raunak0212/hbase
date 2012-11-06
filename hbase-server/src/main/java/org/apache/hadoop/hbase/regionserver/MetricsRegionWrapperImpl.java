/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hbase.regionserver;

import org.apache.hadoop.hbase.CompatibilitySingletonFactory;
import org.apache.hadoop.metrics2.MetricsExecutor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MetricsRegionWrapperImpl implements MetricsRegionWrapper {

  public static final int PERIOD = 45;

  private final HRegion region;
  private ScheduledExecutorService executor;
  private Runnable runnable;
  private long numStoreFiles;
  private long memstoreSize;
  private long storeFileSize;

  public MetricsRegionWrapperImpl(HRegion region) {
    this.region = region;
    this.executor = CompatibilitySingletonFactory.getInstance(MetricsExecutor.class).getExecutor();
    this.runnable = new HRegionMetricsWrapperRunnable();
    this.executor.scheduleWithFixedDelay(this.runnable, PERIOD, PERIOD, TimeUnit.SECONDS);
  }

  @Override
  public String getTableName() {
    return this.region.getTableDesc().getNameAsString();
  }

  @Override
  public String getRegionName() {
    return this.region.getRegionInfo().getEncodedName();
  }

  @Override
  public long getNumStores() {
    return this.region.stores.size();
  }

  @Override
  public long getNumStoreFiles() {
    return numStoreFiles;
  }

  @Override
  public long getMemstoreSize() {
    return memstoreSize;
  }

  @Override
  public long getStoreFileSize() {
    return storeFileSize;
  }

  @Override
  public long getReadRequestCount() {
    return this.region.getReadRequestsCount();
  }

  @Override
  public long getWriteRequestCount() {
    return this.region.getWriteRequestsCount();
  }

  public class HRegionMetricsWrapperRunnable implements Runnable {

    @Override
    public void run() {
      long tempNumStoreFiles = 0;
      long tempMemstoreSize = 0;
      long tempStoreFileSize = 0;

      for (Store store : region.stores.values()) {
        tempNumStoreFiles += store.getStorefilesCount();
        tempMemstoreSize += store.getMemStoreSize();
        tempStoreFileSize += store.getStorefilesSize();
      }

      numStoreFiles = tempNumStoreFiles;
      memstoreSize = tempMemstoreSize;
      storeFileSize = tempStoreFileSize;
    }
  }

}