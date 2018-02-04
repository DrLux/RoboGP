package robogp.matchmanager;

import Allenamento.SchedaIstruzione;
import connection.Connection;
import connection.Message;
import connection.MessageObserver;
import connection.PartnerShutDownException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import robogp.robodrome.Robodrome;

/**
 *
 * @author claudia
 */
public class Match implements MessageObserver {

    public static final int ROBOTSINGAME = 8;
    public static final String MatchJoinRequestMsg = "joinMatchRequest";
    public static final String MatchJoinReplyMsg = "joinMatchReply";
    public static final String MatchResponseStart = "startMatch";
    public static final String MatchCancelMsg = "cancelMatch";
    public static final String MancheInit = "MancheRequest";
    public static final String MancheInitResponse = "MancheReply";
    public static final String PoolRequest = "PoolRequest";
    public static final String PoolResponse = "PoolResponse";
    public static final String Winner = "winGame";

    public enum EndGame {
        First, First3, AllButLast
    };

    public enum State {
        Created, Started, Canceled
    };

    private static final String[] ROBOT_COLORS = {"blue", "red", "yellow", "emerald", "violet", "orange", "turquoise", "green"};
    private static final String[] ROBOT_NAMES = {"robot-blue", "robot-red", "robot-yellow", "robot-emerald", "robot-violet", "robot-orange", "robot-turquoise", "robot-green"};
    private final Robodrome theRobodrome;
    private final RobotMarker[] robots;
    private final EndGame endGameCondition;
    private final int nMaxPlayers;
    private final int nRobotsXPlayer;
    private final boolean initUpgrades;
    private State status;
    private List<SchedaIstruzione> global_pool; 
    private List<SchedaIstruzione> clone_pool; 
    private List<RobotMarker> programmed_robot; 

    private final HashMap<String, Connection> waiting;
    private final HashMap<String, Connection> players;

    /* Gestione pattern singleton */
    private static Match singleInstance;

    private Match(String rbdName, int nMaxPlayers, int nRobotsXPlayer, EndGame endGameCond, boolean initUpg) {
        this.nMaxPlayers = nMaxPlayers;
        this.nRobotsXPlayer = nRobotsXPlayer;
        this.endGameCondition = endGameCond;
        this.initUpgrades = initUpg;
        init_pool();
        String rbdFileName = "robodromes/" + rbdName + ".txt";
        this.robots = new RobotMarker[Match.ROBOT_NAMES.length];
        this.theRobodrome = new Robodrome(rbdFileName);
        for (int i = 0; i < Match.ROBOT_NAMES.length; i++) {
            this.robots[i] = new RobotMarker(Match.ROBOT_NAMES[i], Match.ROBOT_COLORS[i]);
        }
        waiting = new HashMap<>();
        players = new HashMap<>();
        this.programmed_robot = new ArrayList<>();
        this.status = State.Created;
    }

    public static Match getInstance(String rbdName, int nMaxPlayers,
            int nRobotsXPlayer, EndGame endGameCond, boolean initUpg) {
        if (Match.singleInstance == null || Match.singleInstance.status == Match.State.Canceled) {
            Match.singleInstance = new Match(rbdName, nMaxPlayers, nRobotsXPlayer, endGameCond, initUpg);
        }
        return Match.singleInstance;
    }

    public static Match getInstance() {
        if (Match.singleInstance == null || Match.singleInstance.status == Match.State.Canceled) {
            return null;
        }
        return Match.singleInstance;
    }

    @Override
    public void notifyMessageReceived(Message msg) {
        String server_key = MatchManagerApp.getAppInstance().getIniziarePartitaController().getKey();      
        switch (msg.getName()){
            case Match.MatchJoinRequestMsg:{
                String nickName = (String) msg.getParameter(0);
                String client_key = (String) msg.getParameter(1);
                Integer error_type = 0;
                if (!server_key.equals(client_key)){
                    error_type = 1; //chiave errata
                } 
                if (error_type == 0 && !nickname_available(nickName)){
                    error_type = 2; //nick occupato                
                }            
                if (error_type == 0){
                    this.waiting.put(nickName, msg.getSenderConnection());
                    MatchManagerApp.getAppInstance().getIniziarePartitaController().matchJoinRequestArrived(msg);
                }  else {

                    System.out.println("Nick presente in lista o in assegnati");
                    try {                        
                        Connection conn = msg.getSenderConnection();
                        Message reply = new Message(Match.MatchJoinReplyMsg);
                        Object[] parameters = new Object[2];
                        parameters[0] = new Boolean(false);
                        parameters[1] = error_type;
                        reply.setParameters(parameters);
                        conn.sendMessage(reply);
                    } catch (PartnerShutDownException ex) {
                        Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            break;
            }
            case Match.MancheInit:{
                add_programmed_robot((RobotMarker[])msg.getParameter(0));
            break;
            }
            case Match.PoolRequest:{
                Connection conn = msg.getSenderConnection();
                send_pool(conn);
            break;
            }
            case Match.Winner:{
                Connection conn = msg.getSenderConnection();
                conn.removeMessageObserver(this);
                if (!conn.isClosed())
                    conn.disconnect();                
            break;
            }
        }      
    }    
      
    public boolean nickname_available(String nickname){
       return (!this.waiting.containsKey(nickname) && !this.players.containsKey(nickname));
    }

    public State getStatus() {
        return this.status;
    }

    public void cancel() {
        this.status = State.Canceled;

        Message msg = new Message(Match.MatchCancelMsg);
        for (String nickname : waiting.keySet()) {
            this.refusePlayer(nickname);
        }

        players.values().stream().forEach((conn) -> {
            try {
                conn.sendMessage(msg);
            } catch (PartnerShutDownException ex) {
                Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public void start() {
        this.status = State.Started;     
        Message msg = new Message(Match.MatchResponseStart);
        players.values().stream().forEach((conn) -> {
            try {
                Object[] parameters = new Object[2];
                parameters[0] = getAssignedRobots();
                parameters[1] = this.endGameCondition;
                msg.setParameters(parameters);
                conn.sendMessage(msg);
            } catch (PartnerShutDownException ex) {
                Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
        public void stop() {
        // PROBABILMENTE NON IMPLEMENTATO NEL CORSO DI QUESTO PROGETTO
    }

    public ArrayList<RobotMarker> getAvailableRobots() {
        ArrayList<RobotMarker> ret = new ArrayList<>();
        for (RobotMarker m : this.robots) {
            if (!m.isAssigned()) {
                ret.add(m);
            }
        }
        return ret;
    }
    
    public ArrayList<RobotMarker> getAssignedRobots() {
        ArrayList<RobotMarker> ret = new ArrayList<>();
        for (RobotMarker m : this.robots) {
            if (m.isAssigned()) {
                ret.add(m);
            }
        }
        return ret;
    }

    public ArrayList<RobotMarker> getAllRobots() {
        ArrayList<RobotMarker> ret = new ArrayList<>();
        for (RobotMarker m : this.robots) {
            ret.add(m);
        }
        return ret;
    }

    public int getRobotsPerPlayer() {
        return this.nRobotsXPlayer;
    }

    public void refusePlayer(String nickname) {
        try {
            Connection conn = this.waiting.get(nickname);
            Message reply = new Message(Match.MatchJoinReplyMsg);
            Object[] parameters = new Object[2];
            parameters[0] = new Boolean(false);
            parameters[1] = new Integer(3); //richiesta rifiutata
            reply.setParameters(parameters);

            conn.sendMessage(reply);

        } catch (PartnerShutDownException ex) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            waiting.remove(nickname);
        }
    }

    public boolean addPlayer(String nickname, List<RobotMarker> selection) {
        boolean added = false;
        try {
            for (RobotMarker rob : selection) {
                int dock = this.getFreeDock();
                rob.assign(nickname, dock);
            }

            Connection conn = this.waiting.get(nickname);

            Message reply = new Message(Match.MatchJoinReplyMsg);
            Object[] parameters = new Object[3];
            parameters[0] = new Boolean(true);
            parameters[1] = this.theRobodrome.getPath();
            parameters[2] = selection.toArray(new RobotMarker[selection.size()]);
            reply.setParameters(parameters);

            conn.sendMessage(reply);
            this.players.put(nickname, conn);
            added = true;
        } catch (PartnerShutDownException ex) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            waiting.remove(nickname);
        }
        return added;

    }

    private int getFreeDock() {
        boolean[] docks = new boolean[this.theRobodrome.getDocksCount()];
        for (RobotMarker rob : this.robots) {
            if (rob.isAssigned()) {
                docks[rob.getDock() - 1] = true;
            }
        }
        int count = 0;
        while (docks[count]) {
            count++;
        }
        if (count < docks.length) {
            return count + 1;
        }
        return -1;
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.nMaxPlayers;
    }
    
    private void init_pool(){
        this.global_pool = new ArrayList<>();
        genera_schede(10,60,10,SchedaIstruzione.Schede.uturn);
        genera_schede(70,410,20,SchedaIstruzione.Schede.turnL);
        genera_schede(80,420,20,SchedaIstruzione.Schede.turnR);
        genera_schede(430,480,10,SchedaIstruzione.Schede.backup);
        genera_schede(490,660,10,SchedaIstruzione.Schede.move1);
        genera_schede(670,780,10,SchedaIstruzione.Schede.move2);
        genera_schede(790,840,10,SchedaIstruzione.Schede.move3);
    }
    
    private void genera_schede(int from, int to, int step, SchedaIstruzione.Schede tipo){
        for (int i = from; i <= to; i = i+step){            
            global_pool.add(new SchedaIstruzione(tipo, from));
            from+= step;
        }
    }
    
    private List<SchedaIstruzione> get_personal_pool(){
        List<SchedaIstruzione> personal_pool = new ArrayList<>();
        int random = 0;
        for (int i = 0; i < 9; i++){
            random = new Random().nextInt(clone_pool.size()-1);//range da 0 a massima size di clone_pool            
            personal_pool.add(clone_pool.remove(random) );
        }
        return personal_pool;
    }
    
    private void clone_schede(){
        clone_pool =  new ArrayList<>(this.global_pool);
    }
    
    public void send_pool(Connection conn){
        clone_schede();
        Message reply = new Message(Match.PoolResponse);           
        List<List<SchedaIstruzione>> pools = new ArrayList<List<SchedaIstruzione>>();
        for (int i = 0; i < nRobotsXPlayer; i++)
            pools.add(get_personal_pool());
        Object[] parameters = new Object[1];
        parameters[0] = pools;
        reply.setParameters(parameters);
        try {
            conn.sendMessage(reply);
        } catch (PartnerShutDownException ex) {
            Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void add_programmed_robot(RobotMarker[] robots){
        for (int i = 0; i < robots.length; i++){
            if (players.containsKey(robots[i].getName()));
                programmed_robot.add(robots[i]);
        }
        if (programmed_robot.size() == this.getAssignedRobots().size()){
            players.values().stream().forEach((conn) -> {                      
            try {      
                clone_schede();
                Message reply = new Message(Match.MancheInitResponse);           
                List<List<SchedaIstruzione>> pools = new ArrayList<List<SchedaIstruzione>>();
                for (int i = 0; i < nRobotsXPlayer; i++)
                    pools.add(get_personal_pool());
                Object[] parameters = new Object[2];
                parameters[0] = pools;
                parameters[1] = programmed_robot;
                reply.setParameters(parameters);
                conn.sendMessage(reply);
            } catch (PartnerShutDownException ex) {
                Logger.getLogger(Match.class.getName()).log(Level.SEVERE, null, ex);
            }           
            });
            programmed_robot.clear();
        }
    }   
}
