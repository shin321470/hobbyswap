package com.hobbyswap.model;

import java.time.LocalDateTime;

/**
 * 純檔案儲存用的資料物件 (POJO)。
 *
 * <p>這個類別「不是」JPA Entity，刻意不使用 @Entity / 資料庫。
 * 它的每一個實例最終都會被寫入專案根目錄下的純文字檔 {@code data/records.csv}，
 * 用來明確示範老師要求的「資料必須記錄於檔案中」。</p>
 *
 * <p>採物件導向封裝 (private 欄位 + getter/setter)。</p>
 */
public class FileRecord {

    private Long id;                 // 流水號 (寫檔時自動產生)
    private String name;             // 物品名稱
    private String category;         // 分類
    private String note;             // 備註
    private LocalDateTime updatedAt; // 最後修改時間

    public FileRecord() {
    }

    public FileRecord(Long id, String name, String category, String note, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.note = note;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
