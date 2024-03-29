package jnameconverter;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.util.HashMap;

public class AppTest {
  @Test
  public void successfulResponse() {
    App app = new App();
    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
    input.setQueryStringParameters(new HashMap<String, String>(){{
      put("q", "nolan");
    }});
    APIGatewayProxyResponseEvent result = app.handleRequest(input, null);
    assertEquals(result.getStatusCode().intValue(), 200);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"error\":false,\"roomaji\":\"nooran\",\"katakana\":\"ノーラン\"}");
  }

  @Test
  public void noInput() {
    App app = new App();
    APIGatewayProxyResponseEvent result = app.handleRequest(null, null);
    assertEquals(result.getStatusCode().intValue(), 500);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"error\":true,\"roomaji\":null,\"katakana\":null}");
  }

  @Test
  public void noQuery() {
    App app = new App();
    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
    APIGatewayProxyResponseEvent result = app.handleRequest(input, null);
    assertEquals(result.getStatusCode().intValue(), 500);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"error\":true,\"roomaji\":null,\"katakana\":null}");
  }

  @Test
  public void maxLengthOver() {
    App app = new App();
    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
    input.setQueryStringParameters(new HashMap<String, String>(){{
      String str = "";
      for (int i = 0; i < 10000; i++) {
        str += "foo";
      }
      put("q", str);
    }});
    APIGatewayProxyResponseEvent result = app.handleRequest(input, null);
    assertEquals(result.getStatusCode().intValue(), 500);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"error\":true,\"roomaji\":null,\"katakana\":null}");
  }
}
