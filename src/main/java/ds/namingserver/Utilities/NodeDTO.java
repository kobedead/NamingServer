package ds.namingserver.Utilities;

import java.io.Serializable;

public class NodeDTO {

    private int ID;

    private String IP;


    public NodeDTO(int ID, String IP) {
        this.ID = ID;
        this.IP = IP;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeDTO node = (NodeDTO) o;
        return ID == node.ID; // Compare only the ID
    }




    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getIP() {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }


    @Override
    public String toString() {
        return "Node{" +
                "ID ='" + ID + '\'' +
                ", IP =" + IP +
                '}';
    }




}
