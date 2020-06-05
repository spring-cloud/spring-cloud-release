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
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jknack.handlebars.Template;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * @author Marcin Grzejszczak
 */
class TemplateGenerator {

	static final File OUTPUT_FOLDER = new File("target/train-docs");

	TemplateGenerator() {
		OUTPUT_FOLDER.mkdirs();
	}

	File generate(List<Project> projects) {
		PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
		try {
			Resource[] resources = resourceLoader.getResources("templates/spring-cloud/*.hbs");
			for (Resource resource : resources) {
				File templateFile = resource.getFile();
				File outputFile = new File(OUTPUT_FOLDER, templateFile.getName().replace(".hbs", ".adoc"));
				Template template = template(templateFile.getName().replace(".hbs", ""));
				Map<String, Object> map = new HashMap<>();
				map.put("projects", projects);
				map.put("properties", new HashMap<>());
				String applied = template.apply(map);
				Files.write(outputFile.toPath(), applied.getBytes());
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return this.OUTPUT_FOLDER;
	}


	private Template template(String template) {
		return HandlebarsHelper.template(template);
	}

}
