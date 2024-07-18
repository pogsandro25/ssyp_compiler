package VM;

import java.util.Arrays;


public class Extern implements Instruction {

    @Override
    public void execute(VmRuntime runtime) {

        // basic print
        if (runtime.getCurrentFunctionName().equals("print")) {
            for (int i = 0; ; i--) {
                int obj = runtime.stackAt(i);

                if (obj == 0) {
                    System.out.print("\n");
                    break;
                } else if (i != 0) {
                    System.out.print(", ");
                }

                System.out.print(obj);
            }

            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("print_array")) {
            for (int i = 0; ; i--) {
                int key = runtime.stackAt(i);

                if (key == 0) {
                    System.out.print("\n");
                    break;
                } else if (i != 0) {
                    System.out.print(", ");
                }

                Integer[] obj = runtime.getArray(key);

                System.out.print(Arrays.toString(obj));
            }

            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("cprint_array")) {
            Integer[] charArray = runtime.getArray(runtime.stackAt(0));
            if (charArray == null) {
                runtime.returnWith(0);
                return;
            }

            for (int c : charArray) {
                System.out.print((char) c);
            }
            System.out.print("\n");

            runtime.returnWith(0);
            return;
        }

        runtime.returnWith(0);
    }

    @Override
    public void println(VmRuntime stack) {
        System.out.println("EXTERN");
    }
}
