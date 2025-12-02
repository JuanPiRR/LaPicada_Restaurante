package modelo;

import java.io.Serializable;

public class Valoracion implements Serializable {

    private long idVenta;
    private int puntaje; // 1..5

    public Valoracion(long idVenta, int puntaje) {
        if (puntaje < 1 || puntaje > 5) throw new IllegalArgumentException("Puntaje debe estar entre 1 y 5.");
        this.idVenta = idVenta;
        this.puntaje = puntaje;
    }

    public long getIdVenta() { return idVenta; }
    public int getPuntaje() { return puntaje; }
}
