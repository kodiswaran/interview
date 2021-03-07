package coffeemachine;

import coffeemachine.main.CoffeeMachineOperator;
import coffeemachine.model.BeverageFormula;
import coffeemachine.model.Response;
import coffeemachine.model.CoffeeMachine;
import coffeemachine.main.IngredientCapacity;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import coffeemachine.util.JsonUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CoffeeMachineEntry {

    private static final String COMMAND_DRINK = "drink";
    private static final String COMMAND_REFILL = "refill";
    private static final int MAX_WAIT_TIME_IN_SECONDS = 5;

    private static final Function<String, List<String>> TRIM_TO_VALID_WORDS = line ->
            Arrays.stream(line.split(","))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

    public static void main(String[] args) throws IOException, ParseException, InterruptedException {
        CoffeeMachine coffeeMachine = createCoffeeMachine("src/main/resources/configuration.json");
        CoffeeMachineOperator coffeeMachineOperator = new CoffeeMachineOperator(coffeeMachine);
        List<List<String>> commands = Files.lines(Paths.get("src/main/resources/input.txt"))
                .map(TRIM_TO_VALID_WORDS)
                .filter(command -> !command.isEmpty())
                .collect(Collectors.toList());

        for(List<String> command: commands) {
            if(COMMAND_DRINK.equals(command.get(0)) && command.size() > 1) {
                makeBeverages(coffeeMachineOperator, command);
            } else if (COMMAND_REFILL.equals(command.get(0)) && command.size() == 3) {
                Response response = coffeeMachineOperator.refill(command.get(1), Integer.parseInt(command.get(2)));
                System.out.println(response.getMessage());
            } else {
                System.out.println("Unknown command! :" + command);
            }
        }
    }

    /**
     * Since the machine has multiple outlets, there can be parallel operations in the machine.
     * Therefore, multiple threads are created based on the operation size and all the drinks
     * are created in parallel based on the ingredients availability
     *
     * @param coffeeMachineOperator the coffee machine operator
     * @param command the list of drinks to be prepared
     */
    private static void makeBeverages(CoffeeMachineOperator coffeeMachineOperator, List<String> command) {
        ExecutorService executor = Executors.newFixedThreadPool(command.size());
        List<Future<Response>> futureResult = IntStream.range(1, command.size())
                .mapToObj(command::get)
                .map(beverage -> executor.submit(() -> coffeeMachineOperator.prepareDrink(beverage)))
                .collect(Collectors.toList());

        executor.shutdown();
        futureResult.stream().map(CoffeeMachineEntry::getResponse)
                .map(Response::getMessage)
                .forEach(System.out::println);
    }

    /**
     * Fetches the response of the operation after waiting for $MAX_WAIT_TIME_IN_SECONDS seconds
     * @param responseFuture the future object
     * @return the response that contains the result of the operation
     */
    private static Response getResponse(Future<Response> responseFuture) {
        Response response = null;
        try {
            response = responseFuture.get(MAX_WAIT_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("Error while getting value from future object " + e);
            response = Response.error("Something went wrong. Please try again after some time.");
        }
        return response;
    }

    /**
     * Creates a coffee machine using the configuration json file that is fed to the system
     * @param filePath the path that contains the configuration json
     * @return the coffee machine instance
     * @throws IOException when the configuration file is not available in the given location
     * @throws ParseException when the configuration file is not in the expected json format
     */
    public static CoffeeMachine createCoffeeMachine(String filePath) throws IOException, ParseException {
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(new FileReader(filePath));;
        JSONObject machineObj = JsonUtil.getValue("machine", jsonObject, JSONObject.class);
        JSONObject outletsObj = JsonUtil.getValue("outlets", machineObj, JSONObject.class);
        int count = JsonUtil.getValue("count_n", outletsObj, Long.class).intValue();

        JSONObject totalItemsQuantityObj = JsonUtil.getValue("total_items_quantity", machineObj, JSONObject.class);
        Map<String, Integer> ingredients = JsonUtil.convertObjectToMap(totalItemsQuantityObj);
        JSONObject beveragesObj = JsonUtil.getValue("beverages", machineObj, JSONObject.class);

        @SuppressWarnings("unchecked")
        Set<String> beverages = (Set<String>) beveragesObj.keySet();
        Map<String, BeverageFormula> beverageFormulas = beverages.stream()
                .map(beverage -> new BeverageFormula(beverage,
                        JsonUtil.convertObjectToMap(JsonUtil.getValue(beverage, beveragesObj, JSONObject.class))))
                .collect(Collectors.toMap(BeverageFormula::getName, Function.identity()));

        return new CoffeeMachine(count, new IngredientCapacity(ingredients), beverageFormulas);
    }
}
