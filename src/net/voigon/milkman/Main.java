package net.voigon.milkman;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.Files;
import com.kirelcodes.miniaturepets.config.Config;
import com.kirelcodes.miniaturepets.loader.PetLoader;
import com.kirelcodes.miniaturepets.mob.Mob;
import com.kirelcodes.miniaturepets.mob.MobManager;
import com.kirelcodes.miniaturepets.pets.PetContainer;
import com.kirelcodes.miniaturepets.utils.EntityAttributes;

import lombok.Getter;

public class Main extends JavaPlugin implements Listener {

	Set<MilkMan>
			mobs = new HashSet<>();
	
	@Getter
	PetContainer
			container;
	
	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);
		
		try {
			this.container = PetLoader.loadFromFile(loadModelByName("Milker", this));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player player = (Player) sender;
		
		if (args.length != 0) {
			player.getLocation().getChunk().unload();
			return false;
		}
		
		getContainer();
		MilkMan mob = new MilkMan(player.getLocation(), this);
		mobs.add(mob);
		
		return false;
	}
	
	public static Mob getMob(LivingEntity le)
	{
		int id = -1;
        try
        {
            if (EntityAttributes.miniatureId.hasAttribute(le))
                id = (int) EntityAttributes.miniatureId.getValue(le);
        }
        catch(Exception ex)
        {
            if(Config.isDebug())
                ex.printStackTrace();
        }
        return MobManager.getMob(id);
		
	}
	public static File loadModelByName(String name, JavaPlugin plugin) throws IOException
    {
        File tempDir = Files.createTempDir();
        File tempFile = new File(tempDir, name + ".mpet");
        FileOutputStream out = new FileOutputStream(tempFile);
        IOUtils.copy(plugin.getResource("models/" + name + ".mpet"), out);
        return tempFile;
    }
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkUnload(ChunkUnloadEvent event) {
		System.out.println("Main onChunkUnload");

		for (Entity entity : event.getChunk().getEntities()) {
			if (entity.getType() == EntityType.ARMOR_STAND) continue;
			if (!(entity instanceof LivingEntity)) continue;
			
			Mob mob = getMob((LivingEntity) entity);
			
			if (!(mob instanceof MilkMan)) continue;
			MilkMan man = (MilkMan) mob;
			try {
				System.out.println("Main saving data");
				man.saveData();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		System.out.println("Main onChunkLoad");

		for (Entity entity : event.getChunk().getEntities()) {
			if (entity.getType() != EntityType.ARMOR_STAND) continue;
			ArmorStand armorStand = (ArmorStand) entity;
			if (armorStand.getChestplate() == null || armorStand.getChestplate().getType() != 
					Material.BARRIER) continue;
			
			try {
				System.out.println("Main loading");
				mobs.add(MilkMan.getMilky(armorStand, this));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
		

}
