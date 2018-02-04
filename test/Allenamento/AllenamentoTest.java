package Allenamento;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import org.junit.Test;
import static org.junit.Assert.*;
import robogp.matchmanager.RobotMarker;
import robogp.robodrome.Direction;
import robogp.robodrome.Robodrome;
import robogp.robodrome.Rotation;
import robogp.robodrome.view.RobodromeView;

/**
 *  Classe che si occupa di testare le funzionalità della modalità
 *  allenamento tramite unit test
 * 
 * @author Sorrentino & Vair
 */
public class AllenamentoTest {
    
   

    /**
     * Test of esegui_istruzione_robot method, of class EseguiAllenamento.
     */
    @Test
    public void testMuoviRobotAvantiDi1() {
        RobotMarker robot1 = new RobotMarker("robot-red", "red");
        robot1.assign("Test", 1);
        // creo la scheda istruzione
        SchedaIstruzione schedaAvantiUno = new SchedaIstruzione(SchedaIstruzione.Schede.move1);
        SchedaIstruzione schedaAvantiDue = new SchedaIstruzione(SchedaIstruzione.Schede.move2);
        SchedaIstruzione schedaAvantiTre = new SchedaIstruzione(SchedaIstruzione.Schede.move3);
        SchedaIstruzione schedaUturn = new SchedaIstruzione(SchedaIstruzione.Schede.uturn);
        SchedaIstruzione schedaTurnR = new SchedaIstruzione(SchedaIstruzione.Schede.turnL);
        SchedaIstruzione schedaTurnL = new SchedaIstruzione(SchedaIstruzione.Schede.turnR);
        SchedaIstruzione schedaIndietro = new SchedaIstruzione(SchedaIstruzione.Schede.backup);
        
        // creo il programma robot e aggiungo la scheda istruzione
        ProgrammaRobot prog = new ProgrammaRobot();
        prog.addScheda(schedaAvantiUno);
        prog.addScheda(schedaAvantiDue);
        prog.addScheda(schedaAvantiTre);
        prog.addScheda(schedaUturn);
        prog.addScheda(schedaTurnR);
        prog.addScheda(schedaTurnL);
        prog.addScheda(schedaIndietro);
        // aggiungo il programma al robot
        robot1.setProgramma(prog);
        // creo Robodrome e Gestore programmazione per creare un esegui allenamento
        RobodromeView rw = new RobodromeView(new Robodrome("robodromes/riskyexchange.txt"),10);
        rw.put_robot_in_dock(robot1);
  
        EseguiAllenamento esegui = new EseguiAllenamento(robot1, rw, null);
        int init_col = rw.getRobotColPos(robot1);
        int init_row = rw.getRobotRowPos(robot1);
        
        System.out.println("testMov1");     
        rw.addRobotMove(robot1, schedaAvantiUno.mov, rw.getRobotDirection(robot1), schedaAvantiUno.rot); //aggiungo l' animazione della scheda da testare
        esegui.esegui_animazione();
        assertEquals(rw.getRobotColPos(robot1),init_col+1);
        assertEquals(init_row,init_row);
        
        System.out.println("testMov2");     
        init_col = rw.getRobotColPos(robot1);
        init_row = rw.getRobotRowPos(robot1);
        rw.addRobotMove(robot1, schedaAvantiDue.mov, rw.getRobotDirection(robot1), schedaAvantiDue.rot); //aggiungo l' animazione della scheda da testare
        esegui.esegui_animazione();
        assertEquals(rw.getRobotColPos(robot1),init_col+2);
        assertEquals(init_row,init_row);
        
        System.out.println("testMov3");     
        init_col = rw.getRobotColPos(robot1);
        init_row = rw.getRobotRowPos(robot1);
        rw.addRobotMove(robot1, schedaAvantiTre.mov, rw.getRobotDirection(robot1), schedaAvantiTre.rot); //aggiungo l' animazione della scheda da testare
        esegui.esegui_animazione();
        assertEquals(rw.getRobotColPos(robot1),init_col+3);
        assertEquals(init_row,init_row);
        
        
        System.out.println("testStepBack");     
        init_col = rw.getRobotColPos(robot1);
        init_row = rw.getRobotRowPos(robot1);
        rw.addRobotMove(robot1, schedaIndietro.mov, rw.getRobotDirection(robot1), schedaIndietro.rot); 
        esegui.esegui_animazione();
        assertEquals(rw.getRobotColPos(robot1),init_col+1);
        assertEquals(init_row,init_row);
        
        
        System.out.println("testTurnR");     
        int init_rot = rw.getRobotDirection(robot1).ordinal();
        rw.addRobotMove(robot1, schedaTurnR.mov, rw.getRobotDirection(robot1), schedaTurnR.rot); 
        esegui.esegui_animazione();
        assertEquals(rw.getRobotDirection(robot1).ordinal(),init_rot-1);
        
        System.out.println("testTurnL");     
        init_rot = rw.getRobotDirection(robot1).ordinal();
        rw.addRobotMove(robot1, schedaTurnL.mov, rw.getRobotDirection(robot1), schedaTurnL.rot); 
        esegui.esegui_animazione();
        assertEquals(rw.getRobotDirection(robot1).ordinal(),init_rot+1);
        
        System.out.println("testUTurn");     
        init_rot = rw.getRobotDirection(robot1).ordinal();
        rw.addRobotMove(robot1, schedaUturn.mov, rw.getRobotDirection(robot1), schedaUturn.rot); 
        esegui.esegui_animazione();
        System.out.println("Prima: "+init_rot+" Dopo: "+rw.getRobotDirection(robot1).ordinal());
        assertEquals(rw.getRobotDirection(robot1).ordinal(),init_rot-2);
    }    
}
