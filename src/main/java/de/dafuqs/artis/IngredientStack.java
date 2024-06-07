package de.dafuqs.artis;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.predicate.*;
import net.minecraft.recipe.*;
import net.minecraft.registry.entry.*;
import net.minecraft.util.dynamic.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public record IngredientStack(Ingredient ingredient, int count, ComponentPredicate components, ItemStack displayStack) {
    
    public static final Codec<IngredientStack> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			Ingredient.DISALLOW_EMPTY_CODEC.fieldOf("ingredient").forGetter(IngredientStack::ingredient),
			Codecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(IngredientStack::count),
			ComponentPredicate.CODEC.optionalFieldOf("components", ComponentPredicate.EMPTY).forGetter(IngredientStack::components),
			ItemStack.CODEC.fieldOf("display_item").forGetter(IngredientStack::displayStack)
	).apply(instance, IngredientStack::new));
    
    public static final PacketCodec<RegistryByteBuf, IngredientStack> PACKET_CODEC = PacketCodec.tuple(
            Ingredient.PACKET_CODEC, IngredientStack::ingredient,
            PacketCodecs.VAR_INT, IngredientStack::count,
            ComponentPredicate.PACKET_CODEC, IngredientStack::components,
            ItemStack.PACKET_CODEC, IngredientStack::displayStack,
            IngredientStack::new);
    
    public static final PacketCodec<RegistryByteBuf, Optional<IngredientStack>> OPTIONAL_PACKET_CODEC = PACKET_CODEC.collect(PacketCodecs::optional);
	public static final IngredientStack EMPTY = new IngredientStack(Ingredient.EMPTY, Items.AIR.getRegistryEntry());
    
    public IngredientStack(Ingredient ingredient, RegistryEntry<Item> displayItem) {
        this(ingredient, 1, displayItem);
    }
    
    public IngredientStack(Ingredient ingredient, int count, RegistryEntry<Item> displayItem) {
        this(ingredient, count, ComponentPredicate.EMPTY, displayItem);
    }
    
    public IngredientStack(Ingredient ingredient, int count, ComponentPredicate components, RegistryEntry<Item> displayItem) {
        this(ingredient, count, components, createDisplayStack(displayItem, count, components));
    }
    
    public IngredientStack(Ingredient ingredient, int count, ComponentPredicate components, ItemStack displayStack) {
        this.ingredient = ingredient;
        this.count = count;
        this.components = components;
        this.displayStack = displayStack;
    }
    
    public IngredientStack withComponents(UnaryOperator<ComponentPredicate.Builder> builderCallback, RegistryEntry<Item> displayItem) {
        return new IngredientStack(this.ingredient, this.count, builderCallback.apply(ComponentPredicate.builder()).build(), displayItem);
    }
    
    private static ItemStack createDisplayStack(RegistryEntry<Item> item, int count, ComponentPredicate components) {
        return new ItemStack(item, count, components.toChanges());
    }
    
    public boolean matches(ItemStack stack) {
        return this.ingredient.test(stack) && stack.getCount() >= this.count && this.components.test(stack);
    }
    
    public Ingredient ingredient() {
        return this.ingredient;
    }
    
    public int count() {
        return this.count;
    }
    
    public ComponentPredicate components() {
        return this.components;
    }
    
    public ItemStack displayStack() {
        return this.displayStack;
    }
    
    public Collection<ItemStack> getStacks() {
        ItemStack[] stacks = ingredient.getMatchingStacks();
        
        if (stacks == null)
            return new ArrayList<>();
        
        return Arrays.stream(stacks)
                .peek(stack -> stack.setCount(count))
                .peek(stack -> stack.applyComponentsFrom(displayStack.getComponents())) // TODO: needed?
                .collect(Collectors.toList());
    }
	
}
