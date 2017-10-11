# MidasTouch

###Compile and run this on your bukkit server.

Put the exported jar file into the plugin folder.

Stops players from breaking blocks. Instead, replaces the block with another. Default is gold blocks. Shout out to King Midas.

Limited MOTD funtionality. Sends the player a message when they join the server. Server messages support inline color codes.

- First time joining message
- Returning user message

All config saved in config.yml . Changes to this can be reloaded.

--------------------

### Commands

Reload the configuration file from disk and update the values in the plugin

    midastouch reload

Change the block which broken blocks are turned into. Must be valid block as defined in the plugin. 

    midastouch block <block>  


Change the message shown to a returning user

    midastouch join <msg>  

Change the message shown to a new user

    midastouch firstjoin <msg>  
    
Disable or reenable the block swapping temporarily

    midastouch.disable
    midastouch.enable

------------------------

### Permissions


Commands are protected with permissions. The top level permission is midastouch.command which allows a user to  access the midastouch subcommands.

Use pex or another permissions manager to add these to your bukkit server for a user.

**e.g.** /pex user laycraft add midastouch.command


```
Permission list.

midastouch.command
midastouch.reload  
midastouch.block  
midastouch.join  
midastouch.firstjoin
midastouch.disable
midastouch.enable
```