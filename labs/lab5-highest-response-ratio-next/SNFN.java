import java.util.*;

public class SNFN {
    static class Process {
        String name;
        int arrivalTime;
        int burstTime;
        int completionTime;
        int turnaroundTime;
        int waitingTime;
        int responseTime;
        boolean started;

        public Process(String name, int arrivalTime, int burstTime) {
            this.name = name;
            this.arrivalTime = arrivalTime;
            this.burstTime = burstTime;
            this.started = false;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean tryAgain = true;

        while (tryAgain) {
            System.out.print("Enter the number of processes (3-5): ");
            int n = scanner.nextInt();

            // Validate input range
            if (n < 3 || n > 5) {
                System.out.println("Please enter 3 to 5 processes only.");
                continue;
            }

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

            // Run HRRN algorithm
            calculateHRRN(processes);

            // Ask if user wants to try again
            System.out.print("\nDo you want to try again? (yes/no): ");
            String response = scanner.next().toLowerCase();
            tryAgain = response.equals("yes") || response.equals("y");
        }

        scanner.close();
        System.out.println("Program ended. Thank you!");
    }

    public static void calculateHRRN(Process[] processes) {
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
                    processes[i].burstTime
            );
        }

        while (completedProcesses < n) {
            Process selectedProcess = null;
            double highestResponseRatio = -1;

            // Find process with highest response ratio that has arrived and not completed
            for (Process p : processesCopy) {
                if (p.completionTime == 0 && p.arrivalTime <= currentTime) {
                    int waitingTime = currentTime - p.arrivalTime;
                    double responseRatio = (double) (waitingTime + p.burstTime) / p.burstTime;

                    if (responseRatio > highestResponseRatio) {
                        highestResponseRatio = responseRatio;
                        selectedProcess = p;
                    }
                }
            }

            // If no process found, advance time
            if (selectedProcess == null) {
                currentTime++;
                continue;
            }

            // Record start time for response time calculation (first time process runs)
            if (!selectedProcess.started) {
                selectedProcess.responseTime = currentTime - selectedProcess.arrivalTime;
                selectedProcess.started = true;
            }

            // Add to Gantt chart
            ganttProcesses.add(selectedProcess.name);
            ganttTimes.add(currentTime);

            // Execute the selected process to completion (non-preemptive)
            currentTime += selectedProcess.burstTime;
            selectedProcess.completionTime = currentTime;
            selectedProcess.turnaroundTime = selectedProcess.completionTime - selectedProcess.arrivalTime;
            selectedProcess.waitingTime = selectedProcess.turnaroundTime - selectedProcess.burstTime;
            completedProcesses++;

            // Add completion time to Gantt chart
            ganttTimes.add(currentTime);
        }

        // Display results table
        System.out.println("\n+-------+--------------+-------------+----------------+-----------------+--------------+--------------+");
        System.out.println("| PID   | Arrival Time | Burst Time  | Completion Time| Turnaround Time | Waiting Time | Response Time|");
        System.out.println("+-------+--------------+-------------+----------------+-----------------+--------------+--------------+");

        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;
        double totalResponseTime = 0;

        // Sort by process name for display
        Arrays.sort(processesCopy, Comparator.comparing(p -> p.name));

        for (Process p : processesCopy) {
            System.out.printf("| %-5s | %11d  | %10d  | %13d  | %14d  | %11d  | %11d  |\n",
                    p.name, p.arrivalTime, p.burstTime,
                    p.completionTime, p.turnaroundTime, p.waitingTime, p.responseTime);

            totalTurnaroundTime += p.turnaroundTime;
            totalWaitingTime += p.waitingTime;
            totalResponseTime += p.responseTime;
        }

        System.out.println("+-------+--------------+-------------+----------------+-----------------+--------------+--------------+");

        // Calculate and display averages
        double avgTurnaroundTime = totalTurnaroundTime / n;
        double avgWaitingTime = totalWaitingTime / n;
        double avgResponseTime = totalResponseTime / n;

        System.out.println("\nPERFORMANCE METRICS");
        System.out.println("=====================================");
        System.out.printf("Average Turnaround Time: %.2f ms\n", avgTurnaroundTime);
        System.out.printf("Average Waiting Time: %.2f ms\n", avgWaitingTime);
        System.out.printf("Average Response Time: %.2f ms\n", avgResponseTime);
        System.out.println("=====================================");

        // Display Gantt chart without duplicates
        System.out.println("\nGantt Chart:");
        System.out.print("Proc:  ");
        for (int i = 0; i < ganttProcesses.size(); i++) {
            System.out.printf("%-4s", ganttProcesses.get(i));
            if (i < ganttProcesses.size() - 1) {
                System.out.print("| ");
            }
        }
        System.out.print("\nTime:  ");
        // Remove duplicate times from Gantt chart
        List<Integer> uniqueTimes = new ArrayList<>();
        for (int i = 0; i < ganttTimes.size(); i++) {
            if (i == 0 || !ganttTimes.get(i).equals(ganttTimes.get(i - 1))) {
                uniqueTimes.add(ganttTimes.get(i));
            }
        }
        for (int time : uniqueTimes) {
            System.out.printf("%-4d", time);
        }
        System.out.println();
    }
}