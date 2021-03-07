package coffeemachine.main;

import coffeemachine.model.BeverageFormula;
import coffeemachine.model.Response;
import coffeemachine.util.JsonUtil;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@TestInstance(Lifecycle.PER_CLASS)
public class IngredientCapacityTest {

    private JSONObject ingredientObject;
    private Map<String, BeverageFormula> beverageFormulas;

    @BeforeAll
    public void setUp() throws IOException, ParseException {
        ingredientObject = (JSONObject) new JSONParser().parse(new FileReader("src/test/test-resources/ingredients.json"));
        JSONObject beveragesObject = (JSONObject) new JSONParser().parse(new FileReader("src/test/test-resources/beverages.json"));;
        JSONObject beveragesObj = JsonUtil.getValue("beverages", beveragesObject, JSONObject.class);

        @SuppressWarnings("unchecked")
        Set<String> beverages = (Set<String>) beveragesObj.keySet();
        beverageFormulas = beverages.stream()
            .map(beverage -> new BeverageFormula(beverage,
                JsonUtil.convertObjectToMap(JsonUtil.getValue(beverage, beveragesObj, JSONObject.class))))
            .collect(Collectors.toMap(BeverageFormula::getName, Function.identity()));
    }

    @DisplayName("Success scenario")
    @ParameterizedTest
    @ValueSource(strings = {"minimum_ingredient", "more_ingredient"})
    public void prepareDrinkSuccessTests(String testObj) {
        JSONObject totalItemsQuantityObj = JsonUtil.getValue(testObj, ingredientObject, JSONObject.class);
        Map<String, Integer> ingredients = JsonUtil.convertObjectToMap(totalItemsQuantityObj);
        IngredientCapacity ingredientCapacity = new IngredientCapacity(ingredients);
        Response response = ingredientCapacity.prepareDrink(beverageFormulas.get("hot_tea"));
        Assertions.assertTrue(response.isSuccess());
    }

    @DisplayName("Failure scenario")
    @ParameterizedTest
    @ValueSource(strings = {"minimum_quantity"})
    public void prepareDrinkSuccessFailure(String testObj) {
        JSONObject totalItemsQuantityObj = JsonUtil.getValue(testObj, ingredientObject, JSONObject.class);
        Map<String, Integer> ingredients = JsonUtil.convertObjectToMap(totalItemsQuantityObj);
        IngredientCapacity ingredientCapacity = new IngredientCapacity(ingredients);
        Response response = ingredientCapacity.prepareDrink(beverageFormulas.get("hot_tea"));
        Assertions.assertTrue(response.isSuccess());
    }
}
