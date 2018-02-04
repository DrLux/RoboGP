package Allenamento;

import Allenamento.SchedaIstruzione;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Sorrentino & Vair
 */

public class ProgrammaRobot implements Serializable {
    
    private final List<SchedaIstruzione> registriProg;
    private SchedaIstruzione schedaCorrente;
    
    public ProgrammaRobot(List<SchedaIstruzione> programma){
        registriProg = programma;
        schedaCorrente = programma.get(0);
    }
    
    public ProgrammaRobot(){
        registriProg = new ArrayList<SchedaIstruzione>();
    }
     
    
    public SchedaIstruzione getScheda(){
        if (registriProg.isEmpty()){
            return null;
        } else {
            schedaCorrente = registriProg.remove(0);
            return schedaCorrente;
        }
    }
    
    public void addScheda(SchedaIstruzione scheda){
        registriProg.add(scheda);
    }
    
    public void resetProg(){
        registriProg.clear();
    }
    
    public boolean registriVuoti(){
        return 	registriProg.isEmpty(); 
    }
    
    public void printAll(){
        for (SchedaIstruzione scheda: registriProg){
            System.out.print(" | "+scheda.getName());
        }
    }
}


