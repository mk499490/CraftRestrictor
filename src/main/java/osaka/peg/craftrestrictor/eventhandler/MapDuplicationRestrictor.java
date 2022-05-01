package osaka.peg.craftrestrictor.eventhandler;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.CartographyInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import osaka.peg.craftrestrictor.CraftRestrictor;

import java.util.ArrayList;
import java.util.List;

public class MapDuplicationRestrictor implements Listener {
	CraftRestrictor main;
	ItemStack restrictedIndicator;
	List<Integer> restrictedMaps;

	// プラグイン有効化時の初期処理
	public MapDuplicationRestrictor(CraftRestrictor main) {
		this.main = main;

		// Config取得
		restrictedMaps = main.getConfig().getIntegerList("RestrictedMaps");
		main.getLogger().info("Restricted map(s):");
		for (int restrictedMap: restrictedMaps) {
			main.getLogger().info("  - " + restrictedMap);
		}

		// バリアブロックの生成（複製不可のアイコン）
		restrictedIndicator = new ItemStack(Material.BARRIER);
		ItemMeta restrictedIndicatorMeta = restrictedIndicator.getItemMeta();
		restrictedIndicatorMeta.setDisplayName("§c§lこのマップは複製できません！");
		List<String> lore = new ArrayList<>();
		lore.add("ペグショップで購入した装飾用マップは複製できません！");
		lore.add("必要な枚数分購入してください！");
		restrictedIndicatorMeta.setLore(lore);
		restrictedIndicator.setItemMeta(restrictedIndicatorMeta);
	}

	// TODO: 特定ユーザは複製防止をバイパスできるように

	// 製図台用
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getInventory() instanceof CartographyInventory) {
			// 複製しようとしたら、結果の場所を複製不可のアイコンにする
			Bukkit.getScheduler().runTaskLater(main, () -> {
				try {
					if (event.getInventory().getItem(0).getItemMeta() instanceof MapMeta && event.getInventory().getItem(1).getType().equals(Material.MAP)) {
						if (restrictedMaps.contains(((MapMeta) event.getInventory().getItem(0).getItemMeta()).getMapId())) {
							event.getInventory().setItem(2, restrictedIndicator);
						}
					}
				} catch (NullPointerException exception) {
					// めんどくさいので握りつぶす
				}
			}, 1);

			// 複製不可のアイコンを拾えないようにする
			if (event.getCurrentItem() != null && event.getCurrentItem().equals(restrictedIndicator)) {
				event.setCancelled(true);
			}

			// (1tickより早く拾われたとき用)複製された複製不可のマップを拾うことを阻止する
			if (event.getRawSlot() == 2 && event.getCurrentItem().getItemMeta() instanceof MapMeta && restrictedMaps.contains(((MapMeta) event.getCurrentItem().getItemMeta()).getMapId())) {
				event.setCancelled(true);
			}
		}
	}

	// 完成後のアイテムをバリアブロックにセット
	@EventHandler
	public void onPrepareCraftItem(PrepareItemCraftEvent event) {
		ItemMeta resultItemMeta;
		try {
			resultItemMeta = event.getInventory().getResult().getItemMeta();
		} catch (NullPointerException exception) {
			return;
		}
		if (resultItemMeta instanceof MapMeta) {
			if (restrictedMaps.contains(((MapMeta) resultItemMeta).getMapId())) {
				event.getInventory().setResult(restrictedIndicator);
			}
		}
	}

	// 複製不可のアイコンを拾えないようにする
	@EventHandler
	public void onCraftItem(CraftItemEvent event) {
		if (event.getInventory().getResult() != null && event.getInventory().getResult().equals(restrictedIndicator)) {
			event.setCancelled(true);
		}
	}
}
