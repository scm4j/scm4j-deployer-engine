package org.scm4j.ai;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.scm4j.ai.exceptions.ENoConfig;
import org.scm4j.ai.exceptions.EProductNotFound;

import com.google.common.io.Files;

public class ArtifactoryReader {
	
	public static final String PRODUCT_LISTS_FILE_NAME = "product-lists"; 
	public static final String METADATA_FILE_NAME = "maven-metadata.xml";
	public static final String PRODUCT_LIST_GROUP_ID = "org.scm4j.ai";
	public static final String PRODUCT_LIST_ARTIFACT_ID = "product-list";
	public static final String PRODUCT_LIST_FILE_NAME = "product-list.txt";
	
	private final URL url;
	private final String password;
	private final String userName;
	
	public ArtifactoryReader(String url, String userName, String password) throws Exception {
		this.userName = userName;
		this.password = password;
		this.url = new URL(StringUtils.appendIfMissing(url, "/"));
	}
	
	private String getArtifactReleaseVersion(String groupId, String artifactId) throws Exception {
		try (InputStream is = getContentStream(getProductMetaDataURL(groupId, artifactId))) {
			MetadataXpp3Reader reader = new MetadataXpp3Reader();
			Metadata meta = reader.read(is);
			Versioning vers = meta.getVersioning();
			return vers.getRelease();
		} 
	}
	
	public InputStream getContentStream(URL url) throws Exception {
		if (url.getProtocol().equals("file")) {
			return url.openStream();
		} else {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Basic " 
					+ Base64.encodeBase64String((userName + ":" + password).getBytes()));
			return con.getInputStream();
		}
	}
	
	public InputStream getContentStream(String groupId, String artifactId, String version, String extension) throws Exception {
		return getContentStream(getProductUrl(groupId, artifactId, version, extension));
	}

	public Map<String, String> getProducts() throws Exception {
		String productListReleaseVersion = getArtifactReleaseVersion(PRODUCT_LIST_GROUP_ID, PRODUCT_LIST_ARTIFACT_ID);
		
		Map<String, String> products = new HashMap<>();
		URL productUrl = getProductUrl(PRODUCT_LIST_GROUP_ID, PRODUCT_LIST_ARTIFACT_ID, productListReleaseVersion, ".zip");
		try (InputStream is = getContentStream(productUrl)) {
			ZipInputStream zis = new ZipInputStream(is);
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().equals(PRODUCT_LIST_FILE_NAME)) {
					return Utils.readMap(zis);
				}
			}
			return products;
		}
	}
	
	public Boolean hasProduct(String groupId, String artifactId) throws Exception {
		return getProducts().keySet().contains(Utils.coordsToString(groupId, artifactId));
	}
	
	public List<String> getProductVersions(String groupId, String artifactId) throws Exception {
		if (!hasProduct(groupId, artifactId)) {
			throw new EProductNotFound();
		}
		MetadataXpp3Reader reader = new MetadataXpp3Reader();
		try (InputStream is = getProductMetaDataURL(groupId, artifactId).openStream()) {
			Metadata meta = reader.read(is);
			Versioning vers = meta.getVersioning();
			return vers.getVersions();
		} 
	}

	private URL getProductMetaDataURL(String groupId, String artifactId) throws MalformedURLException {
		return new URL(new URL(url, Utils.coordsToUrlStructure(groupId, artifactId) + "/"), METADATA_FILE_NAME);
	}

	public static List<ArtifactoryReader> loadFromWorkingFolder(File workingFolder) throws ENoConfig {
		List<String> repoUrls;
		File reposFile = new File(workingFolder, PRODUCT_LISTS_FILE_NAME);
		if (!reposFile.exists()) {
			throw new ENoConfig(PRODUCT_LISTS_FILE_NAME + " file is not found in the working folder");
		}
		try {
			repoUrls = Files.readLines(reposFile, StandardCharsets.UTF_8);
			List<ArtifactoryReader> res = new ArrayList<>();
			for (String repoUrl : repoUrls) {
				res.add(ArtifactoryReader.getByUrl(repoUrl));
			}
			return res;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static ArtifactoryReader getByUrl(String repoUrl) throws Exception {
		URL url = new URL(repoUrl);
		String userInfoStr = url.getUserInfo();
		if (userInfoStr != null) {
			String[] userInfo = userInfoStr.split(":");
			repoUrl = repoUrl.replace(userInfoStr + "@",  "");
			if (userInfo.length == 2) {
				return new ArtifactoryReader(repoUrl, userInfo[0], userInfo[1]);
			} 
		}
		return new ArtifactoryReader(repoUrl, null, null);
		
	}

	public URL getProductUrl(String groupId, String artifactId, String version, String extension) throws Exception {
		return new URL(this.url, Utils.coordsToRelativeFilePath(groupId, artifactId, version, extension)
				.replace("\\",  "/"));
	}

	@Override
	public String toString() {
		return url.toString();
	}
	
}