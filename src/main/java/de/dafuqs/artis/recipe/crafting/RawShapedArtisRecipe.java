package de.dafuqs.artis.recipe.crafting;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.*;
import de.dafuqs.artis.*;
import de.dafuqs.artis.inventory.crafting.*;
import it.unimi.dsi.fastutil.chars.*;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.*;
import net.minecraft.util.collection.*;
import net.minecraft.util.dynamic.*;
import org.jetbrains.annotations.*;
import oshi.util.tuples.*;

import java.util.*;
import java.util.function.*;

public record RawShapedArtisRecipe(int width, int height, int maxWidth, int maxHeight,
                                   DefaultedList<IngredientStack> ingredientStacks, Optional<Data> data) {
    
    public static final MapCodec<RawShapedArtisRecipe> CODEC = RawShapedArtisRecipe.Data.CODEC.flatXmap(RawShapedArtisRecipe::fromData, (recipe) -> recipe.data().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe")));
    public static final PacketCodec<RegistryByteBuf, RawShapedArtisRecipe> PACKET_CODEC = PacketCodec.of(RawShapedArtisRecipe::writeToBuf, RawShapedArtisRecipe::readFromBuf);
    
    public RawShapedArtisRecipe(int width, int height, int maxWidth, int maxHeight, DefaultedList<IngredientStack> ingredientStacks, Optional<Data> data) {
        this.width = width;
        this.height = height;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.ingredientStacks = ingredientStacks;
        this.data = data;
    }
    
    public static RawShapedArtisRecipe create(Map<Character, IngredientStack> key, String... pattern) {
        return create(key, List.of(pattern));
    }
    
    public static RawShapedArtisRecipe create(Map<Character, IngredientStack> key, List<String> pattern) {
        Data data = new Data(key, pattern);
        return fromData(data).getOrThrow();
    }
    
    private static DataResult<RawShapedArtisRecipe> fromData(Data data) {
        String[] strings = removePadding(data.pattern);
        int width = strings[0].length();
        int height = strings.length;
        DefaultedList<IngredientStack> defaultedList = DefaultedList.ofSize(width * height, IngredientStack.EMPTY);
        CharSet charSet = new CharArraySet(data.key.keySet());
        
        for (int k = 0; k < strings.length; ++k) {
            String string = strings[k];
            
            for (int l = 0; l < string.length(); ++l) {
                char c = string.charAt(l);
                IngredientStack ingredient = c == ' ' ? IngredientStack.EMPTY : data.key.get(c);
                if (ingredient == null) {
                    return DataResult.error(() -> "Pattern references symbol '" + c + "' but it's not defined in the key");
                }
                
                charSet.remove(c);
                defaultedList.set(l + width * k, ingredient);
            }
        }
        
        if (!charSet.isEmpty()) {
            return DataResult.error(() -> "Key defines symbols that aren't used in pattern: " + charSet);
        } else {
            return DataResult.success(new RawShapedArtisRecipe(width, height, maxWidth, maxHeight, defaultedList, Optional.of(data)));
        }
    }
    
    static String[] removePadding(List<String> pattern) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;
        
        for (int m = 0; m < pattern.size(); ++m) {
            String string = pattern.get(m);
            i = Math.min(i, findFirstSymbol(string));
            int n = findLastSymbol(string);
            j = Math.max(j, n);
            if (n < 0) {
                if (k == m) {
                    ++k;
                }
                
                ++l;
            } else {
                l = 0;
            }
        }
        
        if (pattern.size() == l) {
            return new String[0];
        } else {
            String[] strings = new String[pattern.size() - l - k];
            
            for (int o = 0; o < strings.length; ++o) {
                strings[o] = pattern.get(o + k).substring(i, j + 1);
            }
            
            return strings;
        }
    }
    
    private static int findFirstSymbol(String line) {
        int i;
        for (i = 0; i < line.length() && line.charAt(i) == ' '; ++i) {
        }
        return i;
    }
    
    private static int findLastSymbol(String line) {
        int i;
        for (i = line.length() - 1; i >= 0 && line.charAt(i) == ' '; --i) {
        }
        return i;
    }
    
    public boolean matches(RecipeInputInventory inventory) {
        for (int i = 0; i <= inventory.getWidth() - this.width; ++i) {
            for (int j = 0; j <= inventory.getHeight() - this.height; ++j) {
                if (this.matches(inventory, i, j, true)) {
                    return true;
                }
                
                if (this.matches(inventory, i, j, false)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private boolean matches(RecipeInputInventory inventory, int offsetX, int offsetY, boolean flipped) {
        for (int i = 0; i < inventory.getWidth(); ++i) {
            for (int j = 0; j < inventory.getHeight(); ++j) {
                int k = i - offsetX;
                int l = j - offsetY;
                IngredientStack ingredient = IngredientStack.EMPTY;
                if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
                    if (flipped) {
                        ingredient = this.ingredientStacks.get(this.width - k - 1 + l * this.width);
                    } else {
                        ingredient = this.ingredientStacks.get(k + l * this.width);
                    }
                }
                
                if (!ingredient.matches(inventory.getStack(i + j * inventory.getWidth()))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private void writeToBuf(RegistryByteBuf buf) {
        buf.writeVarInt(this.width);
        buf.writeVarInt(this.height);
		for (IngredientStack ingredient : this.ingredientStacks) {
			IngredientStack.PACKET_CODEC.encode(buf, ingredient);
		}
    }
    
    private static RawShapedArtisRecipe readFromBuf(RegistryByteBuf buf) {
        int i = buf.readVarInt();
        int j = buf.readVarInt();
        DefaultedList<IngredientStack> defaultedList = DefaultedList.ofSize(i * j, IngredientStack.EMPTY);
        defaultedList.replaceAll((ingredient) -> IngredientStack.PACKET_CODEC.decode(buf));
        return new RawShapedArtisRecipe(i, j, defaultedList, Optional.empty());
    }
    
    public int width() {
        return this.width;
    }
    
    public int height() {
        return this.height;
    }
    
    public DefaultedList<IngredientStack> ingredientStacks() {
        return this.ingredientStacks;
    }
    
    public Optional<Data> data() {
        return this.data;
    }
    
    public record Data(Map<Character, IngredientStack> key, List<String> pattern) {
        
        private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap((pattern) -> {
            if (pattern.size() > maxHeight) {
                return DataResult.error(new Supplier<>() {
					@Override
					public String get() {
						return "Invalid pattern: too many rows, " + maxHeight + " is maximum";
					}
				});
            } else if (pattern.isEmpty()) {
                return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            } else {
                int i = pattern.getFirst().length();
                Iterator<String> var2 = pattern.iterator();
                
                String string;
                do {
                    if (!var2.hasNext()) {
                        return DataResult.success(pattern);
                    }
                    
                    string = var2.next();
                    if (string.length() > maxWidth) {
                        return DataResult.error(new Supplier<>() {
                            @Override
                            public String get() {
                                return "Invalid pattern: too many columns, " + maxWidth + " is maximum";
                            }
                        });
                    }
                } while (i == string.length());
                
                return DataResult.error(() -> "Invalid pattern: each row must be the same width");
            }
        }, Function.identity());
        
        private static final Codec<Character> KEY_ENTRY_CODEC = Codec.STRING.comapFlatMap((keyEntry) -> {
            if (keyEntry.length() != 1) {
                return DataResult.error(() -> "Invalid key entry: '" + keyEntry + "' is an invalid symbol (must be 1 character only).");
            } else {
                return " ".equals(keyEntry) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(keyEntry.charAt(0));
            }
        }, String::valueOf);
        
        public static final MapCodec<Data> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                Codecs.strictUnboundedMap(KEY_ENTRY_CODEC, IngredientStack.CODEC).fieldOf("key").forGetter((data) -> data.key),
                PATTERN_CODEC.fieldOf("pattern").forGetter((data) -> data.pattern)
        ).apply(instance, Data::new));
        
        public Data(Map<Character, IngredientStack> key, List<String> pattern) {
            this.key = key;
            this.pattern = pattern;
        }
        
        public Map<Character, IngredientStack> key() {
            return this.key;
        }
        
        public List<String> pattern() {
            return this.pattern;
        }
        
    }
    
    private boolean matchesPattern(ArtisCraftingInventory inv, int offsetX, int offsetY, boolean flipped) {
        for (int i = 0; i < inv.getWidth(); ++i) {
            for (int j = 0; j < inv.getHeight(); ++j) {
                int k = i - offsetX;
                int l = j - offsetY;
                IngredientStack ingredientStack = IngredientStack.EMPTY;
                if (k >= 0 && l >= 0 && k < this.width && l < this.height) {
                    if (flipped) {
                        ingredientStack = this.ingredientStacks.get(this.width - k - 1 + l * this.width);
                    } else {
                        ingredientStack = this.ingredientStacks.get(k + l * this.width);
                    }
                }
                
                if (!ingredientStack.matches(inv.getStack(i + j * inv.getWidth()))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // Triplet<XOffset, YOffset, Flipped>
    public @Nullable Triplet<Integer, Integer, Boolean> getRecipeOrientation(ArtisCraftingInventory inv) {
        for (int i = 0; i <= inv.getWidth() - this.width; ++i) {
            for (int j = 0; j <= inv.getHeight() - this.height; ++j) {
                if (this.matchesPattern(inv, i, j, true)) {
                    return new Triplet<>(i, j, true);
                }
                if (this.matchesPattern(inv, i, j, false)) {
                    return new Triplet<>(i, j, false);
                }
            }
        }
        return null;
    }
    
    protected void decrementIngredientStacks(ArtisCraftingInventory inventory, Triplet<Integer, Integer, Boolean> orientation, PlayerEntity player) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                int ingredientStackId = orientation.getC() ? ((this.width - 1) - x) + this.width * y : x + this.width * y;
                int invStackId = (x + orientation.getA()) + inventory.getWidth() * (y + orientation.getB());
                
                IngredientStack ingredientStackAtPos = ingredientStacks.get(ingredientStackId);
                ItemStack invStack = inventory.getStack(invStackId);
                
                if (!invStack.isEmpty()) {
                    Item recipeReminderItem = invStack.getItem().getRecipeRemainder();
                    if (recipeReminderItem == null) {
                        invStack.decrement(ingredientStackAtPos.count());
                    } else {
                        if (inventory.getStack(invStackId).getCount() == ingredientStackAtPos.count()) {
                            ItemStack remainderStack = recipeReminderItem.getDefaultStack();
                            remainderStack.setCount(ingredientStackAtPos.count());
                            inventory.setStack(invStackId, remainderStack);
                        } else {
                            inventory.getStack(invStackId).decrement(ingredientStackAtPos.count());
                            ItemStack remainderStack = recipeReminderItem.getDefaultStack();
                            player.giveItemStack(remainderStack);
                        }
                    }
                }
                
            }
        }
    }
	
}
