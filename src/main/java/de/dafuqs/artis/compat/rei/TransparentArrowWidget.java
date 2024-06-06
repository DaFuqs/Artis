package de.dafuqs.artis.compat.rei;

import de.dafuqs.artis.*;
import me.shedaniel.math.*;
import me.shedaniel.rei.api.client.gui.widgets.*;
import net.minecraft.client.gui.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class TransparentArrowWidget extends Arrow {
	
	private static final Identifier TEXTURE = new Identifier(Artis.MOD_ID, "textures/gui/arrow.png");
	
	@NotNull
	private final Rectangle bounds;
	private double animationDuration = -1;
	
	public TransparentArrowWidget(@NotNull me.shedaniel.math.Rectangle bounds) {
		this.bounds = new Rectangle(Objects.requireNonNull(bounds));
	}
	
	@NotNull
	public static Arrow create(@NotNull Point point) {
		return new TransparentArrowWidget(new Rectangle(point, new Dimension(24, 17)));
	}
	
	@Override
	public double getAnimationDuration() {
		return animationDuration;
	}
	
	@Override
	public void setAnimationDuration(double animationDurationMS) {
		this.animationDuration = animationDurationMS;
		if (this.animationDuration <= 0)
			this.animationDuration = -1;
	}
	
	@NotNull
	@Override
	public Rectangle getBounds() {
		return bounds;
	}
	
	public List<? extends Element> children() {
		return Collections.emptyList();
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.drawTexture(TEXTURE, getX(), getY(), 24, 0, 24, 17, 48, 17);
		if (getAnimationDuration() > 0) {
			int width = MathHelper.ceil((System.currentTimeMillis() / (animationDuration / 24) % 24d));
			context.drawTexture(TEXTURE, getX(), getY(), 0, 0, width, 17, 48, 17);
		}
	}
	
}
