package fr.umontpellier.iut.rails;
import com.google.gson.Gson;
import fr.umontpellier.iut.gui.GameServer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class Jeu implements Runnable {

    private List<Joueur> joueurs; //Liste des joueurs
    private Joueur joueurCourant; //Le joueur dont c'est le tour
    private List<Ville> villes; //Liste des villes représentées sur le plateau de jeu
    private List<Route> routes; //Liste des routes du plateau de jeu
    private List<CouleurWagon> pileCartesWagon; //Pile de pioche face cachée
    private List<CouleurWagon> cartesWagonVisibles; //Cartes de la pioche face visible (normalement 5 cartes face visible)
    private List<CouleurWagon> defausseCartesWagon; //Pile de cartes défaussées au cours de la partie
    /**
     * Pile des cartes "Destination" (uniquement les destinations "courtes", les
     * destinations "longues" sont distribuées au début de la partie et ne peuvent
     * plus être piochées après)
     */
    private List<Destination> pileDestinations;
    private BlockingQueue<String> inputQueue; //File d'attente des instructions recues par le serveur
    private List<String> log; //Messages d'information du jeu

    /**
     * ATTENTION : Cette méthode est à réécrire.
     *
     * Le code indiqué ici est un squelette minimum pour que le jeu se lance et que
     * l'interface graphique fonctionne.
     * Vous devez modifier ce code pour que les différents éléments du jeu soient
     * correctement initialisés.
     */
    public Jeu(String[] nomJoueurs) {

        //Initialisation des entrées/sorties
        inputQueue = new LinkedBlockingQueue<>();
        log = new ArrayList<>();

        //Création des cartes
        pileCartesWagon = new ArrayList<>();
        cartesWagonVisibles = new ArrayList<>();
        defausseCartesWagon = new ArrayList<>();
        pileDestinations = new ArrayList<>();

        //Création des joueurs
        ArrayList<Joueur.Couleur> couleurs = new ArrayList<>(Arrays.asList(Joueur.Couleur.values()));
        Collections.shuffle(couleurs);
        joueurs = new ArrayList<>();
        for (String nom : nomJoueurs) {
            Joueur joueur = new Joueur(nom, this, couleurs.remove(0));
            joueurs.add(joueur);
        }
        joueurCourant = joueurs.get(0);

        //Création des villes et des routes
        Plateau plateau = Plateau.makePlateauEurope();
        villes = plateau.getVilles();
        routes = plateau.getRoutes();
        for (int i=1; i<=12; i++){
            this.pileCartesWagon.add(CouleurWagon.NOIR);
            this.pileCartesWagon.add(CouleurWagon.BLEU);
            this.pileCartesWagon.add(CouleurWagon.BLANC);
            this.pileCartesWagon.add(CouleurWagon.JAUNE);
            this.pileCartesWagon.add(CouleurWagon.ROUGE);
            this.pileCartesWagon.add(CouleurWagon.LOCOMOTIVE);
            this.pileCartesWagon.add(CouleurWagon.VERT);
            this.pileCartesWagon.add(CouleurWagon.ROSE);
            this.pileCartesWagon.add(CouleurWagon.ORANGE);
        }
        this.pileCartesWagon.add(CouleurWagon.LOCOMOTIVE);
        this.pileCartesWagon.add(CouleurWagon.LOCOMOTIVE);
        Collections.shuffle(pileCartesWagon);
        this.remplirCarteVisible();
        this.pileDestinations=Destination.makeDestinationsEurope();
        Collections.shuffle(pileDestinations);
        for (Joueur joueur: joueurs){

            joueur.getCartesWagon().add(this.pileCartesWagon.remove(0));
            joueur.getCartesWagon().add(this.pileCartesWagon.remove(0));
            joueur.getCartesWagon().add(this.pileCartesWagon.remove(0));
            joueur.getCartesWagon().add(this.pileCartesWagon.remove(0));
        }
    }

    public List<CouleurWagon> getPileCartesWagon() {
        return pileCartesWagon;
    }

    public List<CouleurWagon> getCartesWagonVisibles() {
        return cartesWagonVisibles;
    }

    public List<Ville> getVilles() {
        return villes;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public Joueur getJoueurCourant() {
        return joueurCourant;
    }

    /**
     * Exécute la partie
     */
    public void run() {
        /*
         * ATTENTION : Cette méthode est à réécrire.
         * 
         * Cette méthode doit :
         * - faire choisir à chaque joueur les destinations initiales qu'il souhaite
         * garder : on pioche 3 destinations "courtes" et 1 destination "longue", puis
         * le
         * joueur peut choisir des destinations à défausser ou passer s'il ne veut plus
         * en défausser. Il doit en garder au moins 2.
         * - exécuter la boucle principale du jeu qui fait jouer le tour de chaque
         * joueur à tour de rôle jusqu'à ce qu'un des joueurs n'ait plus que 2 wagons ou
         * moins
         * - exécuter encore un dernier tour de jeu pour chaque joueur après
         */
        List<Destination> pileDestinationslong;
        List<Destination> piledefausse;
        List<Destination> cartpossible = new ArrayList<>();
        pileDestinationslong=Destination.makeDestinationsLonguesEurope();
        Collections.shuffle(pileDestinationslong);
        for (Joueur joueur : joueurs) {
            cartpossible.add(piocherDestination());
            cartpossible.add(piocherDestination());
            cartpossible.add(piocherDestination());
            cartpossible.add(pileDestinationslong.remove(0));
            piledefausse=joueur.choisirDestinations(cartpossible,2);
            for (Destination dest : piledefausse) {
                cartpossible.remove(dest);
            }
            cartpossible.clear();
        }
        int i=0;
        joueurCourant=joueurs.get(i);
        while(joueurCourant.getCartesWagon().size()>2){
            joueurCourant.jouerTour();
            i++;
            if (i==joueurs.size()){
                i=0;
            }
            joueurCourant=joueurs.get(i);
        }
        for (Joueur joueuse : joueurs){
            joueuse.jouerTour();
        }
    }

    /**
     * Ajoute une carte dans la pile de défausse.fr.umontpellier.iut.rails.JoueurProfTest
     * Dans le cas peu probable, où il y a moins de 5 cartes wagon face visibles
     * (parce que la pioche
     * et la défausse sont vides), alors il faut immédiatement rendre cette carte
     * face visible.
     *
     * @param c carte à défausser
     */
    public void defausserCarteWagon(CouleurWagon c) {
        if (cartesWagonVisibles.size()<5){
            cartesWagonVisibles.add(c);
        }else {
            defausseCartesWagon.add(c);
        }
    }

    /**
     * Pioche une carte de la pile de pioche
     * Si la pile est vide, les cartes de la défausse sont replacées dans la pioche
     * puis mélangées avant de piocher une carte
     *
     * @return la carte qui a été piochée (ou null si aucune carte disponible)
     */
    public CouleurWagon piocherCarteWagon() {

        if (!pileCartesWagon.isEmpty()) return pileCartesWagon.remove(0);
        else if (!defausseCartesWagon.isEmpty()) {

            for (int i = 0; i < defausseCartesWagon.size(); i++){
                pileCartesWagon.add(defausseCartesWagon.remove(i));
            }
            Collections.shuffle(pileCartesWagon);
            return pileCartesWagon.remove(0);
        }
        else return null;
    }

    /**
     * Retire une carte wagon de la pile des cartes wagon visibles.
     * Si une carte a été retirée, la pile de cartes wagons visibles est recomplétée
     * (remise à 5, éventuellement remélangée si 3 locomotives visibles)
     */
    public void retirerCarteWagonVisible(CouleurWagon c) {

        if (cartesWagonVisibles.contains(c)) {
            cartesWagonVisibles.remove(c); //Apparement on jete la carte
            this.remplirCarteVisible();
        }
    }

    public void remplirCarteVisible(){

        int nbLocomVisibles = 0;
        while (cartesWagonVisibles.size()<5 && !pileCartesWagon.isEmpty()){
            this.cartesWagonVisibles.add(this.piocherCarteWagon());
        }
        do{
            for (int i = 0; i < cartesWagonVisibles.size() && nbLocomVisibles<3; i++) {

                if (cartesWagonVisibles.get(i).name().equals("LOCOMOTIVE")) {
                    nbLocomVisibles++;
                }
            }
            if (nbLocomVisibles>=3){
                for (int i = 0; i < cartesWagonVisibles.size(); i++) {

                    this.pileCartesWagon.add(cartesWagonVisibles.remove(i));
                }
                Collections.shuffle(pileCartesWagon);
                for (int i=0; i<5; i++){
                    cartesWagonVisibles.add(this.piocherCarteWagon());
                }
            }

        }while (nbLocomVisibles >= 3);
    }

    /**
     * Pioche et renvoie la destination du dessus de la pile de destinations.
     * 
     * @return la destination qui a été piochée (ou `null` si aucune destination
     *         disponible)
     */
    public Destination piocherDestination() {
        Destination d;
        if(!pileDestinations.isEmpty()) {
            d = pileDestinations.remove(0);
        }else{
            d = null;
        }
        return d;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");
        for (Joueur j : joueurs) {
            joiner.add(j.toString());
        }
        return joiner.toString();
    }

    /**
     * Ajoute un message au log du jeu
     */
    public void log(String message) {
        log.add(message);
    }

    /**
     * Ajoute un message à la file d'entrées
     */
    public void addInput(String message) {
        inputQueue.add(message);
    }

    /**
     * Lit une ligne de l'entrée standard
     * C'est cette méthode qui doit être appelée à chaque fois qu'on veut lire
     * l'entrée clavier de l'utilisateur (par exemple dans {@code Player.choisir})
     *
     * @return une chaîne de caractères correspondant à l'entrée suivante dans la
     *         file
     */
    public String lireLigne() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Envoie l'état de la partie pour affichage aux joueurs avant de faire un choix
     *
     * @param instruction l'instruction qui est donnée au joueur
     * @param boutons     labels des choix proposés s'il y en a
     * @param peutPasser  indique si le joueur peut passer sans faire de choix
     */
    public void prompt(String instruction, Collection<String> boutons, boolean peutPasser) {
        System.out.println();
        System.out.println(this);
        if (boutons.isEmpty()) {
            System.out.printf(">>> %s: %s <<<%n", joueurCourant.getNom(), instruction);
        } else {
            StringJoiner joiner = new StringJoiner(" / ");
            for (String bouton : boutons) {
                joiner.add(bouton);
            }
            System.out.printf(">>> %s: %s [%s] <<<%n", joueurCourant.getNom(), instruction, joiner);
        }

        Map<String, Object> data = Map.ofEntries(
                new AbstractMap.SimpleEntry<String, Object>("prompt", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("instruction", instruction),
                        new AbstractMap.SimpleEntry<String, Object>("boutons", boutons),
                        new AbstractMap.SimpleEntry<String, Object>("nomJoueurCourant", getJoueurCourant().getNom()),
                        new AbstractMap.SimpleEntry<String, Object>("peutPasser", peutPasser))),
                new AbstractMap.SimpleEntry<>("villes",
                        villes.stream().map(Ville::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<>("routes",
                        routes.stream().map(Route::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("joueurs",
                        joueurs.stream().map(Joueur::asPOJO).collect(Collectors.toList())),
                new AbstractMap.SimpleEntry<String, Object>("piles", Map.ofEntries(
                        new AbstractMap.SimpleEntry<String, Object>("pileCartesWagon", pileCartesWagon.size()),
                        new AbstractMap.SimpleEntry<String, Object>("pileDestinations", pileDestinations.size()),
                        new AbstractMap.SimpleEntry<String, Object>("defausseCartesWagon", defausseCartesWagon),
                        new AbstractMap.SimpleEntry<String, Object>("cartesWagonVisibles", cartesWagonVisibles))),
                new AbstractMap.SimpleEntry<String, Object>("log", log));
        GameServer.setEtatJeu(new Gson().toJson(data));
    }

    public List<CouleurWagon> getDefausseCartesWagon( ) {
        return this.defausseCartesWagon;
    }

    public List<Destination> getPileDestinations() {
        return this.pileDestinations;
    }

    public Route nameToRoute(String name){
        int i=0;boolean b=true;Route r= null;
        while (i<this.routes.size() && b){
            r=this.routes.get(i);
            if (r.getNom().equals(name)){
                return r;
            }
            i++;
        }
        return null;
    }
}
