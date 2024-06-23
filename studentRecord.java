import java.io.Serializable;

public class studentRecord implements Serializable{
    private static final long SerialVerUid = 1L;
    String name;
    int rollNo;

    studentRecord(int r, String n){
        this.name = n;
        this.rollNo = r;
    }

    public String toString(){
        return "Name: "+name+" rollNo: "+rollNo;
    }
}
