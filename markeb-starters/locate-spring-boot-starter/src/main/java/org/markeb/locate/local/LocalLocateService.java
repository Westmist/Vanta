package org.markeb.locate.local;

import org.markeb.locate.LocateService;
import org.markeb.locate.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 本地内存定位服务实现（仅用于测试）
 */
public class LocalLocateService implements LocateService {

    private static final Logger log = LoggerFactory.getLogger(LocalLocateService.class);

    private final ConcurrentHashMap<String, Location> locations = new ConcurrentHashMap<>();

    @Override
    public void bind(String playerId, Location location) {
        location.setLastUpdateTime(LocalDateTime.now());
        locations.put(playerId, location);
        log.debug("Bound player {} to node {}", playerId, location.getNodeId());
    }

    @Override
    public void unbind(String playerId) {
        locations.remove(playerId);
        log.debug("Unbound player {}", playerId);
    }

    @Override
    public Optional<Location> locate(String playerId) {
        return Optional.ofNullable(locations.get(playerId));
    }

    @Override
    public List<Location> locateAll(List<String> playerIds) {
        List<Location> result = new ArrayList<>();
        for (String playerId : playerIds) {
            Location location = locations.get(playerId);
            if (location != null) {
                result.add(location);
            }
        }
        return result;
    }

    @Override
    public boolean isOnline(String playerId) {
        return locations.containsKey(playerId);
    }

    @Override
    public List<String> getPlayersByNode(String nodeId) {
        return locations.entrySet().stream()
                .filter(e -> nodeId.equals(e.getValue().getNodeId()))
                .map(e -> e.getKey())
                .collect(Collectors.toList());
    }

    @Override
    public long getOnlineCount() {
        return locations.size();
    }

    @Override
    public void refresh(String playerId) {
        Location location = locations.get(playerId);
        if (location != null) {
            location.setLastUpdateTime(LocalDateTime.now());
        }
    }
}

