package org.scm4j.deployer.engine;

import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

class ArtifactoryReader {

    static final String METADATA_FILE_NAME = "maven-metadata.xml";
    static final String LOCAL_METADATA_FILE_NAME = "maven-metadata-local.xml";

    private final URL url;
    private final String password;
    private final String userName;

    @SneakyThrows
    ArtifactoryReader(String url, String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.url = new URL(StringUtils.appendIfMissing(url, "/"));
    }

    @SneakyThrows
    static ArtifactoryReader getByUrl(String repoUrl) {
        URL url = new URL(repoUrl);
        String userInfoStr = url.getUserInfo();
        if (userInfoStr != null) {
            String[] userInfo = userInfoStr.split(":");
            repoUrl = repoUrl.replace(userInfoStr + "@", "");
            if (userInfo.length == 2) {
                return new ArtifactoryReader(repoUrl, userInfo[0], userInfo[1]);
            }
        }
        return new ArtifactoryReader(repoUrl, null, null);
    }

    @SneakyThrows
    List<String> getProductVersions(String groupId, String artifactId) {
        MetadataXpp3Reader reader = new MetadataXpp3Reader();
        URL url = getProductMetaDataURL(groupId, artifactId);
        try (InputStream is = getContentStream(url)) {
            Metadata meta = reader.read(is);
            Versioning vers = meta.getVersioning();
            return vers.getVersions();
        } catch (FileNotFoundException e) {
            return Collections.emptyList();
        }
    }

    @SneakyThrows
    String getProductListReleaseVersion() {
        @Cleanup
        InputStream is = getContentStream(getProductMetaDataURL(ProductList.PRODUCT_LIST_GROUP_ID,
                ProductList.PRODUCT_LIST_ARTIFACT_ID));
        MetadataXpp3Reader reader = new MetadataXpp3Reader();
        Metadata meta = reader.read(is);
        Versioning vers = meta.getVersioning();
        return vers.getRelease();
    }

    @SneakyThrows
    private InputStream getContentStream(URL url) {
        if (url.getProtocol().equals("file")) {
            return url.openStream();
        } else {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("GET");
            if (userName != null && password != null)
                con.setRequestProperty("Authorization", "Basic "
                        + Base64.encodeBase64String((userName + ":" + password).getBytes()));
            return con.getInputStream();
        }
    }

    @SneakyThrows
    private URL getProductMetaDataURL(String groupId, String artifactId) {
        return new URL(new URL(url, Utils.coordsToUrlStructure(groupId, artifactId) + "/"), METADATA_FILE_NAME);
    }

    @SneakyThrows
    URL getProductUrl(String groupId, String artifactId, String version, String extension) {
        return new URL(this.url, Utils.coordsToRelativeFilePath(groupId, artifactId, version, extension)
                .replace("\\", "/"));
    }

    @Override
    public String toString() {
        return url.toString();
    }
}