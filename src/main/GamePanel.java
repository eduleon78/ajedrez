package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

public class GamePanel extends JPanel implements Runnable {
	
	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	final int FPS = 60;
	Thread gameThread;
	Board board = new Board();
	
	Mouse mouse = new Mouse();
	
	// Piezas
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static ArrayList<Piece> simPieces = new ArrayList<>();
	Piece activeP;
	public static Piece castlingP;  
	
	// Color
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;
	
	// Booleans
	boolean canMove;
	boolean validSquare;
			
	
	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH,HEIGHT));
		setBackground(Color.black);
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		
		setPieces();
		copyPieces(pieces, simPieces);
	}
	
	public void launchGame() {
		gameThread = new Thread(this);
		gameThread.start();
	}
	
	public void setPieces() {
		
		// Equipo blanco
		pieces.add(new Pawn(WHITE,0,6));
		pieces.add(new Pawn(WHITE,1,6));
		pieces.add(new Pawn(WHITE,2,6));
		pieces.add(new Pawn(WHITE,3,6));
		pieces.add(new Pawn(WHITE,4,6));
		pieces.add(new Pawn(WHITE,5,6));
		pieces.add(new Pawn(WHITE,6,6));
		pieces.add(new Pawn(WHITE,7,6));
		pieces.add(new Rook(WHITE,0,7));
		pieces.add(new Rook(WHITE,7,7));
		pieces.add(new Knight(WHITE,1,7));
		pieces.add(new Knight(WHITE,6,7));
		pieces.add(new Bishop(WHITE,2,7));
		pieces.add(new Bishop(WHITE,5,7));
//		pieces.add(new Queen(WHITE,3,7));
		pieces.add(new Queen(WHITE,4,4));
		pieces.add(new King(WHITE,4,7));
//		pieces.add(new King(WHITE,4,4));
		
		// Equipo negro
		pieces.add(new Pawn(BLACK,0,1));
		pieces.add(new Pawn(BLACK,1,1));
		pieces.add(new Pawn(BLACK,2,1));
		pieces.add(new Pawn(BLACK,3,1));
		pieces.add(new Pawn(BLACK,4,1));
		pieces.add(new Pawn(BLACK,5,1));
		pieces.add(new Pawn(BLACK,6,1));
		pieces.add(new Pawn(BLACK,7,1));
		pieces.add(new Rook(BLACK,0,0));
		pieces.add(new Rook(BLACK,7,0));
		pieces.add(new Knight(BLACK,1,0));
		pieces.add(new Knight(BLACK,6,0));
		pieces.add(new Bishop(BLACK,2,0));
		pieces.add(new Bishop(BLACK,5,0));
		pieces.add(new Queen(BLACK,3,0));
		pieces.add(new King(BLACK,4,0));
	}
	
	public void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		
		target.clear();
		for(int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}
	
	@Override
	public void run() {
		
		// Game loop		
		double drawInterval = 1000000000/FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		
		while(gameThread != null) {
			
			currentTime = System.nanoTime();
			
			delta += (currentTime - lastTime)/drawInterval;
			lastTime = currentTime;
			
			if(delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
		
	}
	
	private void update() {
		
		// Boton del raton presionado		
		if(mouse.pressed) {
			if(activeP == null) {
				
				for(Piece piece : simPieces) {					
					if(piece.color == currentColor &&
							piece.col == mouse.x/Board.SQUARE_SIZE &&
							piece.row == mouse.y/Board.SQUARE_SIZE) {		
						
						activeP = piece;
					}
				}
			}
			else {
				simulate();
			}
		}
		
		// Mouse button released
		if(mouse.pressed == false) {
			
			if(activeP != null) {
				
				if(validSquare) {
					
					// MOVE CONFIRMED
					
					// update the piece list in case a piece has been captured and removed during the simulation
					copyPieces(simPieces, pieces);
					activeP.updatePosition();
					if(castlingP != null) {
						castlingP.updatePosition();
					}
					
					changePlayer();
				}
				else {
					// The move is not valid so reset everything
					copyPieces(pieces, simPieces);
					activeP.resetPosition();
					activeP = null;
				}
			}
		}
	}
	private void simulate() {
		
		canMove = false;
		validSquare = false;
		
		copyPieces(pieces, simPieces);
		
		// Reset the castling
		if(castlingP != null) {
			castlingP.col =castlingP.preCol;
			castlingP.x =castlingP.getX(castlingP.col);
			castlingP = null;
		}
		
		activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
		activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
		activeP.col = activeP.getCol(activeP.x);
		activeP.row = activeP.getRow(activeP.y);
		
		if(activeP.canMove(activeP.col, activeP.row)) {
			
			canMove = true;
			
			if(activeP.hittingP != null) {
				simPieces.remove(activeP.hittingP.getIndex());
			}
			
			checkCastling();
			
			validSquare = true;
		}
		
	}
	private void checkCastling() {
		
		if(castlingP != null) {
			if(castlingP.col == 0) {
				castlingP.col += 3;
			}
			else if(castlingP.col == 7) {
				castlingP.col -= 2;
			}
			castlingP.x = castlingP.getX(castlingP.col);
		}
	}
	private void changePlayer() {
		
		if(currentColor == WHITE) {
			currentColor = BLACK;
		}
		else {
			currentColor = WHITE;
		}
		activeP = null;
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		
		// Tablero		
		board.draw(g2);
		
		// Piezas
		for(Piece p : simPieces) {
			p.draw(g2);
		}
		
		if(activeP != null) {
			if(canMove) {
				g2.setColor(Color.white);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE, 
						Board.SQUARE_SIZE, Board.SQUARE_SIZE);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				
				
				activeP.draw(g2);
			}
			
			// STATUS MESSAGES
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
			g2.setColor(Color.white);
			
			if(currentColor == WHITE) {
				g2.drawString("Equipo Blanco", 840, 550);
			}
			else {
				g2.drawString("Equipo Negro", 840, 250);
			}
			
			
		}
	}

}
