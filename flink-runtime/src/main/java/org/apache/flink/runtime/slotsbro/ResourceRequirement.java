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

package org.apache.flink.runtime.slotsbro;

import org.apache.flink.runtime.clusterframework.types.ResourceProfile;

import java.io.Serializable;

/**
 * TODO: Add javadoc.
 */
public class ResourceRequirement implements Serializable {

	private static final long serialVersionUID = -928823805589379478L;

	// TODO: check what the TM slot offer contains if the reosurce profile is unknown
	private final ResourceProfile resourceProfile;

	private final int numberOfRequiredSlots;

	public ResourceRequirement(ResourceProfile resourceProfile, int numberOfRequiredSlots) {
		this.resourceProfile = resourceProfile;
		this.numberOfRequiredSlots = numberOfRequiredSlots;
	}

	public ResourceProfile getResourceProfile() {
		return resourceProfile;
	}

	public int getNumberOfRequiredSlots() {
		return numberOfRequiredSlots;
	}
}
