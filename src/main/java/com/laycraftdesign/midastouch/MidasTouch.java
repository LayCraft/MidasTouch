package com.laycraftdesign.midastouch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MidasTouch extends JavaPlugin implements Listener {
	// general attributes are loaded into RAM the first time they are used.
	Material blockType;
	boolean active = true;
	
	@Override
	public void onEnable() {
		//register events in this class instead of an external class because the plugin is simple
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		// Save a default config file copy from config.yml. Does not overwrite an existing file.
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();// workaround to avoid a bug.
		getLogger().info("engaged.");
		
		// clear the server's RAM version of the block type.
		this.blockType = null;
		active = true; //plugin state
	}

	@Override
	public void onDisable() {
		//this is really insubstantial. All config settings are pulled in on-demand.
		getLogger().info("disengaged.");
//		saveConfig();
		//zero out globals
		this.blockType = null;
		active = false; //plugin state
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("midastouch")) {
			//permissions not checking properly
			if (args.length<1) {
				//to use the plugin there must always be one argument
				return false;
			} else if (args.length>=1) {
				args[0]= args[0].toLowerCase();
				
				String message = "";
				//get additional arguments as singular string. Will not run if args.length==1.
				for (int i=1; i<args.length; i++) message = message + " " + args[i];
				message = stringSanitize(message); //clean all user input after subcommand. includes a string trim.
				
				switch (args[0]){
					case "block":
						// if the user doesn't have the correct permission, the function sends them a message and onCommand terminates normally.
						if(!hasCommandPermission(sender, "midastouch." + args[0])) return true;
						//set block to first arg. If it fails it defaults to gold.
						if (!blockSet(message)) { 
							sender.sendMessage("Supported blocks: cake, coal, pumpkin, tnt, wood, wool, gold");
						} else {
							sender.sendMessage("Block type set to: " + message);
						}
						return true;
						
					case "disable":
						if(!hasCommandPermission(sender, "midastouch." + args[0])) return true;
						this.active = false;
						sender.sendMessage("MidasTouch block swapping disabled.");
						return true;
						
					case "enable":
						if(!hasCommandPermission(sender, "midastouch." + args[0])) return true;
						this.active = true;
						sender.sendMessage("MidasTouch block swapping enabled.");
						return true;
						
					case "firstjoin":
						//must check that argument is empty so not to set message to ""
						if(!hasCommandPermission(sender, "midastouch." + args[0])) return true;
						if(message.equals("")) {
							sender.sendMessage("Usage is /midastouch join Welcome back!");
							return true;
						}
						changeMessage("noobMessage", message);
						// Tell user message is changed and confirm that it has worked collects the message in previously used memory
						sender.sendMessage("Returning user message set to:");
						message = getConfig().getString("noobMessage", "message retrieval error!");
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message ));
						return true;
						
					case "join":
						if(!hasCommandPermission(sender, "midastouch." + args[0])) return true;
						if(message.equals("")) {
							sender.sendMessage("Usage is /midastouch firstjoin Welcome noob!");
							return true;
						}
						changeMessage("message", message);
						// Tell user message is changed and confirm that it has worked collects the message in previously used memory
						sender.sendMessage("New user message set to:");
						message = getConfig().getString("message", "message retrieval error!");
						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message ));
						return true;
						
					case "reload":
						//not working
						if(!hasCommandPermission(sender, "midastouch." + args[0])) return true;
						getLogger().info("reloading");
						onDisable();
						reloadConfig();
						getLogger().info("config reloaded");
						onEnable();
						getLogger().info("reload complete");
						return true;
						
					default:
						// fall through to the message in plugin.yml by returning false
						break;
				}
				
			}
		}
		
		getLogger().info(sender.getName() + " used a command that was not recognized.");//debugging
		// the command is malformed or something therefore...
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public boolean onPlayerJoin(PlayerJoinEvent e) {
		String message = "Problem loading message from config file.";
		// TODO: make this file data.yml or something purgeable not the main config. new way of i/o to learn. :-)
		
		if (getConfig().getBoolean("players."+ e.getPlayer().getName())) {
			// get message from config, translate alt colours 
			message = getConfig().getString("message", "Config file error.");
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			return true;
			
		} else if (!getConfig().getBoolean("players."+ e.getPlayer().getName())) {
			// get message from config, translate alt colours 
			message = getConfig().getString("noobMessage", "Config file error.");
			e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
			
			// add the new player username to the config file
			// TODO: player data could be an int that counts their logins so that we can purge infrequently joining users later.
			this.getConfig().set("players."+ e.getPlayer().getName(), true);
			this.saveConfig();
//			this.reloadConfig();
			getLogger().info("New player should be added to config.");
			return true;
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockBreak(BlockBreakEvent e) {
		
		// if the global block material is not set in RAM load it.
		if (this.blockType == null) {
			// put block type into RAM from config file if not already there. yml does not support material types.
			//Default for block() function is gold. Error in config results in gold blocks. The midas touch.
			blockSet(getConfig().getString("blockType"));
		}
		
		// check that block is not a liquid and that the plugin state is active
		if (!e.getBlock().isLiquid() && this.active) {
			//Don't turn liquid into gold. Even Midas has limits.
			//this prevents the block from being deleted. 
			e.setCancelled(true);
			//change block to gold
			e.getBlock().setType(blockType);
		}
	}

	/* This function takes a string argument about what kind of block is the current one. It writes the block type into RAM. 
	 * It returns false if there was no match for the requested blocktype.*/
	public boolean blockSet(String type) {
		if (type.isEmpty()) {
			return false;
		} else {
			// check that the type is valid. It may be any case when coming into this function
			type = stringSanitize(type).toLowerCase();
		
			// This seems really inelegant but it is a way that I can restrict user choices.
			switch (type) {
				case "cake":
					this.blockType = Material.CAKE_BLOCK;
					break;
				case "coal":
					this.blockType = Material.COAL_BLOCK;
					break;
				case "glass":
					this.blockType = Material.GLASS;
					break;
				case "pumpkin":
					this.blockType = Material.PUMPKIN;
					break;
				case "tnt":
					this.blockType = Material.TNT;
					break;
				case "wood":
					this.blockType = Material.WOOD;
					break;
				case "wool":
					this.blockType = Material.WOOL;
					break;
				case "gold":
					this.blockType = Material.GOLD_BLOCK;
					break;
				default:
					this.blockType = Material.GOLD_BLOCK; //Note: not persistent after a reload. Whatever was last successful.
					return false;
			}
			// if blocktype set properly then save the successful string for future loads from config.
			getConfig().set("blockType", type);
			saveConfig();
//			reloadConfig();
			return true;
		}
	}
	
	
	public boolean changeMessage(String location, String message) {

		//save to config file
		getConfig().set(location, message);
		saveConfig();
//		reloadConfig();
		return true;
	}

	
	private String stringSanitize(String dirty) {
		String clean = dirty.trim();
		// TODO: check for potential injections and special characters here.
		// only allow ascii.
		// trim whitespace from ends
		// check forums for caveats
		// carriage returns
		// REGEX?
		return clean;
	}
	// check permission for the command that the user wants to do. 
	private boolean hasCommandPermission(CommandSender user, String permission) {
		if (user.hasPermission(permission)) return true;
		user.sendMessage(user.getName() + " has insufficient privilege for action. Needs "+ permission);
		return false;
	}

}
