package org.scm4j.deployer.engine.loggers;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.artifact.Artifact;

@Slf4j
public class RepositoryLogger
		extends AbstractRepositoryListener {

	public void artifactDownloading(RepositoryEvent event) {
		if (!event.getArtifact().getExtension().equals("pom")) {
			Artifact art = event.getArtifact();
			log.info("Downloading " + art.getArtifactId() + "-" + art.getVersion() + " from " + event.getRepository()
					.getId());
		}
	}

}

