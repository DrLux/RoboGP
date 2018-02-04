package Giocare;

import Allenamento.AllenarsiApp;
import Allenamento.SchedaIstruzione;
import connection.Connection;
import connection.Message;
import connection.MessageObserver;
import connection.PartnerShutDownException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import robogp.matchmanager.RobotMarker;
import robogp.robodrome.Direction;
import robogp.robodrome.Rotation;
import robogp.robodrome.view.RobodromeAnimationObserver;
import robogp.robodrome.view.RobodromeView;
import java.util.PriorityQueue;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import robogp.matchmanager.Match;

/**
 *
 * @author Sorrentino Vair
 */
public class EseguiGiocare extends Thread implements RobodromeAnimationObserver, MessageObserver{
    private ArrayList<RobotMarker> all_robots;
    private RobodromeView rv;
    private Giocare.GestoreProgrammazione gp;
    private Map<SchedaIstruzione,RobotMarker> scheda_robot;
    private PriorityQueue<SchedaIstruzione> lista_schede;
    private int init_dock;
    private boolean animationReady = true;
    private boolean finished = false;
    private JLabel feedback;
    private int totRobotToWin = 0;
    private int total_checkpoints;
    private Connection conn;

    
    
    public EseguiGiocare (ArrayList<RobotMarker> robots, RobodromeView rv, Giocare.GestoreProgrammazione gp, JLabel feedback, Connection conn){
        this.all_robots = robots;
        this.rv = rv;
        rv.addObserver(this); //EseguiAllenamento è observer delle animaizioni di robodromeView
        this.gp = gp;
        scheda_robot = new HashMap<>();//associazione di ogni robot alla propria scheda 
        init_lista_schede(); //inizializza la lista ordinata delle schede con priorita
        init_dock = 1;
        this.feedback = feedback;
        this.totRobotToWin = 1;
        total_checkpoints = rv.get_total_checkpoints();
        this.conn = conn;
        conn.addMessageObserver(this);
    }
    
    public void init_lista_schede(){
        //classe interna per poter fare la comparazione di schede
        class PrioritaSchede implements Comparator<SchedaIstruzione>{ 

            @Override
            public int compare(SchedaIstruzione o1, SchedaIstruzione o2) {
                return Integer.compare(o1.getPriorita(), o2.getPriorita());
            }
        }
        PrioritaSchede ordine_prioritario = new PrioritaSchede(); //oggetto usato dalla lista di priorita per ordinare le schede      
        lista_schede = new PriorityQueue<SchedaIstruzione>(8, ordine_prioritario.reversed()); //lista di priorita per le schede, massimo 8 robot paralleli
    }
    
    public void run(){ 
        boolean win = false;
        for (int i = 0;  i <5 && !win; i++){
            esegui_prog_robots();
            esegui_robodromo();
            win = controlla_vittoria();
            esegui_laser();
            esegui_tuchAndsave();            
        }
        if (!win){
            if (gp.miei_robots.length > 1)
                updateFeedback("*** Programma i tuoi robot ***");
            else
                updateFeedback("*** Programma il tuo robot ***");
        }
    }
    
    public void esegui_prog_robots(){     
        ordina_schede_priorita();
        SchedaIstruzione temp_scheda;
        while (!lista_schede.isEmpty()){
            temp_scheda = lista_schede.poll();
            esegui_istruzione_robot(scheda_robot.get(temp_scheda),temp_scheda);
        }
    }
    
    //ordinale le 5 schede del turno e le associa ai rispettivi robot
    public void  ordina_schede_priorita(){        
        SchedaIstruzione temp_scheda;
        scheda_robot.clear(); //svuoto i dati del turno precedente
        lista_schede.clear();
        for (RobotMarker robot : all_robots){
            temp_scheda = robot.getIstruzione();
            scheda_robot.put(temp_scheda,robot);
            lista_schede.add(temp_scheda);
        }
    }

    //fetcha la prossima istruzione dal robot e la esegue
    public void esegui_istruzione_robot(RobotMarker robot, SchedaIstruzione scheda){
        gp.showScheda(robot, scheda);
        rv.addFocusMove(robot); //sposta la “finestra” sul robodromo per mostrare il robot specificato.
        Direction dir = rv.getRobotDirection(robot);
        if (scheda.reverse) //se la scheda prevede un reverse della direzione
            dir = rv.reverseDir(dir);
        if (scheda.rot == Rotation.NO) //controllo se ci sono movimenti da fare sul robodromeo
            controllaAnimazione(robot, scheda.mov, dir); //controllo che il campo sia libero
        else{
            rv.addRobotMove(robot, scheda.mov, dir, scheda.rot); //aggiungo l' animazione di rotazione
            esegui_animazione();
        }
    }
        
    //Controlla il campo davanti il robot prima di eseguire le azioni  
    private void controllaAnimazione(RobotMarker robot, int move, Direction dir){
        int row = rv.getRobotRowPos(robot);
        int col = rv.getRobotColPos(robot);
        RobotMarker[] robot_to_push;
        ArrayList<RobotMarker> temp_list = new ArrayList<>(); //lista che contiene i robot da spingere facendo un passo
  
        for (int i=0; i<move; i++){ //controllo una cella alla volta per "move" volte
        check_checkpoint(robot);
            if ('X' == get_cell_robot(robot) || 'P' == get_cell_robot(robot)){
                check_robot_fall(robot);
                break;
            }

            if (checkNext(robot, i, dir, temp_list)  == 'W'){ //se l'ultimo robot ha un muro non permette a nessuno il movimento
                return;                    
            }

            robot_to_push = temp_list.toArray(new RobotMarker[temp_list.size()]); //converte la lista in array
            temp_list.clear();

            if (robot_to_push.length != 0){ //se risulta almeno una volta che c'è un robot da spingere
                rv.addRobotMove(robot, 1, dir, Rotation.NO, robot_to_push); //a questo punto tutti si muovono
                esegui_animazione();
                check_robot_fall(robot_to_push[robot_to_push.length-1]); //se l'ultimo finisce sul burrone, cadrà                
            } else {
                rv.addRobotMove(robot, 1, dir, Rotation.NO);
            }
            esegui_animazione();
            check_checkpoint(robot);
        }            
        check_robot_fall(robot); //se il robot finisce su un burrone, cade
    }
    
    //Dice cosa troverai avanti a te
    public char checkNext(RobotMarker robot, int step, Direction dir, ArrayList<RobotMarker> toPush){
        int row = rv.getRobotRowPos(robot);
        int col = rv.getRobotColPos(robot);
        char cellType = rv.getTypeCell(row, col);
        
        if ('X' == cellType || 'P' == cellType){ //X = cella fuori dal robodromo, P = burrone                    
            check_robot_fall(robot);
            return 'N';
        }
        
        if (rv.wallOnCell(row, col, dir))
            return 'W';
        
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
                
        if (rv.wallOnCell(row, col, rv.reverseDir(dir)))            
            return 'W'; //wall                       
        
        RobotMarker new_robot_to_push = rv.robotNear(row,col,all_robots);
        
        if (new_robot_to_push == null){
            return 'N'; //null,niente
        } else {
            toPush.add(new_robot_to_push);
            return checkNext(new_robot_to_push,step, dir, toPush); 
        }
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
        for (RobotMarker robot : all_robots){
            esegui_nastro_express(robot, true); //il parametro true indica che si tratta di nastro express
            esegui_nastro(robot); //qui eseguo il nastro normale
            esegui_rotatoria(robot);
            check_robot_fall(robot); //nel caso il nastro mi trasportasse in un fosso */
        }
    }
    
    public void esegui_nastro(RobotMarker robot){
        if ('B' == get_cell_robot(robot)){
            RobotMarker[] robot_to_push = beltToPush(robot);
            if (robot_to_push.length != 0){ //se risulta almeno una volta che c'è un robot da spingere
                rv.belt_robot(robot,robot_to_push);
            } else {
                rv.belt_robot(robot);        
            }
            esegui_animazione();
        }
    }
    
    public void esegui_nastro_express(RobotMarker robot, boolean express){        
        if ('E' == get_cell_robot(robot)){
            RobotMarker[] robot_to_push = beltToPush(robot);
            if (robot_to_push.length != 0){ //se risulta almeno una volta che c'è un robot da spingere
                rv.belt_robot(robot,robot_to_push);
            } else {
                rv.belt_robot(robot);        
            }
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
       
    public void check_robot_fall(RobotMarker robot){
        if ('X' == get_cell_robot(robot) || 'P' == get_cell_robot(robot)){
            if (rv.robotVisible(robot))
                damageRobot(robot,10);
            updateFeedback("***"+robot.getName()+" ha perso una vita***");
        }
    }
    
    public void check_checkpoint(RobotMarker robot){
        int check = rv.get_checkpoint(robot);
        if (check > 0){
            gp.incSalute(robot);
            gp.putCheckPoint(robot, check);
            gp.updateRespawn( robot, rv.getRobotColPos(robot), rv.getRobotRowPos(robot));
            updateFeedback("***"+robot.getName()+" ha raggiunto un checkpoint***");
        }
    }
    
    public void check_repair(RobotMarker robot){
        if ( rv.onRepOrUpd(robot)){
            gp.incSalute(robot);
            updateFeedback("***"+robot.getName()+" recuper un punto salute***");
        }
    }
    
    
    public RobotMarker[] beltToPush(RobotMarker robot){
        RobotMarker[] robot_to_push;
        ArrayList<RobotMarker> temp_list = new ArrayList<>();

        char next = checkNext(robot,1, rv.beltDirection(robot), temp_list);

        robot_to_push = temp_list.toArray(new RobotMarker[temp_list.size()]); //converte la lista in array
        temp_list.clear();

        return robot_to_push;
    }
    
    
    public void respawn(RobotMarker robot){
        if (gp.getVite(robot) >= 0 || gp.getPuntiSalute(robot) >= 0){ //controllo aggiuntivo
            if (gp.getCheck(robot) == 0)
                rv.put_robot_in_dock(robot);
            else
                rv.placeRobot(robot, rv.getRobotDirection(robot) , gp.getRowResp(robot), gp.getColResp(robot), true);
        }
        this.esegui_animazione();
    }
    
    public void esegui_tuchAndsave(){
        for (RobotMarker robot : all_robots){
            check_repair(robot);
            gp.hideScheda(robot);
        }
    }
    
    public boolean controlla_vittoria(){
        boolean win = false;
        for (RobotMarker robot : all_robots){
            if (gp.getCheck(robot) == total_checkpoints){            
                gp.setWin(robot);
                rv.addRobotFall(robot);
                esegui_animazione();
                rv.placeRobot(robot, Direction.E , 1, 1, false);
                esegui_animazione();                
                totRobotToWin--;
            }
            win = totRobotToWin <= 0;
            if (win){
                updateFeedback("***Il giocatore "+robot.getOwner()+" ha VINTO!***");
                esegui_vittoria();
                return true;
            }
        }
        return win; 
    }
    
    public void esegui_vittoria(){
        for (RobotMarker robot : all_robots){
            if (rv.robotVisible(robot)){
                gp.hideScheda(robot);
                gp.robot_morto(robot);
                rv.addRobotFall(robot);
                esegui_animazione();
                rv.placeRobot(robot, Direction.E , 1, 1, false);
            }
        }
        Message reply = new Message(Match.Winner);
        try {
            conn.sendMessage(reply);
        } catch (PartnerShutDownException ex) {
            Logger.getLogger(GiocareApp.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    public void setNumRobotToWin(int n){
        this.totRobotToWin = n;
    }
    
    public void esegui_laser(){
        for (RobotMarker robot : all_robots){ //uno sparo per ogni robot
            if (rv.robotVisible(robot)){ //se il robot è in campo
                Direction dir = rv.getRobotDirection(robot);
                int start = 0;
                int end = 0;
                boolean hitWall = false;
                boolean hitRobot = false;
                int col = rv.getRobotColPos(robot);
                int row = rv.getRobotRowPos(robot);
                RobotMarker colpito = null;

                //lo spazio si muove sulle righe se il robot è orientato in verticale
                if (dir == Direction.N || dir == Direction.S)
                    start = row;
                else
                    start = col;

                end = start; 
                while (!hitRobot && !hitWall && end >= 0 && row <= rv.getRowCount() && col <= rv.getColCount()){ //finche non colpisci qualcosa e rimani nel robodrome               

                    switch(dir){ //controlla nelle caselle successive
                        case E:
                            col++;
                            end++;
                            break;
                        case N:        
                            row--;
                            end--;
                            break;
                        case S:
                            row++;
                            end++;
                            break;
                        case W:
                            col--;
                            end--;
                            break;
                    }

                    hitWall = rv.wallOnCell(row, col, rv.reverseDir(dir)) || rv.wallOnCell(row, col, dir); //pone a true se incontra un muro

                    if (rv.wallOnCell(row, col, rv.reverseDir(dir))){
                        if (dir == Direction.S || dir == Direction.E)
                            end--;
                        else
                            end++;
                    }

                    if (!hitWall){
                        colpito = rv.robotNear(row,col,all_robots);
                        hitRobot = colpito != null;
                    }
                }

                rv.addLaserFire(robot, dir, start,  end, hitRobot, hitWall);
                esegui_animazione();
                if (colpito != null){
                    rv.addRobotHit(colpito, rv.reverseDir(dir)); //animazione del colpo di laser
                    damageRobot(colpito,1);
                    esegui_animazione();
                }
                rv.addHideLaser();
                esegui_animazione();
            }
        }
    }
    
    public void damageRobot(RobotMarker robot, int salutePersa){
        
        if (gp.getVite(robot) >= 0 || gp.getPuntiSalute(robot) >= 0){ //se il robot è vivo
            if (!gp.perdiSalute(robot, salutePersa)){ //se non dovesse sopravvivere alla perdita di punti
                gp.perdiVita(robot); //automaticamente ricarica i punti salute
                rv.addRobotFall(robot); //animazione di morte
                esegui_animazione();
                updateFeedback("***"+robot.getName()+" è stato DISTRUTTO***");
                if (gp.getVite(robot) > 0 || gp.getPuntiSalute(robot) > 0) //se ha ancora vite    
                    respawn(robot);
                else 
                    rv.placeRobot(robot, Direction.W, 0, 0, false);                
            }
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
    
    public void updateLabelFeedback(String update){
        this.feedback.setText(update);
    }
    
    private void updateFeedback(String feedback) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateLabelFeedback(feedback);
        }
        });
    }

    @Override
    public void notifyMessageReceived(Message msg) {
        
    }
    
    
       
}

