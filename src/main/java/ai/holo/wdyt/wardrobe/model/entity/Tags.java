package ai.holo.wdyt.wardrobe.model.entity;


import java.util.List;

public record Tags(List<Color> colors, List<String> seasons, List<String> types, List<String> tags) {

    public Tags {
        colors = colors == null ? List.of() : List.copyOf(colors);
        seasons = seasons == null ? List.of() : List.copyOf(seasons);
        types = types == null ? List.of() : List.copyOf(types);
        tags = tags == null ? List.of() : List.copyOf(tags);
    }
}
