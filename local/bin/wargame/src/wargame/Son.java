package wargame;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;


/**
 * Classe permettant de jouer des sons simples correspondant à différentes actions
 */
public final class Son {
	/* Pour charger un fichier midi */
	private Sequence sequence;
	/* Pour charger un lecteur */
	private Sequencer sequencer; 
	/* Pour savoir si le son en arrière est stoppé ou non */
	private boolean sonArriereActive = false;

	/**
	 * Constructeur permettant d'instancier l'objet qui manipulera le son d'arrière plan
	 * @throws InvalidMidiDataException Si problème avec le fichier midi
	 * @throws IOException Erreur d'entrée pour le fichier midi
	 * @throws MidiUnavailableException Si le fichier midi est indisponible
	*/
	public Son() throws MidiUnavailableException, InvalidMidiDataException, IOException {
		sequencer = MidiSystem.getSequencer();
		sequencer.open();
		
	    chargeSonArriere();
	    
	    /* Ajout d'un évènement pour être prévenu lorsque la musique est terminée */
	    sequencer.addMetaEventListener(new MetaEventListener() {
			public void meta(MetaMessage message) {
				/* Si la musique est terminée */
				if(message.getType() == 47){
					/* On stoppe le sequencer puis on le ferme, et on charge puis joue une autre musique */
					stopSonArriere();
					
					chargeSonArriere();
					
					try {
						joueSonArriere();
					} catch (MidiUnavailableException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
	}
	
	/**
	 * Méthode permettant de jouer le son en arrière plan
	 * @throws MidiUnavailableException
	 */
	public void joueSonArriere() throws MidiUnavailableException{   
	    sequencer.start();
	    sonArriereActive = true;
	}
	
	/** 
	 * Méthode permettant de stopper le son d'arrière plan
	 */
	public void stopSonArriere(){
		sequencer.stop();
		sonArriereActive = false;
	}
	
	
	/**
	 * Méthode permettant de savoir si le son est actuellement joué ou non
	 * @return booléen a vrai si le son est effectivement en train d'être joué, false sinon
	 */
	public boolean getSonArriereActive(){
		return sonArriereActive;
	}
	
	/**
	 * Méthode permettant de charger un son arrière aléatoire parmi les musiques du dossier
	 */
	public void chargeSonArriere(){
		int num = 1 + (int)(Math.random() * (IConfig.NOMBRE_MUSIQUE_ARRIERE_PLAN));
		try {
			/* On charge une autre séquence */
			sequence = MidiSystem.getSequence(new File(IConfig.CHEMIN_MUSIQUE + "arriere_plan" + num + ".mid"));
	
			try {
				sequencer.setSequence(sequence);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			}
		} catch (InvalidMidiDataException | IOException e) {
			e.printStackTrace();
		}	
	}
	
	
	/* Méthode statique privée permettant de jouer un son wav */
	private static void joueWav(String son){
		AudioClip ac = null;
		try {
			ac = Applet.newAudioClip(new URL("file:" + IConfig.CHEMIN_MUSIQUE + son));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		ac.play();
	}
	
	/**
	 * Méthode statique permettant de jouer le son d'un arc
	 */
	public static void joueArc(){
		joueWav("arc.wav");
	}
	
	/** 
	 * Méthode statique permettant de jouer le son d'une épée
	 */
	public static void joueEpee(){
		joueWav("epee.wav");
	}	
	
	/**
	 * Méthode statique permettant de jouer le son d'un personnage courant sur l'herbe
	 */
	public static void joueCourir(){
		joueWav("courir.wav");
	}
	
	/**
	 * Méthode statique permettant de jouer le son d'un personnage marchant sur l'herbe
	 */
	public static void joueMarcher(){
		joueWav("marcher.wav");
	}
	
	
}
