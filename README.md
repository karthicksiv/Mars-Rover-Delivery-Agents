# Package-Delivery-Agents

## Project Description

This project involves designing a team of homogeneous agents that cooperate to complete a package delivery task. The agents are placed in a 50x50 grid with randomly located packages and delivery locations. The objective is to handle any number of packages, destinations, and team sizes.

### PacPercept
Each agent receives a PacPercept which has the following methods:

public VisibleAgent[] getVisAgents()
public VisiblePackage[] getVisPackages()
public String[] getMessages()
public boolean feelBump()
public VisiblePackage getHeldPackage()

### Actions
Available actions for each agent are:

Dropoff
Idle
Move
Pickup
Say

### Simulator Execution
To get started, you can clone this repository and run the code on your local machine. The code is well-documented, making it easy to understand and modify. You can experiment with different configurations and parameters to test the performance of your agent team.

To start the simulator with n agents defined in a package userid, p packages, and d destinations, type:

java pacworld.PackageWorld kas221 n p d

Contributions to this project are welcome. If you have any suggestions or improvements, please submit a pull request.





