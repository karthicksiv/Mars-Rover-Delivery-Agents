package kas221;

import agent.*;
import pacworld.*;
import java.util.*;

/** 
 * Define class as an extension of Agent
*/
public class PacAgent extends Agent{

    int X = 0;
    int Y = 0;
    int destX = -1;
    int destY = -1;
    int rand;

    private String id;
    private int [][] internal;
    private VisibleAgent[] visAgents;  
    private VisiblePackage[] visPackages;  
    private String[] messages; 
    private int worldSize;              
    private VisiblePackage target;
    private VisiblePackage heldPackage;    
    private ArrayList<VisiblePackage> packages;
    private int temp = 0;
    private boolean bump;
    private int prevDir;
    private int tempQuad;
    private Random random;
    private int tempLo;
    private int exploreX;
    private int exploreY;
    private int idx;
    private int exploreid;
    private int exploreIdx;
    private int counter;
    
    private final int[][] grid;
    private final int width;
    private final int height;

    /**
     * Constructor for the PacAgent class.
     *
     * @param id The identifier for this agent.
     */
    public PacAgent(int id){
        // Call the superclass constructor with the given id.
        super(id);

        // Set the agent's id as a string.
        this.id = "Agent" + id;

        // Initialize the agent's internal 2D grid. I don't end up saving an internal state, my explore logic doesn't require it
        this.internal = new int [50][50];

        // Set the grid's width and height.
        this.grid = internal;
        this.width = grid.length;
        this.height = grid[0].length;

        // Initialize the list of visible packages.
   		this.packages = new ArrayList<VisiblePackage>();
        
        // Set the initially held package to null (no package held).
   		this.heldPackage = null;

        // Initialize the random object for random actions. Used for bump handling
        this.random = new Random();

        // Initialize the bump flag to false.
        this.bump = false;

        // Initialize the previous direction to 0. This is used for dropoff handling
        this.prevDir = 0;

        // Initialize various indexes and counters to 0.
        this.idx = 0;
        this.exploreIdx = 0;
        this.counter = 0;

        // If the agent id is odd (0-based indexing), set the following values:
        if(id+1 % 2 == 0){
            this.exploreid = 1;
            this.tempLo = 1;
            this.exploreX = 5;
            this.exploreY = 5;
        }
        // If the agent id is even (0-based indexing), set the following values:
        else{
            this.exploreid = 0;
            this.tempLo = 4;
            this.exploreX = 45;
            this.exploreY = 45;
        }
    }

    /**
     * Updates the agent's state based on the provided percept.
     *
     * @param p The percept received by the agent.
     */
    public void see(Percept p){
            // Cast the input Percept to PacPercept, which is specific to the package delivery simulation.
            PacPercept perc = (PacPercept) p;

            // Update the visible agents from the percept.
   			visAgents = perc.getVisAgents();
            // Update the visible packages from the percept.
   			visPackages = perc.getVisPackages();
            // Update the messages from the percept.
   			messages = perc.getMessages();
            // Update the held package from the percept.
   			heldPackage = perc.getHeldPackage();
            // Update the world size from the percept.
   			worldSize = perc.getWorldSize();
            // Update the bump flag based on whether the agent felt a bump in the percept.
            bump = perc.feelBump();

            // Update the agent's internal state based on the visible agents and packages.
   			updatePacPercept(visAgents, visPackages);
   }

   /**
     * Calculates the Manhattan distance between the agent's current position and the given coordinates.
     *
     * @param x The x-coordinate of the target position.
     * @param y The y-coordinate of the target position.
     * @return The Manhattan distance between the agent's position and the target position.
    */
   public int distance(int x, int y){
   	  return (Math.abs(this.X - x) + Math.abs(this.Y - y));
   }

   /**
     * Updates the agent's internal state based on the visible agents and packages.
     *
     * @param visAgents    An array of visible agents from the percept.
     * @param visPackages  An array of visible packages from the percept.
    */
   public void updatePacPercept(VisibleAgent [] visAgents, VisiblePackage [] visPackages){
    // Iterate through the visible agents to find the current agent's position.
    for(int i = 0; i < visAgents.length; i++){
        // If the current agent is found in the visible agents array,
        // update its position (X, Y) with the new coordinates.
        if(id.equals(visAgents[i].getId())){
            X = visAgents[i].getX();
            Y = visAgents[i].getY();
        }
    }
    // Iterate through the visible packages to update the list of packages in sight.
    for(int i = 0; i < visPackages.length; i++){
        boolean isAdded = true;

        // If the package or its destination is out of bounds, skip this package.
        if((visPackages[i].getX() > worldSize || visPackages[i].getX() < 0) || (visPackages[i].getY() > worldSize || visPackages[i].getY() < 0) || (visPackages[i].getDestX() > worldSize || visPackages[i].getDestX() < 0) || (visPackages[i].getDestY() > worldSize || visPackages[i].getDestY() < 0)){
            continue;
        }

        // If the agent already has packages in its list,
        // check if the visible package is not already in the list and not held by another agent.
        if(packages.size() > 0){
            for(int j = 0; j < packages.size(); j++){
                // If the package is already in the list or is held by another agent,
                // set isAdded to false and break the loop.
                if((packages.get(j).equals(visPackages[i])) || (visPackages[i].isHeld())){
                    isAdded = false;
                    break;
                }
            }
            // If the package is not in the list and not held by another agent,
            // add it to the packages list.
            if(isAdded == true){
                packages.add(visPackages[i]);
            }
        }
        else{
            // If the agent's packages list is empty,
            // add all visible packages that are not held by other agents.
            for(int k = 0; k < visPackages.length; k++){
                if(!(visPackages[k].isHeld())){
                    packages.add(visPackages[k]);
                }
            }
        }
    }
   }

   /**
     * This method selects the next action for the agent based on its current state,
     * such as whether it's holding a package or if it has a target package.
     *
     * @return The next action for the agent to perform.
    */
   public Action selectAction(){
   		Action action = new Idle();
        // If the agent has bumped into something, move in a random direction.
        if(bump){
            bump = false;
            int rand = random.nextInt(4);
            return new Move(rand);
        }

        // If the agent is holding a package, deliver it to its destination.
        if(heldPackage != null){
            int destX = heldPackage.getDestX();
            int destY = heldPackage.getDestY();
            if(destX == this.X && destY == this.Y-1){
                prevDir = 0;
                return new Dropoff(Direction.NORTH);
            }
            else if(destX == this.X && destY == this.Y+1){
                prevDir = 2;
                return new Dropoff(Direction.SOUTH);
            }
            else if(destX == this.X-1 && destY == this.Y){
                prevDir = 3;
                return new Dropoff(Direction.WEST);
            }
            else if(destX == this.X+1 && destY == this.Y){
                prevDir = 1;
                return new Dropoff(Direction.EAST);
            }
            System.out.println("Travelling to destination");
            action = getNextMove(destX, destY, "Destination");
            return action;
        }
        // If the agent does not have a target package, find one or explore the environment.
        if(target == null){
            if(packages.size()>0){
                for(int i = 0;i<packages.size();i++){
                    System.out.println(id + " " + packages.get(i) + " " + temp);
                    System.out.println(heldPackage == null);
                }
                temp++;
                //target = packages.remove(0);
                idx = findNearestPackage(packages);
                target = packages.remove(idx);
            }
            else{
                action = explore();
            }
        }
        // If the agent has a target package, move towards it and attempt to pick it up.
        else{
            int tX = target.getX();
            int tY = target.getY();

            int distX = tX - X;
            int distY = tY - Y;

            // If the target is directly above, below, right or left of the agent, move in the direction
            if(distX == 0 || distY==0){
                if(distX==0){
                    if(distY==1){
                        prevDir = 2;
                        action = new Pickup(Direction.SOUTH);
                        destX = target.getDestX();
                        destY = target.getDestY();
                        heldPackage = target;
                        target = null;
                    }
                    else if(distY==-1){
                        prevDir = 0;
                        action = new Pickup(Direction.NORTH);
                        destX = target.getDestX();
                        destY = target.getDestY();
                        heldPackage = target;
                        target = null;
                    }
                    else if(distY>1){
                        prevDir = 2;
                        action = new Move(Direction.SOUTH);
                    }
                    else if(distY<-1){
                        prevDir = 0;
                        action = new Move(Direction.NORTH);
                    }
                }
                else if(distY == 0){
                    if(distX==1){
                        prevDir = 1;
                        action = new Pickup(Direction.EAST);
                        destX = target.getDestX();
                        destY = target.getDestY();
                        heldPackage = target;
                        target = null;
                    }
                    else if(distX==-1){
                        prevDir = 3;
                        action = new Pickup(Direction.WEST);
                        destX = target.getDestX();
                        destY = target.getDestY();
                        heldPackage = target;
                        target = null;
                    }
                    else if(distX>1){
                        prevDir = 1;
                        action = new Move(Direction.EAST);
                    }
                    else if(distX<-1){
                        prevDir = 3;
                        action = new Move(Direction.WEST);
                    }
                }
            }
            // Navigate to the target using the getNextMove method
            else{
                String targetAction = this.id + "is finding a target not on X or Y plane";
                action = getNextMove(tX, tY, targetAction);
            }
        }
        return action;
   }

   /**
     * This method calculates the next move for the agent to get closer to the target
     * coordinates (targetX, targetY) based on its current position.
     *
     * @param targetX The target x-coordinate.
     * @param targetY The target y-coordinate.
     * @param status A string representing the current status of the agent.
     * @return The next move for the agent to perform in order to get closer to the target.
    */
   public Move getNextMove(int targetX, int targetY, String status) {

    int dx = this.X-targetX;
    int dy = this.Y-targetY;

    System.out.println(status);
    // Determine if the agent should move horizontally or vertically.
    // Move horizontally.
    if (Math.abs(dx)>=Math.abs(dy)){
        if (this.X > targetX) {
            System.out.println(X + " " + Y);
            prevDir = 3;
            return new Move(Direction.WEST);
            
        } else {
            System.out.println(X + " " + Y);
            prevDir = 1;
            return new Move(Direction.EAST);
            
        }
    } 
    // Move vertically.
    else if(Math.abs(dx)<Math.abs(dy)){
        
        if (this.Y > targetY) {
            System.out.println(X + " " + Y);
            prevDir = 0;
            return new Move(Direction.NORTH);
        
        } else {
            System.out.println(X + " " + Y);
            prevDir = 2;
            return new Move(Direction.SOUTH);
            
        }
    }
    return null;
   }

   /**
     * This method finds the index of the nearest package to the agent's current position
     * in the given ArrayList of VisiblePackage objects.
     *
     * @param packages An ArrayList of VisiblePackage objects representing the packages in the environment.
     * @return The index of the nearest package to the agent's current position.
    */
   private int findNearestPackage(ArrayList<VisiblePackage> packages){
    int nearestPackage = 0;
    double minDistance = Double.MAX_VALUE;

    // Iterate through the packages to find the nearest one.
    for (int i = 0; i < packages.size(); i++) {
        // Calculate the distance between the agent's position and the package's position.
        double distance = getDistance(this.X, this.Y, packages.get(i).getX(), packages.get(i).getY());

        // If the calculated distance is less than the current minimum distance,
        // update the minimum distance and the nearest package index.
        if (distance < minDistance) {
            minDistance = distance;
            nearestPackage = i;
        }
    }

    // Return the index of the nearest package.
    return nearestPackage;
   }

   /**
     * This method calculates the Euclidean distance between two points (x1, y1) and (x2, y2)
     * in a 2D space.
     *
     * @param x1 The x-coordinate of the first point.
     * @param y1 The y-coordinate of the first point.
     * @param x2 The x-coordinate of the second point.
     * @param y2 The y-coordinate of the second point.
     * @return The Euclidean distance between the two points.
    */
   private double getDistance(int x1, int y1, int x2, int y2) {
    int dx = x1 - x2;
    int dy = y1 - y2;
    return Math.sqrt(dx * dx + dy * dy);
   }

    // This is the old explore function, kept in case something breaks...
//    private Action explore(){
//     Action temp = null;
//     this.tempLo = getLo();
//     if (tempLo == 1){
//         this.exploreX = 5;
//         this.exploreY = 5;
//         temp = getNextMove(5,5, "explore quad 1");
//         return temp;
//     }
//     else if (tempLo == 2){
//         this.exploreX = 5;
//         this.exploreY = 45;
//         temp = getNextMove(5,45, "explore quad 2");
//     }   
//     else if (tempLo == 3){
//         this.exploreX = 45;
//         this.exploreY = 5;
//         temp = getNextMove(45, 5, "explore quad 3");
//     }
//     else if (tempLo == 4){
//         this.exploreX = 45;
//         this.exploreY = 45;
//         temp = getNextMove(45,45, "explore quad 4");
//     }
//     return temp;
    
//    }
    /**
     * This method is responsible for defining the exploration behavior of the agent.
     * The agent will explore the grid in a pattern defined by quadrants, where each
     * quadrant has a specific target position to move towards. Once the agent reaches all corners,
     * it will follow a new exploration path which is a square that has a smaller "radius". This repeats until
     * all packages are found.
     *
     * @return The next action the agent should take in order to explore the grid.
    */
    private Action explore(){
        Action temp = null;
        this.tempLo = getLo();

        // If the exploration index exceeds 6, reset it and update the counter
        if (exploreIdx > 6){
            counter = counter + 11;
            exploreIdx = 0;
        }

        // Define target positions for each quadrant and get the next move
        if (tempLo == 1){
            this.exploreX = 5 + counter;
            this.exploreY = 5 + counter;
            temp = getNextMove(this.exploreX, this.exploreY, "explore quad 1");
        }
        else if (tempLo == 2){
            this.exploreX = 5 + counter;
            this.exploreY = (worldSize - 5) - counter;
            temp = getNextMove(this.exploreX, this.exploreY, "explore quad 2");
        }   
        else if (tempLo == 3){
            this.exploreX = (worldSize - 5) - counter;
            this.exploreY = 5 + counter;
            temp = getNextMove(this.exploreX, this.exploreY, "explore quad 3");
        }
        else if (tempLo == 4){
            this.exploreX = (worldSize - 5) - counter;
            this.exploreY = (worldSize - 5) - counter;
            temp = getNextMove(this.exploreX, this.exploreY, "explore quad 4");
        }
        return temp;
        
    }

    /**
     * Determines the next exploration quadrant for the agent based on its current position
     * and exploration coordinates.
     *
     * @return The next exploration quadrant (1, 2, 3, or 4) or the current quadrant if it
     *         hasn't been fully explored yet.
     */
    private int getLo(){
        // If the agent is in quadrant 1 and has reached or passed the exploration point
        if (tempLo == 1){
            if(this.X <= this.exploreX && this.Y <= this.exploreY){
                exploreIdx++;
                return 2;
            }
        }
        // If the agent is in quadrant 2 and has reached or passed the exploration point
        else if(tempLo == 2){
            if(this.X <= this.exploreX && this.Y >= this.exploreY){
                exploreIdx++;
                return 4;
            }
        }
        // If the agent is in quadrant 3 and has reached or passed the exploration point
        else if(tempLo == 3){
            if(this.X >= this.exploreX && this.Y <= this.exploreY){
                exploreIdx++;
                return 1;
            }
        }
        // If the agent is in quadrant 4 and has reached or passed the exploration point
        else if(tempLo == 4){
            if(this.X >= this.exploreX && this.Y >= this.exploreY){
                exploreIdx++;
                return 3;
            }
        }
        return this.tempLo;
    }

    // Previous getLo function, saved in case of breakage
//    private int getLo(){
//     if (this.X == this.exploreX && this.Y == this.exploreY){
//         if (tempLo == 1){
//             return 2;
//         }
//         else if(tempLo == 2){
//             return 4;
//         }
//         else if(tempLo == 3){
//             return 1;
//         }
//         else if(tempLo == 4){
//             return 3;
//         }
//     }
//     else{
//         return this.tempLo;
//     }
//    }

    /**
     * Retrieves the identifier of the agent.
     *
     * @return The agent's identifier as a string.
     */
    public String getId() {
        return id;
    }    

    // This is code that I tried to get running that uses Dijkstra's algorithm to find the shortest path to the target.
    // This did not work even after debugging for hours, so I opted to use a simpler pathfinder

//    public Move shortestPath(int startX, int startY, int endX, int endY) {
//         int[][] distances = new int[width][height];
//         boolean[][] visited = new boolean[width][height];
//         int[][] prev = new int[width][height];

//         for (int i = 0; i < width; i++) {
//             Arrays.fill(distances[i], Integer.MAX_VALUE);
//         }
//         distances[startX][startY] = 0;

//         PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[2] - b[2]);
//         pq.offer(new int[]{startX, startY, 0});

//         while (!pq.isEmpty()) {
//             int[] current = pq.poll();
//             int x = current[0];
//             int y = current[1];
//             if (visited[x][y]) continue;
//             visited[x][y] = true;
//             if (x == endX && y == endY) break;
//             for (int i = 0; i < directions.length; i++) {
//                 int newX = x + directions[i][0];
//                 int newY = y + directions[i][1];
//                 if (newX < 0 || newX >= width || newY < 0 || newY >= height) continue;
//                 if (grid[newX][newY] == 0) continue;
//                 int newDist = distances[x][y] + grid[newX][newY];
//                 if (newDist < distances[newX][newY]) {
//                     distances[newX][newY] = newDist;
//                     prev[newX][newY] = x * height + y;
//                     pq.offer(new int[]{newX, newY, newDist});
//                 }
//             }
//         }

//         int x = endX;
//         int y = endY;
//         while (x != startX || y != startY) {
//             int prevX = prev[x][y] / height;
//             int prevY = prev[x][y] % height;
//             int moveDir = -1;
//             for (int i = 0; i < directions.length; i++) {
//                 int newX = x + directions[i][0];
//                 int newY = y + directions[i][1];
//                 if (newX == prevX && newY == prevY) {
//                     moveDir = i;
//                     break;
//                 }
//             }
//             if (moveDir != -1) {
//                 return moves[moveDir];
//             }
//             x = prevX;
//             y = prevY;
//         }
//         // If the starting and ending positions are the same, return a dummy move.
//         return moves[0];
//     }


}
