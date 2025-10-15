package com.game.vanta.netty;

import com.game.vanta.actor.Player;
import io.netty.util.AttributeKey;

public class ChannelAttributeKey {

	public static final AttributeKey<Player> PLAYER_KEY = AttributeKey.valueOf("player");


}
