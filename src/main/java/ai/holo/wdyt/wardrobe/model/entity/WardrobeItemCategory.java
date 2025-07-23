package ai.holo.wdyt.wardrobe.model.entity;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public enum WardrobeItemCategory {
    TOPS("tops", List.of("T-Shirt", "Blouse", "Shirt", "Tank Top", "Crop Top", "Sweater",
            "Cardigan", "Halter Top", "Tube Top", "Long Sleeve", "Polo Shirt", "Camisole",
            "Wrap Top", "Peplum Top", "Bodysuit", "Henley", "Tunic", "Jersey", "Muscle Tee", "Knit Top")),

    BOTTOMS("bottoms", List.of("Jeans", "Trousers", "Leggings", "Shorts", "Skirt", "Culottes",
            "Joggers", "Cargo Pants", "Chinos", "Palazzo Pants", "Bermuda Shorts",
            "Mini Skirt", "Midi Skirt", "Maxi Skirt", "Skort", "Track Pants", "Leather Pants",
            "Denim Shorts", "Wide-Leg Pants", "Bike Shorts")),

    DRESSES("dresses", List.of("Mini Dress", "Midi Dress", "Maxi Dress", "Wrap Dress",
            "Shirt Dress", "Slip Dress", "Bodycon Dress", "A-Line Dress", "Sheath Dress",
            "T-Shirt Dress", "Sweater Dress", "Halter Dress", "Peplum Dress", "Empire Waist Dress",
            "Ball Gown", "Sun Dress", "Cocktail Dress", "Off-Shoulder Dress", "One-Shoulder Dress",
            "Blazer Dress")),

    JUMPSUITS("jumpsuits", List.of("Jumpsuit", "Romper", "Boiler Suit", "Dungarees")),

    OUTERWEAR("outerwear", List.of("Jacket", "Blazer", "Coat", "Trench Coat", "Denim Jacket",
            "Bomber Jacket", "Leather Jacket", "Puffer Jacket", "Parka", "Cape", "Anorak",
            "Peacoat", "Overcoat", "Wrap Coat", "Quilted Jacket", "Varsity Jacket",
            "Faux Fur Coat", "Windbreaker", "Poncho")),

    FOOTWEAR("footwear", List.of("Sneakers", "Heels", "Boots", "Sandals", "Flats", "Loafers",
            "Mules", "Wedges", "Oxfords", "Ankle Boots", "Knee-High Boots", "Slippers",
            "Running Shoes", "Platform Shoes", "Flip-Flops", "Espadrilles", "Brogues",
            "Combat Boots", "Chelsea Boots")),

    SWIMWEAR("swimwear", List.of("Bikini", "One-Piece", "Tankini", "Swim Shorts", "Swim Trunks",
            "Cover-Up", "Swim Skirt", "Swim Dress")),

    ACCESSORIES("accessories", List.of("Hat", "Scarf", "Belt", "Sunglasses", "Watch", "Necklace",
            "Bracelet", "Earrings", "Ring", "Hair Clip", "Hairband", "Headband", "Gloves", "Tie",
            "Bow Tie", "Brooch", "Handbag", "Backpack", "Tote Bag", "Clutch"));

    private final String displayName;
    private final List<String> types;

    WardrobeItemCategory(String displayName, List<String> types) {
        this.displayName = displayName;
        this.types = types;
    }

    public static WardrobeItemCategory fromValue(String label) {
        return Arrays.stream(WardrobeItemCategory.values())
                .filter(c -> c.getDisplayName().equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown category: " + label));
    }

    public List<String> filterSubcategories(String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return types;
        }
        String lower = searchTerm.toLowerCase();
        return types.stream()
                .filter(t -> t.toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}
