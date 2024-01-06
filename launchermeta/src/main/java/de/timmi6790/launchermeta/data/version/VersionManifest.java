package de.timmi6790.launchermeta.data.version;

import java.util.List;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record VersionManifest(Latest latest, List<Version> versions) {

    @Jacksonized
    @Builder
    public record Latest(String release, String snapshot) {}
}
