package coffeemachine.main;

import coffeemachine.model.BeverageFormula;
import coffeemachine.model.CoffeeMachine;
import coffeemachine.model.Response;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class CoffeeMachineOperator {

    private final CoffeeMachine coffeeMachine;

    /**
     * First we check whether the ingredients are available and then we prepare the beverage
     * based on the formula
     *
     * @param beverageName the beverage name
     * @return response that states whether it prepared the beverage or not
     */
    public synchronized Response prepareDrink( final String beverageName) {
        if (!coffeeMachine.getAvailableBeverages().containsKey(beverageName))
            return Response.error("Unknown beverage selected");

        final IngredientCapacity ingredientCapacity = coffeeMachine.getIngredientCapacity();
        final BeverageFormula beverageFormula = coffeeMachine.getAvailableBeverages().get(beverageName);

        final Map<String, Integer> ingredientsToReduce = beverageFormula.getIngredients();
        List<String> unavailableIngredients = ingredientCapacity.getUnavailableIngredients(ingredientsToReduce);
        if (!unavailableIngredients.isEmpty())
            return Response.error(String.format("%s cannot be prepared because %s %s not available",
                beverageFormula.getName(), unavailableIngredients, unavailableIngredients.size() > 1 ? "are": "is"));
        else {
            ingredientCapacity.reduceIngredients(ingredientsToReduce);
            return Response.success(String.format("%s is prepared", beverageFormula.getName()));
        }
    }

    /**
     * Refills a particular ingredient in the machine
     * @param ingredientName the ingredient name to be refilled
     * @param addQuantity the quantity to be refilled
     * @return the response stating whether it is refilled or not
     */
    public synchronized Response refill(final String ingredientName, int addQuantity) {
        return coffeeMachine.getIngredientCapacity().refill(ingredientName, addQuantity);
    }

    /**
     * Fetches the list of beverages that the machine has formulas to prepare
     * This list can be used to display the beverage name on the console
     *
     * @return the list of beverages names
     */
    public Set<String> getBeverages() {
        return coffeeMachine.getAvailableBeverages().keySet();
    }
}
