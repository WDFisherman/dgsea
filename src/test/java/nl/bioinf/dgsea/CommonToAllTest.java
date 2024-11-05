package nl.bioinf.dgsea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonToAllTest {

    @Test
    void setLoggingScope() {
        CommonToAll commonToAll = new CommonToAll();
        commonToAll.setVerbose(new boolean[]{true});
        commonToAll.setLoggingScope();
        // global changes should affect local class level changes.
        assertEquals(Level.ERROR, LogManager.getLogger().getLevel());

        commonToAll.setVerbose(new boolean[]{true, true});
        commonToAll.setLoggingScope();
        assertEquals(Level.WARN, LogManager.getLogger().getLevel());

        commonToAll.setVerbose(new boolean[]{true, true, true});
        commonToAll.setLoggingScope();
        assertEquals(Level.INFO, LogManager.getLogger().getLevel());

        commonToAll.setVerbose(new boolean[]{true, true, true, true});
        commonToAll.setLoggingScope();
        assertEquals(Level.DEBUG, LogManager.getLogger().getLevel());
    }
}