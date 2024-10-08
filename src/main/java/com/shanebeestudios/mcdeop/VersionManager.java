package com.shanebeestudios.mcdeop;

import de.timmi6790.launchermeta.LauncherMeta;
import de.timmi6790.launchermeta.data.release.ReleaseManifest;
import de.timmi6790.launchermeta.data.version.Version;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VersionManager {
    private static final OffsetDateTime MINIMUM_RELEASE_TIME = OffsetDateTime.parse("2019-08-28T15:00:00Z");
    private static final Set<String> SPECIAL_VERSIONS = Set.of("1.14.4");

    private final LauncherMeta launcherMeta;

    @Getter(lazy = true)
    private final List<Version> versions = this.fetchVersions();

    @Inject
    public VersionManager(final LauncherMeta launcherMeta) {
        this.launcherMeta = launcherMeta;
    }

    private boolean hasMappings(final Version version) {
        return version.releaseTime().isAfter(MINIMUM_RELEASE_TIME) || SPECIAL_VERSIONS.contains(version.id());
    }

    private List<Version> fetchVersions() {
        final List<Version> fetchedVersions;
        try {
            fetchedVersions = this.launcherMeta.getVersionManifest().versions();
        } catch (final IOException e) {
            log.error("Failed to fetch version manifest", e);
            return List.of();
        }

        // Remove versions without mappings
        fetchedVersions.removeIf(version -> !this.hasMappings(version));

        // Sort versions after time
        fetchedVersions.sort((o1, o2) -> o2.releaseTime().compareTo(o1.releaseTime()));

        return fetchedVersions;
    }

    public Optional<Version> getVersion(final String id) {
        return this.getVersion(version -> version.id().equals(id));
    }

    public Optional<Version> getVersion(final Predicate<Version> predicate) {
        for (final Version version : this.getVersions()) {
            if (predicate.test(version)) {
                return Optional.ofNullable(version);
            }
        }

        return Optional.empty();
    }

    public ReleaseManifest getReleaseManifest(final Version version) throws IOException {
        return this.launcherMeta.getReleaseManifest(version);
    }
}
