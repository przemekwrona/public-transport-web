package pl.wrona.webserver.job;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Predicate;

@AllArgsConstructor
public class FileCreationDatePredicate implements Predicate<Path> {

    private final int minutes;

    public FileCreationDatePredicate() {
        this.minutes = 5;
    }

    @Override
    public boolean test(Path path) {
        // 1. Read all basic file attributes
        BasicFileAttributes attr = null;

        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LocalDateTime localDateTime = Optional.ofNullable(attr)
                .map(BasicFileAttributes::creationTime)
                .map(FileTime::toInstant)
                .map(instant -> instant.atZone(ZoneId.systemDefault()).toLocalDateTime())
                .orElse(LocalDateTime.MAX);

        return LocalDateTime.now().minusMinutes(minutes).isAfter(localDateTime);
    }
}
