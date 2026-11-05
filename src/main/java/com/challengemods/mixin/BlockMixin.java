package com.challengemods.mixin;

import com.challengemods.Mode;
import com.challengemods.ModeManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "getDroppedStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)Ljava/util/List;",
            at = @At("RETURN"), cancellable = true)
    private static void challengemods$randomDrops(BlockState state, ServerWorld world, BlockPos pos,
                                                  BlockEntity be, Entity entity, ItemStack tool,
                                                  CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!ModeManager.isActive(Mode.RANDOM_DROPS)) return;
        List<ItemStack> original = cir.getReturnValue();
        if (original == null || original.isEmpty()) return; // Werkzeug-Regeln bleiben wie im Vanilla-Spiel
        Item item = ModeManager.dropMap.get(state.getBlock());
        if (item == null) return;
        int count = 0;
        for (ItemStack s : original) count += s.getCount();
        List<ItemStack> out = new ArrayList<>();
        out.add(new ItemStack(item, Math.max(1, Math.min(count, item.getMaxCount()))));
        cir.setReturnValue(out);
    }
}
