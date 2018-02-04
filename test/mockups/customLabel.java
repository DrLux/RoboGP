package mockups;

import Allenamento.SchedaIstruzione;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 *
 * @author sorre
 */
public class customLabel extends JLabel {
    SchedaIstruzione scheda;
    
    public customLabel(){
        super();
        this.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/schedaVuota.png"))); 
        this.setText("0");
        setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setHorizontalTextPosition(SwingConstants.CENTER);
        setMaximumSize(new java.awt.Dimension(110, 110));
        setMinimumSize(new java.awt.Dimension(110, 110));
        setVerticalTextPosition(SwingConstants.BOTTOM);
    }
    
    public void cusSetIcon(String nomeOggetto){
        this.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/"+nomeOggetto+".png"))); 
    }
    
    public void setScheda(SchedaIstruzione scheda){
        this.scheda = scheda;
    }
    
    public SchedaIstruzione getScheda(){
        return this.scheda;
    }
    
    public void updateIcon(){
        this.cusSetIcon(scheda.getName());
        this.setText(""+scheda.getPriorita());
    }
    
    public SchedaIstruzione removeScheda(){        
        this.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/schedaVuota.png"))); 
        this.setText("0");
        SchedaIstruzione temp = scheda;
        scheda = null;
        return temp;
    }
    
}
