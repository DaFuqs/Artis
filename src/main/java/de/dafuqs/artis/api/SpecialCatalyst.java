package de.dafuqs.artis.api;

import net.minecraft.item.*;

/**
 * An item that has special behavior when used as a catalyst in an Artis table.
 */
public interface SpecialCatalyst {
	
	/**
	 * @param stack The stack being used as a catalyst
	 * @param cost  The catalyst cost of the recipe
	 * @return Whether this stack fulfills the required cost
	 */
	boolean matchesCatalyst(ItemStack stack, int cost);
	
	/**
	 * Consume something from this stack as part of an Artis catalyst.
	 *
	 * @param catalyst The stack being consumed as catalyst
	 * @param cost     How many units are being consumed
	 */
	void consumeCatalyst(ItemStack catalyst, int cost);
	
}
