/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.internal;

import java.util.Objects;

class TemplateProject {

	final String name;

	final String version;

	final String antoraVersion;

	final String include;

	TemplateProject(String name, String version, String include) {
		this.name = name;
		this.version = version;
		this.include = include;
		this.antoraVersion = toAntora(version);
	}

	private String toAntora(String version) {
		// 4.1.0-M1
		// 4.1.0
		// 4.1.0-SNAPSHOT
		String[] split = version.split("\\.");
		boolean snapshot = version.contains("SNAPSHOT");
		String majorMinor = split[0] + "." + split[1]; // 4.1
		if (snapshot) {
			return majorMinor + "-SNAPSHOT";
		}
		return majorMinor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TemplateProject that = (TemplateProject) o;
		return Objects.equals(name, that.name) && Objects.equals(version, that.version)
				&& Objects.equals(antoraVersion, that.antoraVersion) && Objects.equals(include, that.include);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, version, antoraVersion, include);
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getInclude() {
		return include;
	}

	public String getAntoraVersion() {
		return antoraVersion;
	}

}
