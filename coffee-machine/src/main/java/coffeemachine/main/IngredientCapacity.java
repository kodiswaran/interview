package coffeemachine.main;

import coffeemachine.model.BeverageFormula;
import coffeemachine.model.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class IngredientCapacity {

    @Getter
    private final Map<String, Integer> ingredients;

    /**
     * First we check whether the ingredients are available and then we prepare the beverage
     * based on the formula
     *
     * @param beverageFormula the formula of the beverage which contains the beverage name and
     *                       the formula on how to prepare the drink
     * @return response that states whether it prepared the beverage or not
     */
    public synchronized Response prepareDrink( BeverageFormula beverageFormula) {
        Map<String, Integer> ingredientsToReduce = beverageFormula.getIngredients();
        List<String> unavailableIngredients = getUnavailableIngredients(ingredientsToReduce);
        if (!unavailableIngredients.isEmpty())
            return Response.error(String.format("%s cannot be prepared because %s %s not available",
                    beverageFormula.getName(), unavailableIngredients, unavailableIngredients.size() > 1 ? "are": "is"));
        else {
            ingredientsToReduce.forEach((key, value) -> ingredients.put(key, ingredients.get(key) - value));
            return Response.success(String.format("%s is prepared", beverageFormula.getName()));
        }
    }

    /**
     * Check whether all the ingredients are available in the machine to prepare the expected drink
     * If there are ingredients that are not available in the machine, then the list is sent back to the caller
     *
     * @param ingredients the list of ingredients along with the quantity that is required to make the beverage
     * @return the list of ingredients that are not available in the machine to prepare the beverage
     */
    public List<String> getUnavailableIngredients( final Map<String, Integer> ingredients ) {
        final List<String> unavailableIngredients =  new ArrayList<>();
        for (Map.Entry<String, Integer> entry : ingredients.entrySet())
            if (!this.ingredients.containsKey(entry.getKey()) || this.ingredients.get(entry.getKey()) < entry.getValue())
                unavailableIngredients.add(entry.getKey());
        return unavailableIngredients;
    }

    /**
     * Reduces the ingredients in the machine with respect to the input quantities
     *
     * @param ingredientsToReduce the ingredients and theor quantities to be reduced
     * @return the response with the result
     */
    public Response reduceIngredients(final Map<String, Integer> ingredientsToReduce) {
        ingredientsToReduce.forEach((key, value) -> ingredients.put(key, ingredients.get(key) - value));
        return Response.success("success");
    }

    /**
     * Refills a particular ingredient in the machine
     * @param ingredient the ingredient name to be refilled
     * @param addQuantity the quantity to be refilled
     * @return the response stating whether it is refilled or not
     */
    public Response refill(String ingredient, int addQuantity) {
        ingredients.put(ingredient, addQuantity + ingredients.getOrDefault(ingredient, 0));
        return Response.success("Refill successful");
    }
}
