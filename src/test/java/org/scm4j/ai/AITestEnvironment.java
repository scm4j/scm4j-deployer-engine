package org.scm4j.ai;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class AITestEnvironment {
	
	private File baseTestFolder;
	private File envFolder;
	private File artifactoriesFolder;
	private File artifactory1Folder;
	private File artifactory2Folder;
	private String artifactory1Url;
	private String artifactory2Url;
	private File productListFile;

	public void prepareEnvironment() throws IOException {
		File baseTestFolderFile = new File(System.getProperty("java.io.tmpdir"), "scm4j-ai-test");
		FileUtils.deleteDirectory(baseTestFolderFile);
		baseTestFolder = Files.createDirectory(baseTestFolderFile.toPath()).toFile();
		createArtifactories();
		createEnvironment();
		writeReposInProductList(ArtifactoryWriter.PRODUCT_LIST_DEFAULT_VERSION);
		writeReposInProductList(ArtifactoryWriter.PRODUCT_LIST_VERSION);
	}
	
	private void createArtifactories() throws IOException {
		artifactoriesFolder = Files
				.createDirectory(new File(baseTestFolder, "art").toPath())
				.toFile();
		artifactory1Folder = Files
				.createDirectory(new File(artifactoriesFolder, "RemoteArtifactory1").toPath())
				.toFile();
		artifactory2Folder = Files
				.createDirectory(new File(artifactoriesFolder, "RemoteArtifactory2").toPath())
				.toFile();
		artifactory1Url = "file://localhost/" + artifactory1Folder.getPath().replace("\\", "/");
		artifactory2Url = "file://localhost/" + artifactory2Folder.getPath().replace("\\", "/");
	}

	private void createEnvironment() throws IOException {
		envFolder = Files.createDirectory(new File(baseTestFolder, "env").toPath()).toFile();
	}

	private void writeReposInProductList(String version) throws IOException {
		productListFile = new File(artifactory1Folder, Utils.coordsToRelativeFilePath(ProductList.PRODUCT_LIST_GROUP_ID,
				ProductList.PRODUCT_LIST_ARTIFACT_ID, version, ".yml"));
		if(!productListFile.exists()) {
			productListFile.getParentFile().mkdirs();
			productListFile.createNewFile();
		}
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);
		Map<String, ArrayList<String>> productList = new HashMap<>();
		ArrayList<String> repos = new ArrayList<>();
		repos.add(artifactory1Url);
		repos.add(artifactory2Url);
		productList.put(ProductList.PRODUCTS, new ArrayList<>());
		productList.put(ProductList.REPOSITORIES, repos);
		String yamlOutput = yaml.dump(productList);
		FileWriter fw = new FileWriter(productListFile);
		fw.write(yamlOutput);
		fw.flush();
		fw.close();
	}
	
	public File getBaseTestFolder() {
		return baseTestFolder;
	}

	public File getEnvFolder() {
		return envFolder;
	}

	public File getArtifactoriesFolder() {
		return artifactoriesFolder;
	}

	public File getArtifactory1Folder() {
		return artifactory1Folder;
	}

	public File getArtifactory2Folder() {
		return artifactory2Folder;
	}

	public String getArtifactory1Url() {
		return artifactory1Url;
	}

	public String getArtifactory2Url() {
		return artifactory2Url;
	}
}
