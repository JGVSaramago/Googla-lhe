package lib;

import java.io.Serializable;

public class SetWorkerDisponibleMessage implements Serializable {

    private final int WORKER_ID;

    public SetWorkerDisponibleMessage(int WORKER_ID) {
        this.WORKER_ID = WORKER_ID;
    }

    public int getWORKER_ID() {
        return WORKER_ID;
    }
}
