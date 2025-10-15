package pl.wrona.webserver.bussiness.gtfs.download;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static File zip(File directory) throws IOException {
        File file = new File("%s.zip".formatted(directory.getName()));
        try (final FileOutputStream fos = new FileOutputStream(file)) {
            try (ZipOutputStream zipOut = new ZipOutputStream(fos)) {
                Files.walk(directory.toPath())
                        .map(Path::toFile)
                        .filter(File::isFile)
                        .forEach(fileToZip -> {
                            try (FileInputStream fis = new FileInputStream(fileToZip)) {
                                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                                zipOut.putNextEntry(zipEntry);

                                byte[] bytes = new byte[1024];
                                int length;
                                while ((length = fis.read(bytes)) >= 0) {
                                    zipOut.write(bytes, 0, length);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }

        return file;
    }
}
