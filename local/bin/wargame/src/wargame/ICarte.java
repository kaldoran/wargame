package wargame;

import java.awt.Point;

/**
 * Interface comprenant les méthodes et attributs d'une classe carte
 */
public interface ICarte {
 //	Element getElement(Position pos);
/*
	Heros trouveHeros(); // Trouve aléatoirement un héros sur la carte
	Heros trouveHeros(Position pos); // Trouve un héros choisi aléatoirement parmi les 8 positions adjacentes de pos

	void mort(Soldat perso);
	boolean actionHeros(Position pos, Position pos2);
//	void jouerSoldats(PanneauJeu pj);	
	*/
	
	/** Déplace un soldat sur la carte.
	 * @param soldat    Soldat à deplacer.
	 * @param direction Direction du soldat.
	 * @param x         Offset X d'origine.
	 * @param y         Offset Y d'origine.
	 */
	void deplaceSoldat(Soldat soldat, char direction, int x, int y);

	/** Genere aléatoirement une carte. */
	void generer();
	
	/** Teste l'existence d'une case sur la carte.
	 * 
	 * @param x Coordonnée en X d'une case.
	 * @param y Coordonnée en Y d'une case.
	 * @return  true si la case existe, false sinon.
	 */
	boolean existe(int x, int y);
	
	/** Trouve une position vide aléatoirement sur la carte. 
	 * Utilisable pour placer des Soldats.
	 * @param type Type de Soldat (Soldat.HOMME ou Soldat.MONSTRE)
	 * @return     La position vide.
	 * */
	Point trouvePositionVide(char type);
	
	/** Sauvegarde une carte.
	 * @param num Numéro de la sauvegarde.
	 */
	void sauvegarde(int num);
	
	/** Charge une carte.
	 * @param num Numéro de la sauvegarde.
	 */
	void charge(int num);
}