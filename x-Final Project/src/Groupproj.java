import java.util.*;

public class Groupproj {

    // ==================== PROCESS CLASS ====================
    static class Process {
        private int id;
        private int[] allocated;
        private int[] maximum;
        private int[] need;
        private boolean finished;

        public Process(int id, int[] allocated, int[] maximum) {
            this.id = id;
            this.allocated = allocated.clone();
            this.maximum = maximum.clone();
            this.need = new int[allocated.length];
            this.finished = false;

            // Calculate need matrix
            for (int i = 0; i < allocated.length; i++) {
                need[i] = maximum[i] - allocated[i];
            }
        }

        public int getId() { return id; }
        public int[] getAllocated() { return allocated; }
        public int[] getMaximum() { return maximum; }
        public int[] getNeed() { return need; }
        public boolean isFinished() { return finished; }
        public void setFinished(boolean finished) { this.finished = finished; }

        public boolean canBeSatisfied(int[] available) {
            for (int i = 0; i < need.length; i++) {
                if (need[i] > available[i]) {
                    return false;
                }
            }
            return true;
        }

        public void releaseResources(int[] available) {
            for (int i = 0; i < allocated.length; i++) {
                available[i] += allocated[i];
                allocated[i] = 0;
            }
            finished = true;
        }

        @Override
        public String toString() {
            return "P" + id;
        }
    }

    // ==================== BANKER'S ALGORITHM ====================
    static class BankersAlgorithm {
        private Process[] processes;
        private int[] available;
        private int resourceCount;

        public BankersAlgorithm(Process[] processes, int[] available) {
            this.processes = processes;
            this.available = available.clone();
            this.resourceCount = available.length;
        }

        /**
         * Check if the system is in a safe state using Banker's Algorithm
         */
        public boolean isSafeState() {
            int[] work = available.clone();
            boolean[] finish = new boolean[processes.length];
            List<Process> safeSequence = new ArrayList<>();

            // Initialize finish array
            for (int i = 0; i < processes.length; i++) {
                finish[i] = processes[i].isFinished();
            }

            boolean found;
            do {
                found = false;
                for (int i = 0; i < processes.length; i++) {
                    if (!finish[i] && processes[i].canBeSatisfied(work)) {
                        // Process can be satisfied - simulate execution
                        processes[i].releaseResources(work);
                        finish[i] = true;
                        safeSequence.add(processes[i]);
                        found = true;
                        System.out.println("  Executing " + processes[i] + ", Work: " + Arrays.toString(work));
                    }
                }
            } while (found);

            // Check if all processes finished
            boolean allFinished = true;
            for (boolean f : finish) {
                if (!f) {
                    allFinished = false;
                    break;
                }
            }

            if (allFinished) {
                System.out.println("  ✓ System is in SAFE state");
                System.out.print("  Safe sequence: ");
                for (int i = 0; i < safeSequence.size(); i++) {
                    System.out.print(safeSequence.get(i));
                    if (i < safeSequence.size() - 1) System.out.print(" → ");
                }
                System.out.println();
            } else {
                System.out.println("  ✗ System is in UNSAFE state - DEADLOCK DETECTED!");
                System.out.print("  Deadlocked processes: ");
                for (int i = 0; i < processes.length; i++) {
                    if (!finish[i]) {
                        System.out.print("P" + processes[i].getId() + " ");
                    }
                }
                System.out.println();
            }

            return allFinished;
        }

        /**
         * Simulate resource request from a process
         */
        public boolean requestResources(int processId, int[] request) {
            Process process = processes[processId];
            int[] allocated = process.getAllocated();
            int[] need = process.getNeed();

            System.out.println("\n  === Resource Request Simulation ===");
            System.out.println("  " + process + " requests: " + Arrays.toString(request));

            // Step 1: Check if request <= need
            for (int i = 0; i < resourceCount; i++) {
                if (request[i] > need[i]) {
                    System.out.println("  Error: Request exceeds maximum claim");
                    return false;
                }
            }

            // Step 2: Check if request <= available
            for (int i = 0; i < resourceCount; i++) {
                if (request[i] > available[i]) {
                    System.out.println("  Process must wait - resources not available");
                    return false;
                }
            }

            // Step 3: Try to allocate resources temporarily
            for (int i = 0; i < resourceCount; i++) {
                available[i] -= request[i];
                allocated[i] += request[i];
                need[i] -= request[i];
            }

            // Step 4: Check if system remains in safe state
            System.out.println("  Checking if system remains safe after allocation...");
            boolean safe = isSafeState();

            if (!safe) {
                // Rollback allocation
                System.out.println("  Rolling back allocation - request would cause deadlock");
                for (int i = 0; i < resourceCount; i++) {
                    available[i] += request[i];
                    allocated[i] -= request[i];
                    need[i] += request[i];
                }
            } else {
                System.out.println("  Request granted safely");
            }

            return safe;
        }
    }

    // ==================== RESOURCE ALLOCATION GRAPH ====================
    static class ResourceAllocationGraph {
        private Map<Integer, List<Integer>> processToResource;
        private Map<Integer, List<Integer>> resourceToProcess;
        private Map<Integer, Integer> resourceInstances;

        public ResourceAllocationGraph() {
            processToResource = new HashMap<>();
            resourceToProcess = new HashMap<>();
            resourceInstances = new HashMap<>();
        }

        public void addProcess(int processId) {
            processToResource.put(processId, new ArrayList<>());
        }

        public void addResource(int resourceId, int instances) {
            resourceToProcess.put(resourceId, new ArrayList<>());
            resourceInstances.put(resourceId, instances);
        }

        public void addRequestEdge(int processId, int resourceId) {
            processToResource.get(processId).add(resourceId);
        }

        public void addAssignmentEdge(int resourceId, int processId) {
            resourceToProcess.get(resourceId).add(processId);
        }

        /**
         * Detect deadlock using cycle detection
         */
        public List<Integer> detectDeadlock() {
            Set<Integer> visited = new HashSet<>();
            Set<Integer> recursionStack = new HashSet<>();
            Map<Integer, Integer> parent = new HashMap<>();
            List<Integer> deadlockProcesses = new ArrayList<>();

            for (int processId : processToResource.keySet()) {
                if (!visited.contains(processId)) {
                    if (dfsCycleDetection(processId, visited, recursionStack, parent)) {
                        int cycleStart = findCycleStart(processId, parent);
                        reconstructCycle(cycleStart, parent, deadlockProcesses);
                        break;
                    }
                }
            }

            return deadlockProcesses;
        }

        private boolean dfsCycleDetection(int current, Set<Integer> visited,
                                          Set<Integer> recursionStack, Map<Integer, Integer> parent) {
            visited.add(current);
            recursionStack.add(current);

            if (processToResource.containsKey(current)) {
                for (int resourceId : processToResource.get(current)) {
                    if (resourceToProcess.containsKey(resourceId)) {
                        for (int nextProcess : resourceToProcess.get(resourceId)) {
                            if (!visited.contains(nextProcess)) {
                                parent.put(nextProcess, current);
                                if (dfsCycleDetection(nextProcess, visited, recursionStack, parent)) {
                                    return true;
                                }
                            } else if (recursionStack.contains(nextProcess)) {
                                parent.put(nextProcess, current);
                                return true;
                            }
                        }
                    }
                }
            }

            recursionStack.remove(current);
            return false;
        }

        private int findCycleStart(int node, Map<Integer, Integer> parent) {
            Set<Integer> seen = new HashSet<>();
            while (!seen.contains(node)) {
                seen.add(node);
                node = parent.get(node);
            }
            return node;
        }

        private void reconstructCycle(int start, Map<Integer, Integer> parent, List<Integer> cycle) {
            int current = start;
            do {
                cycle.add(current);
                current = parent.get(current);
            } while (current != start);
            cycle.add(start);
            Collections.reverse(cycle);
        }

        public void printGraph() {
            System.out.println("\n  === Resource Allocation Graph ===");
            System.out.println("  Request Edges (Process → Resource):");
            for (Map.Entry<Integer, List<Integer>> entry : processToResource.entrySet()) {
                for (int resourceId : entry.getValue()) {
                    System.out.println("    P" + entry.getKey() + " → R" + resourceId);
                }
            }

            System.out.println("\n  Assignment Edges (Resource → Process):");
            for (Map.Entry<Integer, List<Integer>> entry : resourceToProcess.entrySet()) {
                for (int processId : entry.getValue()) {
                    System.out.println("    R" + entry.getKey() + " → P" + processId);
                }
            }
        }
    }

    // ==================== INPUT UTILITIES ====================
    static Scanner scanner = new Scanner(System.in);

    private static int getIntInput(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input! Please enter a number.");
            scanner.next();
            System.out.print(prompt);
        }
        return scanner.nextInt();
    }

    private static int[] getResourceArrayInput(String resourceName, int size) {
        int[] resources = new int[size];
        System.out.println("Enter " + resourceName + " for " + size + " resources:");
        for (int i = 0; i < size; i++) {
            resources[i] = getIntInput("  Resource R" + i + ": ");
        }
        return resources;
    }

    private static int[] getProcessResourceInput(int processId, int resourceCount, String type) {
        int[] resources = new int[resourceCount];
        System.out.println("Enter " + type + " for Process P" + processId + ":");
        for (int i = 0; i < resourceCount; i++) {
            resources[i] = getIntInput("  Resource R" + i + ": ");
        }
        return resources;
    }

    // ==================== MAIN SIMULATION ====================
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   DEADLOCK DETECTION & PREVENTION");
        System.out.println("         SYSTEM SIMULATION");
        System.out.println("=========================================\n");

        showMainMenu();

        scanner.close();
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Banker's Algorithm - Safe State Detection");
            System.out.println("2. Banker's Algorithm - Resource Request");
            System.out.println("3. Resource Allocation Graph - Deadlock Detection");
            System.out.println("4. Deadlock Prevention Techniques");
            System.out.println("5. Exit");
            System.out.print("Choose an option (1-5): ");

            int choice = getIntInput("");

            switch (choice) {
                case 1:
                    simulateBankersSafeState();
                    break;
                case 2:
                    simulateBankersResourceRequest();
                    break;
                case 3:
                    simulateResourceAllocationGraph();
                    break;
                case 4:
                    simulateDeadlockPrevention();
                    break;
                case 5:
                    System.out.println("Thank you for using the Deadlock Detection System!");
                    return;
                default:
                    System.out.println("Invalid choice! Please enter 1-5.");
            }
        }
    }

    private static void simulateBankersSafeState() {
        System.out.println("\n=== BANKER'S ALGORITHM - SAFE STATE DETECTION ===");

        // Get number of processes and resources
        int numProcesses = getIntInput("Enter number of processes: ");
        int numResources = getIntInput("Enter number of resource types: ");

        // Get available resources
        System.out.println("\n--- AVAILABLE RESOURCES ---");
        int[] available = getResourceArrayInput("available instances", numResources);

        // Get process information
        Process[] processes = new Process[numProcesses];
        System.out.println("\n--- PROCESS INFORMATION ---");
        for (int i = 0; i < numProcesses; i++) {
            System.out.println("\nProcess P" + i + ":");
            int[] allocated = getProcessResourceInput(i, numResources, "allocated resources");
            int[] maximum = getProcessResourceInput(i, numResources, "maximum resources");
            processes[i] = new Process(i, allocated, maximum);
        }

        // Run Banker's Algorithm
        System.out.println("\n--- RUNNING BANKER'S ALGORITHM ---");
        BankersAlgorithm banker = new BankersAlgorithm(processes, available);
        banker.isSafeState();
    }

    private static void simulateBankersResourceRequest() {
        System.out.println("\n=== BANKER'S ALGORITHM - RESOURCE REQUEST ===");

        // Get system configuration
        int numProcesses = getIntInput("Enter number of processes: ");
        int numResources = getIntInput("Enter number of resource types: ");

        // Get available resources
        System.out.println("\n--- AVAILABLE RESOURCES ---");
        int[] available = getResourceArrayInput("available instances", numResources);

        // Get process information
        Process[] processes = new Process[numProcesses];
        System.out.println("\n--- PROCESS INFORMATION ---");
        for (int i = 0; i < numProcesses; i++) {
            System.out.println("\nProcess P" + i + ":");
            int[] allocated = getProcessResourceInput(i, numResources, "allocated resources");
            int[] maximum = getProcessResourceInput(i, numResources, "maximum resources");
            processes[i] = new Process(i, allocated, maximum);
        }

        // Get resource request
        System.out.println("\n--- RESOURCE REQUEST ---");
        int processId = getIntInput("Enter process ID making request (0-" + (numProcesses-1) + "): ");
        if (processId < 0 || processId >= numProcesses) {
            System.out.println("Invalid process ID!");
            return;
        }

        int[] request = new int[numResources];
        System.out.println("Enter resource request for Process P" + processId + ":");
        for (int i = 0; i < numResources; i++) {
            request[i] = getIntInput("  Resource R" + i + ": ");
        }

        // Process the request
        System.out.println("\n--- PROCESSING REQUEST ---");
        BankersAlgorithm banker = new BankersAlgorithm(processes, available);
        banker.requestResources(processId, request);
    }

    private static void simulateResourceAllocationGraph() {
        System.out.println("\n=== RESOURCE ALLOCATION GRAPH ===");

        ResourceAllocationGraph rag = new ResourceAllocationGraph();

        // Get number of processes and resources
        int numProcesses = getIntInput("Enter number of processes: ");
        int numResources = getIntInput("Enter number of resources: ");

        // Add processes
        for (int i = 0; i < numProcesses; i++) {
            rag.addProcess(i);
        }

        // Add resources
        for (int i = 0; i < numResources; i++) {
            int instances = getIntInput("Enter instances for Resource R" + i + ": ");
            rag.addResource(i, instances);
        }

        // Get request edges
        System.out.println("\n--- REQUEST EDGES (Process → Resource) ---");
        System.out.println("Enter process-resource request pairs (enter -1 to stop):");
        while (true) {
            int processId = getIntInput("  Process ID (-1 to stop): ");
            if (processId == -1) break;
            if (processId < 0 || processId >= numProcesses) {
                System.out.println("Invalid process ID!");
                continue;
            }

            int resourceId = getIntInput("  Resource ID: ");
            if (resourceId < 0 || resourceId >= numResources) {
                System.out.println("Invalid resource ID!");
                continue;
            }

            rag.addRequestEdge(processId, resourceId);
        }

        // Get assignment edges
        System.out.println("\n--- ASSIGNMENT EDGES (Resource → Process) ---");
        System.out.println("Enter resource-process assignment pairs (enter -1 to stop):");
        while (true) {
            int resourceId = getIntInput("  Resource ID (-1 to stop): ");
            if (resourceId == -1) break;
            if (resourceId < 0 || resourceId >= numResources) {
                System.out.println("Invalid resource ID!");
                continue;
            }

            int processId = getIntInput("  Process ID: ");
            if (processId < 0 || processId >= numProcesses) {
                System.out.println("Invalid process ID!");
                continue;
            }

            rag.addAssignmentEdge(resourceId, processId);
        }

        // Detect deadlock
        System.out.println("\n--- DETECTING DEADLOCK ---");
        rag.printGraph();

        List<Integer> deadlockProcesses = rag.detectDeadlock();
        if (!deadlockProcesses.isEmpty()) {
            System.out.println("\n  ✗ DEADLOCK DETECTED!");
            System.out.print("  Deadlock cycle: ");
            for (int i = 0; i < deadlockProcesses.size(); i++) {
                System.out.print("P" + deadlockProcesses.get(i));
                if (i < deadlockProcesses.size() - 1) System.out.print(" → ");
            }
            System.out.println();
        } else {
            System.out.println("\n  ✓ No deadlock detected");
        }
    }

    private static void simulateDeadlockPrevention() {
        System.out.println("\n=== DEADLOCK PREVENTION TECHNIQUES ===");

        System.out.println("\n1. MUTUAL EXCLUSION BREAKING:");
        System.out.println("   - Use shared resources or read-only access");
        System.out.println("   - Example: Multiple processes can read a file simultaneously");

        System.out.println("\n2. HOLD AND WAIT PREVENTION:");
        System.out.println("   - Processes request all resources at beginning");
        System.out.println("   - Example: Process requests {CPU, Printer, File} before execution");

        System.out.println("\n3. NO PREEMPTION:");
        System.out.println("   - Resources can be forcibly taken from waiting processes");
        System.out.println("   - Example: OS reclaims memory from stalled processes");

        System.out.println("\n4. CIRCULAR WAIT PREVENTION:");
        System.out.println("   - Impose total ordering on resources (R0 < R1 < R2)");
        System.out.println("   - Processes must request resources in increasing order");

        // Interactive example
        System.out.println("\n--- INTERACTIVE EXAMPLE ---");
        int numResources = getIntInput("Enter number of resources for ordering example: ");

        System.out.println("Resource ordering: ");
        for (int i = 0; i < numResources; i++) {
            System.out.println("  R" + i + " < " + (i < numResources - 1 ? "R" + (i + 1) : "END"));
        }

        System.out.println("\nEnter a resource request sequence:");
        int prevResource = -1;
        boolean valid = true;

        for (int i = 0; i < numResources; i++) {
            int resource = getIntInput("  Request resource R: ");
            if (resource <= prevResource) {
                System.out.println("  ✗ INVALID: Resources must be requested in increasing order!");
                valid = false;
                break;
            }
            prevResource = resource;
        }

        if (valid) {
            System.out.println("  ✓ VALID: Resource request follows prevention rules");
        }

        System.out.println("\n5. PRACTICAL IMPLEMENTATION STRATEGIES:");
        System.out.println("   - Timeout mechanisms for resource requests");
        System.out.println("   - Process termination when deadlock detected");
        System.out.println("   - Resource preemption with rollback capability");
        System.out.println("   - Deadlock detection and recovery protocols");
    }
}