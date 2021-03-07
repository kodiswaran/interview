package coffeemachine.model;

import coffeemachine.main.IngredientCapacity;
import lombok.Data;

import java.util.Map;

@Data
public class CoffeeMachine {
    private final int outlets;
    private final IngredientCapacity ingredientCapacity;
    private final Map<String, BeverageFormula> availableBeverages;
}
