/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2018
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.perk.tree.nodes.key;

import hellfirepvp.astralsorcery.common.constellation.perk.tree.nodes.KeyPerk;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyMagnetDrops
 * Created by HellFirePvP
 * Date: 23.11.2018 / 16:52
 */
public class KeyMagnetDrops extends KeyPerk {

    public KeyMagnetDrops(String name, int x, int y) {
        super(name, x, y);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDropLoot(LivingDropsEvent event) {
        DamageSource source = event.getSource();
        if (source.getTrueSource() != null && source.getTrueSource() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) source.getTrueSource();
            Side side = player.world.isRemote ? Side.CLIENT : Side.SERVER;
            PlayerProgress prog = ResearchManager.getProgress(player, side);
            if (prog.hasPerkEffect(this)) {
                Iterator<EntityItem> iterator = event.getDrops().iterator();
                while (iterator.hasNext()) {
                    EntityItem item = iterator.next();
                    ItemStack drop = item.getItem();
                    if (drop.isEmpty()) {
                        continue;
                    }
                    if (player.addItemStackToInventory(drop)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDropHarvest(BlockEvent.HarvestDropsEvent event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }

        EntityPlayer player = event.getHarvester();
        PlayerProgress prog = ResearchManager.getProgress(player, Side.SERVER);
        if (prog == null || !prog.hasPerkEffect(this)) {
            return;
        }

        List<ItemStack> drops = new ArrayList<>(event.getDrops());

        // Simulate normal drop-logic to see what/which drops to try add before
        // setting chances to 1 with remaining not-capturable drops
        Random r = world.rand;
        for (ItemStack drop : drops) {
            if (r.nextFloat() <= event.getDropChance()) {
                if (player.addItemStackToInventory(drop)) {
                    event.getDrops().remove(drop);
                }
            } else {
                event.getDrops().remove(drop);
            }
        }
        event.setDropChance(1F);
    }

}
