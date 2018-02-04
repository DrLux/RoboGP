package robogp.matchmanager;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Sorrentino & Vair
 */
public class MatchTest {
    
    private final static String ROBODROME = "testPush";
    Match theMatch;
    
    public MatchTest() {
       this.theMatch = Match.getInstance(ROBODROME, 10, 1, Match.EndGame.First, true);
    }
    
    @BeforeClass
    public static void setUpClass() {

    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class Match.
     */
    @Test
    public void testGetInstance_5args() {
        System.out.println("getInstance");
        String rbdName = ROBODROME;
        
        Match result = this.theMatch;
        assertNotEquals(result, null);
        assertEquals(result.getMaxPlayers(), 10);
        assertEquals(result.getRobotsPerPlayer(), 1);

        // check se single instance patter really works
        result = Match.getInstance(rbdName, 10, 10, Match.EndGame.First, false);
        assertNotEquals(result, null);
        assertEquals(result.getMaxPlayers(), 10);
        assertEquals(result.getRobotsPerPlayer(), 1);        

    }

    /**
     * Test of getInstance method, of class Match.
     */
    @Test
    public void testGetInstance_0args() {
        System.out.println("getInstance");
        Match expResult = null;
        Match result = Match.getInstance();
        assertNotEquals(expResult, result);
    }
    
}
