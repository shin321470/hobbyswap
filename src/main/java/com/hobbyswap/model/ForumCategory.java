package com.hobbyswap.model;

public enum ForumCategory {
    TECH("科技數碼"),
    FASHION("流行時尚"),
    GAMING("遊戲動漫"),
    MUSIC("音樂樂器"),
    GENERAL("綜合討論");

    private final String display;
    ForumCategory(String display) { this.display = display; }
    public String getDisplay() { return display; }
}