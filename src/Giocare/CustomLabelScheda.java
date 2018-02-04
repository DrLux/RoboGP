package Giocare;

import Allenamento.SchedaIstruzione;
import Allenamento.SchedaIstruzione.Schede;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * @author Sorrentino & Vair
 */

public class CustomLabelScheda extends JLabel {
    SchedaIstruzione scheda;
    SchedaIstruzione scheda_vuota;
    
    public CustomLabelScheda(){
        super();
        scheda_vuota = new SchedaIstruzione(Schede.empty);
        scheda_vuota.setPriorita(0);
        scheda = scheda_vuota;
        updateIcon();
        setBorder(BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        setHorizontalTextPosition(SwingConstants.CENTER);
        setMaximumSize(new java.awt.Dimension(110, 110));
        setMinimumSize(new java.awt.Dimension(110, 110));
        setVerticalTextPosition(SwingConstants.BOTTOM);
    }
       
    public void setScheda(SchedaIstruzione scheda){
            this.scheda = scheda;
            updateIcon();
    }
    
    public SchedaIstruzione getScheda(){
        SchedaIstruzione temp = scheda;
        scheda = scheda_vuota;
        updateIcon();
        return temp;
    }
    
    public void updateIcon(){
        if(scheda != null) {
            this.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/"+scheda.getName()+".png"))); 
            this.setText(Integer.toString(scheda.getPriorita()));
        }
    }
    
    public boolean isSet(){
        if(scheda == null || scheda.getTipo() == Schede.empty)
            return false;
        else
            return true;
    }
}
