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

package org.apache.flink.runtime.scheduler.declarative;

import org.apache.flink.runtime.jobmanager.scheduler.Locality;
import org.apache.flink.runtime.jobmaster.LogicalSlot;
import org.apache.flink.runtime.jobmaster.SlotOwner;
import org.apache.flink.runtime.jobmaster.SlotRequestId;
import org.apache.flink.runtime.jobmaster.slotpool.PhysicalSlot;
import org.apache.flink.runtime.jobmaster.slotpool.SingleLogicalSlot;
import org.apache.flink.util.Preconditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Shared slot implementation for the {@link DeclarativeScheduler}. */
public class SharedSlot implements SlotOwner, PhysicalSlot.Payload {
    private static final Logger LOG = LoggerFactory.getLogger(SharedSlot.class);

    private final SlotRequestId physicalSlotRequestId;

    private final PhysicalSlot physicalSlot;

    private final Runnable externalReleaseCallback;

    private final Map<SlotRequestId, LogicalSlot> allocatedLogicalSlots;

    private final boolean slotWillBeOccupiedIndefinitely;

    private State state;

    public SharedSlot(
            SlotRequestId physicalSlotRequestId,
            PhysicalSlot physicalSlot,
            boolean slotWillBeOccupiedIndefinitely,
            Runnable externalReleaseCallback) {
        this.physicalSlotRequestId = physicalSlotRequestId;
        this.physicalSlot = physicalSlot;
        this.slotWillBeOccupiedIndefinitely = slotWillBeOccupiedIndefinitely;
        this.externalReleaseCallback = externalReleaseCallback;
        this.state = State.ALLOCATED;
        this.allocatedLogicalSlots = new HashMap<>();
        physicalSlot.tryAssignPayload(this);
    }

    /**
     * Registers an allocation request for a logical slot.
     *
     * <p>The logical slot request is complete once the underlying physical slot request is
     * complete.
     *
     * @return the logical slot
     */
    public LogicalSlot allocateLogicalSlot() {
        LOG.debug("Allocating logical slot from shared slot ({})", physicalSlotRequestId);
        final LogicalSlot slot =
                new SingleLogicalSlot(
                        new SlotRequestId(),
                        physicalSlot,
                        null,
                        Locality.UNKNOWN,
                        this,
                        slotWillBeOccupiedIndefinitely);

        allocatedLogicalSlots.put(slot.getSlotRequestId(), slot);
        return slot;
    }

    @Override
    public void returnLogicalSlot(LogicalSlot logicalSlot) {
        LOG.debug("Returning logical slot to shared slot ({})", physicalSlotRequestId);
        Preconditions.checkState(
                allocatedLogicalSlots.remove(logicalSlot.getSlotRequestId()) != null,
                "Trying to remove a logical slot request which has been either already removed or never created.");
        tryReleaseExternally();
    }

    @Override
    public void release(Throwable cause) {
        LOG.debug("Release shared slot ({})", physicalSlotRequestId);

        // copy the logical slot collection to avoid ConcurrentModificationException
        // if logical slot releases cause cancellation of other executions
        // which will try to call returnLogicalSlot and modify requestedLogicalSlots collection
        final List<LogicalSlot> collect = new ArrayList<>(allocatedLogicalSlots.values());
        for (LogicalSlot allocatedLogicalSlot : collect) {
            allocatedLogicalSlot.releaseSlot(cause);
        }
        allocatedLogicalSlots.clear();
        tryReleaseExternally();
    }

    private void tryReleaseExternally() {
        if (state != State.RELEASED && allocatedLogicalSlots.isEmpty()) {
            state = State.RELEASED;
            LOG.debug("Release shared slot externally ({})", physicalSlotRequestId);
            externalReleaseCallback.run();
        }
    }

    @Override
    public boolean willOccupySlotIndefinitely() {
        return slotWillBeOccupiedIndefinitely;
    }

    private enum State {
        ALLOCATED,
        RELEASED
    }
}