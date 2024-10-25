package nl.bioinf.dgsea;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks if color string to object conversion works.
 */
class CommonChartParamsTest {
    @Test
    void getColorManualAsColors_hex() {
        CommonChartParams params = new CommonChartParams();
        params.setColorManual(new String[] {"0x000000", "0xFFFFFF", "#000000", "040000"});
        assertEquals(Color.black, params.getColorManualAsColors()[0]);
        assertEquals(Color.white, params.getColorManualAsColors()[1]);
        assertEquals(Color.black, params.getColorManualAsColors()[2]);
        assertEquals(new Color(0,64,0), params.getColorManualAsColors()[3]);
    }

    @Test
    void getColorManualAsColors_name() {
        CommonChartParams params = new CommonChartParams();
        params.setColorManual(new String[] {"black", "blue", "magenta", "green"});
        assertEquals(Color.black, params.getColorManualAsColors()[0]);
        assertEquals(Color.blue, params.getColorManualAsColors()[1]);
        assertEquals(Color.magenta, params.getColorManualAsColors()[2]);
        assertEquals(Color.green, params.getColorManualAsColors()[3]);
    }

    @Test
    void getColorManualAsColors_removedInvalidColors() {
        CommonChartParams params = new CommonChartParams();
        params.setColorManual(new String[] {"black", "blue", "magenta", "gene"});
        assertEquals(3, params.getColorManualAsColors().length);
    }

    @Test
    void getColorManualAsColors_codeInvalid() {
        CommonChartParams params = new CommonChartParams();
        params.setColorManual(new String[] {"0xFFFFFFFF", "rgb(255,255,255)"});
        assertEquals(0, params.getColorManualAsColors().length);
    }

    @Test
    void getColorManualAsColors_codeNotIntended() {
        CommonChartParams params = new CommonChartParams();
        params.setColorManual(new String[] {"0xFFF", "0x0000"});
        assertEquals(new Color(0,15,255), params.getColorManualAsColors()[0]);
        assertEquals(Color.black, params.getColorManualAsColors()[1]);
    }

    @Test
    void getColorManualAsColors_nameInvalid() {
        CommonChartParams params = new CommonChartParams();
        params.setColorManual(new String[] {"blac", "lightgreen", "purple", "skyblue"});
        assertEquals(0, params.getColorManualAsColors().length);
    }

}