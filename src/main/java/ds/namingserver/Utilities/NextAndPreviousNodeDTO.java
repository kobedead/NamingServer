package ds.namingserver.Utilities;

public class NextAndPreviousNodeDTO {
    private NodeDTO nextNode;
    private NodeDTO previousNode;

    public NextAndPreviousNodeDTO(NodeDTO nextID, NodeDTO previousID) {
        this.nextNode = nextID;
        this.previousNode = previousID;
    }

    public NodeDTO getNextID() {
        return nextNode;
    }

    public void setNextID(NodeDTO nextID) {
        this.nextNode = nextID;
    }

    public NodeDTO getPreviousID() {
        return previousNode;
    }

    public void setPreviousID(NodeDTO previousID) {
        this.previousNode = previousID;
    }
}
