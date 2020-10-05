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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.StringUtils;

class ConfigurationPropertiesAggregator {

	private static final List<String> wordsToIgnore = Arrays.asList("|===", "|Name | Default | Description");

	List<ConfigurationProperty> mergedConfigurationProperties(Path unpackedDocs) {
		try {
			return Files.walk(unpackedDocs).filter(path -> path.endsWith("_configprops.adoc")).flatMap(path -> {
				try {
					return Files.readAllLines(path).stream()
							.filter(s -> !StringUtils.isEmpty(s) && !wordsToIgnore.contains(s)).map(s -> {
								// |foo|bar|baz -> foo|bar|baz -> split ->
								// foo,bar,baz
								String[] strings = s.substring(1).split("\\|");
								return new ConfigurationProperty(strings[0].trim(), strings[1].trim(),
										strings[2].trim());
							});
				}
				catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}).sorted(Comparator.comparing(o -> o.name)).collect(Collectors.toList());
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
