package Allenamento;


import java.io.Serializable;
import robogp.robodrome.Rotation;

/**
 *
 * @author Sorrentino & Vair
 */
   
public class SchedaIstruzione implements Serializable{    
    public enum Schede{backup, move1, move2, move3, turnL, turnR, uturn, empty}
    private Schede tipo;
    public int mov;
    public Rotation rot;
    public boolean reverse;
    private String name;
    private int priorita;
    
    public SchedaIstruzione(Schede tipoScheda){
        tipo = tipoScheda;
        priorita = 0;
        init();
    }
    
    public SchedaIstruzione(Schede tipoScheda, int priorita){
        tipo = tipoScheda;
        this.priorita = priorita;
        init();
    }
    
    public void init(){
        switch(tipo){
            case backup:
                this.mov = 1;
                this.rot = Rotation.NO;
                this.reverse = true;
                this.name = "card-backup";
            break;
            case move1:
                this.mov = 1;
                this.rot = Rotation.NO;
                this.reverse = false;
                this.name = "card-move1";
            break;
            case move2:
                this.mov = 2;
                this.rot = Rotation.NO;
                this.reverse = false;
                this.name = "card-move2";
            break;
            case move3:
                this.mov = 3;
                this.rot = Rotation.NO;
                this.reverse = false;
                this.name = "card-move3";
            break;
            case turnL:
                this.mov = 0;
                this.rot = Rotation.CCW90;
                this.reverse = false;
                this.name = "card-turnL";
            break;
            case turnR:
                this.mov = 0;
                this.rot = Rotation.CW90;
                this.reverse = false;
                this.name = "card-turnR";
            break;
            case uturn:
                this.mov = 0;
                this.rot = Rotation.CW180;
                this.reverse = false;
                this.name = "card-uturn";
            break;
            default:
                this.mov = 0;
                this.rot = Rotation.NO;
                this.reverse = false;
                this.name = "schedaVuota";
            break;
        }
    }
   
    public String toString(){
        return "Tipo: "+ this.tipo + " | Priorita: " + priorita + " | Mov: " + this.mov + " | rot: " + this.rot;
    }
    
    public String getName(){
        return this.name;
    }
    
    public int getPriorita(){
        return this.priorita;
    }
    
    public void setPriorita(int p){
        this.priorita = p;
    }
    
    public Schede getTipo(){
        return this.tipo;
    }
}