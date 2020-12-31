package CA.ROBBYNATOS;

import org.bukkit.Material;
        import org.bukkit.plugin.java.JavaPlugin;

public class FlyingCarpet extends JavaPlugin {

    private CarpetMovementListener carpetListener;

    public FlyingCarpet(){
        this.carpetListener = new CarpetMovementListener(this);
    }

    @Override
    public void onEnable(){
        super.onEnable();
        getServer().getPluginManager().registerEvents(carpetListener, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}
