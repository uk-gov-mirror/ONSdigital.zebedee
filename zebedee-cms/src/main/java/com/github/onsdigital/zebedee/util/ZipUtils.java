package com.github.onsdigital.zebedee.util;

import com.github.onsdigital.zebedee.exceptions.BadRequestException;
import com.github.onsdigital.zebedee.exceptions.ZebedeeException;
import com.github.onsdigital.zebedee.model.ContentWriter;
import com.github.onsdigital.zebedee.reader.ContentReader;
import org.apache.commons.io.IOUtils;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utilities related to file compression.
 */
public class ZipUtils {

    /**
     * Unzip the given file into the given destination.
     *
     * @param zipFile
     * @param destination
     * @throws IOException
     */
    public static void unzip(final File zipFile, final String destination) throws IOException {
        unzip(new FileInputStream(zipFile), destination);
    }

    public static void unzip(final InputStream inputStream, final String destination) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null && !zipEntry.isDirectory()) {
                String name = zipEntry.getName();
                File file = new File(destination + File.separator + name);
                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    IOUtils.copy(zipInputStream, fileOutputStream);
                }
            }
        }
    }

    /**
     * Zip the given folder into the given zip file.
     *
     * @param folder
     * @param zipFile
     * @throws IOException
     */
    public static void zipFolder(final File folder, final File zipFile, Function<String, Boolean>... filters) throws IOException {
        zipFolder(folder, new FileOutputStream(zipFile), filters);
    }

    public static void zipFolder(final File folder, final OutputStream outputStream, Function<String, Boolean>... filters) throws IOException {
        try (ZipOutputStream zipOutputStream = getZipOutputStream(outputStream)) {
            zipFolder(folder, zipOutputStream, folder.getPath().length() + 1, filters);
        }
    }

    private static void zipFolder(final File folder, final ZipOutputStream zipOutputStream, final int prefixLength, Function<String, Boolean>... filters)
            throws IOException {
        for (final File file : folder.listFiles()) {
            if (file.isFile() && !shouldBeFiltered(filters, file.toString())) {
                final ZipEntry zipEntry = new ZipEntry(file.getPath().substring(prefixLength));
                zipOutputStream.putNextEntry(zipEntry);
                try (FileInputStream inputStream = new FileInputStream(file)) {
                    IOUtils.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            } else if (file.isDirectory()) {
                zipFolder(file, zipOutputStream, prefixLength, filters);
            }
        }
    }

    /**
     * If any of the given filters return true, the uri should be filtered
     *
     * @param filters
     * @param uri
     * @return
     */
    private static boolean shouldBeFiltered(Function<String, Boolean>[] filters, String uri) {
        for (Function<String, Boolean> filter : filters) {
            if (filter.apply(uri))
                return true;
        }
        return false;
    }

    public static void zipFolderWithEncryption(final File folder, final File zipFile, SecretKey key, Function<String, Boolean>... filters) throws IOException {
        zipFolderWithEncryption(folder, EncryptionUtils.encryptionOutputStream(zipFile.toPath(), key), key, filters);
    }

    private static void zipFolderWithEncryption(final File folder, final OutputStream outputStream, SecretKey key, Function<String, Boolean>... filters) throws IOException {
        try (ZipOutputStream zipOutputStream = getZipOutputStream(outputStream)) {
            zipFolderWithEncryption(folder, zipOutputStream, key, folder.getPath().length() + 1, filters);
        }
    }

    private static ZipOutputStream getZipOutputStream(OutputStream outputStream) {
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.setLevel(5); // minimal compression
        return zipOutputStream;
    }

    private static void zipFolderWithEncryption(
            final File folder,
            final ZipOutputStream zipOutputStream,
            SecretKey key,
            final int prefixLength,
            Function<String, Boolean>... filters)
            throws IOException {
        for (final File file : folder.listFiles()) {
            if (file.isFile() && !shouldBeFiltered(filters, file.toString())) {
                final ZipEntry zipEntry = new ZipEntry(file.getPath().substring(prefixLength));
                zipOutputStream.putNextEntry(zipEntry);
                try (InputStream inputStream = EncryptionUtils.encryptionInputStream(file.toPath(), key)) {
                    IOUtils.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            } else if (file.isDirectory()) {
                zipFolderWithEncryption(file, zipOutputStream, key, prefixLength);
            }
        }
    }

    public static void zipFolderWithEncryption(
            final ContentReader contentReader,
            final ContentWriter contentWriter,
            String folderPath,
            String saveUri,
            Function<String, Boolean>... filters) throws IOException, ZebedeeException {
        try (ZipOutputStream zipOutputStream = getZipOutputStream(contentWriter.getOutputStream(saveUri))) {
            zipFolderWithEncryption(contentReader, folderPath, zipOutputStream, folderPath.length() + 1, filters);
        } catch (BadRequestException e) {
            e.printStackTrace();
        }
    }

    private static void zipFolderWithEncryption(
            final ContentReader contentReader,
            String folderUri,
            final ZipOutputStream zipOutputStream,
            final int prefixLength,
            Function<String, Boolean>... filters)
            throws IOException, ZebedeeException {

        File folder = Paths.get(folderUri).toFile();
        for (final File file : folder.listFiles()) {
            String fileUri = contentReader.getRootFolder().relativize(file.toPath()).toString();
            if (file.isFile() && !shouldBeFiltered(filters, file.toString())) {
                final ZipEntry zipEntry = new ZipEntry(file.getPath().substring(prefixLength));
                zipOutputStream.putNextEntry(zipEntry);
                try (InputStream inputStream = contentReader.getResource(fileUri).getData()) {
                    IOUtils.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            } else if (file.isDirectory()) {
                zipFolderWithEncryption(contentReader, contentReader.getRootFolder().resolve(fileUri).toString(), zipOutputStream, prefixLength);
            }
        }
    }
}
