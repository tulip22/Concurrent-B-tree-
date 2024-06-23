import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class bplustreeNode implements Serializable{
    private static final long SerialVerUid = 1L;
    boolean isLeaf;
    List<Integer>keys;
    List<bplustreeNode>children; //used only if the node is internal node
    List<studentRecord>records; //only for leaf nodes
    String nodeId;
    bplustreeNode next; //pointer to the next leaf node

    bplustreeNode(boolean isLeaf){
        this.isLeaf = isLeaf;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.records = new ArrayList<>();
        this.nodeId = UUID.randomUUID().toString();
        this.next = null;
    }
}
