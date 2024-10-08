package com.shanebeestudios.mcdeop;

import com.shanebeestudios.mcdeop.util.Util;
import io.sentry.Sentry;
import javax.swing.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class McDeob {
    public static void main(final String[] args) {
        Sentry.init(options ->
                options.setDsn("https://a431c07b469cad98e4933270c602fb0d@o165625.ingest.sentry.io/4506099651444736"));

        if (args.length == 0) {
            startGUI();
        } else {
            new CommandLineHandler(DaggerMcDebobComponent.create().getVersionManager(), args).run();
        }
    }

    public static String getVersion() {
        String version = McDeob.class.getPackage().getImplementationVersion();

        // The version is not available when running in an IDE
        if (version == null) {
            version = "0.0.0";
        }

        return version;
    }

    private static void startGUI() {
        try {
            if (Util.isRunningMacOS()) {
                System.setProperty("apple.awt.application.appearance", "system");
            } else {
                // makes the window prettier on other systems than macs
                // swing's look and feel is ew
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (ClassNotFoundException
                | InstantiationException
                | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            throw new RuntimeException(e);
        }

        DaggerMcDebobComponent.create().getApp().create();
    }
}
