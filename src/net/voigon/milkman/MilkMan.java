package net.voigon.milkman;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.kirelcodes.miniaturepets.mob.Mob;
import com.kirelcodes.miniaturepets.utils.NBTRW;

import lombok.Getter;

public class MilkMan extends Mob implements InventoryHolder {

	@Getter
	private Inventory inventory;
	
	public MilkMan(Location arg0, Main plugin) {
		super(arg0, plugin.getContainer().getStructure(), EntityType.CHICKEN);

		setCustomName(new Random().nextInt(100000) + "");
		setVisible(false);
		
		inventory = Bukkit.createInventory(this, 9 * 4);
		getPathManager().addPathfinder(new MilkManPathfinder(this));
		
	}
	
	/**
	 * Saves and kills the mob
	 * @throws Exception 
	 */
	public void saveData() throws Exception
	{
		ItemStack storer = NBTRW.writeNBT(
				NBTRW.writeNBT(new ItemStack(Material.BARRIER), "health",(int) getNavigator().getHealth()),
				"name",
				getCustomName()
				);
		
		ArmorStand stander = (ArmorStand)getLocation().getWorld().spawnEntity(getLocation(), EntityType.ARMOR_STAND);
		stander.setChestplate(storer);
		stander.setMarker(true);
		stander.setVisible(false);
		stander.setGravity(false);
		remove();
	}
	
	public static MilkMan getMilky(ArmorStand stand, Main plugin) throws Exception
	{
		ItemStack chestplate= stand.getChestplate();
		if(chestplate == null || chestplate.getType() != Material.BARRIER)
			return null;
		int health = NBTRW.getNBTInt(chestplate, "health");
		String name = NBTRW.getNBTString(chestplate, "name");
		MilkMan man = new MilkMan(stand.getLocation(), plugin);
		stand.remove();
		man.getNavigator().setHealth(health);
		man.setCustomName(name);
		man.setVisible(true);
		return man;
	}
	
}
