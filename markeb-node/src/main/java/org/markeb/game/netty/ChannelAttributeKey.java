package org.markeb.game.netty;

import org.markeb.game.actor.Player;
import io.netty.util.AttributeKey;

public class ChannelAttributeKey {

	public static final AttributeKey<Player> PLAYER_KEY = AttributeKey.valueOf("player");

	public static final AttributeKey<Long> PLAYER_ID_KEY = AttributeKey.valueOf("playerId");

}
