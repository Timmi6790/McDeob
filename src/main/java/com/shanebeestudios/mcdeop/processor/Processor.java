package com.shanebeestudios.mcdeop.processor;

import com.shanebeestudios.mcdeop.processor.decompiler.Decompiler;
import com.shanebeestudios.mcdeop.processor.decompiler.VineflowerDecompiler;
import com.shanebeestudios.mcdeop.processor.remapper.ReconstructRemapper;
import com.shanebeestudios.mcdeop.processor.remapper.Remapper;
import com.shanebeestudios.mcdeop.util.DurationTracker;
import com.shanebeestudios.mcdeop.util.FileUtil;
import com.shanebeestudios.mcdeop.util.Util;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import org.jetbrains.annotations.Nullable;

@Slf4j
public class Processor {
    private final ResourceRequest request;
    private final ProcessorOptions options;

    @Nullable private final ResponseConsumer responseConsumer;

    private final OkHttpClient httpClient;
    private final Remapper remapper;
    private final Decompiler decompiler;

    private final Path jarPath;
    private final Path mappingsPath;
    private final Path remappedJar;
    private final Path decompiledJarPath;
    private final Path decompiledZipPath;

    private Processor(
            final ResourceRequest request,
            final ProcessorOptions options,
            @Nullable final ResponseConsumer responseConsumer) {
        this.request = request;
        this.options = options;

        this.responseConsumer = responseConsumer;
        this.remapper = new ReconstructRemapper();
        this.decompiler = new VineflowerDecompiler();
        this.httpClient = Util.createHttpClient();

        final Path dataFolderPath = this.getDataFolder();
        this.jarPath = dataFolderPath.resolve("source.jar");
        this.mappingsPath = dataFolderPath.resolve("mappings.txt");
        this.remappedJar = dataFolderPath.resolve("remapped.jar");
        this.decompiledJarPath = dataFolderPath.resolve("decompiled");
        this.decompiledZipPath = dataFolderPath.resolve(Path.of("decompiled.zip"));
    }

    public static void runProcessor(
            final ResourceRequest request,
            final ProcessorOptions options,
            @Nullable final ResponseConsumer responseConsumer) {
        try {
            final Processor processor = new Processor(request, options, responseConsumer);
            processor.init();
            processor.cleanup();
        } catch (final Exception e) {
            log.error("Failed to run processor", e);
        } finally {
            Util.forceGC();
        }
    }

    private Path getDataFolder() {
        final String versionFolder = String.format(
                "%s-%s",
                this.request.type().name().toLowerCase(Locale.ENGLISH),
                this.request.getVersion().id());
        final Path folderPath = Util.getBaseDataFolder().resolve(versionFolder);

        try {
            Files.createDirectories(folderPath);
        } catch (final IOException ignore) {
        }

        return folderPath;
    }

    private Optional<ResponseConsumer> getResponseConsumer() {
        return Optional.ofNullable(this.responseConsumer);
    }

    private void sendNewResponse(final String statusMessage) {
        this.getResponseConsumer().ifPresent(consumer -> consumer.onStatusUpdate(statusMessage));
    }

    private void downloadFile(final URL url, final Path path, final String fileType) throws IOException {
        try (final DurationTracker ignored = new DurationTracker(
                duration -> log.info("Successfully downloaded {} file in {}!", fileType, duration))) {
            log.info("Downloading {} file from Mojang...", fileType);
            final Request httpRequest = new Request.Builder().url(url).build();

            try (final Response response = this.httpClient.newCall(httpRequest).execute()) {
                if (response.body() == null) {
                    throw new IOException("Response body was null");
                }

                final long length = response.body().contentLength();
                if (Files.exists(path) && Files.size(path) == length) {
                    log.info("Already have {}, skipping download.", path.getFileName());
                    return;
                }

                FileUtil.remove(path);
                try (BufferedSink sink = Okio.buffer(Okio.sink(path))) {
                    sink.writeAll(response.body().source());
                }
            }
        }
    }

    private boolean isValid() {
        if (this.getJarUrl() == null) {
            log.error(
                    "Failed to find JAR URL for version {}-{}",
                    this.request.type(),
                    this.request.getVersion().id());
            this.sendNewResponse(String.format(
                    "Failed to find JAR URL for version %s-%s",
                    this.request.type(), this.request.getVersion().id()));
            return false;
        }

        if (this.getMappingsUrl() == null) {
            log.error(
                    "Failed to find mappings URL for version {}-{}",
                    this.request.type(),
                    this.request.getVersion().id());
            this.sendNewResponse(String.format(
                    "Failed to find mappings URL for version %s-%s",
                    this.request.type(), this.request.getVersion().id()));
            return false;
        }

        return true;
    }

    @Nullable private URL getJarUrl() {
        return this.request.getJar().orElse(null);
    }

    @Nullable private URL getMappingsUrl() {
        return this.request.getMappings().orElse(null);
    }

    private CompletableFuture<Void> downloadJar() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.downloadFile(this.getJarUrl(), this.jarPath, "JAR");
            } catch (final IOException e) {
                throw new CompletionException(e);
            }

            return null;
        });
    }

    private CompletableFuture<Void> downloadMappings() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                this.downloadFile(this.getMappingsUrl(), this.mappingsPath, "mappings");
            } catch (final IOException exception) {
                throw new CompletionException(exception);
            }

            return null;
        });
    }

    private void remapJar() {
        try (final DurationTracker ignored =
                new DurationTracker(duration -> log.info("Remapping completed in {}!", duration))) {
            this.sendNewResponse("Remapping...");

            if (!Files.exists(this.remappedJar)) {
                log.info("Remapping {} file...", this.jarPath.getFileName());
                this.remapper.remap(this.jarPath, this.mappingsPath, this.remappedJar);
            } else {
                log.info("{} already remapped... skipping mapping.", this.remappedJar.getFileName());
            }
        }
    }

    private void decompileJar(final Path jarPath) throws IOException {
        try (final DurationTracker ignored =
                new DurationTracker(duration -> log.info("Decompiling completed in {}!", duration))) {
            log.info("Decompiling final JAR file.");
            this.sendNewResponse("Decompiling... This will take a while!");

            FileUtil.remove(this.decompiledJarPath);
            Files.createDirectories(this.decompiledJarPath);

            this.decompiler.decompile(jarPath, this.decompiledJarPath);

            if (this.options.zipDecompileOutput()) {
                // Pack the decompiled files into a zip file
                log.info("Packing decompiled files into {}", this.decompiledZipPath);
                this.sendNewResponse("Packing decompiled files ...");
                FileUtil.remove(this.decompiledZipPath);
                FileUtil.zip(this.decompiledJarPath, this.decompiledZipPath);
            }
        }
    }

    public void init() {
        if (!this.isValid()) {
            return;
        }

        try (final DurationTracker ignored = new DurationTracker(duration -> {
            log.info("Completed in {}!", duration);
            this.sendNewResponse(String.format("Completed in %s!", duration));
        })) {
            // Download the JAR and mappings files
            this.sendNewResponse("Downloading JAR & MAPPINGS...");
            CompletableFuture.allOf(this.downloadJar(), this.downloadMappings()).join();

            if (this.options.remap()) {
                this.remapJar();
            }

            if (this.options.decompile()) {
                this.decompileJar(this.options.remap() ? this.remappedJar : this.jarPath);
            }

        } catch (final IOException e) {
            log.error("Failed to run Processor!", e);
        }
    }

    public void cleanup() {
        if (this.remapper instanceof final Cleanup cleanup) {
            cleanup.cleanup();
        }

        if (this.decompiler instanceof final Cleanup cleanup) {
            cleanup.cleanup();
        }
    }
}
