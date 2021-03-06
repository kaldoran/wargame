package wargame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;


/**
 * Classe abstraite représentant la base d'un soldat
 */
public abstract class Soldat extends Charset implements ISoldat
{
	private static final long serialVersionUID = 1L;

	/** Vie d'un soldat. */
	protected int vie;
	
	/** Numéro de la case où se situe le soldat. */
	private Position position;

	/** Est mort ? */
	private boolean mort = false;
	
	/** En train de se deplacer. */
	private boolean seDeplace = false;
	
	/** Le tour est effectué */
	private boolean tourEffectue = false;
	
	/** Offset utilisé pendant le déplacement. */
	protected int offsetX = 0;
	protected int offsetY = 0;

	/** 
	 * Constructeur de base d'un soldat
	 */
	Soldat() {}
	
	/**
	 * Permet de récupérer le nombre de points de vie du soldat
	 * @return Le nombre de points de vie du soldat
	 */
	public int getVie() 
	{
		return vie;
	}
	
	/**
	 * Permet de récupérer le pourcentage des points de vie du soldat
	 * @return Le pourcentage des points de vie du soldat
	 */
	public double getPourcentageVie(){
		return ((double)(vie/(double)this.getVieMax())) * 100.;
	}

	/**
	 * Permet de spécifier un nouveau nombre de points de vie d'un soldat
	 * @param vie Le nombre de points de vie d'un soldat
	 */
	public void setVie(int vie) 
	{
		this.vie = ((vie > this.getVieMax()) ? this.getVieMax() : (vie < 0 ) ? 0 : vie);
	}
	
	/**
	 * Permet de dire si le soldat est en train de se déplacer ou non
	 * @param value Vrai si le soldat est en train de se déplacer, faux sinon
	 */
	public void setSeDeplace(boolean value)
	{
		this.seDeplace = value;
	}
	
	/**
	 * Permet de savoir si le soldat est en train de se déplacer
	 * @return Vrai si le soldat est en train de se déplacer, faux sinon
	 */
	public boolean getSeDeplace()
	{
		return this.seDeplace;
	}
	
	/** Mettre le statut du personnage à mort.
	 * @param mort Vrai si le personnage est mort, faux sinon
	 */
	public void setMort(boolean mort) {
		if(mort){
			this.mort = true;
			this.direction = Direction.HAUT;
			this.animation = 0;
		    this.timer.setDelay(350);
		}
		else{
			this.mort = false;
		}
	}
	
	/** Teste si le personnage est mort. 
	 * @return true si mort, false sinon.
	 */
	public boolean estMort() {
		return mort;
	}

	/**
	 * Teste si le soldat a déjà joué
	 * @return Vrai si il a déjà joué, faux sinon
	 */
	public boolean getAJoue() {
		return tourEffectue;
	}
	
	/**
	 * Permet de dire si le soldat a joué ou non
	 * @param value Vrai si le soldat a déjà joué, faux sinon
	 */
	public void setAJoue(boolean value) {
		tourEffectue = value;
	}
	
	/**
	 * Permet de récupérer la position courante du soldat
	 * @return La position courante du soldat
	 */
	public Position getPosition() {
		return position;
	}

	/** 
	 * Permet de changer la position courante du soldat
	 * @param position La nouvelle position du soldat
	 */
	public void setPosition(Position position) {
		this.position = position;
	}
	
    
	/**
	 * Fonction permettant de créer un combat entre 2 soldats
	 * @param soldat Instance du soldat à attaquer
	 * @param distance Distance séparant soldat1 de soldat2
	 * @return -1 si le soldat qui attaque est mort, 1 si le soldat attaqué est mort, 0 sinon
	 */
	public int combat(Soldat soldat, int distance)
	{	
		int valeur_retour = 0;
		
		/* On joue le bruitage approprié */
		if(distance > 1)
			Son.joueArc();
		else
			Son.joueEpee();
		
		/* On calcule un dégat aléatoire, selon si on est côté à côté ou éloigné du soldat attaque */
		int degat = (distance == 1) ? Aleatoire.nombreAleatoire(1, this.getPuissance()) : this.getTir();
		int numCase = soldat.position.getNumCase();
		
		Infobulle.newMessage(numCase, "-" + degat, IConfig.MESSAGE_NEGATIF, IConfig.MOUV_INFOBULLE_BAS, 30);
				
		/* On met à jour la nouvelle vie du soldat attaqué */
		int vie = soldat.getVie() - degat;
		soldat.setVie(vie);

		/* S'il lui reste encore de la vie */
		if(vie > 0) {
			/* Et si le soldat a une portée assez grande pour répliquer */
			if(soldat.getPortee() >= distance) {
				/* Alors une réplique est crée */
				degat = (distance == 1) ? soldat.getPuissance() / IConfig.COEFFICIENT_REDUC : soldat.getTir() / IConfig.COEFFICIENT_REDUC;
				
				vie = this.getVie() - degat;
				numCase = this.position.getNumCase();
				
				Infobulle.newMessage(numCase, "-" + degat, IConfig.MESSAGE_NEGATIF, IConfig.MOUV_INFOBULLE_BAS, 0);
				this.setVie(vie);
				
				/* Si la réplique est fatale, le soldat qui a attaqué meurt */
				if(this.getVie() <= 0) {
					Son.joueMourir(this);
					this.setMort(true);
					valeur_retour = -1;
				}				
			}
		}
		else {
			Son.joueMourir(soldat);
			soldat.setMort(true);	
			valeur_retour = 1;
		}
		
		this.setAJoue(true);
		
		return(valeur_retour);
	}
	
	/**
	 * Méthode permettant de mettre en repos le soldat
	 * @param afficher_message Si vrai, un message sera affiché
	 * @return -1 si vie au max, >= 0 pour la vie récupérée
	 */
	public int repos(boolean afficher_message) {

		int regain = Aleatoire.nombreAleatoire(0, IConfig.REPOS_MAX);
		int case_courante = this.getPosition().getNumCase();
		
		/* Si la vie du soldat est déjà au max on considere pas qu'il a joué. Cependant on lui met un message */
		if(vie == this.getVieMax()) {
			
			if(!afficher_message)
				return -1;
			
			Infobulle.newMessage(case_courante, "Vie au max", IConfig.MESSAGE_NEUTRE, 2, 0);
			return -1;
		}

		/* Définition du message et de sa couleur */
		Infobulle.newMessage(case_courante, "+ " + regain, IConfig.MESSAGE_POSITIF, IConfig.MOUV_INFOBULLE_HAUT, 0);

		/* On met a jour sa vie et on indique qu'il a joué */
		this.setVie(vie + regain);
		this.setAJoue(true);
		
		return regain;
	}

	/** Dessine la barre de vie du Héros.
	 * @param g : Zone de dessin. 
	 */
	protected void dessineVie(Graphics g)
	{
		Color color;
		int pourcentage_vie = (int) getPourcentageVie();
		
		if(pourcentage_vie >= 70)
			color = Color.green;
		else if(pourcentage_vie >= 40)
			color = Color.orange;
		else
			color = Color.red;
		
		int dx = this.position.x * IConfig.NB_PIX_CASE + IConfig.NB_PIX_CASE;
		int dy = this.position.y * IConfig.NB_PIX_CASE + 2;
		
		/* Contenant. */
		g.setColor(Color.black);
		g.drawRect(dx + offsetX, dy + offsetY, 4, IConfig.NB_PIX_CASE - 2);
		
		/* Contenu. */
		int offset = (int)(IConfig.NB_PIX_CASE * vie / (double)this.getVieMax());
		g.setColor(color);
		g.fillRect(dx + 1 + offsetX , dy + 1 + offsetY + IConfig.NB_PIX_CASE - offset, 3, offset - 3);

	}
	
	/**
	 * Caractéristiques d'un soldat
	 * @return La chaine formatée comprenant les caractéristiques du soldat
	 */
	public String toString(){
		String chaine = this.getNom() + " " + getPosition().toString();
		chaine += "\nVie: " + this.vie + " /" + this.getVieMax();
		chaine += "\nPuissance: " + this.getPuissance();
		chaine += "\nTir: " + this.getTir();
		chaine += "\nPortee: " + this.getPortee();
		
		return chaine;
	}
	
	/** Met à jour le statut du soldat.
	 *  @param e Évènement appellant, le timer.
	 */
    public void actionPerformed(ActionEvent e)
    {    
    	/* Mise à jour du déplacement. */
    	if(seDeplace) {
    		if(offsetX > 0)      
    			offsetX += IConfig.VITESSE_DEPLACEMENT;
    		else if(offsetX < 0) 
    			offsetX -= IConfig.VITESSE_DEPLACEMENT;
    		
    		if(offsetY > 0)      
    			offsetY += IConfig.VITESSE_DEPLACEMENT;
    		else if(offsetY < 0) 
    			offsetY -= IConfig.VITESSE_DEPLACEMENT;
    		
    		if(Math.abs((int)offsetX) >= IConfig.NB_PIX_CASE || Math.abs((int)offsetY) >= IConfig.NB_PIX_CASE)
    		{
    			/* Mise à jour de la nouvelle position. */
    			int x = position.getNumCase() % IConfig.LARGEUR_CARTE;
    			int y = position.getNumCase() / IConfig.LARGEUR_CARTE;

    			if(offsetX > 0)
    				x++;
    			else if(offsetX < 0)
    				x--;

    			if(offsetY > 0)
    				y++;
    			else if(offsetY < 0)
    				y--;
    			
    			position.x = x;
    			position.y = y;
    			
    			/* Remise à zéro du déplacement. */
    			offsetX = offsetY = 0;
    			seDeplace = false;
    			animation = 0;
    		}
    	}
    	
    	/* Mise à jour de l'animation. */
        if(estVisible) {
    		if(this.mort){
    			/* On fait tourner le perso */
    			direction = direction.directionSuivante();
    			
    			if(direction == Direction.HAUT)
    				estVisible = false;
    		}
    		if(seDeplace){
    			if(++animation >= N_ANIMATIONS)
    				animation = 0;
    		}
        }
	}
}
