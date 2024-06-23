import java.io.*;
import java.util.*;

class btree implements Serializable{
    private static final long SerialVerUid = 1L;
    private int maxKeys;
    private bplustreeNode root;
    private TreeMap<Integer,String>IndexMap;

    btree(int m){
        this.maxKeys = m-1;

        this.root = loadRootfromDisk();
        if(root == null){
            System.out.println("Creating new root ");
            this.root = new bplustreeNode(true); //initially root node is leaf node
            saveRoottoDisk();
        }

        //create a treemap for runtime evaluation
        this.IndexMap = loadTreeMapFromDisk();
        
    }

    //save index file
    private void saveTreeMapToDisk(){
        String fileName = "IndexFile.txt";

        //using for-each loop to traverse and save
        try{
            BufferedWriter rw = new BufferedWriter(new FileWriter(fileName));
            for(Map.Entry<Integer,String> entry: IndexMap.entrySet()){
                rw.write(entry.getKey() + "," + entry.getValue());
                rw.newLine(); //move to new line
            }

            rw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    //load index file
    private TreeMap<Integer,String> loadTreeMapFromDisk(){
        String fileName = "IndexFile.txt";
        TreeMap<Integer,String> tm = new TreeMap<>();

        try{
            File file = new File(fileName);

            if(!file.exists()){
                System.out.println("Creating new index file.");
                file.createNewFile();
            }
            BufferedReader rw = new BufferedReader(new FileReader(file));
            String line = rw.readLine();

            while(line != null){
                //split line to ey and value
                String[] arrSplit = line.split(",",2);
                tm.put(Integer.parseInt(arrSplit[0]), arrSplit[1]);
                line = rw.readLine();
            }

            rw.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        return tm;
    }

    //save root of b tree to disk
    private void saveRoottoDisk(){
        try{
            FileOutputStream fout = new FileOutputStream("Root_node_Id");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this.root.nodeId);
            oos.close();
            fout.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private bplustreeNode loadRootfromDisk(){
        System.out.println("Entering load root to disk");
        bplustreeNode node = null;
        
        try{
            
            FileInputStream fin = new FileInputStream("Root_node_Id");
            ObjectInputStream ois = new ObjectInputStream(fin);
            String n = (String)ois.readObject();
            System.out.println(n);
            node = loadNodefromDisk(n);
            ois.close();
            fin.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Root node file not found. Creating new root...");
        }
        catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
            System.out.println("Root not found ...Creating new root");
        }

        return node;
    }

    public void insert(studentRecord rec){
         bplustreeNode temp = root;
         int mk = maxKeys;
         if(temp.isLeaf){
            mk = maxKeys +1;
         }
         if(temp.keys.size() == mk){
            //overflow
            bplustreeNode s = new bplustreeNode(false);
            root = s;
            saveRoottoDisk();
            s.children.add(temp);
            splitChild(s, 0);
            // Now insert into the appropriate child of the new root
            if (rec.rollNo > s.keys.get(0)) {
                temp = loadNodefromDisk(s.children.get(1).nodeId); // Load right child
            } else {
                temp = loadNodefromDisk(s.children.get(0).nodeId); // Load left child
            }
         }
         
        insertNonFull(temp, rec);
        
        //IndexMap.clear();
        //addInternalNodesToIndex(root);
        //saveTreeMapToDisk();
    }

    //adding internal nodes to the index file
    private void addInternalNodesToIndex(bplustreeNode node) {
        if (!node.isLeaf) {
            for (int i = 0; i < node.keys.size(); i++) {
                IndexMap.put(node.keys.get(i), node.nodeId);
                addInternalNodesToIndex(loadNodefromDisk(node.children.get(i).nodeId));
            }
            addInternalNodesToIndex(loadNodefromDisk(node.children.get(node.children.size() - 1).nodeId));
        }
    }
    
    private void splitChild(bplustreeNode parent, int index){
        bplustreeNode fullchild = loadNodefromDisk(parent.children.get(index).nodeId);
        bplustreeNode newchild = new bplustreeNode(fullchild.isLeaf);
        int median = maxKeys/2;

        parent.keys.add(index, fullchild.keys.get(median));
        parent.children.add(index+1, newchild);

        newchild.keys.addAll(fullchild.keys.subList(median+1,fullchild.keys.size()));
        fullchild.keys.subList(median+1, fullchild.keys.size()).clear();

        if(fullchild.isLeaf){
            newchild.records.addAll(fullchild.records.subList(median + 1, fullchild.records.size()));
            fullchild.records.subList(median + 1, fullchild.records.size()).clear();
            newchild.next = fullchild.next;
            fullchild.next = newchild;
        } else {
            newchild.children.addAll(fullchild.children.subList(median + 1, fullchild.children.size()));
            fullchild.children.subList(median+ 1, fullchild.children.size()).clear();
        }
    
        
        //Save the updated shelves (nodes) to disk
        saveNodetoDisk(fullchild);
        saveNodetoDisk(newchild);
        saveNodetoDisk(parent);
    }

    private void insertNonFull(bplustreeNode temp, studentRecord rec){
        int i = temp.keys.size()-1;

        if(temp.isLeaf){
            while(i >= 0 && rec.rollNo < temp.keys.get(i)){
                i--;
            }
            temp.keys.add(i+1, rec.rollNo);
            temp.records.add(i+1, rec);
            saveNodetoDisk(temp);
        }
        else{
            //handling internal nodes
            //find the correct child to descend
            while(i >= 0 && rec.rollNo < temp.keys.get(i)){
                i--;
            }
            i++;

            bplustreeNode child = loadNodefromDisk(temp.children.get(i).nodeId);
            //check if the child node is full
            if(child.isLeaf){
                if(child.keys.size() == maxKeys+1){
                    splitChild(temp, i);
                    if(rec.rollNo > temp.keys.get(i)){
                        i++;
                    }
    
                }
            }
            else{
                if(child.keys.size() == maxKeys){
                    splitChild(temp, i);
                    if(rec.rollNo > temp.keys.get(i)){
                        i++;
                    }
    
                }
            }
            
            insertNonFull(loadNodefromDisk(temp.children.get(i).nodeId), rec);

            //after splitting decide which of the 2 new child boxes to insert

        }
    }

    //retrive main code
    public void retrieve(int rollNo){
        studentRecord s = retrieveHelper(root, rollNo);
        //synchronizing the block of code
        //console is the shared resource so we need to block it
        synchronized (this){
            if(s == null){
                System.out.println("Record not found");
            }
            else{
                System.out.println("Record found");
                System.out.println(s.toString());
            }
        }
        
    }

    //code to retrieve data from index file
    
    //code to retrieve data
    private studentRecord retrieveHelper(bplustreeNode root, int Id){
        int i = 0;
        while(i < root.keys.size() && Id > root.keys.get(i)){
            i++;
        }

        if(i < root.keys.size() && Id == root.keys.get(i)){
            if(root.isLeaf){
                return root.records.get(i);
            }
            else{
                return retrieveHelper(loadNodefromDisk(root.children.get(i).nodeId), Id);
            }
        }
        else if(root.isLeaf){
            return null;
        }
        else{
            return retrieveHelper(loadNodefromDisk(root.children.get(i).nodeId), Id);
        }
    }

    private void saveNodetoDisk(bplustreeNode node){
        try{
            FileOutputStream fout = new FileOutputStream("node_" + node.nodeId + ".ser");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(node);
            fout.close();
            oos.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private bplustreeNode loadNodefromDisk(String nodeId){
        bplustreeNode node = null;
        try{
            FileInputStream fout = new FileInputStream("node_" + nodeId + ".ser");
            ObjectInputStream ois = new ObjectInputStream(fout);
            node = (bplustreeNode) ois.readObject();
            fout.close();
            ois.close();
        }catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
        }

        return node;
    }

    public void printAll(){
        bplustreeNode temp = root;

        System.out.println("Internal Nodes");
        while(temp.isLeaf == false){
            temp = loadNodefromDisk(temp.children.get(0).nodeId);
            for(int i = 0; i < temp.keys.size(); i++){
                System.out.println(temp.keys.get(i));
            }
            
        }

        System.out.println("Leaf Nodes: ");
        while(temp != null){
            for(int i = 0; i < temp.records.size(); i++){
                System.out.print(i);
                System.out.println(temp.records.get(i).toString());
                convertToTxt(temp.records.get(i));
            }
            
            if (temp.next != null) {
                temp = loadNodefromDisk(temp.next.nodeId);
            } else {
                temp = null;
            }
            
            
        }
    }

    private void convertToTxt(studentRecord node){

        String txtFileName = "studentRecordFile.txt";
        try{
            // FileInputStream fileIn = new FileInputStream("node_" + node.nodeId + ".ser");
            // ObjectInputStream in = new ObjectInputStream(fileIn);

            // studentRecord read = ;
            // fileIn.close();
            // in.close();

            FileWriter fw= new FileWriter(txtFileName, true);
            fw.write(node.toString());
            fw.write(System.lineSeparator());
            fw.close();
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        
    }
}
