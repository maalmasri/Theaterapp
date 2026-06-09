package com.maamouniverse.theaterapp;

public class User {
	private Long userId;
	private String userName;
	private String password;
	private UserRole role;
	private boolean istAvtive;
	
	public User(Long id, String username, String password, UserRole role, boolean istActive) {
		this.userId=id;
		this.userName=username;
		this.password=password;
		this.role=role;
		this.istAvtive=istActive;
		
	}
	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * @Oberschließer Tagebuch zum Implemintieren
	 */
	
	

	
	
	
	
	// Getter und Setter
    public Long getId() {
    	return userId;
    	}
    public void setId(Long idUser) {
    	userId = idUser;
    }
    public String getUserName() {
    	return userName;
    	}
    public void setUserName(String username) {
    	userName = username;
    	}
    public UserRole getRole() {
    	return role;
    	}
    public void setRole(UserRole roleUser) {
    	role = roleUser;
    }
    public boolean isActive() {
    	return istAvtive;
    	}
    public void setActive(boolean active) {
    	istAvtive = active;
    	}
	

}
