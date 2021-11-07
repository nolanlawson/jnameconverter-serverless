package jnameconverter;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import com.nolanlawson.japanesenamegenerator.v3.JapaneseNameGenerator;
import com.nolanlawson.japanesenamegenerator.v3.util.Pair;
import com.nolanlawson.japanesenamegenerator.v3.ConversionException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final int MAX_LENGTH = 200;
    private static final Gson gson = new GsonBuilder().create();
    private static final JapaneseNameGenerator japaneseNameGenerator = buildJapaneseNameGenerator();
    private static final JapaneseNameGenerator buildJapaneseNameGenerator() {
        InputStream roomajiInputStream = new BufferedInputStream(
                App.class.getClassLoader().getResourceAsStream(
                        "roomaji_model_20090128_pop1_3_3_min2_fewer_rules_hacked.txt"));
        InputStream directLookupInputstream = new BufferedInputStream(
                App.class.getClassLoader().getResourceAsStream(
                        "all_names.txt"));
        return new JapaneseNameGenerator(roomajiInputStream, directLookupInputstream);
    }

    private ConversionResult getConversionResult(String query) throws ConversionException {
        Pair<String, String> result = japaneseNameGenerator.convertToRomaajiAndKatakana(query);
        return new ConversionResult(false, result.getFirst(), result.getSecond());
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=utf-8");

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        String query = null;
        try {
            query = input.getQueryStringParameters().get("q");
            int len = query.length();
            if (len > MAX_LENGTH) {
                throw new Error("Max length exceeded. Max is " + MAX_LENGTH + " and length is: " + len);
            }
            ConversionResult conversionResult = this.getConversionResult(query);
            String output = gson.toJson(conversionResult);

            headers.put("Cache-Control", "public, max-age=604800, s-max-age=604800");
            return response
                    .withStatusCode(200)
                    .withBody(output);
        } catch (Throwable t) {
            t.printStackTrace();
            ConversionResult conversionResult = new ConversionResult(true, null, null);
            String output = gson.toJson(conversionResult);
            return response
                    .withStatusCode(500)
                    .withBody(output);
        }
    }
}
