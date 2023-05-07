package flipkart.tef.benchmarks;

import flipkart.tef.bizlogics.IBizlogic;
import flipkart.tef.bizlogics.TefContext;
import flipkart.tef.exception.TefExecutionException;

import java.util.Random;

public class TestBizlogics {

    private static final Random random = new Random();

    public static class Bizlogic1 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic2 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic3 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic4 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic5 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic6 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic7 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic8 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic9 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    public static class Bizlogic10 implements IBizlogic {
        @Override
        public void execute(TefContext tefContext) throws TefExecutionException {
            double noOp = doWork();
        }
    }

    private static double doWork() {
        return random.nextDouble();
    }
}
