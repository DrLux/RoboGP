package Giocare;

import Allenamento.SchedaIstruzione;
import javax.swing.Icon;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sorrentino & Vair
 */
public class CustomLabelSchedaTest {
    
    public CustomLabelSchedaTest() {
    }

    /**
     * Test of setScheda method, of class CustomLabelScheda.
     */
    @Test
    public void testSetAndGetScheda() {
        System.out.println("setScheda");
        //test con scheda null
        SchedaIstruzione scheda = null;
        CustomLabelScheda instance = new CustomLabelScheda();
        instance.setScheda(scheda);
        assertEquals(instance.getScheda(), null);
        
        // test con scheda reale
        SchedaIstruzione scheda1 = new SchedaIstruzione(SchedaIstruzione.Schede.move1);
        CustomLabelScheda instance1 = new CustomLabelScheda();
        instance1.setScheda(scheda1);
        assertEquals(instance1.getScheda().getName(), scheda1.getName());
    }

    /**
     * Test of updateIcon method, of class CustomLabelScheda.
     */
    @Test
    public void testUpdateIcon() {
        System.out.println("updateIcon");
        CustomLabelScheda instance = new CustomLabelScheda();
        SchedaIstruzione scheda1 = new SchedaIstruzione(SchedaIstruzione.Schede.move1);
        instance.setScheda(scheda1);
        // creo la stessa icona
        Icon x = new javax.swing.ImageIcon(getClass().getResource("/icons/"+scheda1.getName()+".png"));
        assertTrue(instance.getIcon() != null);
        // confronto i path
        assertEquals(x.toString(), instance.getIcon().toString());
        
        CustomLabelScheda instance1 = new CustomLabelScheda();
        instance1.setScheda(null);
        // con null viene creata una icona vuota
        assertTrue(instance1.getIcon() != null && instance1.getScheda() == null);

        
    }

    /**
     * Test of isSet method, of class CustomLabelScheda.
     */
    @Test
    public void testIsSet() {
        System.out.println("isSet");
        //  test con null
        SchedaIstruzione scheda = null;
        CustomLabelScheda instance = new CustomLabelScheda();
        instance.setScheda(scheda);
        assertEquals(instance.isSet(), false);
        
        // test con scheda reale
        SchedaIstruzione scheda1 = new SchedaIstruzione(SchedaIstruzione.Schede.move1);
        CustomLabelScheda instance1 = new CustomLabelScheda();
        instance1.setScheda(scheda1);
        assertEquals(instance1.isSet(), true);
        
        // test con scheda vuota
        SchedaIstruzione scheda11 = new SchedaIstruzione(SchedaIstruzione.Schede.empty);
        CustomLabelScheda instance11 = new CustomLabelScheda();
        instance11.setScheda(scheda11);
        assertEquals(instance11.isSet(), false);
    }
    
}
