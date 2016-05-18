package net.poweredbyhate.eggrenade;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class Eggrenade extends JavaPlugin implements Listener {

    /*
    This plugin will be done in one class to demonstrate how stupid it is to be premium.
     */

    public static Economy econ = null;

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        checkVault();
    }

    @EventHandler
    public void onHit(ProjectileHitEvent ev) {
        if (ev.getEntity() instanceof Egg && ev.getEntity().getShooter() instanceof Player) {
            Player p = (Player) ev.getEntity().getShooter();
            if (p.hasPermission("eggrenade.active")) goBoom(p, ev.getEntity());
        }
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent ev) {
        if (ev.getEntity() instanceof Egg && ev.getEntity().getShooter() instanceof Player) {
            if (((Player) ev.getEntity().getShooter()).hasPermission("eggrenade.active")) {
                setTrails(ev.getEntity());
            }
        }
    }

    public void goBoom(Player p, Entity ent) {
        if (econ != null) {
            if (econ.getBalance(p) < getConfig().getInt("Cost")) {
                return;
            }
            EconomyResponse e = econ.withdrawPlayer(p, getConfig().getInt("Cost"));
            if (e.transactionSuccess()) {
                doRealBoom(ent);
                return;
            }
            return;
        }
        doRealBoom(ent);
    }

    public void doRealBoom(final Entity ent) {
        final Location l = ent.getLocation();
        new BukkitRunnable() {
            @Override
            public void run() {
                ent.getWorld().createExplosion(l.getX(),l.getY(),l.getZ(), getConfig().getInt("Power"), getConfig().getBoolean("Destroy"), getConfig().getBoolean("Destroy"));
            }
        }.runTaskLater(this, getConfig().getInt("Delay") * 20);
    }

    public void checkVault() {
        if (getConfig().getInt("Cost") != 0) {
            if (!setupEconomy()) {
                getLogger().log(Level.WARNING, "In order to use the economy support, you must have vault.");
                getLogger().log(Level.WARNING, "Please set the cost to 0 in the config.yml to disable economy support");
                getServer().getPluginManager().disablePlugin(this);
            }
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public void setTrails(final Projectile projectile) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (projectile.isDead() || projectile.isOnGround()) {
                    cancel();
                    return;
                }
                projectile.getWorld().playEffect(projectile.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
            }
        }.runTaskAsynchronously(this);
    }

}
