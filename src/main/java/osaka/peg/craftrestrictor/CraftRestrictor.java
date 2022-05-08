package osaka.peg.craftrestrictor;

import osaka.peg.craftrestrictor.restrictors.MapDuplicationRestrictor;
import org.bukkit.plugin.java.JavaPlugin;

public final class CraftRestrictor extends JavaPlugin {

	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new MapDuplicationRestrictor(this), this);
		this.getLogger().info("Enabled");
	}

	@Override
	public void onDisable() {
		getServer().getLogger().info("Disabled");
	}
}