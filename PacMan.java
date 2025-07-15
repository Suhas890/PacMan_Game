import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.List;         // Ensure this is here
import java.util.ArrayList;    // Ensure this is here
import javax.swing.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class LeaderboardEntry {
    private long time;
    private int mapNumber;

    public LeaderboardEntry(long time, int mapNumber) {
        this.time = time;
        this.mapNumber = mapNumber;
    }

    public long getTime() {
        return time;
    }

    public int getMapNumber() {
        return mapNumber;
    }

    @Override
    public String toString() {
        return "Map " + mapNumber + " - Time: " + time + " seconds";
    }
}



public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x, y, width, height;
        Image image;
        int startX, startY;
        char direction = 'U'; // U D L R
        int velocityX = 0, velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;

            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection; // Revert to previous direction
                    updateVelocity();
                }
            }
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize / 4; // Adjust speed as needed
            } else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize / 4; // Adjust speed as needed
            } else if (this.direction == 'L') {
                this.velocityX = -tileSize / 4; // Adjust speed as needed
                this.velocityY = 0;
            } else if (this.direction == 'R') {
                this.velocityX = tileSize / 4; // Adjust speed as needed
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage, blueGhostImage, orangeGhostImage, pinkGhostImage, redGhostImage,
                  pacmanUpImage, pacmanDownImage, pacmanLeftImage, pacmanRightImage;

    private String[][] tileMaps = {
        { // First map
         "XXXXXXXXXXXXXXXXXXX",
        "X     p     o     X",
        "X XX XXX X XXX XX X",
        "X     r           X",
        "X XX X XXX XX X XXX",
        "X                 X",
        "XXXX X  X XXXX XXXX",
        "X    X      X     X",
        "XXXX X    X  S XXXX",
        "X  b X    X       X",
        "XX X X XX XX X XXXX",
        "X    X     X      X",
        "XXX  X XX XX X XXXX",
        "X            b    X",
        "X XX XXX X XXX XX X",
        "X   P             X",
        "XX X X XXX XXX X XX",
        "X                 X",
        "X XX X  X XX X X  X",
        "XXX  X    o X     X",
        "XXXXXXXXXXXXXXXXXXX"
    },
    { // Second map
        "XXXXXXXXXXXXXXXXXXX",
        "X         b       X",
        "X     r           X",
        "X XXX XXXXXXX XXX X",
        "X                 X",
        "XXX XXX XXXXXXX XXX",
        "X                 X",
        "XXXXXX XXXX XXXX XX",
        "X o         S     X",
        "XXXXXX XXXX XXXX XX",
        "X                 X",
        "XXX XXX XXXXXXX XXX",
        "X     P           X",
        "XXXXXXXXX  XXXX XXX",
        "X                 X",
        "XXXXX        XXXXXX",
        "X      b          X",
        "XX         XXXXXXXX",
        "XXXX        p    XX",
        "X    XXXXXXXX   XXX",
        "XXXXXXXXXXXXXXXXXXX"
  
  
    }
};


    HashSet<Block> walls, foods, ghosts; 
    Block pacman, specialPellet; 
    Timer gameLoop; 
    char[] directions = {'U', 'D', 'L', 'R'}; 
    Random random = new Random(); 
    int score = 0, lives = 3; 
    boolean gameOver = false; 
    private int currentMapIndex = 0; 
    private boolean canKill = false; 
    private long killStartTime; 
    private final long killDuration = 5000; 
    private boolean isPaused = false;

    // Leaderboard variables
private static final String LEADERBOARD_FILE = "pacman_leaderboard.txt";
   private Map<String, List<LeaderboardEntry>> leaderboard = new HashMap<>();  // Player name -> list of map entries

    private String username; // Store the player's username
    private long startTime; // Start time for level completion

    PacMan(String username) {
        this.username = username; // Set the username
        leaderboard = loadLeaderboard(currentMapIndex);

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);
        
        loadImages();
        loadMap();
        
        gameLoop=new Timer(50,this);
        gameLoop.start();
        
        startTime = System.currentTimeMillis(); // Start timing when loading a new map
    }

    private void loadImages() {
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();
        
        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();
    }

    public void loadMap() {
        walls=new HashSet<>();
        foods=new HashSet<>();
        ghosts=new HashSet<>();
        
        String[] tileMap=tileMaps[currentMapIndex];
        
        for(int r=0;r<rowCount;r++) {
            if (r < tileMap.length) {
                String row=tileMap[r];
                
                for(int c=0;c<columnCount;c++) {
                    if (c < row.length()) {
                        char tileMapChar=row.charAt(c);
                        int x=c*tileSize;
                        int y=r*tileSize;

                        switch(tileMapChar) {
                            case 'X': // Wall
                                walls.add(new Block(wallImage,x,y,tileSize,tileSize));
                                break;

                            case 'b': // Blue ghost
                                ghosts.add(new Block(blueGhostImage,x,y,tileSize,tileSize));
                                break;

                            case 'o': // Orange ghost
                                ghosts.add(new Block(orangeGhostImage,x,y,tileSize,tileSize));
                                break;

                            case 'p': // Pink ghost
                                ghosts.add(new Block(pinkGhostImage,x,y,tileSize,tileSize));
                                break;

                            case 'r': // Red ghost
                                ghosts.add(new Block(redGhostImage,x,y,tileSize,tileSize));
                                break;

                            case 'P': // PacMan
                                pacman=new Block(pacmanRightImage,x,y,tileSize,tileSize);
                                break;

                            case ' ': // Food
                                foods.add(new Block(null,x+14,y+14,4,4));
                                break;

                            case 'S': // Special Pellet
                                specialPellet=new Block(null,x+14,y+14,4,4);
                                break;
                        }
                    }
                }
            }
        }
        
       resetPositions();
       startTime=System.currentTimeMillis(); // Reset start time when loading a new map
   }

   public void paintComponent(Graphics g) {
       super.paintComponent(g);
       draw(g);
   }

   public void draw(Graphics g) {
    // Existing rendering logic
    super.paintComponent(g);
    g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

    for (Block ghost : ghosts) {
        g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
    }

    for (Block wall : walls) {
        g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
    }

    g.setColor(Color.WHITE);

    for (Block food : foods) {
        g.fillRect(food.x, food.y, food.width, food.height);
    }

    if (specialPellet != null) {
        g.setColor(Color.MAGENTA);
        g.fillRect(specialPellet.x, specialPellet.y, specialPellet.width, specialPellet.height);
    }

    g.setFont(new Font("Arial", Font.PLAIN, 18));

    if (gameOver) {
        g.drawString("Game Over: " + String.valueOf(score), tileSize / 2, tileSize / 2);
    } else {
        g.setColor(Color.RED);
        g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize / 2, tileSize / 2);

        // Display remaining timer for special pellet
        if (canKill) {
            long remainingTime = (killDuration - (System.currentTimeMillis() - killStartTime)) / 1000;
            g.setColor(Color.YELLOW);
            g.drawString("Timer: " + remainingTime + "s", tileSize / 2, tileSize); // Display below the score
        }
    }
}


   public void move() { 
       if (!isPaused && !gameOver) { 
           pacman.x += pacman.velocityX; 
           pacman.y += pacman.velocityY; 

           for(Block wall:walls) { 
               if(collision(pacman,wall)) { 
                   pacman.x -= pacman.velocityX; 
                   pacman.y -= pacman.velocityY; 
                   break; 
               } 
           } 
 switch (pacman.direction) {
            case 'U':
                pacman.image = pacmanUpImage;
                break;
            case 'D':
                pacman.image = pacmanDownImage;
                break;
            case 'L':
                pacman.image = pacmanLeftImage;
                break;
            case 'R':
                pacman.image = pacmanRightImage;
                break;
        }

           Iterator<Block> ghostIterator=ghosts.iterator(); 

           while (ghostIterator.hasNext()) { 
               Block ghost=ghostIterator.next(); 

               if (collision(ghost,pacman)) { 
                   if (random.nextInt(100) < 10) { 
                       do { 
                           int newX=random.nextInt(columnCount)*tileSize; 
                           int newY=random.nextInt(rowCount)*tileSize; 

                           if (!isCollidingWithWall(newX,newY)) { 
                               pacman.x=newX; 
                               pacman.y=newY; 
                               return; 
                           } 

                       } while(true); 
                   } 

                   if (canKill) { 
                       ghostIterator.remove(); 
                       continue; 
                   } else { 
                       lives -= 1; 

                       if(lives <= 0) { 
                           lives=0; 
                           gameOver=true; 

                           handleGameOver(); 
                           return ; 
                       } 

                       resetPositions(); 
                   } 

               } 

               if(random.nextInt(100) < 10) { 
                   char newDirection=directions[random.nextInt(4)]; 
                   ghost.updateDirection(newDirection); 
               } 

               ghost.x += ghost.velocityX ; 
               ghost.y += ghost.velocityY ; 

               for(Block wall:walls){ 
                   if(collision(ghost,wall)||ghost.x<=0||ghost.x+ghost.width>=boardWidth){ 
                       ghost.x -= ghost.velocityX ; 
                       ghost.y -= ghost.velocityY ; 

                       char newDirection=directions[random.nextInt(4)]; 

                       ghost.updateDirection(newDirection); 
                   } 

               } 

           } 

           Block foodEaten=null; 

           for(Block food:foods){ 
               if(collision(pacman ,food)){ foodEaten=food ; score+=10 ; } 

           } 

           foods.remove(foodEaten); 
someLevelCompletionCondition(); 

           if(specialPellet != null && collision(pacman,specialPellet)) { specialPellet=null; canKill=true; killStartTime=System.currentTimeMillis(); score += 50; } 

           if (canKill && System.currentTimeMillis() - killStartTime > killDuration) { canKill=false; } 

       } 

   } 

   public boolean collision(Block a ,Block b){ return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y ; }

   public boolean collision(Rectangle rect ,Block block){ Rectangle blockRect=new Rectangle(block.x ,block.y ,block.width ,block.height); return rect.intersects(blockRect); }

   public void resetPositions(){ pacman.reset(); pacman.velocityX=0 ; pacman.velocityY=0 ; for(Block ghost:ghosts){ ghost.reset(); char newDirection=directions[random.nextInt(4)]; ghost.updateDirection(newDirection); } }

   @Override public void actionPerformed(ActionEvent e){ move(); repaint(); if(gameOver){ gameLoop.stop(); } }

   @Override public void keyTyped(KeyEvent e){}
   
   @Override public void keyPressed(KeyEvent e){}
   
   @Override
public void keyReleased(KeyEvent e) {
    // Toggle pause state when ESC key is released
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        isPaused = !isPaused;

        // Toggle pause state
        if (isPaused) {
            showExitOptions(); // Show exit options if paused
        } else {
            gameLoop.start(); // Resume the game
        }
        return;
    }

    // Pause the game and show the leaderboard when 'L' is pressed
    if (e.getKeyCode() == KeyEvent.VK_L) {
        isPaused = true;
        showLeaderboard(); // Show leaderboard
        return; // No need to resume here, it will be handled in showLeaderboard
    }



    // Handle movement keys for Pacman
    switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
            pacman.updateDirection('U');
	    pacman.image = pacmanUpImage;
            break;

        case KeyEvent.VK_DOWN:
            pacman.updateDirection('D');pacman.image = pacmanDownImage; 
            break;

        case KeyEvent.VK_LEFT:
            pacman.updateDirection('L');pacman.image = pacmanLeftImage;
            break;

        case KeyEvent.VK_RIGHT:
            pacman.updateDirection('R');pacman.image = pacmanRightImage;
            break;

        case KeyEvent.VK_W: // Move up with W key
            pacman.updateDirection('U');
            break;

        case KeyEvent.VK_S: // Move down with S key
            pacman.updateDirection('D');
            break;

        case KeyEvent.VK_A: // Move left with A key
            pacman.updateDirection('L');
            break;

        case KeyEvent.VK_D: // Move right with D key
            pacman.updateDirection('R');
            break;

        case KeyEvent.VK_M: // Switch maps with M key
            currentMapIndex++;
            if (currentMapIndex >= tileMaps.length) {
                currentMapIndex = 0;
            }
	    score = 0;
            lives = 3;
            loadMap();
            break;
    }
}

private boolean levelCompleted = false;

// Update this method to check for level completion
private void someLevelCompletionCondition() {
    // Check if all food items are collected
    if (foods.isEmpty()) {
        levelCompleted = true;  // Set the level as completed
        repaint();
        // Call the method to handle the game over or level completion
        handleGameOver();
        
        // Make sure to update the UI by repainting
        
    }
}


private void loadLeaderboardForCurrentMap() {
    leaderboard = loadLeaderboard(currentMapIndex);  // Load leaderboard for the current map
}
private void saveLeaderboardForCurrentMap() {
    int mapIndex = currentMapIndex;  // Get current map index
    saveLeaderboard(mapIndex, leaderboard);  // Save leaderboard for current map
}

private Map<String, List<LeaderboardEntry>> loadLeaderboard(int mapIndex) {
    Map<String, List<LeaderboardEntry>> loadedLeaderboard = new HashMap<>();
    File leaderboardFile = new File("pacman_leaderboard_map_" + mapIndex + ".txt");  // Ensure each map has its own file

    if (!leaderboardFile.exists()) {
        return loadedLeaderboard;
    }
    
    try (BufferedReader reader = new BufferedReader(new FileReader(leaderboardFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(":");
            if (parts.length == 3) {
                String playerName = parts[0];
                int mapNum = Integer.parseInt(parts[1]);
                long time = Long.parseLong(parts[2]);

                LeaderboardEntry entry = new LeaderboardEntry(time, mapNum);
                loadedLeaderboard.computeIfAbsent(playerName, k -> new ArrayList<>()).add(entry);
            }
        }
    } catch (IOException e) {
        System.err.println("Error loading leaderboard for map " + mapIndex + ": " + e.getMessage());
    }

    return loadedLeaderboard;
}



private void saveLeaderboard(int mapIndex, Map<String, List<LeaderboardEntry>> leaderboard) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter("pacman_leaderboard_map_" + mapIndex + ".txt"))) {
        for (Map.Entry<String, List<LeaderboardEntry>> entry : leaderboard.entrySet()) {
            String playerName = entry.getKey();
            for (LeaderboardEntry leaderboardEntry : entry.getValue()) {
                writer.write(playerName + ":" + leaderboardEntry.getMapNumber() + ":" + leaderboardEntry.getTime());
                writer.newLine();
            }
        }
    } catch (IOException e) {
        System.err.println("Error saving leaderboard for map " + mapIndex + ": " + e.getMessage());
    }
}


private void handleGameOver() {
    long endTime = System.currentTimeMillis();
    long totalTime = (endTime - startTime) / 1000; // Time in seconds

    String message;
    if (levelCompleted) {
        message = "Game Over! You completed the game in " + totalTime + " seconds.";
        
        // Create and add the leaderboard entry for the current player and map
        LeaderboardEntry entry = new LeaderboardEntry(totalTime, currentMapIndex);
        
        // Add the entry to the leaderboard (using player's username)
        leaderboard.computeIfAbsent(username, k -> new ArrayList<>()).add(entry);
        
        // Save the updated leaderboard for the current map
        saveLeaderboardForCurrentMap();
        
        // Show the updated leaderboard
        showLeaderboard(); 
    } else {
        message = "Game Over! You did not complete the level.";
    }

    // Show the message
    JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);

    // Show options for retrying, switching maps, exiting, or starting with a new player
    String[] options = {"Retry", "Switch Maps", "Exit", "New Player"};
    int choice = JOptionPane.showOptionDialog(this,
            "What would you like to do?",
            "Game Over Options",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]); // Default option is "Retry"

    switch (choice) {
        case 0: 
            resetGame(); // Retry the current map
            break; // Retry
        case 1: 
            currentMapIndex++;
            if (currentMapIndex >= tileMaps.length) {
                currentMapIndex = 0; // Loop back to the first map
            }
            resetGame();
 
   
            // Load the leaderboard for the new map
            leaderboard = loadLeaderboard(currentMapIndex);
 	resumeGame();
            break; // Switch Maps
        case 2: 
            System.exit(0); // Exit the game
            break; // Exit
        case 3: 
            newPlayer(); // Prompt for new player
            break; // New Player
    }
}

private void newPlayer() {
    // Prompt for new username
    String newUsername = JOptionPane.showInputDialog("Enter the new player's username:");
    if (newUsername == null || newUsername.trim().isEmpty()) {
        newUsername = "Player"; // Default username if none provided
    }

    // Update the username and reset the game
    username = newUsername;
    currentMapIndex = 0; // Start from the first map
    resetGame(); // Reset game for the new player
}


private void resetGame() {
    // Reset game details
    lives = 3;
    score = 0;
    levelCompleted = false;
    startTime = System.currentTimeMillis();
    loadMap();
    resetPositions();

    // Update the leaderboard for the current map
    loadLeaderboardForCurrentMap();
    gameOver = false;
	repaint();
}

private void showExitOptions() {
    String[] options = {"Resume", "Exit"};
    JOptionPane optionPane = new JOptionPane("Press M to switch maps.\nPress L to see Leaderboard.", JOptionPane.INFORMATION_MESSAGE);

    JDialog dialog = optionPane.createDialog(this, "Game Paused");
    dialog.setModal(true);
    optionPane.addPropertyChangeListener(evt -> {
        String propName = evt.getPropertyName();
        if (JOptionPane.VALUE_PROPERTY.equals(propName)) {
            Object value = evt.getNewValue();
            if ("Resume".equals(value)) {
                isPaused = false;
                gameLoop.start(); // Resume game loop
            } else if ("Exit".equals(value)) {
                System.exit(0); // Exit the game
            }
        }
    });
    optionPane.setOptions(options);
    optionPane.setInitialValue(options[0]);
    dialog.setVisible(true);
}
public void resumeGame() {
    if(isPaused){
    isPaused = false;
    gameLoop.start(); // Resume the game loop
}
}
private void showLeaderboard() {
    // Load the leaderboard for the current map
    loadLeaderboardForCurrentMap();

    StringBuilder leaderboardText = new StringBuilder();
    leaderboardText.append("Leaderboard for Map " + (currentMapIndex + 1) + ":\n\n");

    // Display the leaderboard entries for the current map
    leaderboard.forEach((player, entries) -> {
        for (LeaderboardEntry entry : entries) {
            if (entry.getMapNumber() == currentMapIndex) {
                leaderboardText.append(player + " - " + entry.getTime() + " seconds\n");
            }
        }
    });

    // Display the leaderboard in a message dialog
    JOptionPane.showMessageDialog(this, leaderboardText.toString(), "Leaderboard", JOptionPane.INFORMATION_MESSAGE);
    resumeGame();  // This line should not be here
}




public static void main(String[] args) {
     String username = JOptionPane.showInputDialog("Enter your username:");
     if (username == null || username.trim().isEmpty()) {
         username = "Player"; // Default username
     }

     JFrame frame = new JFrame("PacMan");
     PacMan game = new PacMan(username); // Pass username to the game instance
     frame.add(game);
     frame.pack();
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.setVisible(true);
}

private boolean isCollidingWithWall(int x, int y) {
     Rectangle pacmanBounds = new Rectangle(x, y, pacman.width, pacman.height);
     for (Block wall : walls) {
         if (collision(pacmanBounds, wall)) {
             return true;
         }
     }
     return false;
}
}

