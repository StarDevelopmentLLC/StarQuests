package com.stardevllc.starquests.registry;

import com.stardevllc.starlib.registry.UUIDRegistry;
import com.stardevllc.starquests.QuestPlayer;

public class QuestPlayerRegistry extends UUIDRegistry<QuestPlayer> {
    public QuestPlayerRegistry() {
        super(null, null, QuestPlayer::getUuid, null, null);
    }
}
