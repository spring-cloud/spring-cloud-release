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
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

import org.springframework.util.StringUtils;

import static org.springframework.cloud.internal.Logger.info;

class ArtifactFetcher {

	private final File outputFolder;

	private final RepositorySystem repositorySystem;

	private final RepositorySystemSession session;

	private final List<RemoteRepository> remoteRepository;

	ArtifactFetcher(File outputFolder, String url) {
		this.outputFolder = outputFolder;
		this.repositorySystem = newRepositorySystem();
		this.session = newSession(this.repositorySystem);
		this.remoteRepository = Collections.singletonList(
				new RemoteRepository.Builder("remote", "default", url).build());
	}

	File unpackedDocs(Project project) {
		String artifactName = project.name;
		String version = project.version;
		try {
			Artifact artifact = new DefaultArtifact("org.springframework.cloud",
					artifactName, "sources", "jar", version);
			ArtifactRequest request = new ArtifactRequest(artifact, this.remoteRepository,
					null);
			info(artifactName + ": Resolving artifact [" + artifact
					+ "] using remote repositories " + this.remoteRepository);
			ArtifactResult result = this.repositorySystem.resolveArtifact(this.session,
					request);
			info(artifactName + ": Resolved artifact [" + artifact + "] to "
					+ result.getArtifact().getFile());
			File unpackedDoc = unpackDoc(artifactName,
					result.getArtifact().getFile().toURI());
			info(artifactName + ": Unpacked file to [" + unpackedDoc.getAbsolutePath()
					+ "]");
			return unpackedDoc;
		}
		catch (IllegalStateException ise) {
			throw ise;
		}
		catch (Exception e) {
			// throw new IllegalStateException(
			System.err.println(
					"Exception occurred while trying to download an artifact with name ["
							+ artifactName + "] and version [" + version
							+ "] in remote repo [" + this.remoteRepository);
			// ,e);
			return null;
		}
	}

	private File unpackDoc(String artifactName, URI stubJarUri) {
		File unzippedDocs = new File(outputFolder, artifactName);
		unzippedDocs.mkdirs();
		info(artifactName + ": Unpacking stub from JAR [URI: " + stubJarUri + "]");
		ZipCategory.unzipTo(new File(stubJarUri), unzippedDocs);
		return unzippedDocs;
	}

	private RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class,
				BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		return locator.getService(RepositorySystem.class);
	}

	private RepositorySystemSession newSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
		session.setChecksumPolicy(RepositoryPolicy.CHECKSUM_POLICY_WARN);
		String localRepository = settings().getLocalRepository();
		localRepository = StringUtils.hasText(localRepository) ? localRepository
				: System.getProperty("user.home") + File.separator + ".m2"
						+ File.separator + "repository";
		LocalRepository localRepo = new LocalRepository(localRepository);
		session.setLocalRepositoryManager(
				system.newLocalRepositoryManager(session, localRepo));
		return session;
	}

	private static Settings settings() {
		SettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
		SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
		request.setUserSettingsFile(userSettings());
		SettingsBuildingResult result;
		try {
			result = builder.build(request);
		}
		catch (SettingsBuildingException ex) {
			throw new IllegalStateException(ex);
		}
		return result.getEffectiveSettings();
	}

	private static File userSettings() {
		return new File(new File(System.getProperty("user.home")).getAbsoluteFile(),
				File.separator + ".m2" + File.separator + "settings.xml");
	}

}
