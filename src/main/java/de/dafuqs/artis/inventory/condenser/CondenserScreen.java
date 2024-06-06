package de.dafuqs.artis.inventory.condenser;

import de.dafuqs.artis.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.entity.player.*;
import net.minecraft.text.*;
import net.minecraft.util.*;

public class CondenserScreen extends HandledScreen<CondenserScreenHandler> {
	
	public static final Identifier BACKGROUND = new Identifier(Artis.MOD_ID, "textures/gui/condenser.png");
	
	public CondenserScreen(CondenserScreenHandler handler, PlayerInventory playerInventory, Text title) {
		super(handler, playerInventory, title);
		this.backgroundHeight = 219;
		this.playerInventoryTitleY = 73;
	}
	
	@Override
	protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
		context.drawTexture(BACKGROUND, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
		if (this.handler.isBurning()) {
			int fuelProgress = this.handler.getFuelProgress();
			context.drawTexture(BACKGROUND, x + 56, y + 36 + 12 - fuelProgress, 176, 12 - fuelProgress, 14, fuelProgress + 1);
		}
		
		int cookProgress = this.handler.getCookProgress();
		context.drawTexture(BACKGROUND, x + 79, y + 34, 176, 14, cookProgress + 1, 16);
	}
	
	@Override
	protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
		super.drawForeground(context, mouseX, mouseY);
		
		context.drawText(this.textRenderer, "x" + this.handler.getInputItemCount(), 76, titleY + 16, 4210752, false);
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		renderBackground(context);
		super.render(context, mouseX, mouseY, delta);
		drawMouseoverTooltip(context, mouseX, mouseY);
	}
	
}