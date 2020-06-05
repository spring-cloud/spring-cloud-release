/*
 * Copyright 2013-2019 the original author or authors.
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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.util.FileSystemUtils;

public class SomethingTest {

	@Test
	void should_return_a_list_of_docs_modules_to_download() throws URISyntaxException {
		File testPom = new File(SomethingTest.class.getResource("/test/pom.xml").toURI());

		List<Project> projects = new GenerateReleaseTrainDocs().toDocsList(testPom);

		BDDAssertions.then(projects)
				.extracting("name")
				.containsOnly("spring-cloud-bus-docs", "spring-cloud-build-docs",
				"spring-cloud-cloudfoundry-docs", "spring-cloud-commons-docs", "spring-cloud-circuitbreaker-docs",
				"spring-cloud-config-docs", "spring-cloud-consul-docs", "spring-cloud-contract-docs",
				"spring-cloud-function-docs", "spring-cloud-gateway-docs", "spring-cloud-kubernetes-docs",
				"spring-cloud-netflix-docs", "spring-cloud-openfeign-docs", "spring-cloud-security-docs",
				"spring-cloud-sleuth-docs", "spring-cloud-stream-docs", "spring-cloud-task-docs",
				"spring-cloud-vault-docs", "spring-cloud-zookeeper-docs", "spring-cloud-cli-docs");
		BDDAssertions.then(projects).contains(new Project("spring-cloud-bus-docs", "1.2.3-SNAPSHOT"));
	}

	@Test
	void should_unpack_starters_docs() throws URISyntaxException {
		File testPom = new File(SomethingTest.class.getResource("/test/sleuth-only.xml").toURI());
		String remoteRepo = "https://repo.spring.io/libs-snapshot-local/";
		List<Project> projects = new GenerateReleaseTrainDocs().toDocsList(testPom);

		List<File> files = new GenerateReleaseTrainDocs().downloadDocsModules(projects, "https://repo.spring.io/libs-snapshot-local/");

		BDDAssertions.then(files)
			.isNotEmpty();
	}

	@Test
	void should_generate_the_single_pdf_multi_adoc_pages() {

	}

	@Test
	void should_generate_adocs_from_templates() {
		FileSystemUtils.deleteRecursively(TemplateGenerator.OUTPUT_FOLDER);
		List<Project> projects = Arrays.asList(new Project("spring-cloud-foo", "1.0.0"), new Project("spring-cloud-bar", "2.0.0"));

		File outputFolder = new TemplateGenerator().generate(projects);

		BDDAssertions.then(outputFolder).isNotEmptyDirectory();
	}

}
