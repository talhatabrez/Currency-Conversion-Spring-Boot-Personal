package com.currencyConversion.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CurrencyConverterController {

    private static final String API_URL = "https://v6.exchangerate-api.com/v6/{API_KEY here}/latest/USD";
    private static Map<String, Double> exchangeRates = new HashMap<>();

    static {
        loadExchangeRates();
    }

    private static void loadExchangeRates() {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            JSONObject rates = jsonObject.getJSONObject("conversion_rates");

            Iterator<String> keys = rates.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                double value = rates.getDouble(key);
                exchangeRates.put(key, value);
            }

        } catch (Exception e) {
            System.out.println("Exception caught, try again!");
            e.printStackTrace();
        }
    }

    @GetMapping("/convert")
    public String convert(@RequestParam(name = "amount", required = false, defaultValue = "1") double amount,
                          @RequestParam(name = "currency", required = false, defaultValue = "EUR") String currency,
                          Model model) {
        double convertedAmount = 0.0;
        if (exchangeRates.containsKey(currency.toUpperCase())) {
            double exchangeRate = exchangeRates.get(currency.toUpperCase());
            convertedAmount = amount * exchangeRate;
        }
        model.addAttribute("amount", amount);
        model.addAttribute("currency", currency.toUpperCase());
        model.addAttribute("convertedAmount", convertedAmount);
        return "result";
    }
}
