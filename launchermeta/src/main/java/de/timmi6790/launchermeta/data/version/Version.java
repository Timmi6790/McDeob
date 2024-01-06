package de.timmi6790.launchermeta.data.version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.net.URL;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record Version(String id, VersionType type, URL url, OffsetDateTime releaseTime) {}
