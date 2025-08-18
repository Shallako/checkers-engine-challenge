package com.shalako.checkers.engine;

import com.shalako.checkers.engine.rules.AmericanCheckersRules;
import com.shalako.checkers.engine.rules.GameRules;
import com.shalako.checkers.engine.rules.InternationalDraughtsRules;
import com.shalako.checkers.enums.GameType;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class GameRulesFactory {
    private final Map<GameType, GameRules> rulesCache;

    public GameRulesFactory() {
        rulesCache = new EnumMap<>(GameType.class);
        rulesCache.put(GameType.STANDARD_AMERICAN, new AmericanCheckersRules());
        rulesCache.put(GameType.INTERNATIONAL, new InternationalDraughtsRules());
    }

    public GameRules getRules(GameType gameType) {
        GameRules rules = rulesCache.get(gameType);
        if (rules == null) {
            throw new IllegalArgumentException("No rules defined for game type: " + gameType);
        }
        return rules;
    }
}
