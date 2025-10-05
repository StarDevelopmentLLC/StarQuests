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
import com.stardevllc.starquests.holder.QuestHolder;
import com.stardevllc.starquests.holder.QuestPlayer;
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
    private QuestHolderRegistry holders = new QuestHolderRegistry();
    
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
        QuestHolder.setColorFunction(text -> getColors().colorLegacy(text));
        getInjector().setInstance(questRegistry);
        getInjector().setInstance(questLineRegistry);
        getInjector().setInstance(holders);
        getEventBus().subscribe(this);
        
        registerListeners(this);
        
        registerCommand("quest", new QuestCmd());
        
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handleQuestActionTrigger(player, getPlayer(player.getUniqueId()));
            }
        }, 1L, 1L);
        
        QuestLine.Builder woodenLineBuilder = QuestLine.builder()
                .name("Wooden Resources")
                .onComplete((questLine, holder) -> holder.getPlayer().ifPresent(player -> player.getInventory().addItem(new ItemStack(Material.IRON_INGOT))));
        
        woodenLineBuilder.addQuest(Quest.builder()
                .name("Obtain Workbench")
                .onComplete((quest, holder) -> holder.getPlayer().ifPresent(player -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 2))))
                .addAction(
                        QuestAction.builder(BlockBreakEvent.class)
                                .name("Break 4 Logs")
                                .predicate((a, e, actionData) -> {
                                    if (!e.getBlock().getType().name().contains("_LOG")) {
                                        return Status.FALSE;
                                    }
                                    
                                    Integer count = actionData.modifyData("count", c -> c + 1, 0);
                                    
                                    if (count < 4) {
                                        return Status.IN_PROGRESS;
                                    }
                                    
                                    return Status.COMPLETE;
                                })
                                .onUpdate((a, e, actionData) -> getColors().coloredLegacy(e.getPlayer(), "&eLogs Broken: &a" + actionData.getAsInt("count") + " &e/ &d4")),
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
                                .predicate((a, e, actionData) -> {
                                    if (e.getInventory().getResult().getType() == Material.CRAFTING_TABLE) {
                                        return Status.COMPLETE;
                                    } else {
                                        return Status.FALSE;
                                    }
                                })
                                .requiredActions("craft_4_planks")
                ));
        
        woodenLineBuilder.addQuest(Quest.builder()
                .name("Obtain Wooden Pickaxe")
                .requiredQuests("obtain_workbench")
                .onComplete((quest, holder) -> holder.getPlayer().ifPresent(player -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 10))))
                .addAction(
                        QuestAction.builder(CraftItemEvent.class)
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
        
        questLineRegistry.register(woodenLineBuilder);
        
        QuestLine.Builder stoneLineBuilder = QuestLine.builder()
                .name("Stone Resources")
                .requiredLines("wooden_resources")
                .onComplete((questLine, holder) -> holder.getPlayer().ifPresent(player -> player.getInventory().addItem(new ItemStack(Material.DIAMOND))));
        stoneLineBuilder.addQuest(Quest.builder()
                .name("Stone Pickaxe")
                .onComplete((quest, holder) -> holder.getPlayer().ifPresent(player -> player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 10))))
                .addAction(QuestAction.builder(BlockBreakEvent.class)
                        .name("Mine 3 Stone")
                        .predicate((a, e, actionData) -> {
                            if (e.getBlock().getType() != Material.STONE) {
                                return Status.FALSE;
                            }
                            
                            Integer count = actionData.modifyData("count", c -> c + 1, 0);
                            
                            if (count < 3) {
                                return Status.IN_PROGRESS;
                            }
                            
                            return Status.COMPLETE;
                        })
                        .onUpdate((a, e, actionData) -> getColors().coloredLegacy(e.getPlayer(), "&eStone Mined: &a" + actionData.getAsInt("count") + " &e/ &d3")))
                .addAction(QuestAction.builder(CraftItemEvent.class)
                        .name("Craft Stone Pickaxe")
                        .predicate((a, e, actionData) -> {
                            if (e.getInventory().getResult().getType() == Material.STONE_PICKAXE) {
                                return Status.COMPLETE;
                            } else {
                                return Status.FALSE;
                            }
                        })
                        .requiredActions("mine_3_stone")
                )
        );
        
        questLineRegistry.register(stoneLineBuilder);
    }
    
    public QuestRegistry getQuestRegistry() {
        return questRegistry;
    }
    
    public QuestLineRegistry getQuestLineRegistry() {
        return questLineRegistry;
    }
    
    public QuestHolder<?> getHolder(String key) {
        return this.holders.get(key);
    }
    
    public QuestPlayer getPlayer(UUID uuid) {
        QuestHolder<?> holder = this.holders.get(uuid.toString());
        if (holder instanceof QuestPlayer questPlayer) {
            return questPlayer;
        }
        
        QuestPlayer questPlayer = new QuestPlayer(uuid);
        this.holders.register(questPlayer);
        return questPlayer;
    }
    
    public void handleQuestAction(QuestAction<?> action, Object questActionObject, QuestHolder<?> holder) {
        try {
            //Ignore the action if it is not available, which does prereq and quest checks
            if (!action.isAvailable(holder)) {
                return;
            }
            
            //Get or create action data
            QuestActionData actionData = holder.getData(action);
            
            //Test the action for completion or update the quest
            Status check = action.check(questActionObject, actionData);
            if (check == Status.COMPLETE) {
                //If complete, mark it as complete
                Bukkit.getPluginManager().callEvent(new ActionCompleteEvent(action, actionData));
                action.handleOnComplete(questActionObject, actionData);
                holder.completeAction(action);
                //Mainly a testing message for now
                holder.sendMessage("&c&l[DEBUG] &aCompleted Quest Action: &b" + action.getName());
            }
            
            questsLoop:
            for (Quest quest : this.questRegistry) {
                boolean questComplete = holder.isQuestComplete(quest);
                if (questComplete) {
                    continue;
                }
                
                for (QuestAction<?> questAction : quest.getActions().values()) {
                    if (!holder.isActionComplete(questAction)) {
                        continue questsLoop;
                    }
                }
                
                holder.completeQuest(quest);
                if (quest.getOnComplete() != null) {
                    quest.getOnComplete().apply(quest, holder);
                }
                holder.sendMessage("&c&l[DEBUG] &aCompleted Quest: &b" + quest.getName());
            }
            
            questLineLoop:
            for (QuestLine questLine : this.questLineRegistry) {
                boolean questLineComplete = holder.isQuestLineComplete(questLine);
                if (questLineComplete) {
                    continue;
                }
                
                for (Quest quest : questLine.getQuests().values()) {
                    if (!holder.isQuestComplete(quest)) {
                        continue questLineLoop;
                    }
                }
                
                holder.completeQuestLine(questLine);
                if (questLine.getOnComplete() != null) {
                    questLine.getOnComplete().apply(questLine, holder);
                }
                
                holder.sendMessage("&c&l[DEBUG] &aCompleted Quest Line: &b" + questLine.getName());
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    
    public void handleQuestActionTrigger(Object questActionObject, QuestHolder<?> holder) {
        for (QuestAction<?> action : actionRegistry) {
            if (action.getType().equals(questActionObject.getClass())) {
                handleQuestAction(action, questActionObject, holder);
            }
        }
    }
    
    @SubscribeEvent
    public void onEvent(Event e) {
        //Ignore custom events for quests as these are mainly for formatting and stuff
        if (e instanceof QuestEvent) {
            return;
        }
        
        QuestUtils.getPlayerFromEvent(e).ifPresent(player -> handleQuestActionTrigger(e, getPlayer(player.getUniqueId())));
    }
}