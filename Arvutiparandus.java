import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

public class Arvutiparandus {
    private static final Queue<Arvuti> ooteltööd = new PriorityQueue<>();
    private static final Map<String, Double> tunnitasud = new HashMap<>();
    private static final Map<Arvuti, Double> tehtudTööd = new HashMap<>();

    public static void main(String[] args) throws IOException {

        loeAlgandmedSisse(args[0]);
        //  System.out.println(ooteltööd.poll()); kontrollimiseks, kui kirjutame sisendi lugemist
        loeSisseTunnitasud();
        for (Map.Entry<String, Double> paar : tunnitasud.entrySet()) {
            System.out.println(paar.getKey() + " : " + paar.getValue());
        } //mapi töötajate kontrollimiseks

        alustaTööTsüklit();

//        for (Arvuti arvuti : ooteltööd) {
//            System.out.println(arvuti);
//        }   //failist tööde sisse lugemise kontrolliks
    }

    private static void loeSisseTunnitasud() throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream("tunnitasud.dat"))) {
            int kogus = dis.readInt();
            for (int i = 0; i < kogus; i++) {
                String nimi = dis.readUTF();
                double tunnitasu = dis.readDouble();
                tunnitasud.put(nimi, tunnitasu);
            }
        }
    }

    private static void alustaTööTsüklit() throws IOException {
        try (Scanner scanner = new Scanner(System.in);) {
            while (true) {
                System.out.println("Kas soovid parandada (P), uut tööd registreerida (R) või lõpetada (L) ?");
                String sisend = scanner.nextLine();
                if ("P".equalsIgnoreCase(sisend)) {
                    Arvuti töö = ooteltööd.poll();
                    if (töö != null) {
                        System.out.println("Arvuti info: " + töö);
                        System.out.println("Sisesta parandamiseks kulunud aeg täisminutites:");
                        int aeg = Integer.parseInt(scanner.nextLine());
                        System.out.println("Sisesta enda nimi: ");
                        String nimi = scanner.nextLine();

                        Double tunnitasu = tunnitasud.get(nimi);
                        Double täisHind = töö.arvutaArveSumma(tunnitasu * (aeg / 60.0));
                        tehtudTööd.put(töö, täisHind); //lisame tehtud tööde listi

                        System.out.println("Töö tehtud, arve summa on: " + täisHind + " €!");


                    } else {
                        System.out.println("Kõik töö tehtud");
                    }
                } else if ("R".equalsIgnoreCase(sisend)) {
                    while (true) {
                        System.out.println("Sisesta töö kirjeldus: ");
                        //String kirjeldus = scanner.nextLine();  see rida jääb ära ja teeme järgmisel real kogu töö
                        try {
                            ooteltööd.add(loeArvuti(scanner.nextLine()));
                            //loeArvuti(scanner.nextLine()); kopeerin selle teksti sinna ooteltööd.add reale
                            break;
                        } catch (FormaadiErind formaadiErind) {
                            System.out.println(formaadiErind.getMessage());
                            ;
                        }
                    }
                } else if ("L".equalsIgnoreCase(sisend)) {


                    Double kogusumma = 0.0;
                    for (Double tööSumma : tehtudTööd.values()) {
                        kogusumma+=tööSumma;
                    }

                    väljastaÜlevaade(kogusumma,tehtudTööd,ooteltööd.size());

                    salvestaTehtudTööd();
                    salvestaTegemataTööd();
                } else {
                    System.out.println("vale sisend!");
                }
            }
        }
    }


    private static void väljastaÜlevaade(double summa, Map<Arvuti, Double> tehtud, int ootel){
        System.out.println("Sessiooni kokkuvõte:");
        System.out.println("Teenitud raha: "+ Math.round(summa*100.0)/100.0 +"€");

        System.out.println("Parandatud arvuteid: ");

        Map<String, Integer> erinevadArvutid = new HashMap<String, Integer>();

        for (Arvuti arvuti : tehtud.keySet()){
            String tootja = arvuti.getTootja();
            if (erinevadArvutid.containsKey(tootja)){
                int arvutiteArv = erinevadArvutid.get(tootja)+1;
                erinevadArvutid.put(tootja, arvutiteArv);
            }
            else
                erinevadArvutid.put(tootja, 1);
        }

        for (String tootja : erinevadArvutid.keySet())
            System.out.println("  "+tootja+": "+erinevadArvutid.get(tootja)+"tk");

        System.out.println("Ootele jäi "+ ootel +" arvuti(t).");
    }

    private static void salvestaTegemataTööd() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("ootel.txt"), "UTF-8"))) {
            Arvuti arvuti = ooteltööd.poll();
            while (arvuti != null) {
                bw.write(arvuti.toString());
                arvuti = ooteltööd.poll();
            }
        }
    }

    private static void salvestaTehtudTööd() throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream("tehtud.dot"))) {
            dos.writeInt(tehtudTööd.size());
            for (Map.Entry<Arvuti, Double> paar : tehtudTööd.entrySet()) {
                Arvuti arvuti = paar.getKey();
                dos.writeUTF(arvuti.getTootja());
                dos.writeUTF(arvuti.getRegistreerimisAeg().toString());
                dos.writeDouble(paar.getValue());
            }
        }
    }

    private static void loeAlgandmedSisse(String kasutajaSisend) throws IOException {
        InputStream is;

        if (kasutajaSisend.startsWith("http://") || kasutajaSisend.startsWith("https://")) {
            is = new URL(kasutajaSisend).openStream();
        } else {
            is = new FileInputStream(kasutajaSisend);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String rida = br.readLine();

            while (rida != null) {
                try {
                    ooteltööd.add(loeArvuti(rida));
                    loeArvuti(rida);
                } catch (FormaadiErind formaadiErind) {
                    System.out.println("vigane sisestus!:" + formaadiErind.getMessage());  //trükime käsitsi juurde
                }
                rida = br.readLine();
            }
        }

    }

    private static Arvuti loeArvuti(String rida) throws FormaadiErind {
        // String tootja;
        LocalDateTime registreerimisAeg;

        String[] andmed;
        if (rida.contains("@")) {
            String[] osad = rida.split("@");
            andmed = osad[0].split(";");
            registreerimisAeg = LocalDateTime.parse(osad[1]); //teisendame aja
        } else {
            andmed = rida.split(";");
            registreerimisAeg = LocalDateTime.now();
        }
        String tootja;
        Boolean kiirtöö;

        if (andmed.length == 2 || andmed.length == 3) {
            tootja = andmed[0];
            if (andmed[1].equals("kiirtöö") || andmed[1].equals("tavatöö")) {
                kiirtöö = andmed[1].equals("kiirtöö");
            } else {
                throw new FormaadiErind("pole tavatöö ega kiirtöö");
            }
            if (andmed.length == 3) {
                if (!andmed[2].equals("monitoriga")) {
                    throw new FormaadiErind("Väärtus pole \"monitoriga\"");
                }
                return new VäliseMonitorigaArvuti(tootja, kiirtöö, registreerimisAeg);
            } else {
                return new Arvuti(tootja, kiirtöö, registreerimisAeg);
            }
        } else {
            throw new FormaadiErind("Anmete pikkus pole 2 ega 3");
        }
    }


}



