# HubParkour

Welcome to HubParkour, the ultimate Hub Parkour solution!

## Installation

For instalation instructions, please follow the instructions located on the [spigot page](https://www.spigotmc.org/resources/hubparkour.47713/).

## Developer API

### Using the API as a part of your plugin.

There are 2 methods of including the API in your plugin. You can either attach the JAR as a library in your IDE (not recommended), or you can add a Maven dependency in your `pom.xml`.

Maven:
```xml
<repositories>
       <repository>
            <id>hp-repo</id>
            <url>http://nexus.block2block.me/repository/HubParkour/</url>
        </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.block2block</groupId>
        <artifactId>HubParkour</artifactId>
        <version>2.2.2</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Usage

The JavaDocs can be found [here](https://hubparkour.block2block.me/). The main class for the API is `HubParkourAPI` which can be used to get the player object and other misc things. A full list of methods is available on the JavaDoc page.

There are also several Events that come as a part of the plugin that can be listened to and cancelled. Again, a full list of Events are available in the JavaDocs

### Example

```java
import me.block2block.hubparkour.api.HubParkourAPI;
import me.block2block.hubparkour.api.events.player.ParkourPlayerStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class StartListener implements Listener {
    
    @EventHandler
    public void onStart(ParkourPlayerStartEvent e) {
        if (e.getPlayer().getPlayer().isFlying()) {
            e.setCancelled(true);
            e.getPlayer().getPlayer().sendMessage("You cannot start the parkour if you're flying!");
        }
        
        Bukkit.getLogger().info(HubParkourAPI.getParkour(1).getName());

    }

}
``` 


