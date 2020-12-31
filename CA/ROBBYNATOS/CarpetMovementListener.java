package CA.ROBBYNATOS;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CarpetMovementListener implements Listener {

    //constants
    private final int carpetLength = 7;
    private final String FLYING_CARPET = "FlyingCarpet";
    //carpet materials
    private final Material[] carpets = {Material.CYAN_CARPET, Material.BLACK_CARPET, Material.BLUE_CARPET, Material.BROWN_CARPET,
            Material.GRAY_CARPET, Material.GREEN_CARPET, Material.LIGHT_BLUE_CARPET, Material.LIGHT_GRAY_CARPET, Material.LIME_CARPET,
            Material.MAGENTA_CARPET, Material.ORANGE_CARPET, Material.PINK_CARPET, Material.PURPLE_CARPET, Material.RED_CARPET,
            Material.WHITE_CARPET, Material.YELLOW_CARPET};

    private final int numCarpetColors = 16;

    //defines player interaction with carpets
    private Map<Player, Boolean> isMagicCarpetOn;
    private Map<Player, LinkedList<Location>> carpetCoords;
    private Map<Player, Boolean> isSneaking;
    private FlyingCarpet plugin;

    public CarpetMovementListener(FlyingCarpet plugin){
        this.isMagicCarpetOn = new HashMap<Player, Boolean>();
        this.carpetCoords = new HashMap<Player, LinkedList<Location>>();
        this.isSneaking = new HashMap<Player,Boolean>();
        this.plugin = plugin;
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event){
        Player player = event.getPlayer();
        this.isSneaking.put(player, event.isSneaking());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(isMagicCarpetOn.get(player).booleanValue()) {

            LinkedList<Location> carpet = carpetCoords.get(player);
            Location dst = event.getTo();
            Block newBlock = dst.getBlock();
            if (carpet.size() == 0) {
                carpet.push(dst);
                //select a random carpet from the array of available carpet materials
                newBlock.setMetadata(FLYING_CARPET, new FixedMetadataValue(this.plugin, true));
                newBlock.setType(carpets[ThreadLocalRandom.current().nextInt(0, numCarpetColors)]);

            }
            else{

                Location src = carpet.getFirst();
                if (hasChangedBlock(src, dst)) {

                    carpet.push(dst);
                    //select a random carpet from the array of available carpet materials
                    newBlock.setMetadata(FLYING_CARPET, new FixedMetadataValue(this.plugin, true));
                    newBlock.setType(carpets[ThreadLocalRandom.current().nextInt(0, numCarpetColors)]);
                }//allows a player to descend by sneaking
                else if(isSneaking.get(player)) {

                    Block oldBlock = src.getBlock();

                    Location newLoc = src.clone();
                    newLoc.setY(newLoc.getBlockY() - 1);
                    newBlock = newLoc.getBlock();
                    if(newBlock.getType() == Material.AIR || isCarpet(newBlock)) {
                        oldBlock.setType(Material.AIR);
                        oldBlock.removeMetadata(FLYING_CARPET, plugin);
                        carpet.remove(oldBlock.getLocation());
                        newBlock.setType(carpets[ThreadLocalRandom.current().nextInt(0, numCarpetColors)]);
                        newBlock.setMetadata(FLYING_CARPET, new FixedMetadataValue(this.plugin, true));
                        carpet.push(newLoc);
                    }
                }
                //remove blocks if the carpet is too long
                if (carpet.size() > carpetLength) {
                    Location tail = carpet.remove(carpet.size() - 1);
                    Block tailBlock = tail.getBlock();
                    tailBlock.setType(Material.AIR);
                    tailBlock.removeMetadata(FLYING_CARPET, plugin);
                }
            }
        }
    }

    /**
     * Checks whether the player has moved by at least 1 block
     *
     * @param a the first location
     * @param b the second location
     * @return whether a player has moved between location a and b
     */
    private boolean hasChangedBlock(Location a, Location b){
        return a.getBlockX() != b.getBlockX() || a.getBlockY() != b.getBlockY() || a.getBlockZ() != b.getBlockZ();
    }


    private boolean isCarpet(Block block){
        for(int i = 0; i<numCarpetColors;i++){
            if(block.getType() == carpets[i]){
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event){
        Block block = event.getBlock();
        if(block.hasMetadata(FLYING_CARPET)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Action action = event.getAction();
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        if(item == null){return;}
        //if player right clicks with a golden hoe
        if(hand == EquipmentSlot.OFF_HAND){return;}
        if((item.getType() == Material.GOLDEN_HOE
                && (action == Action.RIGHT_CLICK_AIR
                ||  action == Action.RIGHT_CLICK_BLOCK))) {
            boolean invertedCarpetState = !isMagicCarpetOn.get(player).booleanValue();
            //invert the state of their magic carpet
            isMagicCarpetOn.put(player, invertedCarpetState);
            //create a local chat message indicating this inversion

            //reset all carpet blocks to air
            if(!invertedCarpetState){
                removeCarpet(player);
            }
            player.sendMessage(ChatColor.YELLOW + "Magic Carpet: " + (invertedCarpetState ? ChatColor.GREEN + "ON": ChatColor.RED + "OFF"));

        }
    }


    /**
     * removes all magic carpet blocks created by a player, replacing them with air
     *
     * @param player the player whose carpet is being set to air
     */
    private void removeCarpet(Player player){
        LinkedList<Location> coords = carpetCoords.get(player);
        while(coords.size() > 0){
            Block carpetBlock = coords.removeFirst().getBlock();
            carpetBlock.setType(Material.AIR);
            carpetBlock.removeMetadata(FLYING_CARPET,plugin);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        removeCarpet(player);
        isMagicCarpetOn.remove(player);
        carpetCoords.remove(player);
        isSneaking.remove(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        isMagicCarpetOn.put(player, false);
        carpetCoords.put(player, new LinkedList<Location>());
        isSneaking.put(player,false);
    }


}
