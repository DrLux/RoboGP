package Allenamento;

import javax.swing.JLabel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import robogp.matchmanager.RobotMarker;

/**
 *
 * @author Sorrentino & Vair
 */
public class GestoreProgrammazioneTest {
    
    public GestoreProgrammazioneTest() {
    }

    /**
     * Test of programmaRobot method, of class GestoreProgrammazione.
     */
    @Test
    public void testProgrammaRobot() {
        System.out.println("programmaRobot");
        RobotMarker robot = null;
        GestoreProgrammazione instance = new GestoreProgrammazione();
        instance.programmaRobot(robot);
        
        RobotMarker robot1 = new RobotMarker("test", "red");
        GestoreProgrammazione instance1 = new GestoreProgrammazione();
        instance1.programmaRobot(robot1);
        assertEquals(robot1.getIstruzione(), null);

    }

    /**
     * Test of icon_to_key method, of class GestoreProgrammazione.
     */
    @Test
    public void testIcon_to_key() {
        System.out.println("icon_to_key");
        String label = "card-backup.png";
        String expResult = "card-backup";
        String result = GestoreProgrammazione.icon_to_key(label);
        assertEquals(expResult, result);
        
        String label1 = "";
        String result1 = GestoreProgrammazione.icon_to_key(label1);
        assertEquals(result1, null);
        
        String label2 = null;
        String expResult2 = null;
        String result2 = GestoreProgrammazione.icon_to_key(label2);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of cancProg method, of class GestoreProgrammazione.
     */
    @Test
    public void testCancProg() {
        System.out.println("cancProg");
        RobotMarker robot = null;
        GestoreProgrammazione instance = new GestoreProgrammazione();
        instance.cancProg(robot);

        RobotMarker robot1 = new RobotMarker("test", "red");
        GestoreProgrammazione instance1 = new GestoreProgrammazione();
        instance1.cancProg(robot1);
        assertEquals(robot1.getIstruzione(), null);
    }

    /**
     * Test of add_register method, of class GestoreProgrammazione.
     */
    @Test
    public void testAdd_register() {
        System.out.println("add_register");
        JLabel new_reg = null;
        GestoreProgrammazione instance = new GestoreProgrammazione();
        instance.add_register(new_reg);
        assertEquals(instance.getRegistriProg().size(), 0);
        
        JLabel new_reg1 = new JLabel();
        GestoreProgrammazione instance1 = new GestoreProgrammazione();
        instance1.add_register(new_reg1);
        assertEquals(instance1.getRegistriProg().size(), 1);
        assertEquals(instance1.getRegistriProg().get(0), new_reg1);

    }
    
}
