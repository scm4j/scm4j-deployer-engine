package org.scm4j.ai;

import java.io.*;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

public class Utils {
	
	public static String coordsToString(String groupId, String artifactId, String version, String extension) {
		return coordsToString(groupId, artifactId) + ":" + version + ":" + extension;
	}

	public static String coordsToString(String groupId, String artifactId, String version) {
		return coordsToString(groupId, artifactId) + ":" + version;
	}
	
	public static String coordsToString(String groupId, String artifactId) {
		return groupId + ":" + artifactId;
	}

	public static String coordsToFileName(String artifactId, String version, String extension) {
		return artifactId + "-" + version + extension;
	}

	public static String coordsToFolderStructure(String groupId, String artifactId) {
		return new File(groupId.replace(".", File.separator), artifactId).getPath();
	}
	
	public static String coordsToFolderStructure(String groupId, String artifactId, String version) {
		return new File(coordsToFolderStructure(groupId, artifactId), version).getPath();
	}
	
	public static String coordsToRelativeFilePath(String groupId, String artifactId, String version, String extension) {
		return new File(coordsToFolderStructure(groupId, artifactId, version), 
				coordsToFileName(artifactId, version, extension)).getPath();
	}

	public static String coordsToUrlStructure(String groupId, String artifactId) {
		return coordsToString(groupId, artifactId).replace(".", "/").replace(":", "/");
	}

	public static String coordsToUrlStructure(String groupId, String artifactId, String version) {
		return coordsToString(groupId,artifactId,version).replace(".","/").replace(":","/");
	}

	public static boolean isYml(String extension) {
		if(extension.equals(".yml")) {
			return true;
		}
		return false;
	}

	public static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
				exception.printStackTrace();
			}
		});
		return locator.getService(RepositorySystem.class);
	}

	public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system, File repository) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		LocalRepository localRepo = new LocalRepository(repository);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session,localRepo));

		return session;
	}
}
