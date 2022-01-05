package model;

import java.io.Serial;
import java.io.Serializable;

public class Message implements Serializable {
    
	@Serial
    private static final long serialVersionUID = 123456L;
    private String name;
    private String message;
    private Status status;

    public Message() {
    	
    }

    public Message(String name, String message, Status status) {
        this.name = name;
        this.message = message;
        this.status = status;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Message [name=" + name + ", message=" + message + ", status=" + status + "]";
	}
	
}
