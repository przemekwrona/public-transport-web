package pl.wrona.webserver.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Slf4j
@Component
public class GtfsFileCleanerTask {

    @Scheduled(fixedRate = 60_000)
    public void cleanGtfsFiles() {

        Path startPath = Paths.get("./"); // Replace with your starting directory
        String prefix = "gtfs_"; // The prefix you want to match
        FileCreationDatePredicate fileCreationDatePredicate = new FileCreationDatePredicate();
        try {
            // Files.walk() returns a Stream<Path> that traverses the file tree
            try (Stream<Path> stream = Files.walk(startPath)) {

                stream
                        // 1. Filter out directories, keeping only regular files
                        .filter(Files::isRegularFile)

                        // 2. Filter to match the file prefix
                        .filter(path -> path.getFileName().toString().startsWith(prefix))

                        // 3. Files
                        .filter(fileCreationDatePredicate)

                        // 4. Process each matching file
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("Error during delete file {}", path.toAbsolutePath());
                                throw new RuntimeException(e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("An error occurred during file traversal: {}", e.getMessage());
        }
    }
}
