/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.jobmaster.slotpool;

import org.apache.flink.api.common.time.Time;
import org.apache.flink.runtime.slots.ResourceRequirement;

import java.util.Collection;
import java.util.function.Consumer;

public class DefaultDeclarativeSlotPoolNgFactory implements DeclarativeSlotPoolNgFactory {

	@Override
	public DeclarativeSlotPoolNg create(
			Consumer<? super Collection<ResourceRequirement>> notifyNewResourceRequirements,
			Consumer<? super Collection<? extends PhysicalSlot>> notifyNewSlots,
			Time idleSlotTimeout,
			Time rpcTimeout) {
		return new DefaultDeclarativeSlotPoolNg(
			new DefaultAllocatedSlotPool(),
			notifyNewResourceRequirements,
			notifyNewSlots,
			idleSlotTimeout,
			rpcTimeout);
	}
}
