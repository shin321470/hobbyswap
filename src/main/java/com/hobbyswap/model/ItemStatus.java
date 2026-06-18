package com.hobbyswap.model;

/**
 * 商品狀態。
 *
 * <p>用 enum 取代原本散落各處的字串 ("ON_SALE"、"SOLD"…)，
 * 編譯期就能檢查、不會打錯字，也更符合物件導向。</p>
 *
 * <p>實際存入資料庫時以名稱字串儲存 (見 {@code Item} 的 {@code @Enumerated(EnumType.STRING)})，
 * 與舊資料的欄位值完全相容。</p>
 */
public enum ItemStatus {
    ON_SALE,  // 上架中
    SOLD,     // 已售出
    BANNED,   // 遭管理員強制下架
    DELETED   // 賣家自行刪除
}
