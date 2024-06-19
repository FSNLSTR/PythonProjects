package test.acquire.unit.controller;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.acquire.controller.AcquireGameHelper;
import org.junit.Test;
import static org.junit.Assert.*;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AcquireGameHelperTest {
    @Test
    public void testIsValidCoordinate() {
        // Test with valid coordinates
        assertTrue(AcquireGameHelper.isValidCoordinate(0, 0, 3, 3));
        assertTrue(AcquireGameHelper.isValidCoordinate(1, 2, 3, 3));
        assertTrue(AcquireGameHelper.isValidCoordinate(2, 2, 3, 3));

        // Test with invalid coordinates
        assertFalse(AcquireGameHelper.isValidCoordinate(-1, 0, 3, 3));  // Row is negative
        assertFalse(AcquireGameHelper.isValidCoordinate(0, -1, 3, 3));  // Column is negative
        assertFalse(AcquireGameHelper.isValidCoordinate(3, 0, 3, 3));   // Row is out of bounds
        assertFalse(AcquireGameHelper.isValidCoordinate(0, 3, 3, 3));   // Column is out of bounds
        assertFalse(AcquireGameHelper.isValidCoordinate(3, 3, 3, 3));   // Both row and column are out of bounds

    }

    @Test
    public void testCreateJsonFromArray() {
        AcquireGameHelper helper = new AcquireGameHelper();

        List<JsonNode> elements = new ArrayList<>();
        elements.add(JsonNodeFactory.instance.textNode("element1"));
        elements.add(JsonNodeFactory.instance.textNode("element2"));
        elements.add(JsonNodeFactory.instance.textNode("element3"));

        JsonNode result = helper.createJsonFromArray(elements);

        assertTrue(result.isArray());
        ArrayNode arrayNode = (ArrayNode) result;
        assertEquals(3, arrayNode.size());
        assertEquals("element1", arrayNode.get(0).asText());
        assertEquals("element2", arrayNode.get(1).asText());
        assertEquals("element3", arrayNode.get(2).asText());
    }



}
