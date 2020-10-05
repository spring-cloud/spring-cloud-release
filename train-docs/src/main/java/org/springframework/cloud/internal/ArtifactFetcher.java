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
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import org.springframework.util.FileSystemUtils;

import static org.springframework.cloud.internal.Logger.error;
import static org.springframework.cloud.internal.Logger.info;

class ArtifactFetcher {

	private static final int CONNECT_TIMEOUT = 5000;

	private static final int READ_TIMEOUT = 5000;

	private final File outputFolder;

	private final String repoUrl;

	ArtifactFetcher(File outputFolder, String repoUrl) {
		this.outputFolder = outputFolder;
		this.repoUrl = repoUrl;
	}

	File unpackedDocs(Project project) {
		String projectName = project.name;
		String version = project.version;
		List<String> urls = urls(projectName, version);
		final File downloadedZipsFolder = new File(outputFolder, "downloaded-zips");
		downloadedZipsFolder.mkdirs();
		File outputZip = new File(downloadedZipsFolder, projectName + ".zip");
		for (String url : urls) {
			try {
				info(projectName + ": Fetching sources from [" + url + "]. Please wait...");
				FileUtils.copyURLToFile(new URL(url), outputZip, CONNECT_TIMEOUT, READ_TIMEOUT);
				info(projectName + ": Successfully fetched a zip from [" + url + "] to [" + outputZip + "]");
				break;
			}
			catch (IOException ex) {
				error(projectName + ": Failed to fetch a zip from [" + url + "]");
			}
		}
		if (!outputZip.exists()) {
			error(projectName + ": Exception occurred while trying to download an artifact with name [" + projectName
					+ "] and version [" + version + "]");
			return null;
		}
		return unpackDocs(projectName, outputZip);
	}

	private File unpackDocs(String projectName, File outputZip) {
		File unpackedDoc = unpackDoc(projectName, outputZip);
		info(projectName + ": Unpacked file to [" + unpackedDoc.getAbsolutePath() + "]");
		if (unpackedDoc.isDirectory()) {
			String[] subfolders = unpackedDoc.list();
			if (subfolders == null || subfolders.length != 1) {
				return unpackedDoc;
			}
			moveOneFolderUp(unpackedDoc, subfolders[0]);
		}
		return unpackedDoc;
	}

	private void moveOneFolderUp(File unpackedDoc, String subfolder) {
		File onlySubfolder = new File(unpackedDoc, subfolder);
		try {
			FileSystemUtils.copyRecursively(onlySubfolder, unpackedDoc);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
		FileSystemUtils.deleteRecursively(onlySubfolder);
	}

	private List<String> urls(String projectName, String version) {
		ReleaseType releaseType = ReleaseType.fromVersion(version);
		String sourcesUrl = this.repoUrl + projectName + "/archive/";
		List<String> sourcesUrls = new LinkedList<>();
		if (releaseType == ReleaseType.SNAPSHOT) {
			// 2.0.0-SNAPSHOT or 2.0.0-BUILD-SNAPSHOT -> 2.0.x
			String[] splitVersion = version.split("\\.");
			sourcesUrls.add(sourcesUrl + splitVersion[0] + "." + splitVersion[1] + ".x.zip");
			// fallback
			sourcesUrls.add(sourcesUrl + "master.zip");
		}
		else {
			sourcesUrls.add(sourcesUrl + "v" + version + ".zip");
			sourcesUrls.add(sourcesUrl + version + ".zip");
		}
		return sourcesUrls;
	}

	private File unpackDoc(String artifactName, File zip) {
		File unzippedDocs = new File(outputFolder, artifactName);
		unzippedDocs.mkdirs();
		info(artifactName + ": Unpacking from [" + zip + "]");
		ZipCategory.unzipTo(zip, unzippedDocs);
		return unzippedDocs;
	}

}

enum ReleaseType {

	SNAPSHOT, NON_SNAPSHOT;

	static ReleaseType fromVersion(String version) {
		if (isSnapshot(version)) {
			return ReleaseType.SNAPSHOT;
		}
		return ReleaseType.NON_SNAPSHOT;
	}

	private static boolean isSnapshot(String version) {
		return version.contains("SNAPSHOT");
	}

}
