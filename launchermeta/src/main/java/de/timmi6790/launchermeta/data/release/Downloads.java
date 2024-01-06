package de.timmi6790.launchermeta.data.release;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record Downloads(
        DownloadInfo client,
        @JsonProperty("client_mappings") DownloadInfo clientMappings,
        DownloadInfo server,
        @JsonProperty("server_mappings") DownloadInfo serverMappings) {

    public Optional<DownloadInfo> getClientMappings() {
        return Optional.ofNullable(this.clientMappings);
    }

    public Optional<DownloadInfo> getServerMappings() {
        return Optional.ofNullable(this.serverMappings);
    }
}
