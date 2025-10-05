package com.stardevllc.starquests;

import com.stardevllc.starcore.api.StarEvents;
import com.stardevllc.starlib.eventbus.SubscribeEvent;
import com.stardevllc.starmclib.StarMCLib;
import com.stardevllc.starmclib.plugin.ExtendedJavaPlugin;
import com.stardevllc.starquests.actions.QuestAction;
import com.stardevllc.starquests.actions.QuestActionData;
import com.stardevllc.starquests.actions.function.QuestActionPredicate.Status;
import com.stardevllc.starquests.cmds.QuestCmd;
import com.stardevllc.starquests.events.ActionCompleteEvent;
import com.stardevllc.starquests.events.QuestEvent;
import com.stardevllc.starquests.line.QuestLine;
import com.stardevllc.starquests.quests.Quest;
import com.stardevllc.starquests.registry.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StarQuests extends ExtendedJavaPlugin implements Listener {
    private QuestLineRegistry questLineRegistry;
    private QuestRegistry questRegistry;
    private ActionRegistry actionRegistry;
    private QuestPlayerRegistry players = new QuestPlayerRegistry();
    
    @Override
    public void onEnable() {
        super.onEnable();
        StarMCLib.registerPluginEventBus(getEventBus());
        StarMCLib.registerPluginInjector(this, getInjector());
        StarEvents.addChildBus(getEventBus());
        this.actionRegistry = new ActionRegistry(getInjector());
        Quest.setPrimaryActionRegistry(this.actionRegistry);
        this.questRegistry = new QuestRegistry(getInjector());
        QuestLine.setPrimaryQuestRegistry(this.questRegistry);
        this.questLineRegistry = new QuestLineRegistry(getInjector());
        getInjector().setInstance(questRegistry);
        getInjector().setInstance(questLineRegistry);
        getInjector().setInstance(players);
        getEventBus().subscribe(this);
        
        registerListeners(this);
        
        registerCommand("quest", new QuestCmd());
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handleQuestActionTrigger(player, player);
            }
        }, 1L, 1L);
        
        questRegistry.register(Quest.builder()
                .name("Obtain Workbench")
                .onComplete((quest, player) -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 2)))
                .addAction(
                        QuestAction.builder(BlockBreakEvent.class)
                                .name("Break 4 Logs")
                                .predicate((a, e, playerData) -> {
                                    if (!e.getBlock().getType().name().contains("_LOG")) {
                                        return Status.FALSE;
                                    }
                                    
                                    Integer count = playerData.modifyData("count", c -> c + 1, 0);
                                    
                                    if (count < 4) {
                                        return Status.IN_PROGRESS;
                                    }
                                    
                                    return Status.COMPLETE;
                                })
                                .onUpdate((a, e, playerData) -> getColors().coloredLegacy(e.getPlayer(), "&eLogs Broken: &a" + playerData.getAsInt("count") + " &e/ &d4")),
                        QuestAction.builder(CraftItemEvent.class)
                                .name("Craft 4 Planks")
                                .predicate((a, e, playerData) -> {
                                    ItemStack result = e.getInventory().getResult();
                                    if (!result.getType().name().contains("_PLANK")) {
                                        return Status.FALSE;
                                    }
                                    
                                    Integer count = playerData.modifyData("count", c -> c + result.getAmount(), 0);
                                    
                                    if (count < 4) {
                                        return Status.IN_PROGRESS;
                                    }
                                    
                                    return Status.COMPLETE;
                                })
                                .requiredActions("break_4_logs")
                                .onUpdate((a, e, playerData) -> getColors().coloredLegacy(e.getWhoClicked(), "&ePlanks Crafted: &a" + playerData.getAsInt("count") + " &e/ &d4")),
                        QuestAction.builder(CraftItemEvent.class)
                                .name("Craft Workbench")
                                .predicate((a, e, playerData) -> {
                                    if (e.getInventory().getResult().getType() == Material.CRAFTING_TABLE) {
                                        return Status.COMPLETE;
                                    } else {
                                        return Status.FALSE;
                                    }
                                })
                                .requiredActions("craft_4_planks")
                ));
        
        questRegistry.register(Quest.builder()
                .name("Obtain Wooden Pickaxe")
                .requiredQuests("obtain_workbench")
                .onComplete((quest, player) -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 10)))
                .addAction(QuestAction.builder(CraftItemEvent.class)
                                .name("Craft 2 Sticks")
                                .predicate((a, e, playerData) -> {
                                    ItemStack result = e.getInventory().getResult();
                                    if (result.getType() != Material.STICK) {
                                        return Status.FALSE;
                                    }
                                    
                                    Integer count = playerData.modifyData("count", c -> c + result.getAmount(), 0);
                                    
                                    if (count < 2) {
                                        return Status.IN_PROGRESS;
                                    }
                                    
                                    return Status.COMPLETE;
                                })
                                .onUpdate((a, e, playerData) -> getColors().coloredLegacy(e.getWhoClicked(), "&eSticks Crafted: &a" + playerData.getAsInt("count") + " &e/ &d2")),
                        QuestAction.builder(CraftItemEvent.class)
                                .name("Craft a Wooden Pickaxe")
                                .predicate((a, e, playerData) -> {
                                    if (e.getInventory().getResult().getType() == Material.WOODEN_PICKAXE) {
                                        return Status.COMPLETE;
                                    } else {
                                        return Status.FALSE;
                                    }
                                })
                                .requiredActions("craft_2_sticks")
                ));
    }
    
    public QuestRegistry getQuestRegistry() {
        return questRegistry;
    }
    
    public Quest getQuest(String id) {
        return this.questRegistry.get(id);
    }
    
    public QuestPlayer getPlayer(UUID uuid) {
        return this.players.computeIfAbsent(uuid, QuestPlayer::new);
    }
    
    public void handleQuestAction(QuestAction<?> action, Object questActionObject, Player player) {
        try {
            QuestPlayer questPlayer = getPlayer(player.getUniqueId());
            //Ignore the action if it is not available, which does prereq and quest checks
            if (!action.isAvailable(questPlayer)) {
                return;
            }

            //Get or create action data
            QuestActionData actionData = questPlayer.getData(action);
            
            //Test the action for completion or update the quest
            Status check = action.check(questActionObject, actionData);
            if (check == Status.COMPLETE) {
                //If complete, mark it as complete
                Bukkit.getPluginManager().callEvent(new ActionCompleteEvent(action, actionData));
                action.handleOnComplete(questActionObject, actionData);
                questPlayer.completeAction(action);
                //Mainly a testing message for now
                getColors().coloredLegacy(player, "&aCompleted Quest Action: &b" + action.getName());
            }
            
            questsLoop:
            for (Quest quest : this.questRegistry) {
                boolean questComplete = questPlayer.isQuestComplete(quest);
                if (questComplete) {
                    continue;
                }
                
                for (QuestAction<?> questAction : quest.getActions().values()) {
                    if (!questPlayer.isActionComplete(questAction)) {
                        continue questsLoop;
                    }
                }
                
                questPlayer.completeQuest(quest);
                if (quest.getOnComplete() != null) {
                    quest.getOnComplete().apply(quest, player);
                }
                getColors().coloredLegacy(player, "&aCompleted Quest: &b" + quest.getName());
            }
            
            questLineLoop:
            for (QuestLine questLine : this.questLineRegistry) {
                boolean questLineComplete = questPlayer.isQuestLineComplete(questLine);
                if (questLineComplete) {
                    continue;
                }
                
                for (Quest quest : questLine.getQuests().values()) {
                    if (!questPlayer.isQuestComplete(quest)) {
                        continue questLineLoop;
                    }
                }
                
                questPlayer.completeQuestLine(questLine);
                if (questLine.getOnComplete() != null) {
                    questLine.getOnComplete().apply(questLine, player);
                }
                getColors().coloredLegacy(player, "&aCompleted Quest Line: &b" + questLine.getName());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    
    public void handleQuestActionTrigger(Object questActionObject, Player player) {
        for (Quest quest : this.questRegistry) {
            for (QuestAction<?> action : quest.getActions().values()) {
                handleQuestAction(action, questActionObject, player);
            }
        }
    }
    
    @SubscribeEvent
    public void onEvent(Event e) {
        //Ignore custom events for quests as these are mainly for formatting and stuff
        if (e instanceof QuestEvent) {
            return;
        }
        
        QuestUtils.getPlayerFromEvent(e).ifPresent(player -> handleQuestActionTrigger(e, player));
    }
}