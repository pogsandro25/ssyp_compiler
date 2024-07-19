package VM;

import java.util.Arrays;


public class Extern implements Instruction {

    @Override
    public void execute(VmRuntime runtime) {

        // basic print
        if (runtime.getCurrentFunctionName().equals("print")) {
            int len = -(runtime.stackAt(0) + 1);
            for (int i = -1; i > len; i--) {
                int obj = runtime.stackAt(i);

                if (i != 0) {
                    System.out.print(", ");
                }

                System.out.print(obj);
            }

            System.out.print("\n");

            runtime.returnWith(0);
            return;
        }

        if (runtime.getCurrentFunctionName().equals("print_array")) {
            Integer[] array = runtime.getArray(runtime.stackAt(0));

            assert array != null;

            System.out.println(Arrays.toString(array));
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

        if (runtime.getCurrentFunctionName().equals("len")) {
            Integer[] array = runtime.getArray(runtime.stackAt(0));

            assert array != null;

            runtime.returnWith(array.length);
            return;
        }

        runtime.returnWith(0);
    }

    @Override
    public void println(VmRuntime stack) {
        System.out.println("EXTERN");
    }
}
