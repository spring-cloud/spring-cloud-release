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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Class that reads poms as {@link Model}.
 *
 * @author Marcin Grzejszczak
 */
final class PomReader {

	private PomReader() {
		throw new IllegalStateException("Shouldn't instantiate a utility class");
	}

	/**
	 * Returns a parsed POM.
	 * @param pom location to the pom
	 * @return parsed model
	 */
	public static Model readPom(File pom) {
		if (!pom.exists()) {
			throw new IllegalStateException("File [" + pom.getAbsolutePath() + "] not found");
		}
		String fileText = "";
		try (Reader reader = new FileReader(pom)) {
			if (pom.isFile()) {
				fileText = new String(Files.readAllBytes(pom.toPath()));
			}
			MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
			return xpp3Reader.read(reader);
		}
		catch (XmlPullParserException | IOException e) {
			if (pom.isFile() && fileText.length() == 0) {
				throw new IllegalStateException("File [" + pom.getAbsolutePath() + "] is empty", e);
			}
			throw new IllegalStateException("Failed to read file: " + pom.getAbsolutePath(), e);
		}
	}

}
