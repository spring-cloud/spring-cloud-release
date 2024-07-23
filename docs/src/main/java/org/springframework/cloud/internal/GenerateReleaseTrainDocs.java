/*
 * Copyright 2013-2020 the original author or authors.
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
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;

import static org.springframework.cloud.internal.Logger.info;

public class GenerateReleaseTrainDocs {

	public static void main(String... args) {
		File bomPath = new File(args[0]);
		File starterParentPath = new File(args[1]);
		File generatedTrainDocs = new File(args[2]);
		new GenerateReleaseTrainDocs().generate(bomPath, starterParentPath, generatedTrainDocs);
	}

	void generate(File bomPath, File starterParentPath, File generatedTrainDocs) {
		List<Project> projects = mavenPropertiesToDocsProjects(bomPath);
		info("Found the following projects [" + projects + "]");
		projects.add(springBootVersion(starterParentPath));
		projects.sort(Comparator.comparing(o -> o.name));
		File file = renderAsciidocTemplates(generatedTrainDocs, projects);
		info("Rendered docs templates to [" + file + "]");
	}

	List<Project> mavenPropertiesToDocsProjects(File file) {
		Model model = PomReader.readPom(file);
		Properties properties = model.getProperties();
		return properties.entrySet()
			.stream()
			.filter(e -> e.getKey().toString().endsWith(".version"))
			.map(e -> new Project(e.getKey().toString().replace(".version", ""), e.getValue().toString()))
			.collect(Collectors.toCollection(LinkedList::new));
	}

	Project springBootVersion(File file) {
		Model model = PomReader.readPom(file);
		return new Project("spring-boot", model.getParent().getVersion());
	}

	File renderAsciidocTemplates(File generatedTrainDocs, List<Project> projects) {
		TemplateGenerator templateGenerator = new TemplateGenerator(generatedTrainDocs);
		return templateGenerator.generate(projects);
	}

}
