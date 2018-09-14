package net.voigon.milkman;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.kirelcodes.miniaturepets.mob.pathfinding.Pathfinder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MilkManPathfinder extends Pathfinder {
	
	private final MilkMan
			pet;
		
	Cow
			target;
	
	Set<Cow>
			cows = new HashSet<>();
	
	Player
			targetPlayer;
	
	int
			assholeCount;
	
	boolean
			stopAsshole;
	
	Item
			currentItem;
	
	TargetType
			targetType;
	
	public enum TargetType {
		GOLD, COW, PLAYER
		
	}
	
	@Override
	public boolean shouldStart() {		
		for (Entity entity : pet.getLocation().getWorld().getNearbyEntities(pet.getLocation(), 10, 10, 10)) 
			if (entity.getType() == EntityType.COW)
				return true;
		
		return false;
	}
	
	void setTargetLocation(Location location, TargetType targetType) {
		this.targetType = targetType;
		
		try {
			pet.setTargetLocation(location);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	boolean isTargetValid() {
		if (targetType == null) return true;
		
		if (targetType == TargetType.COW) {
			if (target == null || target.isDead()) return false;
			
		} else if (targetType == TargetType.PLAYER) {
			if (targetPlayer == null || !targetPlayer.isOnline()) return false;
			
		} else if (targetType == TargetType.GOLD) {
			if (currentItem == null || currentItem.isDead()) return false;
			
		}
		
		return true;
	}
	
	void resetTargets() {
		this.target = null;
		this.targetPlayer = null;
		this.currentItem = null;
		
	}
	
	@Override
	public void updateTask() {
		boolean valid = isTargetValid();
		if (pet.onTargetLocation() && valid) return;
		
		System.out.println("MilkManPathfinder updateTask");
		
		if (!valid) 
			resetTargets();
		
		if (currentItem != null) {
			currentItem.remove();
			
			for (ItemStack item : pet.getInventory()) 
				pet.getLocation().getWorld().dropItem(pet.getLocation(), item);
			
			pet.getInventory().clear();
			
			currentItem = null;
			
		}
		
		Item item = findNextIngot();
		
		if (item != null) {
			setTargetLocation(item.getLocation(), TargetType.GOLD);
			currentItem = item;
			return;
		}
		
		if (targetPlayer != null) {
			if (!targetPlayer.isOnline()) // If the target have left
				setTargetPlayer(null);
			else if (stopAsshole) { // If the milkman is done with being a maniak
				setTargetLocation(targetPlayer.getLocation(), TargetType.PLAYER);		
				return;
			}
			
		}
		
		if (target == null) {
			System.out.println("MilkManPathfinder updateTask target is null");
			updateCowTarget();
			return;
		}
		
		// Reached the cow
		Cow cow = (Cow) target;
		if (!cow.isDead()) {
			// Cow is not dead
			cows.add(cow);
			pet.getInventory().addItem(new ItemStack(Material.MILK_BUCKET));

			// The asshole mechanic will make the player wait up to 5 cows after paying the
			// gold if the milkman didn't finish
			if (targetPlayer != null) {
				assholeCount++;
				if (assholeCount == 5)
					stopAsshole = true;
			}

		}

		updateCowTarget();

	}
	
	void updateCowTarget() {
		this.target = getNextCow();
		
		if (this.target != null) {
			setTargetLocation(target.getLocation(), TargetType.COW);
			return;
		}		

	}
	
	void setTargetPlayer(Player player) {
		this.targetPlayer = player;
		this.stopAsshole = false;
		this.assholeCount = 0;
		
	}
	
	Item findNextIngot() {
		for (Entity entity : pet.getLocation().getWorld().getNearbyEntities(pet.getLocation(), 10, 10, 10)
				.stream().filter((entity) -> ((entity instanceof Item) && ((Item)entity).getItemStack()
						.getType() == Material.GOLD_INGOT)).collect(Collectors.toSet())) {
			return (Item) entity;
		}
		
		return null;
	}
	
	Cow getNextCow() {
		for (Entity entity : pet.getLocation().getWorld().getNearbyEntities(pet.getLocation(), 10, 10, 10)) 
			if (entity.getType() == EntityType.COW && !cows.contains(entity)) 
				return (Cow) entity;
				
		return null;
	}

}
