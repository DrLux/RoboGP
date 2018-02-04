package Allenamento;

import java.util.logging.Level;
import java.util.logging.Logger;
import robogp.matchmanager.RobotMarker;
import robogp.robodrome.Direction;
import robogp.robodrome.Rotation;
import robogp.robodrome.view.RobodromeAnimationObserver;
import robogp.robodrome.view.RobodromeView;

/**
 *
 * @author Sorrentino Vair
 */
public class EseguiAllenamento extends Thread implements RobodromeAnimationObserver {
    private RobotMarker robot;
    private RobodromeView rv;
    private GestoreProgrammazione gp;
    private int init_dock;
    private boolean animationReady = true;
    private boolean finished = false;
    
    
    public EseguiAllenamento (RobotMarker robot, RobodromeView rv, GestoreProgrammazione gp){
        this.robot = robot;
        this.rv = rv;
        rv.addObserver(this); //EseguiAllenamento è observer delle animaizioni di robodromeView
        this.gp = gp;
        init_dock = robot.getDock();
    }

    //fetcha la prossima istruzione dal robot e la esegue
    public void esegui_istruzione_robot(RobotMarker robot){
        if(!robot.progFinito()){
            rv.addFocusMove(robot); //sposta la “finestra” sul robodromo per mostrare il robot specificato.
            SchedaIstruzione scheda = robot.getIstruzione();
            Direction dir = rv.getRobotDirection(robot);
            if (scheda.reverse) //se la scheda prevede un reverse della direzione
                dir = rv.reverseDir(dir);
            if (scheda.rot == Rotation.NO) //controllo se ci sono movimenti da fare sul robodromeo
                controllaAnimazione(robot, scheda.mov, dir); //controllo che il campo sia libero
            else
                rv.addRobotMove(robot, scheda.mov, dir, scheda.rot); //aggiungo l' animazione di rotazione
            esegui_animazione();                               
            gp.drop_icon(); //elimino la scheda appena eseguita dal registro         
        }
    }
        
    //Controlla il campo davanti il robot prima di eseguire le azioni  
    private void controllaAnimazione(RobotMarker robot, int move, Direction dir){
        int row = rv.getRobotRowPos(robot);
        int col = rv.getRobotColPos(robot);

        for (int i=0; i<move; i++){ //controllo una cella alla volta per "move" volte
            char cellType = rv.getTypeCell(row, col);
                if ('X' == cellType || 'P' == cellType){ //X = cella fuori dal robodromo, P = burrone                    
                  rv.addRobotFall(robot);
                  return; //altrimenti dopo il for cadrebbe di nuovo 
                }
                if (rv.wallOnCell(row, col, dir)){ //se non ho di fronte un muro...
                    return;                       
                }

                switch(dir){ //sposta virtualmente il robot verso dove si troverebbe dopo l' animazione
                    case E:
                        col++;
                        break;
                    case N:
                        row--;
                        break;
                    case S:
                        row++;
                        break;
                    case W:
                        col--;
                        break;
                }
                if (rv.wallOnCell(row, col, rv.reverseDir(dir))){ //se la cella di fronte me ha un muro
                    return;                       
                }
                rv.addRobotMove(robot, 1, dir, Rotation.NO); //esegue normalmente il pass
                
        }
        //Controlla dove si è finiti dopo l' animazione
        esegui_robot_fall(robot);
    }
    
    public void esegui_animazione(){
        synchronized(this){
            while(!animationReady){
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(AllenarsiApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            rv.play();
            while(!animationReady){
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(AllenarsiApp.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
        }
    }
    
    void esegui_robodromo(){    
        esegui_nastro_express(robot, true); //il parametro true indica che si tratta di nastro express
        esegui_nastro(robot); //qui eseguo il nastro normale
        esegui_rotatoria(robot);
        esegui_robot_fall(robot); //nel caso il nastro mi trasportasse in un fosso       
    }
    
    public void esegui_nastro(RobotMarker robot){
        if ('B' == get_cell_robot(robot)){
            rv.belt_robot(robot);
            esegui_animazione();
        }
    }
    
    public void esegui_nastro_express(RobotMarker robot, boolean express){
        if ('E' == get_cell_robot(robot)){
            rv.belt_robot(robot);
            esegui_animazione();
        }
        if (express)
            esegui_nastro_express(robot,false);
    }
    
    public void esegui_rotatoria(RobotMarker robot){
        if ('F' ==  get_cell_robot(robot)){
            rv.cell_rotation_robot(robot);
            esegui_animazione();           
        }
    }
       
    public void esegui_robot_fall(RobotMarker robot){
        if ('X' == get_cell_robot(robot) || 'P' == get_cell_robot(robot)){
            rv.addRobotFall(robot);
            esegui_animazione();
        }
    }
    
    public char get_cell_robot(RobotMarker robot){
        int row = rv.getRobotRowPos(robot);
        int col = rv.getRobotColPos(robot);
        return rv.getTypeCell(row, col);
    }
    
    @Override
    public void animationStarted() {
        animationReady = false;
    }

    @Override
    public void animationFinished() {           
        synchronized(this){
            animationReady = true;
            notifyAll();
        }  
    }
    
    public void closeThread(boolean close){
        this.finished = close;
    }
    
    public boolean getStatusThread(){
        return this.finished;
    }
    
    public void run(){ 
        gp.programmaRobot(robot); //il Gest. Prog. legge il programma dalla gui e lo inserisce nel robot
        while (!finished){
            esegui_istruzione_robot(robot);
            esegui_robodromo();
            finished = robot.progFinito();
            if (!rv.robotVisible(robot)){ //se il robot è caduto            
                gp.cancProg(robot);            
                rv.put_robot_in_dock(robot);
            }
        }
    }
}
