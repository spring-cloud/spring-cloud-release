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

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.util.FileSystemUtils;

public class GenerateReleaseTrainDocsTests {

	@Test
	void should_return_a_list_of_docs_modules_to_download() throws URISyntaxException {
		File testPom = new File(GenerateReleaseTrainDocsTests.class.getResource("/test/pom.xml").toURI());

		List<Project> projects = new GenerateReleaseTrainDocs().mavenPropertiesToDocsProjects(testPom);

		BDDAssertions.then(projects)
			.extracting("name")
			.containsOnly("spring-cloud-foo-bus", "spring-cloud-foo-build", "spring-cloud-foo-commons",
					"spring-cloud-foo-circuitbreaker", "spring-cloud-foo-config", "spring-cloud-foo-consul",
					"spring-cloud-foo-contract", "spring-cloud-foo-function", "spring-cloud-foo-gateway",
					"spring-cloud-foo-kubernetes", "spring-cloud-foo-netflix", "spring-cloud-foo-openfeign",
					"spring-cloud-foo-task", "spring-cloud-foo-vault", "spring-cloud-foo-zookeeper");
		BDDAssertions.then(projects).contains(new Project("spring-cloud-foo-bus", "2.2.3.RELEASE"));
	}

	@Test
	void should_generate_adocs_from_templates() {
		File file = new File("target/test-train-docs");
		FileSystemUtils.deleteRecursively(file);
		List<Project> projects = Arrays.asList(new Project("spring-cloud-foo-foo", "1.0.0"),
				new Project("spring-cloud-foo-bar", "2.0.0"), new Project("spring-boot", "3.0.0"),
				new Project("spring-cloud", "4.0.0"));

		File outputFolder = new TemplateGenerator(file).generate(projects);

		BDDAssertions.then(outputFolder).isNotEmptyDirectory();
	}

}
