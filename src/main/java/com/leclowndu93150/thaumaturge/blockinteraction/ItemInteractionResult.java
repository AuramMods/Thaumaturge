package com.leclowndu93150.thaumaturge.blockinteraction;

import net.minecraft.world.InteractionResult;

/** Item phase results retained separately from the native 1.20.1 block result. */
public enum ItemInteractionResult {
    SUCCESS(InteractionResult.SUCCESS),
    CONSUME(InteractionResult.CONSUME),
    CONSUME_PARTIAL(InteractionResult.CONSUME_PARTIAL),
    PASS_TO_DEFAULT_BLOCK_INTERACTION(InteractionResult.PASS),
    SKIP_DEFAULT_BLOCK_INTERACTION(InteractionResult.PASS),
    FAIL(InteractionResult.FAIL);

    private final InteractionResult result;

    ItemInteractionResult(InteractionResult result) {
        this.result = result;
    }

    public boolean consumesAction() {
        return result.consumesAction();
    }

    public InteractionResult result() {
        return result;
    }

    public static ItemInteractionResult sidedSuccess(boolean clientSide) {
        return clientSide ? SUCCESS : CONSUME;
    }
}
