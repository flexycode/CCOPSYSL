import java.util.*;
import java.util.InputMismatchException; // Import for catching the error

/**
 * This class implements the First-Come, First-Served (FCFS) CPU scheduling algorithm.
 * It takes a set of processes with their arrival and burst times, calculates scheduling metrics
 * like turnaround and waiting times, and displays the results along with a Gantt chart.
 * REVISED: Added input validation to handle non-integer inputs gracefully.
 */
public class SNFN {

    // The Process class remains unchanged.
    static class Process {
        String name;
        int arrivalTime;
        int burstTime;
        int startTime;
        int completionTime;
        int turnaroundTime;
        int waitingTime;

        Process(String name, int arrivalTime, int burstTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
        }
    }

    // The main method remains largely the same.
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String continueChoice;

        do {
            System.out.println("=== FCFS SCHEDULING ALGORITHM ===\n");

            int numProcesses = getNumberOfProcesses(scanner);
            Process[] processes = new Process[numProcesses];

            inputProcessDetails(scanner, processes, numProcesses);
            sortProcessesByArrivalTime(processes);
            calculateSchedulingMetrics(processes);
            displayResults(processes);
            displayGanttChart(processes);

            System.out.print("\nDo you want to try again? (yes/no): ");
            continueChoice = scanner.next().toLowerCase();
            System.out.println();

        } while (continueChoice.equals("yes") || continueChoice.equals("y"));

        System.out.println("Thank you for using FCFS Scheduling Algorithm!");
        scanner.close();
    }

    /**
     * REVISED: Prompts the user to enter the number of processes and validates the input.
     * It now catches InputMismatchException if the user enters a non-integer value.
     * The number must be an integer between 3 and 5, inclusive.
     * @param scanner The Scanner object to read user input.
     * @return The validated number of processes.
     */
    private static int getNumberOfProcesses(Scanner scanner) {
        int numProcesses = 0; // Initialize to 0

        do {
            try {
                System.out.print("Enter number of processes (3-5): ");
                numProcesses = scanner.nextInt();

                if (numProcesses < 3 || numProcesses > 5) {
                    System.out.println("Error: Please enter a number between 3 and 5.");
                }
            } catch (InputMismatchException e) {
                // This block runs if the user enters something that is not an integer (e.g., "a").
                System.out.println("Error: Invalid input. Please enter a valid number.");
                scanner.next(); // Important: Clears the invalid input from the scanner.
                numProcesses = 0; // Reset to ensure the loop continues.
            }
        } while (numProcesses < 3 || numProcesses > 5);

        return numProcesses;
    }

    /**
     * NEW HELPER FUNCTION: A robust method to get a valid integer from the user.
     * It will keep prompting until a valid integer is entered.
     * @param scanner The Scanner object to read input.
     * @param prompt The message to display to the user.
     * @return A validated integer from the user.
     */
    private static int getValidIntegerInput(Scanner scanner, String prompt) {
        while (true) { // Loop indefinitely until a valid integer is returned.
            try {
                System.out.print(prompt);
                return scanner.nextInt(); // If successful, return the integer and exit the loop.
            } catch (InputMismatchException e) {
                System.out.println("Error: Invalid input. Please enter a whole number.");
                scanner.next(); // Clear the invalid input to prevent an infinite loop.
            }
        }
    }


    /**
     * REVISED: Collects the arrival and burst times for each process from the user.
     * This method now uses the getValidIntegerInput() helper function for robust input validation.
     * @param scanner The Scanner object to read user input.
     * @param processes The array to store the Process objects.
     * @param numProcesses The total number of processes to input.
     */
    private static void inputProcessDetails(Scanner scanner, Process[] processes, int numProcesses) {
        System.out.println("\nEnter process details:");

        for (int i = 0; i < numProcesses; i++) {
            String processName = "P" + (i + 1);

            // Use the new helper function to ensure valid integer input
            int arrivalTime = getValidIntegerInput(scanner, "Enter Arrival Time for " + processName + ": ");
            int burstTime = getValidIntegerInput(scanner, "Enter Burst Time for " + processName + ": ");

            processes[i] = new Process(processName, arrivalTime, burstTime);
        }
    }

    // The functions below this line did not need to be changed.

    /**
     * Sorts the array of processes based on their arrival time in ascending order.
     * This is the core logic step for the FCFS algorithm. It also renames the processes
     * sequentially (P1, P2, etc.) after sorting to maintain a clear order.
     * @param processes The array of Process objects to be sorted.
     */
    private static void sortProcessesByArrivalTime(Process[] processes) {
        Arrays.sort(processes, new Comparator<Process>() {
            @Override
            public int compare(Process p1, Process p2) {
                return Integer.compare(p1.arrivalTime, p2.arrivalTime);
            }
        });

        for (int i = 0; i < processes.length; i++) {
            processes[i].name = "P" + (i + 1);
        }
    }

    /**
     * Calculates the scheduling metrics for each process after they have been sorted.
     * It determines the start, completion, turnaround, and waiting times.
     * @param processes The sorted array of Process objects.
     */
    private static void calculateSchedulingMetrics(Process[] processes) {
        int currentTime = 0;

        for (int i = 0; i < processes.length; i++) {
            Process process = processes[i];

            if (currentTime < process.arrivalTime) {
                currentTime = process.arrivalTime;
            }

            process.startTime = currentTime;
            process.completionTime = process.startTime + process.burstTime;
            process.turnaroundTime = process.completionTime - process.arrivalTime;
            process.waitingTime = process.turnaroundTime - process.burstTime;

            currentTime = process.completionTime;
        }
    }

    /**
     * Displays the final results in a formatted table. It shows all process details
     * and scheduling metrics, along with the average turnaround and waiting times.
     * @param processes The array of processes with calculated metrics.
     */
    private static void displayResults(Process[] processes) {
        System.out.println("\n=== SCHEDULING RESULTS ===");
        System.out.printf("%-8s %-12s %-10s %-15s %-15s %-12s%n",
                "Process", "Arrival Time", "Burst Time", "Completion Time",
                "Turnaround Time", "Waiting Time");
        System.out.println("--------------------------------------------------------------------------------");

        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;

        for (Process process : processes) {
            System.out.printf("%-8s %-12d %-10d %-15d %-15d %-12d%n",
                    process.name, process.arrivalTime, process.burstTime,
                    process.completionTime, process.turnaroundTime, process.waitingTime);

            totalTurnaroundTime += process.turnaroundTime;
            totalWaitingTime += process.waitingTime;
        }

        double avgTurnaroundTime = totalTurnaroundTime / processes.length;
        double avgWaitingTime = totalWaitingTime / processes.length;

        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Average Turnaround Time: %.2f%n", avgTurnaroundTime);
        System.out.printf("Average Waiting Time: %.2f%n", avgWaitingTime);
    }

    /**
     * Displays a simple text-based Gantt chart to visualize the process execution sequence.
     * The chart shows the order of processes and their completion times.
     * @param processes The sorted array of processes that have been scheduled.
     */
    private static void displayGanttChart(Process[] processes) {
        System.out.println("\n=== GANTT CHART ===");

        System.out.print("|");
        for (Process process : processes) {
            System.out.printf(" %s |", process.name);
        }
        System.out.println();

        System.out.print(processes[0].startTime);
        for (Process process : processes) {
            int digits = String.valueOf(process.completionTime).length();
            int spaces = 4 - digits;
            for (int i = 0; i < spaces; i++) {
                System.out.print(" ");
            }
            System.out.print(process.completionTime);
        }
        System.out.println();
    }
}