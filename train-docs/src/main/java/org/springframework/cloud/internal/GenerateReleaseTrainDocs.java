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
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.maven.model.Model;

import static org.springframework.cloud.internal.Logger.info;

public class GenerateReleaseTrainDocs {

	final ExecutorService service;

	public static void main(String... args) {
		File bomPath = new File(args[0]);
		File starterParentPath = new File(args[1]);
		String repoUrl = args[2];
		File unzippedDocs = new File(args[3]);
		File generatedTrainDocs = new File(args[4]);
		new GenerateReleaseTrainDocs().generate(bomPath, starterParentPath, repoUrl,
				unzippedDocs, generatedTrainDocs);
	}

	GenerateReleaseTrainDocs() {
		this.service = Executors.newFixedThreadPool(4);
	}

	void generate(File bomPath, File starterParentPath, String repoUrl, File unzippedDocs,
			File generatedTrainDocs) {
		List<Project> projects = mavenPropertiesToDocsProjects(bomPath);
		info("Found the following projects [" + projects + "]");
		List<File> outputFolders = downloadSources(unzippedDocs, projects, repoUrl);
		projects.add(springBootVersion(starterParentPath));
		projects.sort(Comparator.comparing(o -> o.name));
		List<ConfigurationProperty> configurationProperties = mergeConfigurationProperties(
				unzippedDocs);
		File file = renderAsciidocTemplates(generatedTrainDocs, projects,
				configurationProperties);
		info("Rendered docs templates to [" + file + "]");
		new ResourcesCopier().copy(outputFolders, file);
	}

	List<Project> mavenPropertiesToDocsProjects(File file) {
		Model model = PomReader.readPom(file);
		Properties properties = model.getProperties();
		// Due to issues with Stream docs in Hoxton we just remove spring cloud stream
		// from the docs
		return properties.entrySet().stream()
				.filter(e -> e.getKey().toString().endsWith(".version")
						&& !e.getKey().toString().contains("spring-cloud-stream"))
				.map(e -> new Project(e.getKey().toString().replace(".version", ""),
						e.getValue().toString()))
				.collect(Collectors.toCollection(LinkedList::new));
	}

	Project springBootVersion(File file) {
		Model model = PomReader.readPom(file);
		return new Project("spring-boot", model.getParent().getVersion());
	}

	List<File> downloadSources(File outputFolder, List<Project> projects,
			String repoUrl) {
		ArtifactFetcher fetcher = new ArtifactFetcher(outputFolder, repoUrl);
		try {
			List<Future<File>> futures = new LinkedList<>();
			for (Project project : projects) {
				futures.add(service.submit(() -> fetcher.unpackedDocs(project)));
			}
			List<File> files = futures.stream().map(future -> {
				try {
					return future.get(5, TimeUnit.MINUTES);
				}
				catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}).filter(Objects::nonNull).collect(Collectors.toList());
			info("Unpacked docs modules to the following directories [" + files + "]");
			return files;
		}
		finally {
			service.shutdown();
		}
	}

	List<ConfigurationProperty> mergeConfigurationProperties(File outputFolderWithDocs) {
		ConfigurationPropertiesAggregator aggregator = new ConfigurationPropertiesAggregator();
		return aggregator.mergedConfigurationProperties(outputFolderWithDocs.toPath());
	}

	File renderAsciidocTemplates(File generatedTrainDocs, List<Project> projects,
			List<ConfigurationProperty> configurationProperties) {
		TemplateGenerator templateGenerator = new TemplateGenerator(generatedTrainDocs);
		return templateGenerator.generate(projects, configurationProperties);
	}

}
