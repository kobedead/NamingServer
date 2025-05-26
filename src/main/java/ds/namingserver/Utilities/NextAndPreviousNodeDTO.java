package ds.namingserver.Utilities;

public class NextAndPreviousNodeDTO {
    private Node nextNode;
    private Node previousNode;

    public NextAndPreviousNodeDTO(Node nextID, Node previousID) {
        this.nextNode = nextID;
        this.previousNode = previousID;
    }

    public Node getNextID() {
        return nextNode;
    }

    public void setNextID(Node nextID) {
        this.nextNode = nextID;
    }

    public Node getPreviousID() {
        return previousNode;
    }

    public void setPreviousID(Node previousID) {
        this.previousNode = previousID;
    }
}
