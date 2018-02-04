package Giocare;

import Allenamento.SchedaIstruzione;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import robogp.matchmanager.RobotMarker;

/**
 *
 * @author Sorrentino & Vair
 */
public class GestoreProgrammazione {
    public RobotMarker[] miei_robots;
    private JPanel pannello_robots;
    private HashMap<String, FinestraDiProgrammazione> robot_finestraprog;
    private HashMap<String, RobotPalette> palette;

       
    GestoreProgrammazione(RobotMarker[] robots, JPanel pannello){
        miei_robots = robots;
        pannello_robots = pannello;
        this.robot_finestraprog = new HashMap<>();
        this.palette = new HashMap<>();
        init_panel();
    }
    
    
    public void init_panel(){
        FinestraDiProgrammazione temp_window;
        RobotPalette pl;
        for (int i = 0; i < miei_robots.length; i++){
            temp_window = new FinestraDiProgrammazione(miei_robots[i]); //creo una nuova finestra di prog
            pl = new RobotPalette(miei_robots[i], temp_window);
            temp_window.setVisible(false); //la rendo invisibile
            robot_finestraprog.put(miei_robots[i].getName(), temp_window); //associo la finestra al robot
            palette.put(miei_robots[i].getName(), pl);
            pannello_robots.add(pl); //associo la finestra al click
        }
    }
    
    public void other_panel(ArrayList<RobotMarker> all_robot){
        RobotPalette pl;
        for (RobotMarker robot : all_robot){
            if (!palette.containsKey(robot.getName())){
                pl = new RobotPalette(robot);
                palette.put(robot.getName(), pl);
                pannello_robots.add(pl);
                pannello_robots.validate();
                pannello_robots.repaint();
            }   
        }
    }
     
    public void addPool(String nome_robot, List<SchedaIstruzione> pool){
        robot_finestraprog.get(nome_robot).setPool(pool);
    }
    
    public RobotMarker[] get_programmed_robot(){
        for (int i = 0; i < miei_robots.length; i++){
            miei_robots[i].setProgramma(robot_finestraprog.get(miei_robots[i].getName()).getProgram());
        }
        return this.miei_robots;
    }
    
    public void chiudi_finestre(){
        FinestraDiProgrammazione temp_window;
        for (int i = 0; i < miei_robots.length; i++){
            temp_window = robot_finestraprog.get(miei_robots[i].getName()); 
            temp_window.setVisible(false); //la rendo invisibile
        }
    }
    
    public void updateInfoRobot(RobotMarker robot) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            RobotPalette pl = palette.get(robot.getName());
            if (pl != null){
                pl.updateInfo();
                pannello_robots.validate();
                pannello_robots.repaint();
            }           
        }
        });
    }
    
    public int getPuntiSalute(RobotMarker robot){
        return palette.get(robot.getName()).getPuntiSalute();
    }
    
    public int getVite(RobotMarker robot){
        return palette.get(robot.getName()).getVite();
    }
    
    public boolean perdiSalute (RobotMarker robot, int n){
        return palette.get(robot.getName()).perdiSalute(n);
    }
    
    public void perdiVita(RobotMarker robot){
        palette.get(robot.getName()).perdiVita();
    }
    
    public void incSalute(RobotMarker robot){
        palette.get(robot.getName()).incSalute();
    }
    
    public void putCheckPoint(RobotMarker robot, int check){
        palette.get(robot.getName()).putCheckPoint(check);
    }
    
    public void updateRespawn(RobotMarker robot, int col, int row){
        palette.get(robot.getName()).updateRespawn(col,row);
    }
    
    public int getCheck(RobotMarker robot){
        return palette.get(robot.getName()).getCheck();
    }
    
    public int getRowResp(RobotMarker robot){
        return palette.get(robot.getName()).getRowResp();
    }
    
    public int getColResp(RobotMarker robot){
        return palette.get(robot.getName()).getColResp();
    }
    
    public void setWin(RobotMarker robot){
        palette.get(robot.getName()).robot_vittoria();
    }
    
    public void showScheda(RobotMarker robot, SchedaIstruzione scheda){
        palette.get(robot.getName()).showScheda(scheda);
    }
    
    public void hideScheda(RobotMarker robot){
        palette.get(robot.getName()).hideScheda();
    }
   
    public void robot_morto(RobotMarker robot){
        palette.get(robot.getName()).robot_morto();
    }
    
}
