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

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.util.StringUtils;

public class TestThatGeneratesTheFinalReleaseTrainDocumentationTests {

	/**
	 * FOR SOME REASON I CAN'T RUN MAIN CLASS FROM MAVEN AS A WORKAOUND WE WILL RUN THIS
	 * TEST TO GENERATE THE DOCS
	 */
	@Test
	@Disabled
	void should_generate_adocs_with_values_from_system_property() {
		// System property needs to be passed
		Assumptions.assumeTrue(StringUtils.hasText(System.getProperty("bomPath")));

		String bomPath = System.getProperty("bomPath");
		String starterParentPath = System.getProperty("starterParentPath");
		String repoUrl = System.getProperty("repoUrl");
		String unzippedDocs = System.getProperty("unzippedDocs");
		String generatedTrainDocs = System.getProperty("generatedTrainDocs");

		GenerateReleaseTrainDocs.main(bomPath, starterParentPath, repoUrl, unzippedDocs,
				generatedTrainDocs);

		BDDAssertions.then(new File(generatedTrainDocs)).isNotEmptyDirectory();
	}

}
