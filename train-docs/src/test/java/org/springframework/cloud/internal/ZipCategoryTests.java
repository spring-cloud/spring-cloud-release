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
import java.nio.file.Files;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ZipCategoryTests {

	@Test
	void should_unzip_a_file_to_the_specified_location() throws Exception {
		// given:
		File zipFile = new File(ZipCategoryTests.class.getClassLoader().getResource("file.zip").toURI());
		File tempDir = Files.createTempDirectory("foo").toFile();
		tempDir.deleteOnExit();
		// when:
		ZipCategory.unzipTo(zipFile, tempDir);
		// then:
		BDDAssertions.then(tempDir.listFiles())
				.hasOnlyOneElementSatisfying(file -> {
					BDDAssertions.then(file).hasName("file.txt")
							.hasContent("test");
				});
	}

	@Test
	void should_not_allow_malicious_traversal() throws Exception {
		// given:
		File zipFile = new File(ZipCategoryTests.class.getClassLoader().getResource("zip/zip-malicious-traversal.zip").toURI());
		File tempDir = Files.createTempDirectory("foo").toFile();
		tempDir.deleteOnExit();
		// when:
		try {
			ZipCategory.unzipTo(zipFile, tempDir);
			Assertions.fail("Should throw exception");
		} catch (Exception e) {
				BDDAssertions.then(e.getCause()).hasMessageContaining("is trying to leave the target output directory");
		}
	}
}
