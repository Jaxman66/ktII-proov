import java.time.LocalDateTime;

public class Arvuti implements Comparable<Arvuti> {

    private String tootja;
    private boolean kiirtöö;
    private LocalDateTime registreerimisAeg;

    public Arvuti(String tootja, boolean kiirtöö, LocalDateTime registreerimisAeg) {
        this.tootja = tootja;
        this.kiirtöö = kiirtöö;
        this.registreerimisAeg = registreerimisAeg;
    }

    public double arvutaArveSumma(double baasHind) {
        double täisHind = baasHind + 2;
        if (this instanceof VäliseMonitorigaArvuti) {
            täisHind += 1;
        }
        if (onKiirtöö()) {
            täisHind += 10;
        }
        return täisHind;
    }


    public String getTootja() {
        return tootja;
    }

    public boolean onKiirtöö() {
        return kiirtöö;
    }

    public LocalDateTime getRegistreerimisAeg() {
        return registreerimisAeg;
    }

    @Override
    public int compareTo(Arvuti o) {
        return Boolean.compare(o.onKiirtöö(), this.onKiirtöö());
    }


    @Override
    public String toString() {
        return tootja + ";" +
                (kiirtöö ? "kiirtöö" : "tavatöö") + (this instanceof VäliseMonitorigaArvuti ? ";monitoriga" : "") +
                "@" + registreerimisAeg.toString();
    }
}


