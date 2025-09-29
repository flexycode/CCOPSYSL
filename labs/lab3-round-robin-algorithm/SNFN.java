import java.util.*;

public class SNFN {
    static class Process {
        String name;
        int arrivalTime;
        int burstTime;
        int remainingTime;
        int completionTime;
        int turnaroundTime;
        int waitingTime;

        public Process(String name, int arrivalTime, int burstTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.remainingTime = burstTime;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean tryAgain = true;

        while (tryAgain) {
            System.out.println("=====================================");
            System.out.println("   ROUND ROBIN SCHEDULING ALGORITHM");
            System.out.println("   Operating Systems Lab - ACT3");
            System.out.println("=====================================\n");

            // Get number of processes from user (3 to 5)
            int n = 0;
            while (n < 3 || n > 5) {
                System.out.print("Enter the number of processes (3-5): ");
                n = scanner.nextInt();
                if (n < 3 || n > 5) {
                    System.out.println("Please enter between 3 to 5 processes.");
                }
            }

            // Get time quantum
            System.out.print("Enter Quantum Time: ");
            int quantumTime = scanner.nextInt();

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

                processes[i] = new Process("P" + (i + 1), arrivalTime, burstTime);
            }

            // Run Round Robin algorithm
            calculateRoundRobin(processes, quantumTime);

            // Ask if user wants to try again
            System.out.print("\nDo you want to try again? (yes/no): ");
            String response = scanner.next().toLowerCase();
            tryAgain = response.equals("yes") || response.equals("y");
        }

        scanner.close();
        System.out.println("Program ended. Thank you!");
    }

    public static void calculateRoundRobin(Process[] processes, int quantumTime) {
        int n = processes.length;
        int time = 0;
        int remainingProcesses = n;

        // For Gantt chart
        List<String> ganttProcesses = new ArrayList<>();
        List<Integer> ganttTimes = new ArrayList<>();
        ganttTimes.add(0);

        // Create a copy of processes to preserve original data
        Process[] processesCopy = new Process[n];
        for (int i = 0; i < n; i++) {
            processesCopy[i] = new Process(
                    processes[i].name,
                    processes[i].arrivalTime,
                    processes[i].burstTime
            );
            processesCopy[i].remainingTime = processes[i].burstTime;
        }

        System.out.println("\n=====================================");
        System.out.println("ROUND ROBIN SCHEDULING");
        System.out.println("Time Quantum: " + quantumTime);
        System.out.println("=====================================");

        // Main scheduling loop
        while (remainingProcesses > 0) {
            boolean progressMade = false;

            for (int i = 0; i < n; i++) {
                Process p = processesCopy[i];

                if (p.remainingTime > 0 && p.arrivalTime <= time) {
                    progressMade = true;

                    // Add to Gantt chart
                    ganttProcesses.add(p.name);

                    if (p.remainingTime > quantumTime) {
                        time += quantumTime;
                        p.remainingTime -= quantumTime;
                    } else {
                        time += p.remainingTime;
                        p.remainingTime = 0;
                        p.completionTime = time;
                        p.turnaroundTime = p.completionTime - p.arrivalTime;
                        p.waitingTime = p.turnaroundTime - p.burstTime;
                        remainingProcesses--;
                    }

                    // Add end time to Gantt chart
                    ganttTimes.add(time);
                }
            }

            // If no progress was made, advance time
            if (!progressMade) {
                time++;
            }
        }

        // Display results table
        System.out.println("\n+-------+--------------+-------------+----------------+-----------------+--------------+");
        System.out.println("| PID   | Arrival Time | Burst Time  | Completion Time| Turnaround Time | Waiting Time |");
        System.out.println("+-------+--------------+-------------+----------------+-----------------+--------------+");

        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;

        for (Process p : processesCopy) {
            System.out.printf("| %-5s | %11d  | %10d  | %13d  | %14d  | %11d  |\n",
                    p.name, p.arrivalTime, p.burstTime,
                    p.completionTime, p.turnaroundTime, p.waitingTime);

            totalTurnaroundTime += p.turnaroundTime;
            totalWaitingTime += p.waitingTime;
        }

        System.out.println("+-------+--------------+-------------+----------------+-----------------+--------------+");

        // Calculate and display averages
        double avgTurnaroundTime = totalTurnaroundTime / n;
        double avgWaitingTime = totalWaitingTime / n;

        System.out.println("\n=====================================");
        System.out.println("PERFORMANCE METRICS");
        System.out.println("=====================================");
        System.out.printf("Average Turnaround Time: %.2f units\n", avgTurnaroundTime);
        System.out.printf("Average Waiting Time: %.2f units\n", avgWaitingTime);
        System.out.println("=====================================");

        // Display Gantt chart
        System.out.println("\nGantt Chart:");
        System.out.print("Time:  ");
        for (int i = 0; i < ganttTimes.size(); i++) {
            System.out.printf("%-4d", ganttTimes.get(i));
        }
        System.out.print("\nProc:  ");
        for (int i = 0; i < ganttProcesses.size(); i++) {
            System.out.printf("%-4s", ganttProcesses.get(i));
        }
        System.out.println();
    }
}