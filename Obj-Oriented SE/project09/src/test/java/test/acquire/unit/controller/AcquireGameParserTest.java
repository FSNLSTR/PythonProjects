package test.acquire.unit.controller;
import org.acquire.controller.AcquireGameParser;
import org.junit.Test;
import static org.junit.Assert.*;

public class AcquireGameParserTest {

    @Test
    public void testValidateRowWithValidRow() {
        // Valid row value
        String validRow = "A";
        // throws an exception if validation fails
        AcquireGameParser.validateRow(validRow);

        // Invalid row value
        String invalidRow = "Z"; // Assuming 'Z' is not a valid row value
        RuntimeException exception = assertThrows(RuntimeException.class, () -> AcquireGameParser.validateRow(invalidRow));
        assertEquals("Invalid row:" + invalidRow, exception.getMessage());
    }

    @Test
    public void testValidateColumnWithValidColumn() {
        // Valid column value
        String validColumn = "_3"; // Assuming '1' is a valid column value
        // throws an exception if validation fails
        AcquireGameParser.validateColumn(validColumn);

        // Invalid column value
        String invalidColumn = "25"; // Assuming '13' is not a valid column value
        RuntimeException exception = assertThrows(RuntimeException.class, () -> AcquireGameParser.validateColumn(invalidColumn));
        assertEquals("Invalid column:" + invalidColumn, exception.getMessage());
    }

}


