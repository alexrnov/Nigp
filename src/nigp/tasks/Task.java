package nigp.tasks;

import java.util.logging.Logger;

public abstract class Task {

    protected Logger logger = Logger.getLogger(Task.class.getName());

    protected String[] inputParameters;

    public Task(String[] inputParameters) {
        this.inputParameters = inputParameters;
    }

    public abstract void toSolve() throws TaskException;
}
