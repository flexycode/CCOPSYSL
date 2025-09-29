import java.util.*;

public class SNFN {
    static class Process {
        String name;
        int arrivalTime;
        int burstTime;
        int priority;
        int remainingTime;
        int completionTime;
        int turnaroundTime;
        int waitingTime;
        int executedTime;

        public Process(String name, int arrivalTime, int burstTime, int priority) {
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.priority = priority;
            this.remainingTime = burstTime;
            this.executedTime = 0;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean tryAgain = true;

        while (tryAgain) {
            System.out.print("Enter the number of processes (3-5): ");
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
                System.out.print("  Priority: ");
                int priority = scanner.nextInt();

                processes[i] = new Process("P" + (i + 1), arrivalTime, burstTime, priority);
            }

            // Run Priority algorithm
            calculatePriority(processes);

            // Ask if user wants to try again
            System.out.print("\nDo you want to try again? (yes/no): ");
            String response = scanner.next().toLowerCase();
            tryAgain = response.equals("yes") || response.equals("y");
        }

        scanner.close();
        System.out.println("Program ended. Thank you!");
    }

    public static void calculatePriority(Process[] processes) {
        int n = processes.length;
        int currentTime = 0;
        int completedProcesses = 0;

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
                    processes[i].burstTime,
                    processes[i].priority
            );
            processesCopy[i].remainingTime = processes[i].burstTime;
        }

        // Sort by arrival time for initial processing
        Arrays.sort(processesCopy, Comparator.comparingInt(p -> p.arrivalTime));

        Process currentProcess = null;

        while (completedProcesses < n) {
            // Check for new arrivals and update priorities
            Process highestPriorityProcess = null;
            for (Process p : processesCopy) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                    if (highestPriorityProcess == null || p.priority < highestPriorityProcess.priority) {
                        highestPriorityProcess = p;
                    }
                }
            }

            if (highestPriorityProcess != null) {
                if (currentProcess != highestPriorityProcess) {
                    if (currentProcess != null && currentProcess.remainingTime > 0) {
                        // Record the end of the current process execution
                        ganttTimes.add(currentTime);
                        ganttProcesses.add(currentProcess.name);
                    }
                    currentProcess = highestPriorityProcess;
                }

                // Execute for 1 time unit
                currentProcess.remainingTime--;
                currentProcess.executedTime++;
                currentTime++;

                if (currentProcess.remainingTime == 0) {
                    currentProcess.completionTime = currentTime;
                    currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                    currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                    completedProcesses++;

                    ganttTimes.add(currentTime);
                    ganttProcesses.add(currentProcess.name);
                    currentProcess = null;
                }
            } else {
                currentTime++;
            }
        }

        // Display results table
        System.out.println("\n+-------+--------------+-------------+----------+----------------+-----------------+--------------+");
        System.out.println("| PID   | Arrival Time | Burst Time  | Priority | Completion Time| Turnaround Time | Waiting Time |");
        System.out.println("+-------+--------------+-------------+----------+----------------+-----------------+--------------+");

        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;

        // Reset to original order for display
        Arrays.sort(processesCopy, Comparator.comparing(p -> p.name));

        for (Process p : processesCopy) {
            System.out.printf("| %-5s | %11d  | %10d  | %7d  | %13d  | %14d  | %11d  |\n",
                    p.name, p.arrivalTime, p.burstTime, p.priority,
                    p.completionTime, p.turnaroundTime, p.waitingTime);

            totalTurnaroundTime += p.turnaroundTime;
            totalWaitingTime += p.waitingTime;
        }

        System.out.println("+-------+--------------+-------------+----------+----------------+-----------------+--------------+");

        // Calculate and display averages
        double avgTurnaroundTime = totalTurnaroundTime / n;
        double avgWaitingTime = totalWaitingTime / n;

        System.out.println("\nPERFORMANCE METRICS");
        System.out.println("=====================================");
        System.out.printf("Average Turnaround Time: %.2f ms\n", avgTurnaroundTime);
        System.out.printf("Average Waiting Time: %.2f ms\n", avgWaitingTime);
        System.out.println("=====================================");

        // Display Gantt chart in the correct format
        System.out.println("\nGantt Chart:");
        System.out.print("Proc:  ");
        for (int i = 0; i < ganttProcesses.size(); i++) {
            System.out.printf("%-4s", ganttProcesses.get(i));
            if (i < ganttProcesses.size() - 1) {
                System.out.print("| ");
            }
        }
        System.out.print("\nTime:  ");
        for (Integer ganttTime : ganttTimes) {
            System.out.printf("%-4d", ganttTime);
        }
        System.out.println();
    }
}