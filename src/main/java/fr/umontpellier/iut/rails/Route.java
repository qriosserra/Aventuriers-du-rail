package fr.umontpellier.iut.rails;
import java.util.ArrayList;
import java.util.HashMap;

public class Route {
    /**
     * Première extrémité
     */
    private Ville ville1;
    /**
     * Deuxième extrémité
     */
    private Ville ville2;
    /**
     * Nombre de segments
     */
    private int longueur;
    /**
     * CouleurWagon pour capturer la route (éventuellement GRIS, mais pas LOCOMOTIVE)
     */
    private CouleurWagon couleur;
    /**
     * Joueur qui a capturé la route (`null` si la route est encore à prendre)
     */
    private Joueur proprietaire;
    /**
     * Nom unique de la route. Ce nom est nécessaire pour résoudre l'ambiguïté entre les routes doubles
     * (voir la classe Plateau pour plus de clarté)
     */
    private String nom;

    public Route(Ville ville1, Ville ville2, int longueur, CouleurWagon couleur) {

        this.ville1 = ville1;
        this.ville2 = ville2;
        this.longueur = longueur;
        this.couleur = couleur;
        nom = ville1.getNom() + " - " + ville2.getNom();
        proprietaire = null;
    }

    public Ville getVille1() {
        return ville1;
    }

    public Ville getVille2() {
        return ville2;
    }

    public int getLongueur() {
        return longueur;
    }

    public CouleurWagon getCouleur() {
        return couleur;
    }

    public Joueur getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(Joueur proprietaire) {
        this.proprietaire = proprietaire;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String toLog() {
        return String.format("<span class=\"route\">%s - %s</span>", ville1.getNom(), ville2.getNom());
    }

    @Override
    public String toString() {
        return String.format("[%s - %s (%d, %s)]", ville1, ville2, longueur, couleur);
    }

    /**
     * @return un objet simple représentant les informations de la route
     */
    public Object asPOJO() {

        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", getNom());
        if (proprietaire != null) {

            data.put("proprietaire", proprietaire.getCouleur());
        }
        return data;
    }

    //pas BON !!!!
    public boolean coupValide(Joueur j){
        ArrayList<CouleurWagon> joueur=new ArrayList<>();
        joueur.addAll(j.getCartesWagon());
        boolean b=true;
        if (this.proprietaire==null) {
            int nbcarte = 0;
            ArrayList<CouleurWagon> carte = new ArrayList<>();
            if (this instanceof Ferry) {
                ArrayList<CouleurWagon> ferry = new ArrayList<>();
                for (int i = 0; i < ((Ferry) this).getNbLocomotives(); i++) {
                    ferry.add(CouleurWagon.LOCOMOTIVE);
                }
                if (!joueur.containsAll(ferry)) {
                    b = false;
                }
                if (this.couleur==CouleurWagon.GRIS && b){
                    verifier_gris(joueur,this.longueur-((Ferry) this).getNbLocomotives());
                }else if(b){
                    for (int i = 0; i < longueur - ((Ferry) this).getNbLocomotives(); i++) {
                        carte.add(couleur);
                        if (joueur.containsAll(carte)) {
                            nbcarte++;
                            joueur.remove(carte.get(0));
                        }
                    }
                    if (!(nbcarte == carte.size()) && b) {
                        int nbreste = carte.size() - nbcarte;
                        carte.clear();
                        for (int i = 0; i < nbreste + ((Ferry) this).getNbLocomotives(); i++) {
                            carte.add(CouleurWagon.LOCOMOTIVE);
                        }
                        if (!joueur.containsAll(carte)) {
                            b = false;
                        }
                    }
                }
            } else if (couleur == CouleurWagon.GRIS) {
                b=verifier_gris(joueur,this.longueur);
            } else if (b) {
                for (int i = 0; i < longueur; i++) {
                    carte.add(couleur);
                    if (joueur.containsAll(carte)) {
                        nbcarte++;
                        joueur.remove(carte.get(0));
                    }
                }
                if (!(nbcarte == carte.size())) {
                    int nbreste = carte.size() - nbcarte;
                    carte.clear();
                    for (int i = 0; i < nbreste; i++) {
                        carte.add(CouleurWagon.LOCOMOTIVE);
                    }
                    if (!joueur.containsAll(carte)) {
                             b = false;
                    }
                }
            }
        }else{b=false;}

        return b;

    }

    public boolean verifier_gris(ArrayList<CouleurWagon> joueur, int longueur){
        boolean b=false;
        int nbcarte = 0;
        ArrayList<CouleurWagon> carte = new ArrayList<>();
        int nbcarteplus=0;
        for (int i = 0; i < longueur; i++) {
            carte.add(CouleurWagon.NOIR);
            if (joueur.containsAll(carte)) {
                nbcarte++;
                joueur.remove(carte.get(0));
            }
        }
        nbcarteplus=nbcarte;
        if (nbcarte==longueur){
            b=true;
        }else{
            nbcarte=0;
            carte.clear();
            for (int i = 0; i < longueur; i++) {
                carte.add(CouleurWagon.ROSE);
                if (joueur.containsAll(carte)) {
                    nbcarte++;
                    joueur.remove(carte.get(0));
                }
            }
            if (nbcarte>nbcarteplus)nbcarteplus=nbcarte;
            if (nbcarte==longueur){
                b=true;
            }else{
                nbcarte=0;
                carte.clear();
                for (int i = 0; i < longueur; i++) {
                    carte.add(CouleurWagon.ROUGE);
                    if (joueur.containsAll(carte)) {
                        nbcarte++;
                        joueur.remove(carte.get(0));
                    }
                }
                if (nbcarte>nbcarteplus)nbcarteplus=nbcarte;
                if (nbcarte==longueur){
                    b=true;
                }else{
                    nbcarte=0;
                    carte.clear();
                    for (int i = 0; i < longueur; i++) {
                        carte.add(CouleurWagon.ORANGE);
                        if (joueur.containsAll(carte)) {
                            nbcarte++;
                            joueur.remove(carte.get(0));
                        }
                    }
                    if (nbcarte>nbcarteplus)nbcarteplus=nbcarte;
                    if (nbcarte==longueur){
                        b=true;
                    }else{
                        nbcarte=0;
                        carte.clear();
                        for (int i = 0; i < longueur; i++) {
                            carte.add(CouleurWagon.BLEU);
                            if (joueur.containsAll(carte)) {
                                nbcarte++;
                                joueur.remove(carte.get(0));
                            }
                        }
                        if (nbcarte>nbcarteplus)nbcarteplus=nbcarte;
                        if (nbcarte==longueur){
                            b=true;
                        }else{
                            nbcarte=0;
                            carte.clear();
                            for (int i = 0; i < longueur; i++) {
                                carte.add(CouleurWagon.JAUNE);
                                if (joueur.containsAll(carte)) {
                                    nbcarte++;
                                    joueur.remove(carte.get(0));
                                }
                            }
                            if (nbcarte>nbcarteplus)nbcarteplus=nbcarte;
                            if (nbcarte==longueur){
                                b=true;
                            }else{
                                nbcarte=0;
                                carte.clear();
                                for (int i = 0; i < longueur; i++) {
                                    carte.add(CouleurWagon.BLANC);
                                    if (joueur.containsAll(carte)) {
                                        nbcarte++;
                                        joueur.remove(carte.get(0));
                                    }
                                }
                                if (nbcarte>nbcarteplus)nbcarteplus=nbcarte;
                                if (nbcarte==longueur){
                                    b=true;
                                }else{
                                    nbcarte=0;
                                    carte.clear();
                                    for (int i = 0; i < longueur; i++) {
                                        carte.add(CouleurWagon.VERT);
                                        if (joueur.containsAll(carte)) {
                                            nbcarte++;
                                            joueur.remove(carte.get(0));
                                        }
                                    }
                                    if (nbcarte>nbcarteplus)nbcarteplus=nbcarte;
                                    if (nbcarte==longueur){
                                        b=true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!(nbcarteplus == longueur)) {
            int nbreste = longueur - nbcarteplus;
            carte.clear();
            for (int i = 0; i < nbreste; i++) {
                carte.add(CouleurWagon.LOCOMOTIVE);
            }
            if (!joueur.containsAll(carte)) {
                b = false;
            }
        }
        return b;
    }

    public ArrayList<CouleurWagon> demande(){
        ArrayList<CouleurWagon> demande = new ArrayList<>();
        demande.add(CouleurWagon.LOCOMOTIVE);
        demande.add(this.couleur);
        return demande;
    }

    public ArrayList<CouleurWagon>[] demandegris(){
        ArrayList<CouleurWagon>[] demande = new ArrayList[8];
        demande[0].add(CouleurWagon.VERT);
        demande[1].add(CouleurWagon.NOIR);
        demande[2].add(CouleurWagon.BLANC);
        demande[3].add(CouleurWagon.BLEU);
        demande[4].add(CouleurWagon.JAUNE);
        demande[5].add(CouleurWagon.ROUGE);
        demande[6].add(CouleurWagon.ORANGE);
        demande[7].add(CouleurWagon.ROSE);
        demande[8].add(CouleurWagon.LOCOMOTIVE);

        return demande;
    }
}
