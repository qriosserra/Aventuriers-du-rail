package fr.umontpellier.iut.rails;
import java.util.*;
import java.util.stream.Collectors;

public class Joueur {


    /**
     * Les couleurs possibles pour les joueurs (pour l'interface graphique)
     */
    public static enum Couleur {
        JAUNE, ROUGE, BLEU, VERT, ROSE;
    }
    /**
     * Jeu auquel le joueur est rattaché
     */
    private Jeu jeu;
    /**
     * Nom du joueur
     */
    private String nom;
    /**
     * CouleurWagon du joueur (pour représentation sur le plateau)
     */
    private Couleur couleur;
    /**
     * Nombre de gares que le joueur peut encore poser sur le plateau
     */
    private int nbGares;
    /**
     * Nombre de wagons que le joueur peut encore poser sur le plateau
     */
    private int nbWagons;
    /**
     * Liste des missions à réaliser pendant la partie
     */
    private List<Destination> destinations;
    /**
     * Liste des cartes que le joueur a en main
     */
    private List<CouleurWagon> cartesWagon;
    /**
     * Liste temporaire de cartes wagon que le joueur est en train de jouer pour
     * payer la capture d'une route ou la construction d'une gare
     */
    private List<CouleurWagon> cartesWagonPosees;
    /**
     * Score courant du joueur (somme des valeurs des routes capturées)
     */
    private int score;

    public Joueur(String nom, Jeu jeu, Joueur.Couleur couleur) {

        this.nom = nom;
        this.jeu = jeu;
        this.couleur = couleur;
        nbGares = 3;
        nbWagons = 45;
        cartesWagon = new ArrayList<>();
        cartesWagonPosees = new ArrayList<>();
        destinations = new ArrayList<>();
        score = 12; // chaque gare non utilisée vaut 4 points
    }


    public int getNbGares() {
        return this.nbGares;
    }

    public int getScore() {
        return this.score;
    }

    public String getNom() {
        return nom;
    }

    public Couleur getCouleur() {
        return couleur;
    }

    public int getNbWagons() {
        return nbWagons;
    }

    public Jeu getJeu() {
        return jeu;
    }

    public List<CouleurWagon> getCartesWagonPosees() {
        return cartesWagonPosees;
    }

    public List<CouleurWagon> getCartesWagon() {
        return cartesWagon;
    }

    public List<Destination> getDestinations() {
        return destinations;
    }

    /**
     * Attend une entrée de la part du joueur (au clavier ou sur la websocket) et
     * renvoie le choix du joueur.
     * <p>
     * Cette méthode lit les entrées du jeu ({@code Jeu.lireligne()}) jusqu'à ce
     * qu'un choix valide (un élément de {@code choix} ou de {@code boutons} ou
     * éventuellement la chaîne vide si l'utilisateur est autorisé à passer) soit
     * reçu.
     * Lorsqu'un choix valide est obtenu, il est renvoyé par la fonction.
     * <p>
     * Si l'ensemble des choix valides ({@code choix} + {@code boutons}) ne comporte
     * qu'un seul élément et que {@code canPass} est faux, l'unique choix valide est
     * automatiquement renvoyé sans lire l'entrée de l'utilisateur.
     * <p>
     * Si l'ensemble des choix est vide, la chaîne vide ("") est automatiquement
     * renvoyée par la méthode (indépendamment de la valeur de {@code canPass}).
     * <p>
     * Exemple d'utilisation pour demander à un joueur de répondre à une question
     * par "oui" ou "non" :
     * <p>
     * {@code
     * List<String> choix = Arrays.asList("Oui", "Non");
     * String input = choisir("Voulez vous faire ceci ?", choix, new ArrayList<>(), false);
     * }
     * <p>
     * <p>
     * Si par contre on voulait proposer les réponses à l'aide de boutons, on
     * pourrait utiliser :
     * <p>
     * {@code
     * List<String> boutons = Arrays.asList("1", "2", "3");
     * String input = choisir("Choisissez un nombre.", new ArrayList<>(), boutons, false);
     * }
     *
     * @param instruction message à afficher à l'écran pour indiquer au joueur la
     *                    nature du choix qui est attendu
     * @param choix       une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur
     * @param boutons     une collection de chaînes de caractères correspondant aux
     *                    choix valides attendus du joueur qui doivent être
     *                    représentés par des boutons sur l'interface graphique.
     * @param peutPasser  booléen indiquant si le joueur a le droit de passer sans
     *                    faire de choix. S'il est autorisé à passer, c'est la
     *                    chaîne de caractères vide ("") qui signifie qu'il désire
     *                    passer.
     * @return le choix de l'utilisateur (un élément de {@code choix}, ou de
     * {@code boutons} ou la chaîne vide)
     */
    public String choisir(String instruction, Collection<String> choix, Collection<String> boutons, boolean peutPasser) {

        // on retire les doublons de la liste des choix
        HashSet<String> choixDistincts = new HashSet<>();
        choixDistincts.addAll(choix);
        choixDistincts.addAll(boutons);

        // Aucun choix disponible
        if (choixDistincts.isEmpty()) {

            return "";
        } else {

            // Un seul choix possible (renvoyer cet unique élément)
            if (choixDistincts.size() == 1 && !peutPasser)

                return choixDistincts.iterator().next();
            else {

                String entree;
                // Lit l'entrée de l'utilisateur jusqu'à obtenir un choix valide
                while (true) {

                    jeu.prompt(instruction, boutons, peutPasser);
                    entree = jeu.lireLigne();
                    // si une réponse valide est obtenue, elle est renvoyée
                    if (choixDistincts.contains(entree) || (peutPasser && entree.equals("")))

                        return entree;
                }
            }
        }
    }

    /**
     * Affiche un message dans le log du jeu (visible sur l'interface graphique)
     *
     * @param message le message à afficher (peut contenir des balises html pour la
     *                mise en forme)
     */
    public void log(String message) {
        jeu.log(message);
    }

    @Override
    public String toString() {

        StringJoiner joiner = new StringJoiner("\n");
        joiner.add(String.format("=== %s (%d pts) ===", nom, score));
        joiner.add(String.format("  Gares: %d, Wagons: %d", nbGares, nbWagons));
        joiner.add("  Destinations: "
                + destinations.stream().map(Destination::toString).collect(Collectors.joining(", ")));
        joiner.add("  Cartes wagon: " + CouleurWagon.listToString(cartesWagon));
        return joiner.toString();
    }

    /**
     * @return une chaîne de caractères contenant le nom du joueur, avec des balises
     * HTML pour être mis en forme dans le log
     */
    public String toLog() {
        return String.format("<span class=\"joueur\">%s</span>", nom);
    }

    /**
     * Renvoie une représentation du joueur sous la forme d'un objet Java simple
     * (POJO)
     */
    public Object asPOJO() {

        HashMap<String, Object> data = new HashMap<>();
        data.put("nom", nom);
        data.put("couleur", couleur);
        data.put("score", score);
        data.put("nbGares", nbGares);
        data.put("nbWagons", nbWagons);
        data.put("estJoueurCourant", this == jeu.getJoueurCourant());
        data.put("destinations", destinations.stream().map(Destination::asPOJO).collect(Collectors.toList()));
        data.put("cartesWagon", cartesWagon.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        data.put("cartesWagonPosees", cartesWagonPosees.stream().sorted().map(CouleurWagon::name).collect(Collectors.toList()));
        return data;
    }

    /**
     * Propose une liste de cartes destinations, parmi lesquelles le joueur doit en
     * garder un nombre minimum n.
     * <p>
     * Tant que le nombre de destinations proposées est strictement supérieur à n,
     * le joueur peut choisir une des destinations qu'il retire de la liste des
     * choix, ou passer (en renvoyant la chaîne de caractères vide).
     * <p>
     * Les destinations qui ne sont pas écartées sont ajoutées à la liste des
     * destinations du joueur. Les destinations écartées sont renvoyées par la
     * fonction.
     *
     * @param destinationsPossibles liste de destinations proposées parmi lesquelles
     *                              le joueur peut choisir d'en écarter certaines
     * @param n                     nombre minimum de destinations que le joueur
     *                              doit garder
     * @return liste des destinations qui n'ont pas été gardées par le joueur
     */
    public List<Destination> choisirDestinations(List<Destination> destinationsPossibles, int n) {
        String choix =" ";
        int i;
        ArrayList<String> nomdest = new ArrayList<>();
        ArrayList<Destination> dest= new ArrayList<>();
        for (int j=0; j<destinationsPossibles.size(); j++){
            nomdest.add(destinationsPossibles.get(j).getNom());
        }
        while(destinationsPossibles.size()>n && !choix.equals("")){
            choix=this.choisir(this.nom+" choisie une destination maintenant!",new ArrayList<>(),nomdest,true);
            if (!choix.equals("")) {
                  i=nomdest.indexOf(choix);
                  dest.add(destinationsPossibles.get(i));
                  destinationsPossibles.remove(i);
                  nomdest.remove(i);
             }
        }
        this.destinations.addAll(destinationsPossibles);
        return dest;
    }

    /**
     * Exécute un tour de jeu du joueur. Cette méthode attend que le joueur choisisse une des options suivantes :
     * <p>
     * - le nom d'une carte wagon face visible à prendre ;
     * <p>
     * - le nom "GRIS" pour piocher une carte wagon face cachée s'il reste des
     * cartes à piocher dans la pile de pioche ou dans la pile de défausse ;
     * <p>
     * - la chaîne "destinations" pour piocher des cartes destination ;
     * <p>
     * - le nom d'une ville sur laquelle il peut construire une gare (ville non
     * prise par un autre joueur, le joueur a encore des gares en réserve et assez
     * de cartes wagon pour construire la gare) ;
     * <p>
     * - le nom d'une route que le joueur peut capturer (pas déjà capturée, assez de
     * wagons et assez de cartes wagon) ;
     * <p>
     * - la chaîne de caractères vide pour passer son tour
     * <p>
     * Lorsqu'un choix valide est reçu, l'action est exécutée (il est possible que
     * l'action nécessite d'autres choix de la part de l'utilisateur, comme "choisir les cartes wagon à défausser pour capturer une route" ou
     * "construire une gare", "choisir les destinations à défausser", etc.)
     */
    public void jouerTour() {

        ArrayList<String> choix = new ArrayList<>();

        if (!jeu.getCartesWagonVisibles().isEmpty()) {
            for(CouleurWagon wa:jeu.getCartesWagonVisibles() ){
                choix.add(wa.name());
            }

        }

        if (!jeu.getPileCartesWagon().isEmpty()) choix.add("GRIS");

        if (!jeu.getPileDestinations().isEmpty()) choix.add("destinations");

        for (Route R:jeu.getRoutes()){
            if (R.coupValide(this) && R.getProprietaire()==null){
                choix.add(R.getNom());
            }
        }
        
        //GARES
        if (nbGares > 0) {
            
            for (Ville gare: jeu.getVilles()) {
                
                if (gare.coupValide(this)) {
                    
                    choix.add(gare.getNom());
                }
            }
        }
        
        String choixs=this.choisir("test",choix,choix,true);

        if (this.jeu.getRoutes().contains(jeu.nameToRoute(choixs))){
            this.choixRoute(jeu.nameToRoute(choixs));
        }
        if (choixs.equals("destinations")){
            ArrayList<Destination> dest = new ArrayList<>();
            ArrayList<Destination> destdef = new ArrayList<>();
            dest.add(jeu.piocherDestination());
            dest.add(jeu.piocherDestination());
            dest.add(jeu.piocherDestination());
            destdef= (ArrayList<Destination>) this.choisirDestinations(dest,2);
            for (Destination d:destdef){
                jeu.getPileDestinations().add(d);
            }

        }
        if (this.jeu.getCartesWagonVisibles().contains(CouleurWagon.nomToWagon(choixs)) || choixs.equals("GRIS")){
            if(choixs.equals("LOCOMOTIVE")){
                this.cartesWagon.add(CouleurWagon.LOCOMOTIVE);
                this.jeu.retirerCarteWagonVisible(CouleurWagon.LOCOMOTIVE);
            }else if (!choixs.equals("GRIS")){
                this.cartesWagon.add(CouleurWagon.nomToWagon(choixs));
                this.jeu.retirerCarteWagonVisible(CouleurWagon.nomToWagon(choixs));
                choix.clear();
                if (!jeu.getCartesWagonVisibles().isEmpty()) {
                    for(CouleurWagon wa:jeu.getCartesWagonVisibles() ){
                        choix.add(wa.name());
                    }

                }if (!jeu.getPileCartesWagon().isEmpty()) choix.add("GRIS");
                choixs=this.choisir("test",choix,choix,true);
                if (!choixs.equals("GRIS")) {
                    this.cartesWagon.add(CouleurWagon.nomToWagon(choixs));
                    this.jeu.retirerCarteWagonVisible(CouleurWagon.nomToWagon(choixs));
                }else{
                    this.cartesWagon.add(this.jeu.piocherCarteWagon());
                }

            }else{
                this.cartesWagon.add(this.jeu.piocherCarteWagon());
                choix.clear();
                if (!jeu.getCartesWagonVisibles().isEmpty()) {
                    for(CouleurWagon wa:jeu.getCartesWagonVisibles() ){
                        choix.add(wa.name());
                    }

                }if (!jeu.getPileCartesWagon().isEmpty()) choix.add("GRIS");
                choixs=this.choisir("test",choix,choix,true);
                if (!choixs.equals("GRIS")) {
                    this.cartesWagon.add(CouleurWagon.nomToWagon(choixs));
                    this.jeu.retirerCarteWagonVisible(CouleurWagon.nomToWagon(choixs));
                }else{
                    this.cartesWagon.add(this.jeu.piocherCarteWagon());
                }
            }
        }
        if (jeu.getVilles().contains(jeu.nameToVille(choixs))) {
            choix.clear();
            for (CouleurWagon wagon: this.cartesWagon){
                choix.add(wagon.name());
            }
            String carteselectionne;
            CouleurWagon choixcouleur=null;
            for(int i=0; i<4-nbGares; i++) {
                do {
                    carteselectionne = choisir("qu'elle carte ?", choix, choix, true);
                    if (choixcouleur == null && assezdecarte(4 - nbGares, CouleurWagon.nomToWagon(carteselectionne)) && CouleurWagon.nomToWagon(carteselectionne)!=CouleurWagon.BLEU) {
                        choixcouleur = CouleurWagon.nomToWagon(carteselectionne);
                    }
                } while (choixcouleur != CouleurWagon.nomToWagon(carteselectionne) && CouleurWagon.nomToWagon(carteselectionne)!=CouleurWagon.LOCOMOTIVE);
                this.cartesWagonPosees.add(CouleurWagon.nomToWagon(carteselectionne));
                this.cartesWagon.remove(CouleurWagon.nomToWagon(carteselectionne));
            }
            for (CouleurWagon c:cartesWagonPosees){
                jeu.defausserCarteWagon(c);
            }
            cartesWagonPosees.clear();
            jeu.nameToVille(choixs).setProprietaire(this);
            score -= 4;
            nbGares--;
        }
    }

    public void choixRoute(Route r){
        if (r instanceof Tunnel){// Tunelle
            if (r.getCouleur()!=CouleurWagon.GRIS) {// Pas gris
                ArrayList<String> str = new ArrayList<>();
                CouleurWagon coul = null;
                int lon =r.getLongueur();
                for (CouleurWagon couls:this.getCartesWagon()){
                    str.add(couls.name());
                }
                String choix;
                for (int i=0; i<lon; i++) {
                    do{
                        if(str.size()>0) {
                            choix = choisir(this.nom + " choisir les carte a dépensé", str, str, false);
                            coul = CouleurWagon.nomToWagon(choix);
                        }
                    }while (r.getCouleur()!=coul && coul!=CouleurWagon.LOCOMOTIVE);
                    this.cartesWagon.remove(coul);
                    this.cartesWagonPosees.add(coul);
                    str.clear();
                    for (CouleurWagon couls:this.getCartesWagon()){
                        str.add(couls.name());
                    }
                }
                ArrayList<CouleurWagon> ajout = new ArrayList<>();
                int nbajout=0,
                    nbajoueur=0;
                boolean b=true;
                StringBuilder s= new StringBuilder();
                for (int i=0;i<3;i++){
                    ajout.add(jeu.getPileCartesWagon().remove(0));
                    if (ajout.get(i)==r.getCouleur() || ajout.get(i)==CouleurWagon.LOCOMOTIVE){
                        nbajout++;
                    }
                    s.append(ajout.get(i).toString()).append(" ");
                }
                log(s.toString());
                for (CouleurWagon c:this.cartesWagon){
                    if (c==r.getCouleur() || c==CouleurWagon.LOCOMOTIVE){
                        nbajoueur++;
                    }
                }
                for (CouleurWagon c:ajout){
                    jeu.defausserCarteWagon(c);
                }
                if (nbajout==0){
                    for (CouleurWagon c : cartesWagonPosees) {
                        jeu.defausserCarteWagon(c);
                    }
                    this.cartesWagonPosees.clear();
                    this.score += r.point();
                    r.setProprietaire(this);
                }else {
                    boolean passe=true;
                    if (nbajoueur >= nbajout) {
                        for (int i = 0; i < nbajout && passe; i++) {
                            do {
                                if (str.size() > 0) {
                                    choix = choisir(this.nom + " choisir les carte a dépensé", str, str, true);
                                    if (choix!="") {
                                        coul = CouleurWagon.nomToWagon(choix);
                                    }else{
                                        passe=false;
                                    }
                                }
                            } while (r.getCouleur() != coul && coul != CouleurWagon.LOCOMOTIVE && passe);
                            if (passe) {
                                this.cartesWagonPosees.add(coul);
                                this.cartesWagon.remove(coul);
                            }
                        }
                        if (passe) {
                            for (CouleurWagon c : cartesWagonPosees) {
                                jeu.defausserCarteWagon(c);
                            }
                            this.cartesWagonPosees.clear();
                            this.score += r.point();
                            r.setProprietaire(this);
                        }else{
                            this.cartesWagon.addAll(this.cartesWagonPosees);
                            this.cartesWagonPosees.clear();
                        }
                    } else {
                        this.cartesWagon.addAll(this.cartesWagonPosees);
                        this.cartesWagonPosees.clear();
                    }
                }
            }else{
                ArrayList<String> str = new ArrayList<>();
                CouleurWagon coul = null;
                int lon =r.getLongueur();
                for (CouleurWagon couls:this.getCartesWagon()){
                    str.add(couls.name());
                }
                String choix;
                CouleurWagon wagonchoix=null;
                for (int i=0; i<lon; i++) {
                    do{
                        if(str.size()>0) {
                            choix = choisir(this.nom + " choisir les carte a dépensé", str, str, false);
                            coul = CouleurWagon.nomToWagon(choix);
                            if (wagonchoix==null && this.assezdecarte(r.getLongueur(),coul) && coul!=CouleurWagon.LOCOMOTIVE){
                                wagonchoix=coul;
                            }
                        }
                    }while (wagonchoix!=coul && coul!=CouleurWagon.LOCOMOTIVE);
                    this.cartesWagon.remove(coul);
                    this.cartesWagonPosees.add(coul);
                    str.clear();
                    for (CouleurWagon couls:this.getCartesWagon()){
                        str.add(couls.name());
                    }
                }
                ArrayList<CouleurWagon> ajout = new ArrayList<>();
                int nbajout=0,
                        nbajoueur=0;
                boolean b=true;
                StringBuilder s= new StringBuilder();
                for (int i=0;i<3;i++){
                    ajout.add(jeu.getPileCartesWagon().remove(0));
                    if (ajout.get(i)==wagonchoix || ajout.get(i)==CouleurWagon.LOCOMOTIVE){
                        nbajout++;
                    }
                    s.append(ajout.get(i).toString()).append(" ");
                }
                log(s.toString());
                for (CouleurWagon c:this.cartesWagon){
                    if (c==wagonchoix || c==CouleurWagon.LOCOMOTIVE){
                        nbajoueur++;
                    }
                }
                for (CouleurWagon c:ajout){
                    jeu.defausserCarteWagon(c);
                }
                if (nbajout==0){
                    for (CouleurWagon c : cartesWagonPosees) {
                        jeu.defausserCarteWagon(c);
                    }
                    this.cartesWagonPosees.clear();
                    this.score += r.point();
                    r.setProprietaire(this);
                }else {
                    boolean passe=true;
                    if (nbajoueur >= nbajout) {
                        for (int i = 0; i < nbajout && passe; i++) {
                            do {
                                if (str.size() > 0) {
                                    choix = choisir(this.nom + " choisir les carte a dépensé", str, str, true);
                                    if (choix!="") {
                                        coul = CouleurWagon.nomToWagon(choix);
                                    }else{
                                        passe=false;
                                    }
                                }
                            } while (wagonchoix != coul && coul != CouleurWagon.LOCOMOTIVE && passe);
                            if (passe) {
                                this.cartesWagonPosees.add(coul);
                                this.cartesWagon.remove(coul);
                            }
                        }
                        if (passe) {
                            for (CouleurWagon c : cartesWagonPosees) {
                                jeu.defausserCarteWagon(c);
                            }
                            this.cartesWagonPosees.clear();
                            this.score += r.point();
                            r.setProprietaire(this);
                        }else{
                            this.cartesWagon.addAll(this.cartesWagonPosees);
                            this.cartesWagonPosees.clear();
                        }
                    } else {
                        this.cartesWagon.addAll(this.cartesWagonPosees);
                        this.cartesWagonPosees.clear();
                    }
                }
            }


        }else if(r instanceof Ferry){//les ferry tjr gris
                ArrayList<String> str = new ArrayList<>();
                CouleurWagon coul = null,
                             choixcoul=null;
                int lon =r.getLongueur(),
                    nbloc=0,
                    nbcoul=0;
                for (CouleurWagon couls:this.getCartesWagon()){
                    str.add(couls.name());
                }
                String choix;
                do{
                    this.cartesWagon.addAll(cartesWagonPosees);
                    this.cartesWagonPosees.clear();
                    for (int i = 0; i < lon; i++) {
                        do {
                            if (str.size() > 0) {
                                choix = choisir(this.nom + " choisir les carte a dépensé", str, str, false);
                                coul = CouleurWagon.nomToWagon(choix);
                                if (choixcoul==null && !(coul.equals(CouleurWagon.LOCOMOTIVE)) && assezdecarteloc(r.getLongueur(),coul, (Ferry) r)){
                                    choixcoul=coul;
                                }
                                if (coul.equals(CouleurWagon.LOCOMOTIVE)) {
                                    nbloc++;
                                } else if (coul == choixcoul) {
                                    nbcoul++;
                                }
                            }
                        } while (choixcoul != coul && coul != CouleurWagon.LOCOMOTIVE);
                        this.cartesWagon.remove(coul);
                        this.cartesWagonPosees.add(coul);
                        str.clear();
                        for (CouleurWagon couls : this.getCartesWagon()) {
                            str.add(couls.name());
                        }
                    }
                }while(nbloc< ((Ferry) r).getNbLocomotives() || nbloc+nbcoul<r.getLongueur());
                for (CouleurWagon c: cartesWagonPosees) {
                    jeu.defausserCarteWagon(c);
                }
                this.cartesWagonPosees.clear();
                this.score+=r.point();
                r.setProprietaire(this);
        }



        else{// Pour les route
            if (r.getCouleur()!=CouleurWagon.GRIS) {// pas grise
                ArrayList<String> str = new ArrayList<>();
                CouleurWagon coul = null;
                int lon =r.getLongueur();
                for (CouleurWagon couls:this.getCartesWagon()){
                    str.add(couls.name());
                }
                String choix;
                for (int i=0; i<lon; i++) {
                    do{
                        if(str.size()>0) {
                            choix = choisir(this.nom + " choisir les carte a dépensé", str, str, false);
                            coul = CouleurWagon.nomToWagon(choix);
                        }
                    }while (r.getCouleur()!=coul && coul!=CouleurWagon.LOCOMOTIVE);
                    this.cartesWagon.remove(coul);
                    this.cartesWagonPosees.add(coul);
                    str.clear();
                    for (CouleurWagon couls:this.getCartesWagon()){
                        str.add(couls.name());
                    }
                }
                for (CouleurWagon c: cartesWagonPosees) {
                    jeu.defausserCarteWagon(c);
                }
                this.cartesWagonPosees.clear();
                this.score+=r.point();
                r.setProprietaire(this);
            }else{
                ArrayList<String> str = new ArrayList<>();
                CouleurWagon coul = null,
                        choixcoul=null;
                int lon =r.getLongueur();
                for (CouleurWagon couls:this.getCartesWagon()){
                    str.add(couls.name());
                }
                String choix;
                this.cartesWagon.addAll(cartesWagonPosees);
                this.cartesWagonPosees.clear();
                for (int i = 0; i < lon; i++) {
                    do {
                        if (str.size() > 0) {
                            choix = choisir(this.nom + " choisir les carte a dépensé", str, str, false);
                            coul = CouleurWagon.nomToWagon(choix);
                            if (choixcoul==null && !(coul.equals(CouleurWagon.LOCOMOTIVE)) && assezdecarte(r.getLongueur(),coul)){
                                choixcoul=coul;
                            }
                        }
                    } while (choixcoul != coul && coul != CouleurWagon.LOCOMOTIVE);
                    this.cartesWagon.remove(coul);
                    this.cartesWagonPosees.add(coul);
                    str.clear();
                    for (CouleurWagon couls : this.getCartesWagon()) {
                        str.add(couls.name());
                    }
                }
                for (CouleurWagon c: cartesWagonPosees) {
                    jeu.defausserCarteWagon(c);
                }
                this.cartesWagonPosees.clear();
                this.score+=r.point();
                r.setProprietaire(this);
            }
        }
    }

    public boolean assezdecarte(int nb, CouleurWagon C){
        boolean b=false;
        int i=0;
        int nbcarte=0;
        while(!b && i<this.cartesWagon.size()){
            if (this.cartesWagon.get(i)==C || this.cartesWagon.get(i)==CouleurWagon.LOCOMOTIVE) {
                nbcarte++;
            }
            if(nbcarte>=nb){
                b=true;
            }
            i++;
        }
        return b;
    }

    public boolean assezdecarteloc(int nb, CouleurWagon C, Ferry r){
        boolean b=false;
        int i=0;
        int nbcarte=0;
        int nbloc=0;
        while(!b && i<this.cartesWagon.size()){
            if (this.cartesWagon.get(i)==CouleurWagon.LOCOMOTIVE){
                nbloc++;
            }
            if (this.cartesWagon.get(i)==C || (this.cartesWagon.get(i)==CouleurWagon.LOCOMOTIVE && nbloc>=r.getNbLocomotives()) ){
                nbcarte++;
            }
            if(nbcarte>=nb){
                b=true;
            }
            i++;
        }
        return b;
    }
}