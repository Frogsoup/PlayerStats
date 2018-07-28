package me.frog.playerstats.util;

import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.plugin.Plugin;

import java.io.*;

public class FileManager {

    /**
     * This method will attempt to load the file specified
     *  under resource. If it does not exist it will create
     *  the file and then load it.
     *
     * @param plugin The plugin, or main class.
     * @param resource The file to be created or loaded.
     * @return The file.
     */
    public File loadConfigurationFile(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();

        if(!folder.exists())
            folder.mkdir();

        File resourceFile = new File(folder, resource);

        try {
            if(!resourceFile.exists()) {
                resourceFile.createNewFile();

                try(InputStream input = plugin.getResourceAsStream(resource)) {
                    OutputStream output = new FileOutputStream(resourceFile);
                    ByteStreams.copy(input, output);
                }
            }
        }catch(IOException exception) {
            exception.printStackTrace();
        }

        return resourceFile;
    }
}