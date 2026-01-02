package com.yirankuma.yrcloudbackpack.EventListener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import com.yirankuma.yrcloudbackpack.Manager.InventoryManager;
import com.yirankuma.yrcloudbackpack.YRCloudBackpack;

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

//    @EventHandler
//    public void onDataPacketReceive(DataPacketReceiveEvent event) {
//        DataPacket dp = event.getPacket();
//        if (dp instanceof NeteaseLoginPacket){
//            System.out.println(((NeteaseLoginPacket) dp).proxyUid);
//        }
//    }
}
