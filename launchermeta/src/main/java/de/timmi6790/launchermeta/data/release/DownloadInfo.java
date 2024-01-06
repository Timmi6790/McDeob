package de.timmi6790.launchermeta.data.release;

import java.net.URL;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record DownloadInfo(String sha1, long size, URL url) {}
