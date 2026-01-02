package com.yirankuma.yrcloudbackpack.Schemas;

import java.util.LinkedHashMap;
import java.util.Map;

public class Schemas {
    // 定义背包表结构
    public static Map<String, String> inventorySchema = new LinkedHashMap<String, String>() {{
        put("player_id", "VARCHAR(36) PRIMARY KEY");     // 玩家UUID
        put("player_name", "VARCHAR(16)");               // 玩家名称
        put("inventory_data", "LONGTEXT");               // 背包物品数据
        put("armor_data", "TEXT");                       // 装备数据
        put("offhand_data", "TEXT");                       // 装备数据
        put("last_server", "VARCHAR(50)");               // 最后所在服务器
        put("last_updated", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");
    }};
}
