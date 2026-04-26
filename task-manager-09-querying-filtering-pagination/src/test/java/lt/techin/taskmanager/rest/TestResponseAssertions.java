package lt.techin.taskmanager.rest;

import org.springframework.test.util.JsonPathExpectationsHelper;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public final class TestResponseAssertions {

    private TestResponseAssertions() {
    }

    public static MvcResult expectStatus(ResultActions action, int expectedStatus, String message) throws Exception {
        MvcResult result = action.andReturn();
        assertEquals(
                expectedStatus,
                result.getResponse().getStatus(),
                message + responseSuffix(result)
        );
        return result;
    }

    public static void assertJsonText(MvcResult result, String pointer, String expected, String message) {
        Object value = evaluate(result, pointer, message);
        assertEquals(expected, String.valueOf(value), message + responseSuffix(result));
    }

    public static void assertJsonInt(MvcResult result, String pointer, int expected, String message) {
        Object value = evaluate(result, pointer, message);
        assertEquals(expected, ((Number) value).intValue(), message + responseSuffix(result));
    }

    public static void assertJsonLong(MvcResult result, String pointer, long expected, String message) {
        Object value = evaluate(result, pointer, message);
        assertEquals(expected, ((Number) value).longValue(), message + responseSuffix(result));
    }

    public static void assertJsonBoolean(MvcResult result, String pointer, boolean expected, String message) {
        Object value = evaluate(result, pointer, message);
        assertEquals(expected, (Boolean) value, message + responseSuffix(result));
    }

    public static void assertJsonArraySize(MvcResult result, String pointer, int expected, String message) {
        Object value = evaluate(result, pointer, message);
        assertEquals(expected, ((java.util.List<?>) value).size(), message + responseSuffix(result));
    }

    public static void assertJsonArrayEmpty(MvcResult result, String pointer, String message) {
        Object value = evaluate(result, pointer, message);
        assertEquals(0, ((java.util.List<?>) value).size(), message + responseSuffix(result));
    }

    public static void assertJsonPresent(MvcResult result, String pointer, String message) {
        evaluate(result, pointer, message);
    }

    private static Object evaluate(MvcResult result, String pointer, String message) {
        try {
            String jsonPath = toJsonPath(pointer);
            JsonPathExpectationsHelper helper = new JsonPathExpectationsHelper(jsonPath);
            Object value = helper.evaluateJsonPath(result.getResponse().getContentAsString());
            if (value == null) {
                fail(message + " JSON value was null at " + jsonPath + "." + responseSuffix(result));
            }
            return value;
        } catch (Exception exception) {
            fail(message + " Could not read JSON value at " + toJsonPath(pointer) + "." + responseSuffix(result));
            return null;
        }
    }

    private static String toJsonPath(String pointer) {
        if (pointer == null || pointer.isBlank() || "/".equals(pointer)) {
            return "$";
        }
        String[] parts = pointer.split("/");
        StringBuilder builder = new StringBuilder("$");
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (part.chars().allMatch(Character::isDigit)) {
                builder.append("[").append(part).append("]");
            } else {
                builder.append(".").append(part);
            }
        }
        return builder.toString();
    }

    private static String responseSuffix(MvcResult result) {
        try {
            return " Actual status: " + result.getResponse().getStatus() + ", body: " + result.getResponse().getContentAsString();
        } catch (Exception exception) {
            return " Actual status: " + result.getResponse().getStatus() + ", body: <unavailable>";
        }
    }
}
