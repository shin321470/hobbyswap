package com.hobbyswap.service;

import com.hobbyswap.model.FileRecord;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 以「純文字 CSV 檔案」作為資料來源的服務層。
 *
 * <p>本服務完全不使用資料庫 (JPA)，所有「寫入 / 讀取 / 修改 / 刪除」
 * 都直接以 Java 原生檔案 I/O ({@link BufferedReader} / {@link BufferedWriter})
 * 操作專案根目錄下的 {@code data/records.csv}。</p>
 *
 * <p>目的：明確、無爭議地示範老師要求的「資料必須記錄於檔案中」。</p>
 */
@Service
public class FileRecordService {

    /** 資料檔位置：專案根目錄下的 data/records.csv (與 H2 資料庫檔放在同一個 data 資料夾)。 */
    private static final Path DATA_FILE = Paths.get("data", "records.csv");

    /** CSV 標題列。 */
    private static final String HEADER = "id,name,category,note,updatedAt";

    // ===== 讀取：把整個檔案讀進記憶體，轉成物件清單 =====
    public List<FileRecord> findAll() {
        List<FileRecord> records = new ArrayList<>();
        if (!Files.exists(DATA_FILE)) {
            return records; // 檔案還沒建立 -> 回傳空清單
        }
        try (BufferedReader reader = Files.newBufferedReader(DATA_FILE, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {        // 第一列是標題，略過
                    firstLine = false;
                    continue;
                }
                if (line.isBlank()) {
                    continue;
                }
                records.add(parseLine(line));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("讀取資料檔失敗: " + DATA_FILE, e);
        }
        return records;
    }

    // ===== 寫入 (新增)：產生新 id，加入清單後寫回檔案 =====
    public void add(String name, String category, String note) {
        List<FileRecord> records = findAll();
        long nextId = records.stream().mapToLong(FileRecord::getId).max().orElse(0L) + 1;
        records.add(new FileRecord(nextId, safe(name), safe(category), safe(note), LocalDateTime.now()));
        writeAll(records);
    }

    // ===== 修改：找到指定 id 的資料，更新欄位後重寫整個檔案 =====
    public void update(Long id, String name, String category, String note) {
        List<FileRecord> records = findAll();
        for (FileRecord r : records) {
            if (r.getId().equals(id)) {
                r.setName(safe(name));
                r.setCategory(safe(category));
                r.setNote(safe(note));
                r.setUpdatedAt(LocalDateTime.now());
                break;
            }
        }
        writeAll(records);
    }

    // ===== 刪除：濾掉指定 id 後重寫檔案 =====
    public void delete(Long id) {
        List<FileRecord> records = findAll();
        records.removeIf(r -> r.getId().equals(id));
        writeAll(records);
    }

    // ===== 共用：把整份清單覆寫回檔案 =====
    private void writeAll(List<FileRecord> records) {
        try {
            if (DATA_FILE.getParent() != null) {
                Files.createDirectories(DATA_FILE.getParent()); // 確保 data/ 資料夾存在
            }
            try (BufferedWriter writer = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();
                for (FileRecord r : records) {
                    writer.write(toLine(r));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("寫入資料檔失敗: " + DATA_FILE, e);
        }
    }

    // ===== 物件 -> CSV 一列 =====
    private String toLine(FileRecord r) {
        return String.join(",",
                String.valueOf(r.getId()),                 // id 為數字，不需引號
                escape(r.getName()),
                escape(r.getCategory()),
                escape(r.getNote()),
                escape(r.getUpdatedAt() == null ? "" : r.getUpdatedAt().toString()));
    }

    // ===== CSV 一列 -> 物件 =====
    private FileRecord parseLine(String line) {
        String[] cols = splitCsv(line);
        Long id = Long.parseLong(cols[0].trim());
        String name = cols.length > 1 ? cols[1] : "";
        String category = cols.length > 2 ? cols[2] : "";
        String note = cols.length > 3 ? cols[3] : "";
        LocalDateTime updatedAt = (cols.length > 4 && !cols[4].isBlank())
                ? LocalDateTime.parse(cols[4]) : null;
        return new FileRecord(id, name, category, note, updatedAt);
    }

    /** 把使用者輸入中的換行去掉，避免破壞「一筆資料一行」的格式。 */
    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r", " ").replace("\n", " ").trim();
    }

    /** 用雙引號包住欄位，並把內部的雙引號變成兩個 (標準 CSV 跳脫規則)。 */
    private String escape(String value) {
        String v = (value == null) ? "" : value;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    /**
     * 解析一行 CSV，正確處理「被雙引號包住、且內部含逗號或引號」的欄位。
     * 回傳的欄位已經是去除引號、還原後的最終字串。
     */
    private String[] splitCsv(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); // 連續兩個引號 -> 一個引號字元
                    i++;
                } else {
                    inQuotes = !inQuotes; // 切換「是否在引號內」
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }
}
