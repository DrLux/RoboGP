package robogp.matchmanager;

import Allenamento.ProgrammaRobot;
import Allenamento.SchedaIstruzione;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import robogp.robodrome.image.ImageUtil;

/**
 *
 * @author claudia
 */
public class RobotMarker implements Serializable {

    private transient BufferedImage robotImage;
    private final String name;
    private final String color;
    private String owner;
    private int dockNumber;
    private ProgrammaRobot programma;
    private int punti_salute;
    private int vite;
    

    public RobotMarker(String name, String color) {
        this.name = name;
        this.color = color;
        this.dockNumber = 0;
        this.punti_salute = 10;
        this.vite = 3;
        programma = null;
    }
    
    public void setProgramma(ProgrammaRobot prog){
        this.programma = prog;
    }
    
    public SchedaIstruzione getIstruzione(){
        if (this.programma != null)          
            return this.programma.getScheda();
         else 
            return null;
    }

    
    public boolean progFinito(){
        if (this.programma != null){
            System.out.println("Finito: "+this.programma.registriVuoti());
            return this.programma.registriVuoti();
        }
        else
            return true;
    }
    
    public void resetProg(){
        this.programma = null;
    }
    
    public BufferedImage getImage(int size) {
        if (this.robotImage == null) {
            String imgFile = "robots/" + name + ".png";
            try {
                robotImage = ImageIO.read(new File(imgFile));
            } catch (IOException ex) {
                Logger.getLogger(RobotMarker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ImageUtil.scale(ImageUtil.superImpose(null, this.robotImage),size, size);
    }

    public void assign(String nickname, int dock) {
        this.owner = nickname;
        this.dockNumber = dock;
    }

    public void free() {
        this.owner = null;
        this.dockNumber = 0;
    }

    public boolean isAssigned() {
        return (this.dockNumber > 0);
    }

    public String getOwner() {
        return this.owner;
    }

    public int getDock() {
        return this.dockNumber;
    }
    
    public String getName() {
        return name;
    }
    
   
        
    public void printProg(){
        System.out.println("Robot "+this.name+": ");
        this.programma.printAll();
    }
    
}
