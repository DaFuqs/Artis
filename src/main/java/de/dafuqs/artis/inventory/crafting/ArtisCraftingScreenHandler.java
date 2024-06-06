package de.dafuqs.artis.inventory.crafting;

import de.dafuqs.artis.*;
import de.dafuqs.artis.api.*;
import de.dafuqs.artis.inventory.slot.*;
import io.github.cottonmc.cotton.gui.*;
import io.github.cottonmc.cotton.gui.client.*;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.*;
import net.fabricmc.api.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.recipe.*;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.*;
import net.minecraft.server.world.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ArtisCraftingScreenHandler extends SyncedGuiDescription implements RecipeProvider {
	private final ArtisCraftingRecipeType tableType;
	private final PlayerEntity player;
	private final ArtisCraftingInventory craftInv;
	private final CraftingResultInventory resultInv;
	private final ScreenHandlerContext context;
	
	private final WPlainPanel mainPanel;
	private final WItemSlot craftingGrid;
	private final ArtisCraftingResultSlot resultSlot;
	private final WPlayerInvPanel playerInv;
	private WItemSlot catalystSlot;
	
	//This constructor gets called on the client when the server wants it to open the screenHandler,
	//The client will call the other constructor with an empty Inventory and the screenHandler will automatically
	//sync this empty inventory with the inventory on the server.
	public ArtisCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
		this(syncId, playerInventory, new SimpleInventory(9));
	}
	
	//This constructor gets called from the BlockEntity on the server without calling the other constructor first, the server knows the inventory of the container
	//and can therefore directly provide it as an argument. This inventory will then be synced to the client.
	public ArtisCraftingScreenHandler(ArtisCraftingRecipeType tableType, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
		super(tableType.getScreenHandlerType(), syncId, playerInventory, getBlockInventory(context), getBlockPropertyDelegate(context));
		
		this.tableType = tableType;
		this.player = playerInventory.player;
		this.context = context;
		
		this.resultInv = new CraftingResultInventory();
		this.craftInv = new ArtisCraftingInventory(this, tableType.getWidth(), tableType.getHeight());
		if (tableType.hasBlockEntity()) {
			for (int i = 0; i < blockInventory.size(); i++) {
				craftInv.setStack(i, blockInventory.getStack(i));
			}
		}
		ContainerLayout layout = new ContainerLayout(tableType.getWidth(), tableType.getHeight(), tableType.hasCatalystSlot());
		
		mainPanel = new WPlainPanel();
		setRootPanel(mainPanel);
		
		this.resultSlot = new ArtisCraftingResultSlot(player, craftInv, resultInv, 0, 1, 1, true);
		int offsetX = 8;
		mainPanel.add(resultSlot, layout.getResultX() + offsetX, layout.getResultY() + 5);
		
		if (getArtisCraftingRecipeType().hasCatalystSlot()) {
			this.catalystSlot = WItemSlot.of(craftInv, craftInv.size() - 1);
			mainPanel.add(catalystSlot, layout.getCatalystX() + offsetX, layout.getCatalystY() + 1);
			
			WLabel catalystCost = new WLabel(Text.empty(), 0xAA0000).setHorizontalAlignment(HorizontalAlignment.CENTER);
			mainPanel.add(catalystCost, layout.getCatalystX() + offsetX, layout.getCatalystY() + 19);
		}
		
		this.craftingGrid = WItemSlot.of(craftInv, 0, getArtisCraftingRecipeType().getWidth(), getArtisCraftingRecipeType().getHeight());
		mainPanel.add(craftingGrid, layout.getGridX() + offsetX, layout.getGridY() + 1);
		
		this.playerInv = this.createPlayerInventoryPanel();
		mainPanel.add(playerInv, layout.getPlayerX() + offsetX, layout.getPlayerY());
		
		WLabel label = new WLabel(tableType.getName(), 0x404040);
		mainPanel.add(label, 8, 6);
		
		WSprite arrow = new WSprite(new Identifier(Artis.MOD_ID, "textures/gui/translucent_arrow.png"));
		mainPanel.add(arrow, layout.getArrowX() + offsetX, layout.getArrowY() + 5, 22, 15);
		
		mainPanel.validate(this);
		
		int width = Math.max(176, 74 + tableType.getWidth() * 18);
		int height;
		if (tableType.hasCatalystSlot()) {
			height = Math.max(150, 120 + tableType.getHeight() * 18);
		} else {
			height = Math.max(140, 120 + tableType.getHeight() * 18);
		}
		mainPanel.setSize(width, height);
	}
	
	private static BackgroundPainter slotColor(int color) {
		return (matrices, left, top, panel) -> {
			int lo = ScreenDrawing.multiplyColor(color, 0.5F);
			int bg = 0x4C000000;
			int hi = ScreenDrawing.multiplyColor(color, 1.25F);
			if (!(panel instanceof WItemSlot slot)) {
				ScreenDrawing.drawBeveledPanel(matrices, left - 1, top - 1, panel.getWidth() + 2, panel.getHeight() + 2, lo, bg, hi);
			} else {
				for (int x = 0; x < slot.getWidth() / 18; ++x) {
					for (int y = 0; y < slot.getHeight() / 18; ++y) {
						if (slot.isBigSlot()) {
							ScreenDrawing.drawBeveledPanel(matrices, x * 18 + left - 3, y * 18 + top - 3, 24, 24, lo, bg, hi);
						} else {
							ScreenDrawing.drawBeveledPanel(matrices, x * 18 + left, y * 18 + top, 18, 18, lo, bg, hi);
						}
					}
				}
			}
		};
	}
	
	public ArtisCraftingInventory getCraftInv() {
		return craftInv;
	}
	
	public CraftingResultInventory getResultInv() {
		return resultInv;
	}
	
	public PlayerEntity getPlayer() {
		return player;
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public void addPainters() {
		int color = tableType.getColor();
		if (tableType.hasColor()) {
			mainPanel.setBackgroundPainter(BackgroundPainter.createColorful(color));
			craftingGrid.setBackgroundPainter(slotColor(color));
			if (tableType.hasCatalystSlot())
				catalystSlot.setBackgroundPainter(slotColor(color));
			resultSlot.setBackgroundPainter(slotColor(color));
			playerInv.setBackgroundPainter(slotColor(color));
		} else {
			mainPanel.setBackgroundPainter(BackgroundPainter.VANILLA);
			craftingGrid.setBackgroundPainter(BackgroundPainter.SLOT);
			if (tableType.hasCatalystSlot())
				catalystSlot.setBackgroundPainter(BackgroundPainter.SLOT);
			resultSlot.setBackgroundPainter(BackgroundPainter.SLOT);
			playerInv.setBackgroundPainter(BackgroundPainter.SLOT);
		}
	}
	
	@Override
	public void onClosed(PlayerEntity player) {
		super.onClosed(player);
		this.context.run((world, pos) -> {
			if (!tableType.hasBlockEntity()) {
				dropInventory(player, craftInv);
			} else {
				for (int i = 0; i < craftInv.size(); i++) {
					blockInventory.setStack(i, craftInv.getStack(i));
				}
			}
		});
	}
	
	@Override
	public int getCraftingWidth() {
		return tableType.getWidth();
	}
	
	@Override
	public int getCraftingHeight() {
		return tableType.getHeight();
	}
	
	@Override
	public boolean matches(Recipe recipe) {
		return recipe.matches(craftInv, player.getWorld());
	}
	
	@Override
	public int getCraftingResultSlotIndex() {
		return 0;
	}
	
	@Override
	public int getCraftingSlotCount() {
		return getArtisCraftingRecipeType().getWidth() * getArtisCraftingRecipeType().getHeight();
	}
	
	@Override
	public void onContentChanged(Inventory inv) {
		super.onContentChanged(resultInv);
		if (world instanceof ServerWorld serverWorld) {
			updateResult(serverWorld, craftInv, resultInv, tableType);
		}
	}
	
	public static void updateResult(ServerWorld world, ArtisCraftingInventory inv, CraftingResultInventory resultInv, ArtisCraftingRecipeType artisCraftingRecipeType) {
		ItemStack itemstack = ItemStack.EMPTY;
		
		RecipeEntry<ArtisCraftingRecipe> recipe = findRecipe(artisCraftingRecipeType, inv, world, resultInv.getLastRecipe());
		if (recipe != null) {
			itemstack = recipe.value().craft(inv, world.getRegistryManager());
		}
		
		resultInv.setStack(0, itemstack);
		if (recipe != null) {
			resultInv.setLastRecipe(recipe);
		}
	}
	
	@Override
	public ArtisCraftingRecipeType getArtisCraftingRecipeType() {
		return tableType;
	}
	
	@Override
	public ItemStack quickMove(PlayerEntity player, int slotIndex) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot.hasStack()) {
			int tableSlotCount = getCraftingSlotCount() + 1 + (tableType.hasCatalystSlot() ? 1 : 0);
			if (slotIndex == getCraftingResultSlotIndex()) {
				return handleShiftCraft(player, this, slot, craftInv, resultInv, tableSlotCount, tableSlotCount + 36);
			}
			ItemStack toTake = slot.getStack();
			stack = toTake.copy();
			
			if (slotIndex < tableSlotCount) {
				if (!this.insertItem(stack, tableSlotCount, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.insertItem(stack, 0, tableSlotCount, false)) {
				return ItemStack.EMPTY;
			}
			
			if (stack.isEmpty()) {
				slot.setStack(ItemStack.EMPTY);
			} else {
				slot.markDirty();
			}
			
			if (slotIndex == getCraftingResultSlotIndex()) {
				player.dropItem(toTake, false);
			}
			
			slot.onTakeItem(player, toTake);
			
		}
		
		return stack;
	}
	
	@Override
	public void onSlotClick(int slotNumber, int button, SlotActionType action, PlayerEntity player) {
		if (slotNumber == getCraftingResultSlotIndex() && action == SlotActionType.QUICK_MOVE) {
			quickMove(player, slotNumber);
		} else {
			super.onSlotClick(slotNumber, button, action, player);
		}
	}
	
	@Override
	public void populateRecipeMatcher(RecipeMatcher finder) {
		this.craftInv.provideRecipeInputs(finder);
	}
	
	@Override
	public void clearCraftingSlots() {
		this.craftInv.clear();
		this.resultInv.clear();
	}
	
	@Override
	public boolean canInsertIntoSlot(ItemStack stack, Slot slot) {
		return slot.inventory != this.resultInv && super.canInsertIntoSlot(stack, slot);
	}
	
	public static ItemStack handleShiftCraft(PlayerEntity player, ArtisCraftingScreenHandler container, Slot resultSlot, ArtisCraftingInventory input, CraftingResultInventory craftResult, int outStart, int outEnd) {
		ItemStack outputCopy = ItemStack.EMPTY;
		if (resultSlot != null && resultSlot.hasStack()) {
			Recipe<?> recipe = findRecipe(container.tableType, input, player.getWorld(), craftResult.getLastRecipe());
			while (recipe != null && recipe.matches(input, player.getWorld())) {
				ItemStack recipeOutput = resultSlot.getStack().copy();
				outputCopy = recipeOutput.copy();
				
				recipeOutput.getItem().onCraftByPlayer(recipeOutput, player.getWorld(), player);
				
				if (!player.getWorld().isClient && !container.insertItem(recipeOutput, outStart, outEnd, true)) {
					return ItemStack.EMPTY;
				}
				
				resultSlot.onQuickTransfer(recipeOutput, outputCopy);
				resultSlot.markDirty();
				
				if (!player.getWorld().isClient && recipeOutput.getCount() == outputCopy.getCount()) {
					return ItemStack.EMPTY;
				}
				
				resultSlot.onTakeItem(player, recipeOutput);
				player.dropItem(recipeOutput, false);
			}
		}
		return craftResult.getLastRecipe() == null ? ItemStack.EMPTY : outputCopy;
	}
	
	public static RecipeEntry<?> findRecipe(ArtisCraftingRecipeType type, ArtisCraftingInventory inv, World world, @Nullable Identifier lastId) {
		Optional<RecipeEntry<ArtisCraftingRecipe>> foundRecipe = world.getRecipeManager().getFirstMatch(type, inv, world, lastId);
		if (foundRecipe.isPresent()) {
			return foundRecipe.get();
		}
		
		if (type.shouldIncludeNormalRecipes()) {
			Optional<RecipeEntry<CraftingRecipe>> foundVanillaRecipe = world.getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inv.getCraftingInventory(), world, lastId);
			if (foundVanillaRecipe.isPresent()) {
				return foundVanillaRecipe.get();
			}
		}
		
		return null;
	}
	
}
