

package com.hpi.memory;

public class Node {

	private String id ;

	public Node(String id) {
		this.id = id ;
	}

	public String getId() {
		return id ;
	}

    public boolean equals ( Object obj ) {
		return ((Node)obj).getId().equals(id) ;
	}
    
    public int hashCode() {
    	return id.hashCode() ;
    }

}