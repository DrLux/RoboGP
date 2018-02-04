package Allenamento;


import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import robogp.matchmanager.RobotMarker;

/**
 *
 * @author Sorretino & Vair
 */
public class GestoreProgrammazione {
    private final Map<String, SchedaIstruzione> icon_to_schede;
    public final List<JLabel> registriProg;
    private int icona_attuale;
    public int tot_reg;
    
    public GestoreProgrammazione(){ 
            icon_to_schede = new HashMap<>();
            icon_to_schede.put("card-move1",new SchedaIstruzione(SchedaIstruzione.Schede.move1));
            icon_to_schede.put("card-move2",new SchedaIstruzione(SchedaIstruzione.Schede.move2));
            icon_to_schede.put("card-move3",new SchedaIstruzione(SchedaIstruzione.Schede.move3));            
            icon_to_schede.put("card-turnL",new SchedaIstruzione(SchedaIstruzione.Schede.turnL));
            icon_to_schede.put("card-turnR",new SchedaIstruzione(SchedaIstruzione.Schede.turnR));
            icon_to_schede.put("card-uturn",new SchedaIstruzione(SchedaIstruzione.Schede.uturn));
            icon_to_schede.put("card-backup",new SchedaIstruzione(SchedaIstruzione.Schede.backup));
            registriProg = new ArrayList<>();
            icona_attuale = 0;
            tot_reg = 0;
    }
    
       
    public void programmaRobot(RobotMarker robot){ 
        ProgrammaRobot prog = new ProgrammaRobot();
        for( JLabel temp : registriProg) {
            if (temp.getIcon() != null){
                String scheda_key = icon_to_key(temp.getIcon().toString());
                SchedaIstruzione scheda = icon_to_schede.get(scheda_key);
                prog.addScheda(scheda);
            } else {
                System.out.println("Registro Vuoto");
            }
        }
        if(robot != null)
            robot.setProgramma(prog);
    }
    
    public static String icon_to_key(String label){
        String temp_key = null;
        if (label != null && !label.equals("")){
            File f = new File(label);        
            temp_key = f.getName().toString();
            temp_key = temp_key.substring(0, temp_key.lastIndexOf('.'));
        }
        return temp_key;
    }
    
    public void cancProg(RobotMarker robot){
        if (robot != null ) {
            robot.resetProg();
            for (int i = icona_attuale; i < tot_reg; i++)
                registriProg.get(i).setIcon(null);
            icona_attuale = 0;
        }
    }
    
    public void add_register(JLabel new_reg){
        if(new_reg != null) {
            registriProg.add(new_reg);
            tot_reg++;  
        }
    }
    
    public void drop_icon(){        
        while ( registriProg.get(icona_attuale).getIcon() == null)            
            icona_attuale = (icona_attuale + 1) % tot_reg;
        registriProg.get(icona_attuale).setIcon(null);
        while ( registriProg.get(icona_attuale).getIcon() == null && icona_attuale != 0)            
            icona_attuale = (icona_attuale + 1) % tot_reg;
    }
    
    
    public List<JLabel> getRegistriProg() {
        return registriProg;
    }
    
    /**
     * @return Returns the element at the specified position in the registers
     */
    public JLabel getRegistroProg(int index) {
        return registriProg.get(index);
    }

}
