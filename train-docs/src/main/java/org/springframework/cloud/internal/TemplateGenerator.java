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
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jknack.handlebars.Template;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static org.springframework.cloud.internal.Logger.info;

/**
 * @author Marcin Grzejszczak
 */
class TemplateGenerator {

	final File outputFolder;

	TemplateGenerator(File outputFolder) {
		this.outputFolder = outputFolder;
		this.outputFolder.mkdirs();
	}

	File generate(List<Project> projects,
			List<ConfigurationProperty> configurationProperties) {
		PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
		try {
			Resource[] resources = resourceLoader
					.getResources("templates/spring-cloud/*.hbs");
			List<TemplateProject> templateProjects = templateProjects(projects);
			for (Resource resource : resources) {
				File templateFile = resource.getFile();
				File outputFile = new File(outputFolder, renameTemplate(templateFile));
				Template template = template(templateFile.getName().replace(".hbs", ""));
				Map<String, Object> map = new HashMap<>();
				map.put("projects", projects);
				map.put("springCloudProjects", templateProjects);
				map.put("properties", configurationProperties);
				String applied = template.apply(map);
				Files.write(outputFile.toPath(), applied.getBytes());
				info("Successfully rendered [" + outputFile.getAbsolutePath() + "]");
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return outputFolder;
	}

	private List<TemplateProject> templateProjects(List<Project> projects) {
		return projects.stream()
				.filter(project -> project.name.startsWith("spring-cloud-"))
				.map(project -> {
					if (project.name.contains("spring-cloud-task")) {
						return new TemplateProject(project.name, project.version,
								"{basedir}/" + project.name
										+ "/spring-cloud-task-docs/src/main/asciidoc/index.adoc[leveloffset=+1]");
					}
					return new TemplateProject(project.name, project.version,
							"{basedir}/" + project.name + "/docs/src/main/asciidoc/"
									+ project.name + ".adoc[leveloffset=+1]");
				}).collect(Collectors.toCollection(LinkedList::new));
	}

	private String renameTemplate(File templateFile) {
		String templateName = templateFile.getName();
		if (templateName.endsWith("-pdf.hbs")) {
			return templateName.replace("-pdf.hbs", ".pdfadoc");
		}
		else if (templateName.endsWith("-single.hbs")) {
			return templateName.replace("-single.hbs", ".htmlsingleadoc");
		}
		return templateName.replace(".hbs", ".adoc");
	}

	private Template template(String template) {
		return HandlebarsHelper.template(template);
	}

}
