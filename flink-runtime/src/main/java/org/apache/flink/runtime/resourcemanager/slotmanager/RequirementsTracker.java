/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.resourcemanager.slotmanager;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.clusterframework.types.ResourceProfile;
import org.apache.flink.runtime.slotsbro.ResourceRequirements;

/**
 * A tracker for {@link ResourceRequirements}.
 */
public interface RequirementsTracker {

	void processResourceRequirements(ResourceRequirements resourceRequirements);

	void notifyNewSlots();

	void notifySlotStatusChange(TaskManagerSlot.State previous, TaskManagerSlot.State current, JobID jobId, ResourceProfile resourceProfile);

	/**
	 * Removes all state from the tracker.
	 */
	void clear();
}
