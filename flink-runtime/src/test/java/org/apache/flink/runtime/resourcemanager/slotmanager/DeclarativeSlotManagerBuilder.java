/*
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

package org.apache.flink.runtime.resourcemanager.slotmanager;

import org.apache.flink.api.common.time.Time;
import org.apache.flink.configuration.ResourceManagerOptions;
import org.apache.flink.runtime.concurrent.Executors;
import org.apache.flink.runtime.concurrent.ScheduledExecutor;
import org.apache.flink.runtime.metrics.groups.SlotManagerMetricGroup;
import org.apache.flink.runtime.metrics.groups.UnregisteredMetricGroups;
import org.apache.flink.runtime.resourcemanager.ResourceManagerId;
import org.apache.flink.runtime.resourcemanager.WorkerResourceSpec;
import org.apache.flink.runtime.testingUtils.TestingUtils;

/** Builder for {@link DeclarativeSlotManagerImpl}. */
public class DeclarativeSlotManagerBuilder {
	private SlotMatchingStrategy slotMatchingStrategy;
	private ScheduledExecutor scheduledExecutor;
	private Time taskManagerRequestTimeout;
	private Time slotRequestTimeout;
	private Time taskManagerTimeout;
	private boolean waitResultConsumedBeforeRelease;
	private WorkerResourceSpec defaultWorkerResourceSpec;
	private int numSlotsPerWorker;
	private SlotManagerMetricGroup slotManagerMetricGroup;
	private int maxSlotNum;
	private int redundantTaskManagerNum;

	private DeclarativeSlotManagerBuilder() {
		this.slotMatchingStrategy = AnyMatchingSlotMatchingStrategy.INSTANCE;
		this.scheduledExecutor = TestingUtils.defaultScheduledExecutor();
		this.taskManagerRequestTimeout = TestingUtils.infiniteTime();
		this.slotRequestTimeout = TestingUtils.infiniteTime();
		this.taskManagerTimeout = TestingUtils.infiniteTime();
		this.waitResultConsumedBeforeRelease = true;
		this.defaultWorkerResourceSpec = WorkerResourceSpec.ZERO;
		this.numSlotsPerWorker = 1;
		this.slotManagerMetricGroup = UnregisteredMetricGroups.createUnregisteredSlotManagerMetricGroup();
		this.maxSlotNum = ResourceManagerOptions.MAX_SLOT_NUM.defaultValue();
		this.redundantTaskManagerNum = ResourceManagerOptions.REDUNDANT_TASK_MANAGER_NUM.defaultValue();
	}

	public static DeclarativeSlotManagerBuilder newBuilder() {
		return new DeclarativeSlotManagerBuilder();
	}

	public DeclarativeSlotManagerBuilder setScheduledExecutor(ScheduledExecutor scheduledExecutor) {
		this.scheduledExecutor = scheduledExecutor;
		return this;
	}

	public DeclarativeSlotManagerBuilder setTaskManagerRequestTimeout(Time taskManagerRequestTimeout) {
		this.taskManagerRequestTimeout = taskManagerRequestTimeout;
		return this;
	}

	public DeclarativeSlotManagerBuilder setSlotRequestTimeout(Time slotRequestTimeout) {
		this.slotRequestTimeout = slotRequestTimeout;
		return this;
	}

	public DeclarativeSlotManagerBuilder setTaskManagerTimeout(Time taskManagerTimeout) {
		this.taskManagerTimeout = taskManagerTimeout;
		return this;
	}

	public DeclarativeSlotManagerBuilder setWaitResultConsumedBeforeRelease(boolean waitResultConsumedBeforeRelease) {
		this.waitResultConsumedBeforeRelease = waitResultConsumedBeforeRelease;
		return this;
	}

	public DeclarativeSlotManagerBuilder setSlotMatchingStrategy(SlotMatchingStrategy slotMatchingStrategy) {
		this.slotMatchingStrategy = slotMatchingStrategy;
		return this;
	}

	public DeclarativeSlotManagerBuilder setDefaultWorkerResourceSpec(WorkerResourceSpec defaultWorkerResourceSpec) {
		this.defaultWorkerResourceSpec = defaultWorkerResourceSpec;
		return this;
	}

	public DeclarativeSlotManagerBuilder setNumSlotsPerWorker(int numSlotsPerWorker) {
		this.numSlotsPerWorker = numSlotsPerWorker;
		return this;
	}

	public DeclarativeSlotManagerBuilder setSlotManagerMetricGroup(SlotManagerMetricGroup slotManagerMetricGroup) {
		this.slotManagerMetricGroup = slotManagerMetricGroup;
		return this;
	}

	public DeclarativeSlotManagerBuilder setMaxSlotNum(int maxSlotNum) {
		this.maxSlotNum = maxSlotNum;
		return this;
	}

	public DeclarativeSlotManagerBuilder setRedundantTaskManagerNum(int redundantTaskManagerNum) {
		this.redundantTaskManagerNum = redundantTaskManagerNum;
		return this;
	}

	public DeclarativeSlotManagerImpl build() {
		final SlotManagerConfiguration slotManagerConfiguration = new SlotManagerConfiguration(
			taskManagerRequestTimeout,
			slotRequestTimeout,
			taskManagerTimeout,
			waitResultConsumedBeforeRelease,
			slotMatchingStrategy,
			defaultWorkerResourceSpec,
			numSlotsPerWorker,
			maxSlotNum,
			redundantTaskManagerNum);

		return new DeclarativeSlotManagerImpl(
			scheduledExecutor,
			slotManagerConfiguration,
			slotManagerMetricGroup);
	}

	public DeclarativeSlotManagerImpl buildAndStartWithDirectExec(ResourceManagerId resourceManagerId, ResourceActions resourceManagerActions) {
		final DeclarativeSlotManagerImpl slotManager = build();
		slotManager.start(resourceManagerId, Executors.directExecutor(), resourceManagerActions);
		return slotManager;
	}
}