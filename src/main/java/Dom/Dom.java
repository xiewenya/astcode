package Dom;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by bresai on 2016/12/21.
 */
public class Dom {
    private String domType = "span";
    private LinkedList<String> classList = new LinkedList<>();
    private String id;
    private String name;
    private String type;

    public Dom() {
    }

    public Dom(String domType) {
        this.domType = domType;
    }

    public void setDomType(String domType) {
        this.domType = domType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addClass(String cls) {
        this.classList.add(cls);
    }

    public void setType(String type) {
        this.type = type;
    }

    private String getClassList(){
        if (this.classList.isEmpty()){
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("class='");
        Iterator<String> iterator = this.classList.iterator();
        while (iterator.hasNext()){
            builder.append(iterator.next());
            if (iterator.hasNext()){
                builder.append(" ");
            }
        }

        builder.append("' ");
        return builder.toString();
    }

    private String getId() {
        if (this.id == null){
            return "";
        }

        return "id='" + this.id + "' ";
    }

    private String getName(){
        if (this.id == null){
            return "";
        }

        return "name='" + this.name + "' ";
    }

    private String getType(){
        if (this.type == null){
            return "";
        }

        return "type='" + this.type + "' ";
    }

    public String getDomStart(){
        return "<" + this.domType + " " + getId() + getName() + getType() + getClassList() + ">";
    }

    public String getDomEnd(){
        return "</" + this.domType + ">";
    }
}
