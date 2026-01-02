package com.yirankuma.yrcloudbackpack.Manager;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.inventory.PlayerOffhandInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.StringItem;
import cn.nukkit.nbt.tag.CompoundTag;
import com.google.gson.Gson;
import com.yirankuma.yrcloudbackpack.YRCloudBackpack;
import com.yirankuma.yrdatabase.YRDatabase;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.yirankuma.yrcloudbackpack.Schemas.Schemas.*;

public class InventoryManager {
    private final YRCloudBackpack plugin;
    private final String schemaName;

    public InventoryManager(YRCloudBackpack plugin) {
        this.plugin = plugin;
        this.schemaName = plugin.getInventorySchemaName();
    }

    public void loadPlayerInventory(Player player) {
        YRDatabase.getDatabaseManager().smartGet(schemaName, player, inventorySchema)
                .thenAccept(data -> {
                    if (data != null && data.containsKey("inventory_data")) {
                        // 有缓存数据，加载到玩家背包
                        String inventoryJson = (String) data.get("inventory_data");
                        String armorJson = (String) data.get("armor_data");
                        String offhandJson = (String) data.get("offhand_data");

                        loadInventoryFromJson(player, inventoryJson, armorJson, offhandJson);
                        player.sendMessage("§a背包数据已从云端加载！");
                    } else {
                        // 没有缓存数据，保存当前背包作为初始数据
                        savePlayerInventory(player);
                        player.sendMessage("§e首次使用跨服背包，当前背包已同步到云端。");
                    }
                })
                .exceptionally(throwable -> {
                    player.sendMessage("§c背包数据加载失败：" + throwable.getMessage());
                    Server.getInstance().getLogger().error("加载玩家背包失败: " + player.getName(), throwable);
                    return null;
                });
    }

    public void savePlayerInventory(Player player) {
        String playerName = player.getName(); // 获取玩家名称
        String inventoryJson = serializeInventoryToJson(player.getInventory());
        String armorJson = serializeArmorToJson(player.getInventory());
        String offhandJson = serializeOffhandToJson(player.getOffhandInventory());

        // 包含所有需要保存的数据
        Map<String, Object> inventoryData = Map.of(
                "player_name", playerName,  // 添加玩家名称
                "inventory_data", inventoryJson,
                "armor_data", armorJson,
                "offhand_data", offhandJson
                // "last_updated", new Timestamp(System.currentTimeMillis())  // 使用Timestamp对象
                // last_server 暂时不写，按你的要求
        );

        // 缓存设置为永久（-1）
        YRDatabase.getDatabaseManager().smartSet(schemaName, player, inventoryData, inventorySchema, -1)
                .thenAccept(success -> {
                    if (success) {
                        player.sendMessage("§a背包数据已保存到云端！");
                    } else {
                        player.sendMessage("§c背包数据保存失败！");
                    }
                })
                .exceptionally(throwable -> {
                    player.sendMessage("§c背包数据保存异常：" + throwable.getMessage());
                    Server.getInstance().getLogger().error("保存玩家背包失败: " + player.getName(), throwable);
                    return null;
                });
    }

    private String serializeInventoryToJson(PlayerInventory inventory) {
        Map<String, Object> inventoryData = new HashMap<>();

        // 遍历所有背包槽位
        for (Map.Entry<Integer, Item> entry : inventory.slots.entrySet()) {
            int slot = entry.getKey();
            Item item = entry.getValue();

            // 跳过空物品
            if (item == null || item.getId() == 0) {
                continue;
            }

            Map<String, Object> itemData = new HashMap<>();
            itemData.put("identifier", item.getNamespaceId());
            itemData.put("damage", item.getDamage());
            itemData.put("count", item.getCount());
            itemData.put("name", item.getName());

            // 处理NBT数据
            if (item.hasCompoundTag()) {
                byte[] compoundTag = item.getCompoundTag();
                if (compoundTag != null && compoundTag.length > 0) {
                    // 将字节数组转换为Base64字符串存储
                    itemData.put("nbt", java.util.Base64.getEncoder().encodeToString(compoundTag));
                } else {
                    itemData.put("nbt", null);
                }
            } else {
                itemData.put("nbt", null);
            }

//            // 添加其他可能需要的属性
//            itemData.put("customName", item.getCustomName());
//            itemData.put("lore", item.getLore());

            // 使用槽位作为key
            inventoryData.put(String.valueOf(slot), itemData);
        }

//        // 添加背包元数据
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("size", inventory.getSize());
//        metadata.put("serializedAt", System.currentTimeMillis());
//        inventoryData.put("_metadata", metadata);

        // 转换为JSON字符串
        try {
            return new Gson().toJson(inventoryData);
        } catch (Exception e) {
            Server.getInstance().getLogger().error("序列化背包数据失败", e);
            return "";
        }
    }

    private String serializeArmorToJson(PlayerInventory inventory) {
        Map<String, Object> armorData = new HashMap<>();

        // 获取装备槽位的物品
        Item[] armorContents = inventory.getArmorContents();
        String[] armorSlots = {"helmet", "chestplate", "leggings", "boots"};

        for (int i = 0; i < armorContents.length && i < armorSlots.length; i++) {
            Item item = armorContents[i];

            if (item == null || item.getId() == 0) {
                armorData.put(armorSlots[i], null);
                continue;
            }

            Map<String, Object> itemData = new HashMap<>();
            itemData.put("identifier", item.getNamespaceId());
            itemData.put("damage", item.getDamage());
            itemData.put("count", item.getCount());
            itemData.put("name", item.getName());

            // 处理NBT数据
            if (item.hasCompoundTag()) {
                byte[] compoundTag = item.getCompoundTag();
                if (compoundTag != null && compoundTag.length > 0) {
                    itemData.put("nbt", java.util.Base64.getEncoder().encodeToString(compoundTag));
                } else {
                    itemData.put("nbt", null);
                }
            } else {
                itemData.put("nbt", null);
            }

            itemData.put("customName", item.getCustomName());
            itemData.put("lore", item.getLore());

            armorData.put(armorSlots[i], itemData);
        }


//        // 添加装备元数据
//        Map<String, Object> metadata = new HashMap<>();
//        metadata.put("serializedAt", System.currentTimeMillis());
//        armorData.put("_metadata", metadata);

        try {
            return new Gson().toJson(armorData);
        } catch (Exception e) {
            Server.getInstance().getLogger().error("序列化装备数据失败", e);
            return "";
        }
    }


    private String serializeOffhandToJson(PlayerOffhandInventory offhandInventory) {
        Map<String, Object> armorData = new HashMap<>();

        // 获取装备槽位的物品
        Item offHandItem = offhandInventory.getItem(0);
        String slotKey = "offhand";
        if (offHandItem == null || offHandItem.getId() == 0) {
            armorData.put(slotKey, null);
        } else {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("identifier", offHandItem.getNamespaceId());
            itemData.put("damage", offHandItem.getDamage());
            itemData.put("count", offHandItem.getCount());
            itemData.put("name", offHandItem.getName());

            // 处理NBT数据
            if (offHandItem.hasCompoundTag()) {
                byte[] compoundTag = offHandItem.getCompoundTag();
                if (compoundTag != null && compoundTag.length > 0) {
                    itemData.put("nbt", java.util.Base64.getEncoder().encodeToString(compoundTag));
                } else {
                    itemData.put("nbt", null);
                }
            } else {
                itemData.put("nbt", null);
            }

            itemData.put("customName", offHandItem.getCustomName());
            itemData.put("lore", offHandItem.getLore());

            armorData.put(slotKey, itemData);
        }
        try {
            return new Gson().toJson(armorData);
        } catch (Exception e) {
            Server.getInstance().getLogger().error("序列化副手数据失败", e);
            return "";
        }
    }

    private void loadInventoryFromJson(Player player, String inventoryJson, String armorJson, String offhandJson) {
        try {
            // 加载背包数据
            PlayerInventory inventory = player.getInventory();
            PlayerOffhandInventory offhandInventory = player.getOffhandInventory();
            if (inventoryJson != null && !inventoryJson.isEmpty()) {
                Gson gson = new Gson();
                Map<String, Object> inventoryData = gson.fromJson(inventoryJson, Map.class);
                inventory.clearAll(); // 清空当前背包

                for (Map.Entry<String, Object> entry : inventoryData.entrySet()) {
                    String key = entry.getKey();

                    // 跳过元数据
                    if (key.equals("_metadata")) {
                        continue;
                    }

                    try {
                        int slot = Integer.parseInt(key);
                        Map<String, Object> itemData = (Map<String, Object>) entry.getValue();

                        Item item = createItemFromData(itemData);
                        if (item != null) {
                            inventory.setItem(slot, item);
                        }
                    } catch (NumberFormatException e) {
                        // 忽略非数字槽位
                    }
                }
            }

            // 加载装备数据
            if (armorJson != null && !armorJson.isEmpty()) {
                Gson gson = new Gson();
                Map<String, Object> armorData = gson.fromJson(armorJson, Map.class);

                String[] armorSlots = {"helmet", "chestplate", "leggings", "boots"};
                Item[] armorItems = new Item[4];

                for (int i = 0; i < armorSlots.length; i++) {
                    Object itemDataObj = armorData.get(armorSlots[i]);
                    if (itemDataObj != null) {
                        Map<String, Object> itemData = (Map<String, Object>) itemDataObj;
                        armorItems[i] = createItemFromData(itemData);
                    }
                }

                inventory.setArmorContents(armorItems);
            }
            // 加载副手
            if (offhandJson != null && !offhandJson.isEmpty()) {
                Gson gson = new Gson();
                Map<String, Object> offhandData = gson.fromJson(offhandJson, Map.class);

                String slotName = "offhand";
                Item offhandItem = null;

                Object itemDataObj = offhandData.get(slotName);
                if (itemDataObj != null) {
                    Map<String, Object> itemData = (Map<String, Object>) itemDataObj;
                    offhandItem = createItemFromData(itemData);
                }
                if (offhandItem != null) {
                    offhandInventory.setItem(0, offhandItem);
                }
            }

        } catch (Exception e) {
            Server.getInstance().getLogger().error("反序列化背包数据失败", e);
        }
    }

    private Item createItemFromData(Map<String, Object> itemData) {
        try {
            String identifier = itemData.get("identifier").toString();
            int damage = ((Double) itemData.get("damage")).intValue();
            int count = ((Double) itemData.get("count")).intValue();

            Item item = Item.fromString(identifier);
            item.setDamage(damage);
            item.setCount(count);

//            // 设置自定义名称
//            String customName = (String) itemData.get("customName");
//            if (customName != null && !customName.isEmpty()) {
//                item.setCustomName(customName);
//            }
//
//            // 设置描述
//            Object loreObj = itemData.get("lore");
//            if (loreObj instanceof List) {
//                List<String> lore = (List<String>) loreObj;
//                item.setLore(lore.toArray(new String[0]));
//            }

            // 恢复NBT数据
            String nbtBase64 = (String) itemData.get("nbt");
            if (nbtBase64 != null && !nbtBase64.isEmpty()) {
                try {
                    byte[] nbtData = java.util.Base64.getDecoder().decode(nbtBase64);
                    item.setCompoundTag(nbtData);
                } catch (Exception e) {
                    Server.getInstance().getLogger().warning("恢复物品NBT数据失败: " + e.getMessage());
                }
            }

            return item;
        } catch (Exception e) {
            Server.getInstance().getLogger().error("创建物品失败", e);
            return null;
        }
    }

    public void persistPlayerInventory(Player player) {
        System.out.println("持久化！");
        YRDatabase.getDatabaseManager().persistAndClearCache(schemaName, player, inventorySchema)
                .thenAccept(success -> {
                    if (success) {
                        player.sendMessage("§a背包数据已持久化到数据库！");
                    } else {
                        // 假如redis未开启，则忽略失败
                        if (YRDatabase.getDatabaseManager().isRedisConnected()) {
                            player.sendMessage("§c背包数据持久化失败！ ");
                        }
                    }
                });
    }
}