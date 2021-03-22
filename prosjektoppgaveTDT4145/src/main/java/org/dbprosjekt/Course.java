package org.dbprosjekt;

public class Course {
    String name;
    String term;
    String id;
    public Course(String name, String term, String id){
        this.id = id;
        this.name = name;
        this.term = term;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTerm() {
        return term;
    }

    public String toString(){
        return name + " " + term;
    }
}
