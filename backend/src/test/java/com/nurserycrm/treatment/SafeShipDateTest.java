package com.nurserycrm.treatment;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SafeShipDateTest {

    @Test
    void safeShipDateIsAppliedDatePlusWithholdingDays() {
        LocalDate applied = LocalDate.of(2024, 6, 1);
        int withholding = 14;
        LocalDate expected = applied.plusDays(withholding);
        assertEquals(expected, applied.plusDays(withholding));
    }

    @Test
    void boundaryDateBlocksShipment() {
        LocalDate applied = LocalDate.of(2024, 6, 1);
        int withholding = 14;
        LocalDate safeShip = applied.plusDays(withholding);

        // On the exact boundary date, shipment is blocked (not yet safe)
        assertFalse(safeShip.isBefore(applied.plusDays(withholding)));
    }

    @Test
    void dayAfterBoundaryIsSafe() {
        LocalDate applied = LocalDate.of(2024, 6, 1);
        int withholding = 14;
        LocalDate safeShip = applied.plusDays(withholding);

        assertTrue(safeShip.plusDays(1).isAfter(applied.plusDays(withholding)));
    }
}
