package com.hobbyswap.service;

import com.hobbyswap.model.FileRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileRecordServiceTest {
    private final FileRecordService records = new FileRecordService();

    @BeforeEach
    void useOnlyIsolatedTestDirectory() throws IOException {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        assertTrue(cwd.endsWith(Path.of("target", "test-workdir")),
                "CSV tests must run in Maven Surefire's target/test-workdir, not project data/");
        Files.deleteIfExists(Path.of("data", "records.csv"));
    }

    @Test
    void recordsRoundTripWithCommasQuotesAndNewlines() {
        records.add("Camera, lens", "Collectibles", "He said \"go\"\nnext line");

        List<FileRecord> saved = records.findAll();
        assertEquals(1, saved.size());
        assertEquals("Camera, lens", saved.getFirst().getName());
        assertEquals("He said \"go\" next line", saved.getFirst().getNote());

        Long id = saved.getFirst().getId();
        records.update(id, "Updated, camera", "Photo", "new note");
        assertEquals("Updated, camera", records.findAll().getFirst().getName());
        assertEquals("Photo", records.findAll().getFirst().getCategory());

        records.delete(id);
        assertTrue(records.findAll().isEmpty());
    }
}
