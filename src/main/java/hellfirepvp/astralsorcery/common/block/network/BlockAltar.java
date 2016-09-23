package hellfirepvp.astralsorcery.common.block.network;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.client.util.RenderingUtils;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.block.BlockCustomName;
import hellfirepvp.astralsorcery.common.block.BlockMarble;
import hellfirepvp.astralsorcery.common.block.BlockVariants;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.tile.IVariantTileProvider;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: BlockAltar
 * Created by HellFirePvP
 * Date: 01.08.2016 / 20:52
 */
public class BlockAltar extends BlockStarlightNetwork implements BlockCustomName, BlockVariants {

    private static AxisAlignedBB boxDiscovery = FULL_BLOCK_AABB;

    public static PropertyEnum<AltarType> ALTAR_TYPE = PropertyEnum.create("altartype", AltarType.class);

    public BlockAltar() {
        super(Material.ROCK, MapColor.GRAY);
        setHardness(3.0F);
        setSoundType(SoundType.STONE);
        setResistance(25.0F);
        setHarvestLevel("pickaxe", 3);
        setCreativeTab(RegistryItems.creativeTabAstralSorcery);
        setDefaultState(this.blockState.getBaseState().withProperty(ALTAR_TYPE, AltarType.ALTAR_1));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(!worldIn.isRemote) {
            TileAltar ta = MiscUtils.getTileAt(worldIn, pos, TileAltar.class);
            if(ta != null) {
                switch (ta.getAltarLevel()) {
                    case DISCOVERY:
                        AstralSorcery.proxy.openGui(CommonProxy.EnumGuiId.ALTAR_DISCOVERY, playerIn, worldIn, pos.getX(), pos.getY(), pos.getZ());
                        return true;
                    case ATTENUATION:
                        break;
                    case CONSTELLATION_CRAFT:
                        break;
                    case TRAIT_CRAFT:
                        break;
                    case ENDGAME:
                        break;
                }
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        RenderingUtils.playBlockBreakParticles(pos,
                BlocksAS.blockMarble.getDefaultState()
                        .withProperty(BlockMarble.MARBLE_TYPE, BlockMarble.MarbleBlockType.RAW));
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
        return true;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return state.getValue(ALTAR_TYPE) != null;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (AltarType type : AltarType.values()) {
            list.add(new ItemStack(item, 1, type.ordinal()));
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        TileAltar ta = MiscUtils.getTileAt(source, pos, TileAltar.class);
        if(ta != null) {
            TileAltar.AltarLevel al = ta.getAltarLevel();
            switch (al) {
                case DISCOVERY:
                    return boxDiscovery;
                case ATTENUATION:
                    break;
                case CONSTELLATION_CRAFT:
                    break;
                case TRAIT_CRAFT:
                    break;
                case ENDGAME:
                    break;
            }
        }
        return super.getBoundingBox(state, source, pos);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        AltarType type = state.getValue(ALTAR_TYPE);
        if (type == null) return null;
        return type.provideTileEntity(world, state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return meta < AltarType.values().length ? getDefaultState().withProperty(ALTAR_TYPE, AltarType.values()[meta]) : getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        AltarType type = state.getValue(ALTAR_TYPE);
        return type == null ? 0 : type.ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ALTAR_TYPE);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return super.getPickBlock(world.getBlockState(pos), target, world, pos, player); //Waila fix. wtf. why waila. why.
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    public String getIdentifierForMeta(int meta) {
        AltarType mt = getStateFromMeta(meta).getValue(ALTAR_TYPE);
        return mt == null ? "null" : mt.getName();
    }

    @Override
    public List<IBlockState> getValidStates() {
        List<IBlockState> ret = new LinkedList<>();
        for (AltarType type : AltarType.values()) {
            ret.add(getDefaultState().withProperty(ALTAR_TYPE, type));
        }
        return ret;
    }

    @Override
    public String getStateName(IBlockState state) {
        return state.getValue(ALTAR_TYPE).getName();
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    public static enum AltarType implements IStringSerializable, IVariantTileProvider {

        ALTAR_1((world, state) -> new TileAltar(TileAltar.AltarLevel.DISCOVERY)),
        ALTAR_2((world, state) -> new TileAltar(TileAltar.AltarLevel.ATTENUATION)),
        ALTAR_3((world, state) -> new TileAltar(TileAltar.AltarLevel.CONSTELLATION_CRAFT)),
        ALTAR_4((world, state) -> new TileAltar(TileAltar.AltarLevel.TRAIT_CRAFT)),
        ALTAR_5((world, state) -> new TileAltar(TileAltar.AltarLevel.ENDGAME));

        //Ugly workaround to make constructors nicer
        private final IVariantTileProvider provider;

        private AltarType(IVariantTileProvider provider) {
            this.provider = provider;
        }

        @Override
        public TileEntity provideTileEntity(World world, IBlockState state) {
            return provider.provideTileEntity(world, state);
        }

        @Override
        public String getName() {
            return name().toLowerCase();
        }

        @Override
        public String toString() {
            return getName();
        }
    }
}
