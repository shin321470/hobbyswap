package com.hobbyswap.controller;

import com.hobbyswap.service.FileRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 「檔案資料管理」頁面的控制器。
 *
 * <p>放在 /admin/records 之下，沿用既有的管理員權限保護
 * (見 {@code SecurityConfig} 的 adminFilterChain，/admin/** 需要 ADMIN 角色)。</p>
 *
 * <p>所有動作都委派給 {@link FileRecordService}，資料實際存於 data/records.csv 純文字檔。</p>
 */
@Controller
@RequestMapping("/admin/records")
public class FileRecordController {

    @Autowired
    private FileRecordService fileRecordService;

    /** 讀取：顯示檔案中所有資料。 */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("records", fileRecordService.findAll());
        return "admin/records";
    }

    /** 寫入：新增一筆資料到檔案。 */
    @PostMapping("/add")
    public String add(@RequestParam String name,
                      @RequestParam(required = false) String category,
                      @RequestParam(required = false) String note) {
        fileRecordService.add(name, category, note);
        return "redirect:/admin/records";
    }

    /** 修改：更新指定 id 的資料。 */
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) String note) {
        fileRecordService.update(id, name, category, note);
        return "redirect:/admin/records";
    }

    /** 刪除：移除指定 id 的資料。 */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        fileRecordService.delete(id);
        return "redirect:/admin/records";
    }
}
