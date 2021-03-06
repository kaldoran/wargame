package wargame;

import java.util.LinkedList;

import javax.swing.JLabel;

/**
 * Classe permettant de gérer un historique
 */
public class Historique extends JLabel implements IConfig
{
	private static final long serialVersionUID = 7045910608318694418L;

	/** Liste des messages de l'historique */
	private LinkedList<HistoriqueMessage> fileHistorique;
	/** Nombre de messages dans la liste */
	private int nbMessage;
	/** Numéro du message actuellement affiché */
	private int numMessage;
	/** Numéro du message d'historique (ou numéro de l'action du joueur) */
	private int numHistorique;
	
	/**
	 * Constructeur par défaut
	 */
	public Historique(){
		super();
		fileHistorique = new LinkedList<HistoriqueMessage>();
		nbMessage = numMessage = numHistorique = 0;
	}
	
	/**
	 * Constructeur permettant d'ajouter un texte de présentation à l'historique et de préciser son alignement horizontal
	 * @param texte Le texte
	 * @param alignement_horizontal Constante d'alignement
	 */
	public Historique(String texte, int alignement_horizontal){
		super(texte, alignement_horizontal);
		fileHistorique = new LinkedList<HistoriqueMessage>();
		nbMessage = numMessage = numHistorique = 0;
	}


	/**
	 * Classe privée permettant d'instancier un message
	 */
	private class HistoriqueMessage{
		private String message;
		private int numero;

		/** Constructeur par défaut, avec comme paramètre le texte du message */
		HistoriqueMessage(String msg) {
			this.message = msg;
			this.numero = numHistorique;
		}	
		
		/**
		 * Méthode permettant d'afficher un message
		 */
		public String toString(){
			return("[" + this.numero + "] " + this.message);
		}
	}

	/**
	 * Méthode permettant d'ajouter un message à l'historique
	 * @param message Le message à ajouter
	 */
	public void addMessage(String message) {
		if(this.getTailleHistorique() > IConfig.TAILLE_MAX_HISTORIQUE){
			fileHistorique.removeFirst();
			nbMessage--;
		}
		
		HistoriqueMessage m = new HistoriqueMessage(message);
		numMessage = nbMessage++;
		numHistorique++;
		
		fileHistorique.add(m);
		this.setText(m.toString());
	}

	/**
	 * Méthode permettant de récupérer le message numéro x
	 * @param x Le numéro du message à récupérer
	 * @return Le texte du message
	 */
	public String getMessage(int x) {
		return fileHistorique.get(x).toString();
	}
	
	/**
	 * Méthode permettant d'afficher le message numéro x
	 * @param x Le numéro du message
	 */
	public void afficherMessage(int x) {
		this.setText(this.getMessage(x).toString());
		numMessage = x;
	}
	
	/**
	 * Méthode permettant de récupérer le premier message de l'historique
	 * @return Le texte du premier message de l'historique
	 * @throws Exception Levée si la file est vide ! (On ne peut pas afficher le dernier d'une file vide)
	 */
	public String getPremier() throws Exception {
		if(fileHistorique.isEmpty())
			throw new Exception();
		
		return fileHistorique.getFirst().toString();
	}
	
	/**
	 * Méthode permettant d'afficher le premier message de l'historique
	 */
	public void afficherPremier(){
		if(fileHistorique.isEmpty())
			return;
		
		try {
			this.setText(this.getPremier());
		} catch (Exception e) {
			e.printStackTrace();
		}
		numMessage = 0;
	}
	
	/**
	 * Méthode permettant de récupérer le dernier message de l'historique
	 * @return Le dernier message de l'historique
	 * @throws Exception Levée si la file est vide ! (On ne peut pas afficher le dernier d'une file vide)
	 */
	public String getDernier() throws Exception {
		if(fileHistorique.isEmpty())
			throw new Exception();
		
		return (fileHistorique.getLast().toString());
	}
	
	/**
	 * Méthode permettant d'afficher le dernier message de l'historique
	 */
	public void afficherDernier() {
		if(fileHistorique.isEmpty())
			return;
		
		try {
			this.setText(this.getDernier());
		} catch (Exception e) {
			e.printStackTrace();
		}
		numMessage = this.getTailleHistorique();
	}
	
	/**
	 * Méthode permettant d'afficher le message précédent 
	 */
	public void afficherMessagePrecedent() {
		if(numMessage > 0){
			numMessage--;
			this.afficherMessage(numMessage);
		}
	}
	
	/**
	 * Méthode permettant d'afficher le message suivant
	 */
	public void afficherMessageSuivant(){
		if(numMessage < nbMessage - 1){
			numMessage++;
			this.afficherMessage(numMessage);
		}
	}
	
	/**
	 * Méthode permettant de récupérer la taille de l'historique
	 * @return La taille de l'historique
	 */
	public int getTailleHistorique() {
		return nbMessage;
	}	
	
	/**
	 * Méthode permettant de remettre à zéro l'historique
	 */
	public void reset(){
		nbMessage = numMessage = numHistorique = 0;
		fileHistorique.clear();
		this.setText(""); 
	}
}
