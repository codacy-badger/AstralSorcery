package hellfirepvp.astralsorcery.common.cmd;

import hellfirepvp.astralsorcery.common.constellation.Constellation;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.Tier;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ProgressionTier;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.lib.MultiBlockArrays;
import hellfirepvp.astralsorcery.common.registry.RegistryStructures;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.Tuple;
import hellfirepvp.astralsorcery.common.util.struct.StructureBlockArray;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommandAstralSorcery
 * Created by HellFirePvP
 * Date: 07.05.2016 / 13:39
 */
public class CommandAstralSorcery extends CommandBase {

    private List<String> cmdAliases = new ArrayList<String>();

    public CommandAstralSorcery() {
        this.cmdAliases.add("astralsorcery");
        this.cmdAliases.add("as");
    }

    @Override
    public String getCommandName() {
        return "astralsorcery";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/astralsorcery <action> [player] [arguments...]";
    }

    @Override
    public List<String> getCommandAliases() {
        return cmdAliases;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 1;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            sender.addChatMessage(new TextComponentString("§cNot enough arguments."));
            sender.addChatMessage(new TextComponentString("§cType \"/astralsorcery help\" for help"));
            return;
        }
        if (args.length >= 1) {
            String identifier = args[0];
            if (identifier.equalsIgnoreCase("help")) {
                displayHelp(sender);
            } else if (identifier.equalsIgnoreCase("constellation") || identifier.equalsIgnoreCase("constellations")) {
                if (args.length == 1) {
                    listConstellations(sender);
                } else if (args.length == 2) {
                    listConstellations(server, sender, args[1]);
                } else if (args.length == 3) {
                    addConstellations(server, sender, args[1], args[2]);
                }
            } else if (identifier.equalsIgnoreCase("progress") || identifier.equalsIgnoreCase("prog")) {
                if(args.length <= 2) {
                    showProgress(server, sender, args.length == 1 ? sender.getName() : args[1]);
                } else if(args.length == 3) {
                    modifyProgress(server, sender, args[1], args[2]);
                }
            } else if (identifier.equalsIgnoreCase("reset")) {
                if (args.length == 2) {
                    wipeProgression(server, sender, args[1]);
                }
            } else if (identifier.equalsIgnoreCase("build")) {
                if(args.length == 2) {
                    buildStruct(server, sender, args[1]);
                } else {
                    RegistryStructures.init(); //Reload
                }
            }
        }
    }

    private void buildStruct(MinecraftServer server, ICommandSender sender, String name) {
        StructureBlockArray array;
        try {
            Field f = MultiBlockArrays.class.getDeclaredField(name);
            f.setAccessible(true);
            array = (StructureBlockArray) f.get(null);
        } catch (NoSuchFieldException e) {
            sender.addChatMessage(new TextComponentString("§cFailed! " + name + " doesn't exist!"));
            return;
        } catch (IllegalAccessException e) {
            return; //doesn't happen
        }
        EntityPlayer exec;
        try {
            exec = getCommandSenderAsPlayer(sender);
        } catch (PlayerNotFoundException e) {
            sender.addChatMessage(new TextComponentString("§cFailed! Couldn't find you as player in the world!"));
            return;
        }
        RayTraceResult res = MiscUtils.rayTraceLook(exec, 60);
        if(res == null) {
            sender.addChatMessage(new TextComponentString("§cFailed! Couldn't find the block you're looking at?"));
            return;
        }
        BlockPos hit;
        switch (res.typeOfHit) {
            case BLOCK:
                hit = res.getBlockPos();
                break;
            case MISS:
            case ENTITY:
            default:
                sender.addChatMessage(new TextComponentString("§cFailed! Couldn't find the block you're looking at?"));
                return;
        }
        sender.addChatMessage(new TextComponentString("§aStarting to build " + name + " at " + hit.toString() + "!"));
        array.placeInWorld(exec.worldObj, hit);
        sender.addChatMessage(new TextComponentString("§aBuilt " + name + "!"));
    }

    private void wipeProgression(MinecraftServer server, ICommandSender sender, String otherPlayerName) {
        Tuple<EntityPlayer, PlayerProgress> prTuple = tryGetProgressWithMessages(server, sender, otherPlayerName);
        if (prTuple == null) {
            return;
        }
        PlayerProgress progress = prTuple.value;
        EntityPlayer other = prTuple.key;

        ResearchManager.wipeKnowledge(other);
        sender.addChatMessage(new TextComponentString("§aWiped " + otherPlayerName + "'s data!"));
    }

    private void modifyProgress(MinecraftServer server, ICommandSender sender, String otherPlayerName, String argument) {
        Tuple<EntityPlayer, PlayerProgress> prTuple = tryGetProgressWithMessages(server, sender, otherPlayerName);
        if (prTuple == null) {
            return;
        }
        PlayerProgress progress = prTuple.value;
        EntityPlayer other = prTuple.key;
        if(argument.equalsIgnoreCase("all")) {
            if(!ResearchManager.maximizeTier(other)) {
                sender.addChatMessage(new TextComponentString("§cFailed! Could not load Progress for (" + otherPlayerName + ") !"));
            } else {
                sender.addChatMessage(new TextComponentString("§aMaximized ProgressionTier for " + otherPlayerName + " !"));
            }
        } else {
            Optional<ProgressionTier> did = ResearchManager.stepTier(other);
            if(!did.isPresent()) {
                sender.addChatMessage(new TextComponentString("§cCould not step Progress for " + otherPlayerName + " ! (Is already at max)"));
            } else {
                if(did.get() != null) {
                    sender.addChatMessage(new TextComponentString("§aPlayer " + otherPlayerName + " advanced to Tier " + did.get().name() + "!"));
                } else {
                    sender.addChatMessage(new TextComponentString("§cFailed! Could not load Progress for (" + otherPlayerName + ") !"));
                }
            }
        }
    }

    private void showProgress(MinecraftServer server, ICommandSender sender, String otherPlayerName) {
        Tuple<EntityPlayer, PlayerProgress> prTuple = tryGetProgressWithMessages(server, sender, otherPlayerName);
        if (prTuple == null) {
            return;
        }
        PlayerProgress progress = prTuple.value;
        EntityPlayer other = prTuple.key;
        sender.addChatMessage(new TextComponentString("§aPlayer " + otherPlayerName + "'s progression tier: " + progress.getTierReached().name()));
    }

    private void addConstellations(MinecraftServer server, ICommandSender sender, String otherPlayerName, String argument) {
        Tuple<EntityPlayer, PlayerProgress> prTuple = tryGetProgressWithMessages(server, sender, otherPlayerName);
        if (prTuple == null) {
            return;
        }
        PlayerProgress progress = prTuple.value;
        EntityPlayer other = prTuple.key;
        if (argument.equals("all")) {
            Collection<Constellation> constellations = ConstellationRegistry.getAllConstellations();
            if (!ResearchManager.discoverConstellations(constellations, other)) {
                sender.addChatMessage(new TextComponentString("§cFailed! Could not load Progress for (" + otherPlayerName + ") !"));
                return;
            }
            other.addChatMessage(new TextComponentString("§aDiscovered all Constellations!"));
            sender.addChatMessage(new TextComponentString("§aSuccess!"));
        } else {
            Constellation c = ConstellationRegistry.getConstellationByName(argument);
            if (c == null) {
                sender.addChatMessage(new TextComponentString("§cUnknown constellation: " + argument));
                return;
            }
            if (!ResearchManager.discoverConstellation(c, other)) {
                sender.addChatMessage(new TextComponentString("§cFailed! Could not load Progress for (" + otherPlayerName + ") !"));
                return;
            }
            other.addChatMessage(new TextComponentString("§aDiscovered constellation " + c.getName() + "!"));
            sender.addChatMessage(new TextComponentString("§aSuccess!"));
        }
    }

    private void listConstellations(MinecraftServer server, ICommandSender sender, String otherPlayerName) {
        Tuple<EntityPlayer, PlayerProgress> prTuple = tryGetProgressWithMessages(server, sender, otherPlayerName);
        if (prTuple == null) {
            return;
        }
        PlayerProgress progress = prTuple.value;
        EntityPlayer other = prTuple.key;
        sender.addChatMessage(new TextComponentString("§c" + otherPlayerName + " has discovered the constellations:"));
        if (progress.getKnownConstellations().size() == 0) {
            sender.addChatMessage(new TextComponentString("§c NONE"));
            return;
        }
        for (String s : progress.getKnownConstellations()) {
            sender.addChatMessage(new TextComponentString("§7" + s));
        }
    }

    private Tuple<EntityPlayer, PlayerProgress> tryGetProgressWithMessages(MinecraftServer server, ICommandSender sender, String otherPlayerName) {
        EntityPlayer other;
        try {
            other = getPlayer(server, sender, otherPlayerName);
        } catch (PlayerNotFoundException e) {
            sender.addChatMessage(new TextComponentString("§cSpecified player (" + otherPlayerName + ") is not online!"));
            return null;
        }
        if (other == null) {
            sender.addChatMessage(new TextComponentString("§cSpecified player (" + otherPlayerName + ") is not online!"));
            return null;
        }
        PlayerProgress progress = ResearchManager.getProgress(other.getUniqueID());
        if (progress == null) {
            sender.addChatMessage(new TextComponentString("§cCould not get Progress for (" + otherPlayerName + ") !"));
            return null;
        }
        return new Tuple<>(other, progress);
    }

    private void displayHelp(ICommandSender sender) {
        sender.addChatMessage(new TextComponentString("§a/astralsorcery constellation§7 - lists all constellations"));
        sender.addChatMessage(new TextComponentString("§a/astralsorcery constellation [playerName]§7 - lists all discovered constellations of the specified player if he/she is online"));
        sender.addChatMessage(new TextComponentString("§a/astralsorcery constellation [playerName] <cName;all>§7 - player specified discovers the specified constellation or all or resets all"));
        sender.addChatMessage(new TextComponentString("§a/astralsorcery progress [playerName]§7 - displays progress information about the player (Enter no player to view your own)"));
        sender.addChatMessage(new TextComponentString("§a/astralsorcery progress [playerName] <step;all>§7 - set the progression"));
        sender.addChatMessage(new TextComponentString("§a/astralsorcery reset [playerName]§7 - resets all progression-related data for that player."));
        sender.addChatMessage(new TextComponentString("§a/astralsorcery build [structure]§7 - builds the named structure wherever the player is looking at."));
    }

    private void listConstellations(ICommandSender sender) {
        for (Tier tier : ConstellationRegistry.ascendingTiers()) {
            sender.addChatMessage(new TextComponentString("§cTier: " + tier.tierNumber() + " - showupChance: " + tier.getShowupChance()));
            for (Constellation c : tier.getConstellations()) {
                sender.addChatMessage(new TextComponentString("§7" + c.getName()));
            }
        }
    }

}
