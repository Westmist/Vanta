package org.vanta.game.netty;

import org.vanta.game.actor.Player;
import io.netty.util.AttributeKey;

public class ChannelAttributeKey {

	public static final AttributeKey<Player> PLAYER_KEY = AttributeKey.valueOf("player");


}
