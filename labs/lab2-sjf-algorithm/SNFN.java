import java.util.*;

/**
 * SJF (Shortest Job First) Scheduling Algorithm Implementation
 * This program implements both Non-Preemptive and Preemptive versions
 * Students: Jay Arre Talosig & Ruth Margel Mendoza
 * Subject: CCOPSYSL - Operating Systems
 * Activity: LAB ACT2 - SJF Algorithm
 */

// Process class to represent each process with its attributes
class Process {
    int processId;          // Unique identifier for the process
    int arrivalTime;        // Time at which process arrives in ready queue
    int burstTime;          // CPU burst time required by the process
    int remainingTime;      // Remaining burst time (used in preemptive)
    int completionTime;     // Time at which process completes execution
    int turnaroundTime;     // Total time from arrival to completion
    int waitingTime;        // Time spent waiting in ready queue

    /**
     * Constructor to initialize a process
     * @param processId - unique process identifier
     * @param arrivalTime - arrival time of process
     * @param burstTime - CPU burst time required
     */
    public Process(int processId, int arrivalTime, int burstTime) {
        this.processId = processId;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.completionTime = 0;
        this.turnaroundTime = 0;
        this.waitingTime = 0;
    }
}

/**
 * Main class implementing SJF Scheduling Algorithm
 */
public class SNFN {

    /**
     * Main method - entry point of the program
     * Provides menu-driven interface for SJF scheduling
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=====================================");
        System.out.println("   SJF SCHEDULING ALGORITHM");
        System.out.println("   Operating Systems Lab - ACT2");
        System.out.println("=====================================\n");

        // Get number of processes from user
        System.out.print("Enter the number of processes: ");
        int n = scanner.nextInt();

        // Create array to store processes
        Process[] processes = new Process[n];

        // Input process details from user
        System.out.println("\nEnter process details:");
        for (int i = 0; i < n; i++) {
            System.out.println("\nProcess " + (i + 1) + ":");
            System.out.print("  Arrival Time: ");
            int arrivalTime = scanner.nextInt();
            System.out.print("  Burst Time: ");
            int burstTime = scanner.nextInt();

            processes[i] = new Process(i + 1, arrivalTime, burstTime);
        }

        // Menu for selecting scheduling type
        System.out.println("\n=====================================");
        System.out.println("Select Scheduling Type:");
        System.out.println("1. Non-Preemptive SJF");
        System.out.println("2. Preemptive SJF (SRTF)");
        System.out.print("Enter your choice (1 or 2): ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                nonPreemptiveSJF(processes);
                break;
            case 2:
                preemptiveSJF(processes);
                break;
            default:
                System.out.println("Invalid choice! Running Non-Preemptive SJF by default.");
                nonPreemptiveSJF(processes);
        }

        scanner.close();
    }

    /**
     * Implements Non-Preemptive SJF Scheduling
     * Once a process starts executing, it runs to completion
     * @param processes - array of processes to schedule
     */
    public static void nonPreemptiveSJF(Process[] processes) {
        int n = processes.length;
        boolean[] completed = new boolean[n];  // Track completed processes
        int currentTime = 0;                   // Current system time
        int completedCount = 0;                // Number of completed processes

        System.out.println("\n=====================================");
        System.out.println("NON-PREEMPTIVE SJF SCHEDULING");
        System.out.println("=====================================");
        System.out.println("\nGantt Chart:");
        System.out.print("| ");

        // Continue until all processes are completed
        while (completedCount < n) {
            int shortestIndex = -1;
            int shortestBurst = Integer.MAX_VALUE;

            // Find the process with the shortest burst time among arrived processes
            for (int i = 0; i < n; i++) {
                if (!completed[i] &&
                        processes[i].arrivalTime <= currentTime &&
                        processes[i].burstTime < shortestBurst) {
                    shortestBurst = processes[i].burstTime;
                    shortestIndex = i;
                }
            }

            // If no process has arrived yet, advance time
            if (shortestIndex == -1) {
                currentTime++;
                continue;
            }

            // Execute the selected process
            Process currentProcess = processes[shortestIndex];
            System.out.print("P" + currentProcess.processId + " | ");

            // Update current time and process completion details
            currentTime += currentProcess.burstTime;
            currentProcess.completionTime = currentTime;
            currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
            currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;

            completed[shortestIndex] = true;
            completedCount++;
        }

        System.out.println("\n");

        // Display results
        displayResults(processes);
    }

    /**
     * Implements Preemptive SJF (Shortest Remaining Time First - SRTF)
     * Process with the shortest remaining time gets CPU, can preempt running process
     * @param processes - array of processes to schedule
     */
    public static void preemptiveSJF(Process[] processes) {
        int n = processes.length;
        int currentTime = 0;
        int completedCount = 0;
        int prevProcess = -1;

        // Create a copy to preserve original burst times
        Process[] processesCopy = new Process[n];
        for (int i = 0; i < n; i++) {
            processesCopy[i] = new Process(
                    processes[i].processId,
                    processes[i].arrivalTime,
                    processes[i].burstTime
            );
        }

        System.out.println("\n=====================================");
        System.out.println("PREEMPTIVE SJF SCHEDULING");
        System.out.println("=====================================");
        System.out.println("\nExecution Timeline:");

        // Continue until all processes complete
        while (completedCount < n) {
            int shortestIndex = -1;
            int shortestRemaining = Integer.MAX_VALUE;

            // Find process with shortest remaining time
            for (int i = 0; i < n; i++) {
                if (processesCopy[i].arrivalTime <= currentTime &&
                        processesCopy[i].remainingTime > 0 &&
                        processesCopy[i].remainingTime < shortestRemaining) {
                    shortestRemaining = processesCopy[i].remainingTime;
                    shortestIndex = i;
                }
            }

            // If no process has arrived, advance time
            if (shortestIndex == -1) {
                currentTime++;
                continue;
            }

            // Print process switch if different from previous
            if (prevProcess != shortestIndex) {
                System.out.println("Time " + currentTime + ": Process P" +
                        processesCopy[shortestIndex].processId + " starts/resumes");
                prevProcess = shortestIndex;
            }

            // Execute current process for one time unit
            processesCopy[shortestIndex].remainingTime--;
            currentTime++;

            // Check if process completed
            if (processesCopy[shortestIndex].remainingTime == 0) {
                completedCount++;
                processesCopy[shortestIndex].completionTime = currentTime;
                processesCopy[shortestIndex].turnaroundTime =
                        processesCopy[shortestIndex].completionTime -
                                processesCopy[shortestIndex].arrivalTime;
                processesCopy[shortestIndex].waitingTime =
                        processesCopy[shortestIndex].turnaroundTime -
                                processesCopy[shortestIndex].burstTime;

                System.out.println("Time " + currentTime + ": Process P" +
                        processesCopy[shortestIndex].processId + " completed");
            }
        }

        System.out.println();

        // Display results
        displayResults(processesCopy);
    }

    /**
     * Displays the scheduling results in a formatted table
     * Shows process details and calculates average times
     * @param processes - array of scheduled processes
     */
    public static void displayResults(Process[] processes) {
        System.out.println("=====================================");
        System.out.println("SCHEDULING RESULTS");
        System.out.println("=====================================");

        // Table header
        System.out.println("\n+-------+---------+-------+------------+------------+---------+");
        System.out.println("| PID   | Arrival | Burst | Completion | Turnaround | Waiting |");
        System.out.println("+-------+---------+-------+------------+------------+---------+");

        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;

        // Display each process details
        for (Process p : processes) {
            System.out.printf("| P%-4d | %7d | %5d | %10d | %10d | %7d |\n",
                    p.processId, p.arrivalTime, p.burstTime,
                    p.completionTime, p.turnaroundTime, p.waitingTime);

            totalTurnaroundTime += p.turnaroundTime;
            totalWaitingTime += p.waitingTime;
        }

        System.out.println("+-------+---------+-------+------------+------------+---------+");

        // Calculate and display averages
        double avgTurnaroundTime = totalTurnaroundTime / processes.length;
        double avgWaitingTime = totalWaitingTime / processes.length;

        System.out.println("\n=====================================");
        System.out.println("PERFORMANCE METRICS");
        System.out.println("=====================================");
        System.out.printf("Average Turnaround Time: %.2f units\n", avgTurnaroundTime);
        System.out.printf("Average Waiting Time: %.2f units\n", avgWaitingTime);
        System.out.println("=====================================");

        // Additional analysis
        System.out.println("\nALGORITHM ANALYSIS:");
        System.out.println("- SJF minimizes average waiting time");
        System.out.println("- Non-preemptive: Simple but may cause convoy effect");
        System.out.println("- Preemptive (SRTF): Better response time but more overhead");
        System.out.println("- Time Complexity: O(n²) for process selection");
    }
}