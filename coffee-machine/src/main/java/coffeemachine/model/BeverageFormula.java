package coffeemachine.model;

import lombok.Data;

import java.util.Map;

@Data
public class BeverageFormula {
    private final String name;
    private final Map<String, Integer> ingredients;
}
