package com.mudgame.entities.maps.cities;

import com.mudgame.entities.GameMap;
import com.mudgame.entities.Room;
import com.mudgame.entities.maps.BaseMapBuilder;
import com.mudgame.entities.Direction;

public class NeoTokyoBuilder extends BaseMapBuilder {
    @Override
    public void buildMap(GameMap map) {
        // Main areas
        Room shibuyaCrossing = createAndAddRoom(map,
                "Shibuya Crossing",
                "A massive intersection filled with holographic advertisements and crowds."
        );

        Room securityPost = createAndAddRoom(map,
                "Shibuya Security Post",
                "A reinforced security station overlooking the crossing. Security bots patrol the area."
        );

        Room maintenanceBay = createAndAddRoom(map,
                "Shibuya Maintenance Bay",
                "A cluttered maintenance area filled with spare parts and repair equipment."
        );

        // Add weapon shop exterior
        Room weaponShopExterior = createAndAddRoom(map,
                "Takeda's Weapons",
                "A traditional storefront with paper lanterns flanking the entrance. " +
                        "Through the shop window, you can see the gleam of high-tech weaponry displayed on elegant wooden stands."
        );

        // Add weapon shop interior
        Room weaponShopInterior = createAndAddRoom(map,
                "Weapon Shop",
                "A traditional-style shop with a modern twist. High-tech weapons line the walls, " +
                        "each displayed on elegant wooden stands. Holographic price tags float beside each item."
        );

        // Connect the main street rooms
        connectBidirectional(map, shibuyaCrossing, Direction.NORTH, securityPost);
        connectBidirectional(map, securityPost, Direction.EAST, maintenanceBay);
        connectBidirectional(map, shibuyaCrossing, Direction.EAST, weaponShopExterior);

        // Connect shop exterior to interior using ENTER/EXIT
        connectBidirectional(map, weaponShopExterior, Direction.ENTER, weaponShopInterior);
    }
}