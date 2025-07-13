package com.yirankuma.yrcloudbackpack.EventListener;

import cn.nukkit.Player;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.AttributeModifier;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerToggleSprintEvent;
import com.yirankuma.yrcloudbackpack.Manager.InventoryManager;
import com.yirankuma.yrcloudbackpack.YRCloudBackpack;
import com.yirankuma.yrdatabase.YRDatabase;

import java.util.UUID;

import static com.yirankuma.yrcloudbackpack.Schemas.Schemas.inventorySchema;

public class EventListener implements Listener {
    public InventoryManager inventoryManager;

    public EventListener() {
        inventoryManager = YRCloudBackpack.getInstance().getInventoryManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        YRCloudBackpack.getInstance().getInventoryManager().loadPlayerInventory(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        inventoryManager.savePlayerInventory(player);
        inventoryManager.persistPlayerInventory(player);
    }
}
