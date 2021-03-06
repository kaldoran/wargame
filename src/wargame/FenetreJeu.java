package wargame;

import java.awt.Color;
import java.awt.Dimension;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe permettant de dessiner la fenêtre du jeu (avec ses menus...)
 */
public class FenetreJeu extends JFrame
{
	private static final long serialVersionUID = 7794325642011100784L;

	/** Carte du jeu. */
    private Carte carte;
	
	/* Barre d'état située en bas de la fenêtre */
	private JPanel barreEtat;
    private JSeparator separateurHaut, separateurBas;
    
    /* Historique et information de la barre d'état */
    private Historique historique;
    private JLabel information;
    
	/* Menu */
	private JMenuBar menu;
	private JMenu jeu;
	private JMenu sauvegarder;
	private JMenu charger;
	private JMenu config;
		
	/* Options des menus */
	/** Nouvelle partie. */
	private JMenuItem nouveau;
	/** Quitter. */
	private JMenuItem quitter;
	/** Liste des sauvegardes. */
	private JMenuItem []sauvegarde;
	/** Liste des slots de chargement. */
	private JMenuItem []slot;
	/** Activer / Désactiver son */
	private JMenuItem son;
	/** Activer / Désactiver le brouillard */
	private JMenuItem brouillard;
	
	/* Sous-menu (avec icones) */
	private JPanel sousMenu;
	
	/* Icones de ce sous-menu */
	private JButton boutonCharger;
	private JButton boutonSauvegarder;
	private JButton navigerHistoriquePremier;
	private JButton navigerHistoriquePrecedent;
	private JButton navigerHistoriqueSuivant;
	private JButton navigerHistoriqueDernier;
	private JButton exit;
	
	/** Bouton de fin de tour */
	private JButton finTour;
	
	/** Boolean qui considere un seul scroll a la souris sur 2 */
	private boolean scroll = false;

    /** Compteur servant à l'initialisation des évènements de sauvegardes. */
	private static int k = 0;
	
	/* Navigation au clavier */
	/** Numéro de la case du héros actionné */
	int numHeros = 0;
	/** 
	 * Tableau contenant les touches actionnées. Dans l'ordre : HAUT, BAS, GAUCHE, DROITE
	 */
	boolean[] tabKey = {false, false, false, false};
	
	/* Timer */
	/** 
	 * Boolean qui indique si le timer est activé ou non
	 */
	boolean timerOn = false;
	/** Timer. */
	Timer timer = new Timer(IConfig.DELAI_TOUCHE, new ActionListener() {
		public void actionPerformed (ActionEvent event) {
			if(timerOn) {
				carte.changePos(tabKey);
				timerOn = false;
			}
		}
	});
	
	/** Objet Son servant à gérer le son d'arrière plan */
	private Son sonArriere;
	
	/** Code Konami, menu triche */
	private int[] touchesKonami = { KeyEvent.VK_UP, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_DOWN, 
		       KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, 
		       KeyEvent.VK_B, KeyEvent.VK_A, KeyEvent.VK_ENTER };
	
	
	private int caseActuelle = 0;
	private boolean konami = false;
	
	private JMenu menuTriche;
	private JMenuItem itemGagner;
	private JMenuItem itemPerdre;
	private JMenuItem itemArmagedon;
	private JMenuItem itemAjoutMonstre;
	private JMenuItem itemAjoutHeros;
	private JMenuItem itemMortSubite;
	
	/**
	 * Méthode privée permettant de récupérer la date de la dernière modification d'un fichier
	 * @param f Le fichier
	 * @return La date de la dernière modification
	 */
    private String getDate(File f)
    {
		return new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format( new Date(f.lastModified()));
    }
	
    /**
     * Met en route le timer
     */
	private void timer() {
		if(timerOn)
			return;
		
		timer.setDelay(IConfig.DELAI_TOUCHE);	
		timer.start();
		timerOn = true;
	}
	
	/**
	 * Constructeur de la fenetre de jeu
	 * @throws InvalidMidiDataException Son midi invalide
	 * @throws IOException Problème avec les fichiers 
	 * @throws MidiUnavailableException Son midi indispoible
	 */
	public FenetreJeu() throws InvalidMidiDataException, IOException, MidiUnavailableException
	{
		/* Création du titre et de l'icone */
		this.setTitle("Wargame");
        this.setIconImage(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "icone.png")).getImage());
        
		/* Création d'une carte vide. */
		carte = new Carte();
		carte.setPreferredSize(new Dimension(IConfig.LARGEUR_CARTE * IConfig.NB_PIX_CASE, 
				 IConfig.HAUTEUR_CARTE * IConfig.NB_PIX_CASE));
		
		/* Création des menus principaux. */
		menu        = new JMenuBar();
		jeu         = new JMenu("Jeu");
		sauvegarder = new JMenu("Sauvegarder");
		charger     = new JMenu("Charger");
		config		= new JMenu("Configuration");
		finTour 	= new JButton("Fin de tour");

		/* Création des options des menus. */
		nouveau = new JMenuItem("Nouvelle partie");
		quitter = new JMenuItem("Quitter");
		
		son = new JMenuItem("Désactiver le son");
		brouillard = new JMenuItem("Désactiver le brouillard");
		
		sauvegarde = new JMenuItem[IConfig.NB_SAUVEGARDES];
		slot = new JMenuItem[IConfig.NB_SAUVEGARDES];

		for(int i = 0; i < IConfig.NB_SAUVEGARDES; i++)
		{
			sauvegarde[i] = new JMenuItem("Sauvegarde " + i);
			slot[i] = new JMenuItem("Sauvegarde " + i);
		}
	    
		/* Initialisation des menus. */
	    jeu.add(nouveau);
	    jeu.addSeparator();
	    jeu.add(quitter);

	    for(k = 0; k < IConfig.NB_SAUVEGARDES; k++)
	    {
	    	sauvegarder.add(sauvegarde[k]);
	    	sauvegarder.setEnabled(false);
	    	charger.add(slot[k]);
	    	
	    	sauvegarde[k].addActionListener(new ActionListener() {
	    		private final int NUM = k;
		    	public void actionPerformed(ActionEvent arg0) 
		    	{
					carte.sauvegarde(IConfig.CHEMIN_SAUVEGARDE + IConfig.NOM_SAUVEGARDE + NUM + ".ser");
		    	}       
		    });
	    	
	    	slot[k].addActionListener(new ActionListener() {
	    		private final int NUM = k;
		    	public void actionPerformed(ActionEvent arg0) 
		    	{
					carte.charge(IConfig.CHEMIN_SAUVEGARDE + IConfig.NOM_SAUVEGARDE + NUM + ".ser");

				    menu.add(Box.createHorizontalGlue()); 
				    menu.add(finTour);
				    setJMenuBar(menu);
		    	}       
		    });
	    }
	    
	    config.add(son);
	    config.add(brouillard);
	    
	    /* Ajout des menus dans la barre de menus. */
	    menu.add(jeu);
	    menu.add(sauvegarder);
	    menu.add(charger);
	    menu.add(config);
	    
	    this.setJMenuBar(menu);
	    this.setVisible(true);
	    
	    /* Actions des menus. */
	    
	    /* Quitter. */
	    quitter.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) 
	    	{
	    		System.exit(0);
	    	}       
	    });
	    quitter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
	    
	    /* Nouvelle partie. */
	    nouveau.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{ 
				if(carte.isGeneree()){
					sonArriere.stopSonArriere();
					try {
						sonArriere.chargeSonArriere();
					} catch (InvalidMidiDataException e1) {
						e1.printStackTrace();
					}
					try {
						sonArriere.joueSonArriere();
					} catch (MidiUnavailableException e1) {
						e1.printStackTrace();
					}
				}
				
				carte.generer();	
				
				historique.reset();
				sauvegarder.setEnabled(true);
				
			    menu.add(Box.createHorizontalGlue()); 
			    finTour.setPreferredSize(new Dimension(150,10));
			    menu.add(finTour);
			    setJMenuBar(menu);
			}
		});
	    nouveau.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
	    
	    /* Activation désactivation son */
	    son.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{ 
				if(sonArriere.getSonArriereActive()){
					sonArriere.stopSonArriere();
					Son.sonBruitageActive = false;
					son.setText("Activer le son");
				}
				else{
					try {
						Son.sonBruitageActive = true;
						sonArriere.joueSonArriere();
					} catch (MidiUnavailableException e1) {
						e1.printStackTrace();
					}
					son.setText("Désactiver le son");
				}
			}
		});
	    son.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
	   
	    brouillard.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{ 
					if(carte.getBrouillardActive()) {
						carte.setBrouillardActive(false);
						brouillard.setText("Activer le brouillard");
					}
					else {
						carte.setBrouillardActive(true);
						brouillard.setText("Désactiver le brouillard");
					}
						
			}
		});
	    brouillard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
	    
	    /* Sauvegarder */
	    sauvegarder.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) 
			{
				File dir = new File(IConfig.CHEMIN_SAUVEGARDE);
				if(!dir.exists())
					dir.mkdir();
					
				for(int i = 0; i < IConfig.NB_SAUVEGARDES; i++)
				{
					File f = new File(IConfig.CHEMIN_SAUVEGARDE + IConfig.NOM_SAUVEGARDE + i + ".ser");
					
					if(f.exists())
						sauvegarde[i].setText(getDate(f));					
				}
			}

			public void menuDeselected(MenuEvent e) {}
			public void menuCanceled(MenuEvent e) {}
		});
	    	
	    /* Charger */
	    charger.addMenuListener(new MenuListener() {
			public void menuSelected(MenuEvent e) 
			{
				finTour.setPreferredSize(new Dimension(150,10));
				for(int i = 0; i < IConfig.NB_SAUVEGARDES; i++)
				{
					File f = new File(IConfig.CHEMIN_SAUVEGARDE + IConfig.NOM_SAUVEGARDE + i + ".ser");
		    	
					if(f.exists())
					{
						slot[i].setText(getDate(f));
						slot[i].setEnabled(true);
					}
					else
						slot[i].setEnabled(false);
				}
			}
	
			public void menuDeselected(MenuEvent e) {}
			public void menuCanceled(MenuEvent e) {}
			
		});
	    
	    finTour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{ 
				if(carte.isGeneree()){
					carte.reinitAJoue();
					requestFocus();
				}
			}
		});
	    
	    
	    /* Création du sous menu */
	    sousMenu = new JPanel();
	    sousMenu.setPreferredSize(new Dimension(carte.getWidth(), 35));
	    
	    boutonCharger = new JButton(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "load.png")));
	    boutonCharger.setToolTipText("Charger"); 
	    
		boutonSauvegarder = new JButton(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "save.png")));
		boutonSauvegarder.setToolTipText("Sauvegarder"); 
		
		navigerHistoriquePremier = new JButton(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "first.png")));
		navigerHistoriquePremier.setToolTipText("Revenir au premier message de l'historique"); 
		
		navigerHistoriquePrecedent = new JButton(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "previous.png")));
		navigerHistoriquePrecedent.setToolTipText("Message précédent de l'historique"); 
		
		navigerHistoriqueSuivant = new JButton(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "next.png")));
		navigerHistoriqueSuivant.setToolTipText("Message suivant de l'historique"); 
		
		navigerHistoriqueDernier = new JButton(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "last.png")));
		navigerHistoriqueDernier.setToolTipText("Dernier message de l'historique"); 
		
		exit = new JButton(new ImageIcon(this.getClass().getResource(IConfig.CHEMIN_IMAGE + "exit.png")));
		exit.setToolTipText("Quitter le jeu"); 
		 
		/* Pour éviter les répétitions */
		JButton liste_boutons[] = {boutonCharger, boutonSauvegarder, navigerHistoriquePremier, navigerHistoriquePrecedent, navigerHistoriqueSuivant, navigerHistoriqueDernier, exit};
		for(JButton bouton : liste_boutons){
			bouton.setBackground(Color.LIGHT_GRAY);
			bouton.setOpaque(true);
			bouton.setPreferredSize(new Dimension(30,30));
			sousMenu.add(bouton);
		}
	    
		/* Événements du sous menu */		
	    navigerHistoriquePremier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				historique.afficherPremier();
				requestFocus();
			}
		});
	    
	    navigerHistoriquePrecedent.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				historique.afficherMessagePrecedent();
				requestFocus();
			}
		});
	    
	    navigerHistoriqueSuivant.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				historique.afficherMessageSuivant();
				requestFocus();
			}
		});
	    
	    navigerHistoriqueDernier.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				historique.afficherDernier();
				requestFocus();
			}
		});
	    
	    boutonCharger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 	{ 
				JFileChooser fichier = new JFileChooser();
		        fichier.setDialogTitle("Ouvrir fichier");
		        fichier.setCurrentDirectory(new File("."));
		        fichier.setFileFilter(new FileNameExtensionFilter("Sauvegarde wargame (*.ser)", "ser"));

		        int choix = fichier.showOpenDialog(carte);
                if (choix != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(carte, "Erreur : le fichier n'est pas conforme", "Erreur, fichier incorrect", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
               	File fichier_choisi = fichier.getSelectedFile();

                if(fichier_choisi.getPath().endsWith(".ser") == false) {
                    JOptionPane.showMessageDialog(carte, "Erreur : le fichier n'est pas conforme", "Erreur, fichier incorrect", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                	                
                carte.charge(fichier_choisi.getPath());
				requestFocus();
			}
		});
	    
	    boutonSauvegarder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				if(!carte.isGeneree())
					return;
				
			    JFileChooser fichier = new JFileChooser();
			    fichier.setDialogTitle("Sauvegarder fichier");
			    fichier.setCurrentDirectory(new File("."));
			    fichier.setFileFilter(new FileNameExtensionFilter("Sauvegarde wargame (*.ser)", "ser"));

			    int choix = fichier.showOpenDialog(carte);
			    if (choix != JFileChooser.APPROVE_OPTION)
			    	return;
	                
			    File fichier_choisi = fichier.getSelectedFile();

			    if(fichier_choisi.getPath().endsWith(".ser") == false)
			    	fichier_choisi = new File(fichier_choisi + ".ser");
	                
			    if (fichier_choisi.exists()){
			    	choix = JOptionPane.showConfirmDialog(carte, "Le fichier " + fichier_choisi + " existe déjà\nVoulez-vous vraiment l'écraser ?", "Fichier déjà existant", JOptionPane.YES_NO_OPTION);
			    	if (choix == JOptionPane.NO_OPTION)  return;
			    }
	                
			    carte.sauvegarde(fichier_choisi.getPath());
				requestFocus();
			}
		});
	    
	    exit.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		System.exit(0);
	    	}       
	    });

        barreEtat = new JPanel();
        
        information = new JLabel("Pour commencer, créez une nouvelle partie ou chargez en une.", JLabel.LEFT);
        historique = new Historique("Ici s'affichera l'historique des actions.", JLabel.RIGHT);

        historique.addMouseListener( new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				String s = "";
				for(int i = 0; i < historique.getTailleHistorique(); i++)
					s += historique.getMessage(i) + "\n";
				carte.setAffichageHistorique(s);
			}
			public void mouseExited(MouseEvent e) {
				carte.setAffichageHistorique("");
			}
        });
        
        barreEtat.setSize(new Dimension(carte.getWidth(), 16));
        barreEtat.setLayout(new BoxLayout(barreEtat, BoxLayout.X_AXIS));
        
        barreEtat.add(information);
        barreEtat.add(Box.createHorizontalGlue());
        barreEtat.add(historique);

        menuTriche = new JMenu("Triche");
		
		itemGagner = new JMenuItem("Gagner la partie");
		itemPerdre = new JMenuItem("Perdre la partie");
		itemArmagedon = new JMenuItem("Armagedon : Off");
		itemAjoutHeros = new JMenuItem("Ajout Heros : Off");
		itemAjoutMonstre = new JMenuItem("Ajout Monstre : Off");
		itemMortSubite = new JMenuItem("Mort Subite");
			
		itemGagner.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(carte.isGeneree())
	    			carte.joueurGagne();
	    	}       
	    });

	    itemPerdre.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(carte.isGeneree())
	    			carte.joueurPerd();
	    	}       
	    });
	    
	    itemArmagedon.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(carte.isGeneree()) {
	    			if(carte.getAjoutHeros()) itemAjoutHeros.doClick();
	    			if(carte.getAjoutMonstre()) itemAjoutMonstre.doClick();
	    			
	    			itemArmagedon.setText("Armagedon : " + ((carte.getArmagedon()) ? "Off" : "On") );
	    			carte.setArmagedon(!carte.getArmagedon());
	    		}
	    	}       
	    });
	    
	    itemAjoutHeros.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(carte.isGeneree()) {
	    			if(carte.getArmagedon()) itemArmagedon.doClick();
	    			if(carte.getAjoutMonstre()) itemAjoutMonstre.doClick();
	    			
	    			itemAjoutHeros.setText("Ajout Heros : " + ((carte.getAjoutHeros()) ? "Off" : "On") );
	    			carte.setAjoutHeros(!carte.getAjoutHeros());
	    		}
	    	}       
	    });
	    
	    itemAjoutMonstre.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(carte.isGeneree()) {
	    			if(carte.getArmagedon()) itemArmagedon.doClick();
	    			if(carte.getAjoutHeros()) itemAjoutHeros.doClick();
	    			
	    			itemAjoutMonstre.setText("Ajout Monstre : " + ((carte.getAjoutMonstre()) ? "Off" : "On") );
	    			carte.setAjoutMonstre(!carte.getAjoutMonstre());
	    		}
	    	}       
	    });
	    
		itemMortSubite.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		if(carte.isGeneree())
	    			carte.mortSubite();
	    	}       
	    });
		
		menuTriche.add(itemGagner);
		menuTriche.add(itemPerdre);
		menuTriche.addSeparator();
		menuTriche.add(itemArmagedon);
		menuTriche.addSeparator();
		menuTriche.add(itemAjoutHeros);
		menuTriche.add(itemAjoutMonstre);
		menuTriche.addSeparator();
		menuTriche.add(itemMortSubite);
		
		menuTriche.setVisible(konami);
        
		menu.add(menuTriche);
		
	    /** Capture des actions au clavier */
		/* Aucun evenement n'est recu par le listener.
		 * car le sous systeme utilise les consideres comme des touches de déplacement dans la fenetre.
		 */
		setFocusTraversalKeysEnabled(false);
	    
		addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
		    	int touche = e.getKeyCode();
		    	
		    	verifieKonami(touche);
		    	if(touche == KeyEvent.VK_F1)
		    		boutonCharger.doClick();
		    	else if(touche == KeyEvent.VK_F2)
		    		boutonSauvegarder.doClick();
		    	else if(touche == KeyEvent.VK_F3)
		    		navigerHistoriquePremier.doClick();
		    	else if(touche == KeyEvent.VK_F4)
		    		navigerHistoriquePrecedent.doClick();
		    	else if(touche == KeyEvent.VK_F5)
		    		navigerHistoriqueSuivant.doClick();
		    	else if(touche == KeyEvent.VK_F6)
		    		navigerHistoriqueDernier.doClick();
		    	else if(touche == KeyEvent.VK_F7)
		    		exit.doClick();
		    	
		    	if(!carte.isGeneree())
		    		return;
		    	
		    	if (touche == KeyEvent.VK_TAB) { 
		    		numHeros = carte.trouverProchainHeros(numHeros);
		    	}
		    	else if(touche == KeyEvent.VK_UP) {
		    		timer();
		    		tabKey[0] = true;
		    	}
		    	else if(touche == KeyEvent.VK_DOWN) {
		    		timer();
		    		tabKey[1] = true;
		    	}
		    	else if(touche == KeyEvent.VK_LEFT) {
		    		timer();
		    		tabKey[2] = true;
		    	}
		    	else if(touche == KeyEvent.VK_RIGHT) {
		    		timer();
		    		tabKey[3] = true;
		    	} 	
		    }

			public void keyReleased(KeyEvent e) { 
				int touche = e.getKeyCode();
				
		    	if(touche == KeyEvent.VK_UP)   		
		    		tabKey[0] = false;
		    	else if(touche == KeyEvent.VK_DOWN) 	
		    		tabKey[1] = false;
		    	else if(touche == KeyEvent.VK_LEFT)	
		    		tabKey[2] = false;
		    	else if(touche == KeyEvent.VK_RIGHT)	
		    		tabKey[3] = false;
			}
		});
		
		/** Capture des actions de la molette */
		addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				scroll = !scroll;
				if(!scroll)
					return;
				
				/* Molette vers le haut */
	            if (e.getWheelRotation() < 0) 
	            	historique.afficherMessagePrecedent();
	            else
	            	historique.afficherMessageSuivant();
			}
		});
		
	    /* Événements de carte */
	    carte.onStateRealized(new CarteListener() {
			
			@Override
			public void joueurPerd() {
				sonArriere.jouePerdu();
			}
			
			@Override
			public void joueurGagne() {
				sonArriere.joueGagne();
			}
			
			@Override
			public void deplaceMonstre() {
				finTour.setEnabled(!finTour.isEnabled());
			}
			
			@Override
			public void historique(String s) {
				historique.addMessage(s);
			}
			
			@Override
			public void information(String s) {
				information.setText(s);
			}
		});	    
	    
	    /* Création de la barre d'état avec ses séparateurs */
        separateurHaut = new JSeparator(SwingConstants.HORIZONTAL);
        separateurHaut.setBackground(Color.black);
       
        separateurBas = new JSeparator(SwingConstants.HORIZONTAL);
        separateurBas.setBackground(Color.black);
	    
	    /* On ajoute à la fenêtre les différents éléments que l'on a créé */
	    Box box = Box.createVerticalBox();
	    
	    box.add(sousMenu);
        box.add(separateurHaut);
	    box.add(carte);
        box.add(separateurBas);
	    box.add(barreEtat);
	    this.add(box);
	            
        /* On joue le son d'arrière plan */
		sonArriere = new Son();
		sonArriere.joueSonArriere();
        
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setResizable(false);
	    this.pack();
	    
	    this.setVisible(true);
	    this.requestFocus();
	}
	
	/**
	 * Méthode privée permettant de vérifier si on actionne le code konami
	 * @param touche_pressee
	 * @return False sauf si le code Konami est actionné
	 */
	private boolean verifieKonami(int touche_pressee) {
		if(touche_pressee == touchesKonami[caseActuelle]) {
			caseActuelle++;
			
			if(caseActuelle == touchesKonami.length) {
				konami = !konami;
				menuTriche.setVisible(konami);
				caseActuelle = 0;
				return true;
			}
		}
		else
			caseActuelle = 0;	 

		return false;
	}
	
	public static void main(String[] args) throws InvalidMidiDataException, IOException, MidiUnavailableException
	{
		FenetreJeu fenetre = new FenetreJeu();
		fenetre.setVisible(true);
	}	
}

