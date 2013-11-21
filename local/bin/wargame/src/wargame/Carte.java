package wargame;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import wargame.Charset.Direction;

/** Classe de la Carte du jeu.
 *	@author ABHAMON Ronan 
 */
public class Carte extends JPanel implements ActionListener, Serializable
{	
	private static final long serialVersionUID = 1845646587235566472L;

	protected static int nbHerosRestant = IConfig.NB_HEROS;
	protected static int nbMonstresRestant = IConfig.NB_MONSTRES;
	protected static int nbToPlay = nbHerosRestant - 1; 
	protected static int tour = 0;

	public int getNbHerosRestant() {
		return nbHerosRestant;
	}

	public static int getNbMonstresRestant() {
		return nbMonstresRestant;
	}

	public static void nbMonstresRestantDec() {
		nbMonstresRestant--;
	}

	public static void nbHerosRestantDec() {
		Carte.nbHerosRestant--;
	}

	public void nbToPlayDef() {
		Carte.nbToPlay--;
	}

	/** Tileset de la carte. */
	private Tileset tileset;

	/** Carte. */
	protected char []carte;

	/** Monstres. */
	private Monstre []monstre;

	/** Héros. */
	private Heros []heros;

	/** Table de jeu de la carte. */
	private static Soldat []soldat;

	/** La carte est-elle générée ? */
	private boolean generee = false;

	/** Indique la case courante sélectionnée.
	 * Correspond également au soldat selectionné dans le combat.
	 */
	private int caseActionnee = -1;

	/** Indique le soldat actuellement pointé par le curseur de la souris */
	private Soldat soldatPointe = null;

	private JLabel image;

	/** Timer. */
	Timer timer;

	/** Constructeur par défaut. 
	 * @throws MidiUnavailableException 
	 * @throws IOException 
	 * @throws InvalidMidiDataException */
	Carte() throws InvalidMidiDataException, IOException, MidiUnavailableException
	{		
		/* Initialisation taux de rafraichissement. */
		timer = new Timer((int)(1000.0 * 1.0 / IConfig.FPS), this);
		timer.setInitialDelay(0);
		timer.start();

		/* Création d'une carte vide. */
		carte = new char [IConfig.LARGEUR_CARTE * IConfig.LARGEUR_CARTE];	

		image = new JLabel( new ImageIcon( IConfig.CHEMIN_IMAGE + "image_presentation.png"));
		this.add(image);

		/* Capture de la souris. */
		addMouseListener(new MouseAdapter() {
			/* Au clic */
			public void mouseClicked(MouseEvent e) { 
				if(!generee)
					return;

				/* On crée une position aux coordonnées du clic de la souris pour connaitre la coordonnée de la case (et non pas du pixel) */
				Position pos = new Position(e.getX() / IConfig.NB_PIX_CASE , e.getY() / IConfig.NB_PIX_CASE);
				int case_cliquee = pos.getNumCase();

				/* On vient de cliquer sur la même case : on veut se reposer */
				if(case_cliquee == caseActionnee && !soldat[case_cliquee].getAJoue()) {
					soldat[case_cliquee].repos(true);
					nbToPlayDef();
				}

				/* On change de héros si la case n'est pas vide, qu'il s'agit bien d'un soldat et que le soldat est affiché. */
				if(soldat[case_cliquee] != null 
					&& soldat[case_cliquee] instanceof Heros 
					&& soldat[case_cliquee].estVisible() 
				) {
					caseActionnee = case_cliquee;
				}
				/* Sinon on va faire une action en rapport avec le soldat sélectionné. */
				else {
					if(caseActionnee != -1) {
						/* Distance entre la case cliquée et la case actionnée */
						int distance = new Position(caseActionnee).distance(new Position(case_cliquee));

						/* Si on clique sur un monste alors combat */
						if(soldat[case_cliquee] instanceof Monstre 
							&& distance <= soldat[caseActionnee].getPortee() 
							&& !soldat[caseActionnee].getAJoue() 
							&& soldat[case_cliquee].estVisible() 
						) {
							soldat[caseActionnee].combat(soldat[case_cliquee], distance);
							nbToPlayDef();
							return;
						}


						/* On a un soldat selectionné et on clique sur une case autour (à une distance de 1 autour du soldat)
						 * -> On veut se déplacer sur la nouvelle case.
						 */
						if(distance == 1 
							&& tileset.getTile(carte[case_cliquee]).estPraticable() 
							&& soldat[case_cliquee] == null && !soldat[caseActionnee].getAJoue()
						) {
							FenetreJeu.information.setText(soldat[caseActionnee].nom + " se deplace en " + caseActionnee );

							nbToPlayDef();
							soldat[case_cliquee] = soldat[caseActionnee];
							soldat[caseActionnee] = null;

							/* On déplace le soldat à la nouvelle position */
							deplaceSoldat(soldat[case_cliquee], new Position(case_cliquee));
						}
					}
					/* Fin deplacement on re-initialise la case */
					caseActionnee = -1;
				}
			}
		});

		/* Écouteur permettant de savoir si le curseur pointe sur un soldat */
		addMouseMotionListener(new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {
				/* On vérifie que la carte a bien été générée */
				if(generee){
					/* Récupération de la case de la carte correspondant aux coordonnées du curseur */
					int num_case_curseur = new Position((e.getX() / IConfig.NB_PIX_CASE), (e.getY() / IConfig.NB_PIX_CASE)).getNumCase();

					/* Si il y a bien un soldat pointé, alors on met ce dernier dans l'objet soldatPointe, sinon on le met à null (pas de soldat pointé) */
					if(soldat[num_case_curseur] != null && soldat[num_case_curseur].estVisible())
						soldatPointe = soldat[num_case_curseur];
					else
						soldatPointe = null;
				}
			}
		});
	}

	/** 
	 * Méthode permettant de reinitialiser le tour de tous les soldats
	 */
	public void reinitAJoue() {
		tour++;

		FenetreJeu.information.setText("Début du tour " + tour);

		for(int i = 0; i < IConfig.HAUTEUR_CARTE * IConfig.LARGEUR_CARTE; i++){
			if(soldat[i] != null) {
				if(soldat[i] instanceof Heros && !soldat[i].getAJoue() ) {
					soldat[i].repos(false);
				}
				soldat[i].setAJoue(false);	
			}
		}
		
		nbToPlay = nbHerosRestant;
	}

	/**
	 *  Méthode chargeant un nouveau Tileset
	 */
	private void chargerTileset()
	{
		if(tileset == null)
			try {
				tileset = new Tileset(IConfig.NOM_TILESET);
			} 
		catch(IOException e) {
			System.out.println(e);
			return;
		}		
	}

	/**
	 *  Méthode générant la carte 
	 */
	private void genererCarte()
	{
		int x, y;

		soldat = new Soldat[IConfig.LARGEUR_CARTE * IConfig.HAUTEUR_CARTE];
		chargerTileset();

		/* Couche d'herbe. */
		for(int i = 0; i < IConfig.LARGEUR_CARTE * IConfig.LARGEUR_CARTE; i++){
			Position p = new Position(i);
			tileset.setHerbe(this, p.x, p.y);
		}

		/* Rochers. */
		for(int i = 0; i < IConfig.NB_ROCHERS; i++)
		{
			x = (int)(Math.random() * IConfig.LARGEUR_CARTE);
			y = (int)(Math.random() * IConfig.HAUTEUR_CARTE);
			tileset.setRocher(this, x, y);
		}

		/* Arbres. */
		for(int i = 0; i < IConfig.NB_ARBRES; i++)
		{
			x = (int)(Math.random() * IConfig.LARGEUR_CARTE);
			y = (int)(Math.random() * IConfig.HAUTEUR_CARTE);

			if(!tileset.setArbre(this, x, y)) i--;
		}

		/* Paille. */
		for(int i = 0; i < IConfig.NB_PAILLES; i++)
		{
			x = (int)(Math.random() * IConfig.LARGEUR_CARTE);
			y = (int)(Math.random() * IConfig.HAUTEUR_CARTE);

			if(!tileset.setPaille(this, x, y)) i--;
		}
	}

	/**
	 *  Méthode générant tous les soldats 
	 */
	private void genererSoldats()
	{
		/* Mise à 0 de la carte. */
		for(int i = 0; i < IConfig.LARGEUR_CARTE * IConfig.HAUTEUR_CARTE; i++)
			soldat[i] = null;

		/* Tableaux de soldats. */
		heros = new Heros[IConfig.NB_HEROS];
		monstre = new Monstre[IConfig.NB_MONSTRES];

		/* Chargement des soldats de base. */
		try {
			for(int i = 0; i < IConfig.NB_HEROS; i++)
			{
				heros[i] = new Heros(ISoldat.TypesH.getTypeHAlea());
				heros[i].setDirection(Direction.GAUCHE);
			}
			for(int i = 0; i < IConfig.NB_MONSTRES; i++)
			{
				monstre[i] = new Monstre(ISoldat.TypesM.getTypeMAlea());
				monstre[i].setDirection(Direction.DROITE);
			}
		} 
		catch(IOException e) {
			System.out.println(e);
			return;
		}	

		/* Positionnement des soldats. */
		for(int i = 0; i < IConfig.NB_HEROS; i++) {
			Position pos = new Position(this.trouvePositionVide(Soldat.HEROS));

			soldat[pos.getNumCase()] = heros[i];
			soldat[pos.getNumCase()].setPosition(pos);
		}

		for(int i = 0; i < IConfig.NB_MONSTRES; i++) {
			Position pos = new Position(this.trouvePositionVide(Soldat.MONSTRE));

			soldat[pos.getNumCase()] = monstre[i];
			soldat[pos.getNumCase()].setPosition(pos);
		}
	}

	/** Déplace un soldat sur la carte.
	 * @param soldat    Soldat à deplacer.
	 * @param direction Direction du soldat.
	 * @param x         Offset X d'origine.
	 * @param y         Offset Y d'origine.
	 */
	private void deplaceSoldat(Soldat soldat, Position nouvelle_position)
	{
		Son.joueCourir();
		soldat.setAJoue(true);
		int sx = soldat.getPosition().x;
		int sy = soldat.getPosition().y;
		int dx = nouvelle_position.x;
		int dy = nouvelle_position.y;
		int x = 0;
		int y = 0;

		Direction direction = Direction.HAUT;

		if(dx > sx) {
			x = 2;
			direction = Direction.DROITE;
		}
		else if(dx < sx) {
			x = -2;
			direction = Direction.GAUCHE;
		}
		if(dy > sy) {
			y = 2;
			direction = Direction.BAS;
		}
		else if(dy < sy) {
			y = -2;
			direction = Direction.HAUT;
		}

		soldat.setSeDeplace(true);
		soldat.setDirection(direction);

		soldat.offsetX = x;
		soldat.offsetY = y;
	}


	/**
	 *  Retourne la position du premier héros trouvé aux alentours de p. Null si il n'y en as pas 
	 *  @param pos Position du soldat où chercher aux alentours
	 *  @param portee Nombre de cases du rayon où on cherche
	 */
	private Position herosAlentour(Position p, int portee){
		for(int x = p.x - portee; x <= p.x + portee; x++){
			for(int y = p.y - portee; y <= p.y + portee; y++){
				Position pos = new Position(x, y);

				if(pos.estValide()){
					/* Si il y a un héros à la case indiquée */
					if(soldat[pos.getNumCase()] != null && soldat[pos.getNumCase()] instanceof Heros){
						return pos;
					}
				}
			}
		}

		return null;
	}

	/**
	 *  Déplace tous les monstres 
	 */
	public void joueMonstres(){
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i=0; i<monstre.length; i++){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Monstre m = monstre[i];
					if(m != null){
						Position p;
		
						if(m.getPourcentageVie() < 10){
							System.out.println("Repos pour moi car :" + m.getPourcentageVie());
							m.repos(true);
						}
						else if((p = herosAlentour(m.getPosition(), m.getPortee())) != null){
							System.out.println("Je combats");
							m.combat(soldat[p.getNumCase()], p.distance(m.getPosition()));
						}
						else{
							System.out.println("Je me déplace");
							Position nouvelle_position = new Position();
							nouvelle_position.x = m.getPosition().x + Aleatoire.nombreAleatoire(-1, 1);
							nouvelle_position.y = m.getPosition().y + Aleatoire.nombreAleatoire(-1, 1);
							deplaceSoldat(m, nouvelle_position);
						}
					}
				}
			}
		});
		
		t.run();
	}

	/** Genere aléatoirement une carte. */
	public void generer()
	{
		generee = true;
		genererCarte();
		genererSoldats();
		caseActionnee = -1;	

		if(image != null){
			image.getParent().remove(image);
			image = null;
		}
	}

	/** Teste si une case existe sur la Carte.
	 * @param x Coordonnée x.
	 * @param y Coordonnée y.
	 * @return  true si existe, false sinon.
	 */
	public boolean existe(int x, int y)
	{
		return (x >= 0 && y >= 0 && x < IConfig.LARGEUR_CARTE && y < IConfig.HAUTEUR_CARTE);
	}

	/** Trouve une position vide aléatoirement sur la carte. 
	 * Utilisable pour placer des Soldats.
	 * @param type Type de Soldat (Soldat.HOMME ou Soldat.MONSTRE)
	 * @return     La position vide.
	 * */
	public Position trouvePositionVide(char type)
	{	
		int dec = type == Soldat.HEROS ? 1 : 0;
		int num_tile;
		Tile tile;
		Position pos = new Position();

		do {
			pos.x = dec * (IConfig.LARGEUR_CARTE / 2) + dec + (int)(Math.random() * (IConfig.LARGEUR_CARTE / 2));
			pos.y = (int)(Math.random() * IConfig.HAUTEUR_CARTE);


			num_tile = carte[pos.getNumCase()];
			tile = tileset.getTile(num_tile);
		} while(soldat[num_tile] != null || !tile.estPraticable() || !existe(pos.x, pos.y) );

		return pos;
	}

	/** Sauvegarde une carte.
	 * @param num Numéro de la sauvegarde.
	 */
	public void sauvegarde(int num)
	{
		try {
			FileOutputStream fichier = new FileOutputStream(IConfig.CHEMIN_SAUVEGARDE + IConfig.NOM_SAUVEGARDE + num + ".ser");
			ObjectOutputStream oos = new ObjectOutputStream(fichier);

			oos.writeObject(carte);
			oos.writeObject(monstre);
			oos.writeObject(heros);
			oos.writeObject(soldat);

			oos.flush();
			oos.close();
		}
		catch(java.io.IOException e) {
			e.printStackTrace();
		}
	}

	/** Charge une carte.
	 * @param num Numéro de la sauvegarde.
	 */
	void charge(int num)
	{

		if(image != null){
			image.getParent().remove(image);
			image = null;
		}

		try {
			FileInputStream fichier = new FileInputStream(IConfig.CHEMIN_SAUVEGARDE + IConfig.NOM_SAUVEGARDE + num + ".ser");
			ObjectInputStream ois = new ObjectInputStream(fichier);

			carte   = (char[])ois.readObject();
			monstre = (Monstre[])ois.readObject();
			heros   = (Heros[])ois.readObject();
			soldat  = (Soldat[])ois.readObject();

			/* Les images ne sont pas sérializées... */
			for(int i = 0; i < IConfig.NB_HEROS; i++)
			{
				heros[i].setImage();
				heros[i].setDirection(Direction.GAUCHE);
			}

			for(int i = 0; i < IConfig.NB_MONSTRES; i++)
			{
				monstre[i].setImage();
				monstre[i].setDirection(Direction.DROITE);
			}

			chargerTileset(); // Charge uniquement si tileset null.
			generee = true;   // Au cas où aucune partie lancée depuis le lancement de l'application.

			ois.close();
		}
		catch(java.io.IOException e) {
			e.printStackTrace();
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	protected void paintComponent(Graphics g) 
	{    	
		Point dest = new Point();
		Point src;

		if(!generee)
			return;

		/* Affichage de la carte. */
		for(int i = 0; i< IConfig.LARGEUR_CARTE; i++)
			for(int j = 0; j < IConfig.HAUTEUR_CARTE; j++) {
				Position pos = new Position(i, j);
				int num_tile = (int)carte[pos.getNumCase()];

				dest.setLocation(i, j);
				src = tileset.getCoord(num_tile);
				tileset.dessiner(g, src, dest);
			}

		/* Case sélectionnée. */
		if(caseActionnee != -1){
			Position coord = new Position(caseActionnee);
			int dx = coord.x;
			int dy = coord.y;

			if(soldat[caseActionnee].getAJoue())
				soldat[caseActionnee].dessineDeplacement(g, dx, dy, IConfig.SOLDAT_UTILISE);
			else {
				for(int i = -1; i <= 1; i++) {
					for(int j = -1; j <= 1; j++) {
						int dxc = dx + i;
						int dyc = dy + j;
						int caseVoisine = dyc * IConfig.LARGEUR_CARTE + dxc;

						if(existe(dxc, dyc) && tileset.getTile(carte[caseVoisine]).estPraticable() && !(soldat[caseVoisine] instanceof Heros))
							soldat[caseActionnee].dessineDeplacement(g, dxc, dyc, IConfig.SOLDAT_DEPLACEMENT_POSSIBLE);
					}
				}

				soldat[caseActionnee].dessineDeplacement(g, dx, dy, IConfig.SOLDAT_SELECTIONNEE);
			}
		}

		/* Affichage des personnages. */
		for(int i = 0; i < soldat.length; i++)
			if(soldat[i] != null && soldat[i].estVisible())
			{
				Position pos = soldat[i].getPosition();

				soldat[i].dessinerAvecOffset(g, pos.x, pos.y, soldat[i].offsetX, soldat[i].offsetY);
			}

		/* Affichage des barres de vie. */
		for(int i = 0; i < soldat.length; i++)
			if(soldat[i] != null && !soldat[i].estMort())
			{
				Position pos = soldat[i].getPosition();
				soldat[i].dessineVie(g, pos.x, pos.y);
			}

		/* Affichage de l'infobulle si un soldat est pointé */
		if(soldatPointe != null ){ 
			Position pos = soldatPointe.getPosition();
			Infobulle.dessinerText(g, pos.x, pos.y, soldatPointe.toString(), IConfig.MESSAGE_NEUTRE, IConfig.ARRIERE_PLAN);
		}

		/* Auto gestion de l'affichage de la file de message */
		Infobulle.dessiner(g);

		FenetreJeu.historique.setText(Carte.nbMonstresRestant+" Monstres restant - "+Carte.nbHerosRestant+" Heros restant");
	}

	public void actionPerformed(ActionEvent e) 
	{	
		if(generee && nbToPlay == 0)
			reinitAJoue();

		repaint();
	}

	public static void setSoldat(int i, Soldat s) {
		soldat[i] = s;
	}
	
	public boolean isGeneree(){
		return this.generee;
	}
}

