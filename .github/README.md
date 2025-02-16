# HubParkour

Welcome to HubParkour, the ultimate Hub Parkour solution! This plugin contains lots of features and is aimed to be as lightweight as possible! It is completely configurable, including all messages and functionality!

## Installation

For installation instructions, please follow the instructions located on the [spigot page](https://www.spigotmc.org/resources/hubparkour.47713/).

## Developer API

### Using the API as a part of your plugin.

There are 2 methods of including the API in your plugin. You can either attach the JAR as a library in your IDE (not recommended), or you can add a Maven dependency in your `pom.xml`.

Maven:
```xml
<repositories>
       <repository>
            <id>hp-repo</id>
            <url>https://nexus.block2block.me/repository/HubParkour/</url>
        </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.block2block.hubparkour</groupId>
        <artifactId>hubparkour-api</artifactId>
        <version>2.8.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

**Note:** Only version 2.7.2+ are deployed to the repository due to issues we encountered with nexus.

### Usage

The JavaDocs can be found [here](http://hubparkour.block2block.me/). The main class for the API is `HubParkourAPI` which can be used to get the player object and other misc things. A full list of methods is available on the JavaDoc page.

There are also several Events that come as a part of the plugin that can be listened to and cancelled. Again, a full list of Events are available in the JavaDocs

### Example

```java
import me.block2block.api.HubParkourAPI;
import me.block2block.api.events.player.ParkourPlayerStartEvent;
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

## Contributions and Code of Conduct

We welcome contributions to HubParkour. If you would like to make a contribution, check out our Contribution Guidelines [here](CONTRIBUTING.md).

We expect all contributors to follow our Code of Conduct, whether that is creating an issue, a PR or any other contribution. You can find our Code of Conduct [here](CODE_OF_CONDUCT.md)

## License

This software is distributed with the Apache 2.0 license. For details about this, please view our full license [here](../LICENSE).

## Support

You can find our support channels in discord, which is [here](https://discord.gg/PVdDNWs). Please make sure you read the messages in #faq and #tickets before you make a ticket.
