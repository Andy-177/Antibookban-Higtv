package paper.plugin.antibookban;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryMoveItemEvent; // 添加这个导入
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import net.kyori.adventure.text.Component;

public final class Antibookban extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {}

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block != null && block.getType() == Material.SHULKER_BOX) {
            ShulkerBox shulkerBox = (ShulkerBox) block.getState();
            checkShulkerBox(shulkerBox.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() == InventoryType.SHULKER_BOX) {
            checkShulkerBox(event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getType() == InventoryType.SHULKER_BOX) {
            checkShulkerBox(event.getInventory());
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        ItemStack pickedItem = event.getItem().getItemStack();
        if (pickedItem.getType() == Material.SHULKER_BOX) {
            // 检查是否是玩家
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity(); // 强制转换为Player
                Block block = event.getItem().getLocation().getBlock();
                if (block.getType() == Material.SHULKER_BOX) {
                    ShulkerBox shulkerBox = (ShulkerBox) block.getState();
                    Inventory inventory = Bukkit.createInventory(null, 27, Component.text("Shulker Box"));

                    // 复制原始潜影盒的内容到库存
                    inventory.setContents(shulkerBox.getInventory().getContents());

                    // 打开潜影盒给玩家
                    player.openInventory(inventory);

                    // 延迟关闭潜影盒
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            // 检查内容并清除成书
                            checkShulkerBox(inventory);

                            // 关闭潜影盒
                            player.closeInventory();
                        }
                    }.runTaskLater(this, 20L); // 20 ticks = 1秒
                }
            }
        }
    }

    // 新增方法：阻止漏斗向潜影盒传输成书
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        if (event.getDestination().getType() == InventoryType.SHULKER_BOX) {
            ItemStack item = event.getItem();
            if (item.getType() == Material.WRITTEN_BOOK) {
                event.setCancelled(true); // 取消传输
                getLogger().info("Blocked a written book from being transferred to a shulker box.");
            }
        }
    }

    private void checkShulkerBox(Inventory inventory) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.WRITTEN_BOOK) {
                inventory.clear(i); // 清除成书
                getLogger().info("Removed a written book from the shulker box.");
            }
        }
    }
}
